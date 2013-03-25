/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Velma, Copyright (c) 2011-2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.velma;

import org.echocat.jomon.net.FreeTcpPortDetector;
import org.echocat.velma.dialogs.SetMasterPasswordDialog;
import org.echocat.velma.server.BodyCreator;
import org.echocat.velma.server.Location;
import org.echocat.velma.server.RequestHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import static java.awt.SystemTray.getSystemTray;
import static java.awt.SystemTray.isSupported;
import static java.lang.System.exit;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.echocat.velma.dialogs.AboutDialog.showAboutDialogListener;
import static org.echocat.velma.MenuUtils.*;
import static org.echocat.velma.dialogs.PasswordEncryptionDialog.showEncryptPasswordDialogListener;
import static org.echocat.velma.PasswordStorage.forgetAllPasswordsNowListener;
import static org.echocat.velma.dialogs.SetMasterPasswordDialog.showMasterPasswordDialogListener;

public class Velma implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory.getLogger(Boot.class);

    private final Server _server;
    private final Configuration _configuration;
    private final PasswordStorage _passwordStorage;

    public Velma(@Nonnull Resources resources, @Nonnull Configuration configuration) throws Exception {
        _configuration = configuration;

        checkForConfiguredMasterPassword(resources, configuration);

        final Location location = new Location();
        final InetAddress address = getLocalHostAddress();
        final int port = getPort(address);
        _passwordStorage = new PasswordStorage(resources, configuration);
        final BodyCreator bodyCreator = new BodyCreator(_passwordStorage);
        final Handler handler = new RequestHandler(location, bodyCreator, resources);
        final SocketConnector connector = createConnector(address, port);

        _server = createAndStartServer(handler, connector);
        prepareSecurityFile(location, address, port);
        configureSystemTray(resources);
    }

    @Override
    public void close() throws Exception {
        _server.stop();
    }

    private void configureSystemTray(@Nonnull Resources resources) throws Exception {
        if (!isSupported()) {
            showMessageDialog(null, resources.getString("systemTrayNotSupported"), resources.getApplicationName(), ERROR_MESSAGE);
            //noinspection CallToSystemExit
            exit(1);
        } else {
            final PopupMenu popupMenu = popupMenu(
                menuItem(resources, "encryptPassword", showEncryptPasswordDialogListener(resources, _passwordStorage)),
                menuItem(resources, "changeMasterPassword", showMasterPasswordDialogListener(resources, _passwordStorage, _configuration)),
                menuItem(resources, "forgetAllPasswordsNow", forgetAllPasswordsNowListener(_passwordStorage)),
                menuItem(resources, "about", showAboutDialogListener(resources)),
                menuItem(resources, "exit", exitApplicationListener())
            );
            final Image icon = findBestMatchingIconBy(getSystemTray().getTrayIconSize(), resources);
            final TrayIcon trayIcon = new TrayIcon(icon);
            trayIcon.setPopupMenu(popupMenu);
            trayIcon.setImageAutoSize(false);
            trayIcon.setToolTip(resources.getApplicationName());

            getSystemTray().add(trayIcon);
        }
    }

    @Nonnull
    protected Image findBestMatchingIconBy(@Nonnull Dimension trayIconSize, @Nonnull Resources resources) {
        final int targetSize = getMidSizeOf(trayIconSize);
        Image result = null;
        for (Image icon : resources.getIcons()) {
            if (result == null) {
                result = icon;
            } else {
                final int oldDiff = getDiff(result, targetSize);
                final int currentDiff = getDiff(icon, targetSize);
                if (oldDiff > currentDiff) {
                    result = icon;
                }
            }
        }
        if (result == null) {
            throw new IllegalStateException("No icon found.");
        }
        return result;
    }

    @Nonnegative
    protected int getDiff(@Nonnull Image of, @Nonnegative int toTargetSize) {
        final int imageSize = of.getWidth(null);
        return imageSize > toTargetSize ? imageSize - toTargetSize : toTargetSize - imageSize;
    }

    @Nonnegative
    protected int getMidSizeOf(@Nonnull Dimension size) {
        final double w = (int) size.getWidth();
        final double h = (int) size.getHeight();
        final double result;
        if (w > h) {
            result = ((w - h) / 2d) + w;
        } else if (h > w) {
            result = ((h - w) / 2d) + h;
        } else {
            result = h;
        }
        return (int) result;
    }

    private void checkForConfiguredMasterPassword(@Nonnull Resources resources, @Nonnull Configuration configuration) {
        if (!configuration.hasMasterPassword()) {
            final SetMasterPasswordDialog setMasterPasswordDialog = new SetMasterPasswordDialog(resources, null, configuration);
            setMasterPasswordDialog.activate();
        }
    }

    @Nonnegative
    private int getPort(@Nonnull InetAddress localHostAddress) throws IOException {
        final FreeTcpPortDetector detector = new FreeTcpPortDetector(localHostAddress, 40000, 65000);
        return detector.detect();
    }

    @Nonnull
    private InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getByName("localhost");
    }

    @Nonnull
    private SocketConnector createConnector(@Nonnull InetAddress address, @Nonnegative int port) throws IOException {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(port);
        connector.setHost(address.getHostName());
        return connector;
    }

    @Nonnull
    private Server createAndStartServer(@Nonnull Handler handler, @Nonnull SocketConnector connector) throws Exception {
        final Server server = new Server();
        server.addConnector(connector);
        server.setHandler(handler);
        server.start();
        LOG.info("Started listener at port " + connector.getPort() + ".");
        return server;
    }

    private void prepareSecurityFile(@Nonnull Location location, @Nonnull InetAddress address, @Nonnegative int port) throws IOException {
        new SecurityFilePreparer().prepare(location.toUrl(address, port));
    }

}
