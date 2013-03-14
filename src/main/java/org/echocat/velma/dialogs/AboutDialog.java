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

package org.echocat.velma.dialogs;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.echocat.velma.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static java.awt.Desktop.Action.BROWSE;
import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.awt.Font.DIALOG;
import static java.awt.Font.PLAIN;
import static java.awt.RenderingHints.*;
import static javax.imageio.ImageIO.read;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class AboutDialog extends ActivateEnabledDialog {

    private static final Logger LOG = LoggerFactory.getLogger(AboutDialog.class);
    private final BufferedImage _backgroundImage;

    public AboutDialog(@Nonnull Resources resources) {
        super(resources, "aboutTitle");
        try {
            _backgroundImage = read(getClass().getResource("background.png"));
        } catch (IOException e) {
            throw new RuntimeException("Could not load background image.", e);
        }
        createIntroduction(resources);
        createButtonBar(resources);
    }

    @Nonnull
    @Override
    protected Container createContainer() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                final Graphics2D g2d = (Graphics2D) g;
                final Dimension size = getSize();
                g2d.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                final double sourceWidth = _backgroundImage.getWidth();
                final double sourceHeight = _backgroundImage.getHeight();
                final double targetWidth = size.getWidth();
                final double targetHeight = size.getHeight();
                final double relWidth = sourceWidth / targetWidth;
                final double relHeight = sourceHeight / targetHeight;
                final double width;
                final double height;
                final double x;
                final double y;
                if (relWidth > relHeight) {
                    width = sourceWidth / relHeight;
                    height = sourceHeight / relHeight;
                    x = ((targetWidth - width) / 2);
                    y = 0;
                } else {
                    width = sourceWidth / relWidth;
                    height = sourceHeight / relWidth;
                    x = 0;
                    y = ((targetHeight - height) / 2);
                }

                g2d.drawImage(_backgroundImage, (int) x, (int) y, (int) width, (int) height, this);
            }
        };
    }

    @Override
    protected MigLayout createLayout() {
        final MigLayout layout = new MigLayout(
            new LC().fillX().wrapAfter(1).width("480px").noCache(),
            new AC().gap("rel").grow().fill(),
            new AC().gap("10")
        );
        return layout;
    }

    protected void createIntroduction(@Nonnull Resources resources) {
        final URL iconUrl = resources.getIconUrl(48);

        final StringBuilder body = new StringBuilder();
        body.append("<html>");
        body.append("<head><style>" +
            "td { margin-right: 10px; }" +
            "</style></head>");
        body.append("<body style='font-family: sans; font-size: 1em'><table><tr>");
        body.append("<td valign='top'><img src='").append(iconUrl).append("' /></td>");
        body.append("<td valign='top'>");
        body.append("<h2>").append(escapeHtml4(resources.getApplicationName()));
        final String version = resources.getVersion();
        if (!isEmpty(version)) {
            body.append("<br/><span style='font-size: 0.6em'>").append(resources.formatEscaped("versionText", version)).append("</span>");
        }
        body.append("</h2>");

        body.append("<p>Copyright 2011-2012 <a href='https://echocat.org'>echocat</a></p>");
        body.append("<p><a href='http://mozilla.org/MPL/2.0/'>").append(resources.formatEscaped("licensedUnder", "MPL 2.0")).append("</a></p>");

        body.append("<p><table cellpadding='0' cellspacing='0'>");
        body.append("<tr><td>").append(resources.formatEscaped("xHomepage", "echocat")).append(":</td><td><a href='https://echocat.org'>echocat.org</a></td></tr>");
        body.append("<tr><td>").append(resources.formatEscaped("xHomepage", "Velma")).append(":</td><td><a href='https://velma.echocat.org'>velma.echocat.org</a></td></tr>");
        body.append("</table></p>");

        body.append("<h4>").append(resources.formatEscaped("developers")).append("</h4><table cellpadding='0' cellspacing='0'>");
        body.append("<tr><td>Gregor Noczinski</td><td><a href='mailto:gregor@noczinski.eu'>gregor@noczinski.eu</a></td><td><a href='https://github.com/blaubaer'>github.com/blaubaer</a></td></tr>");
        body.append("</table>");

        body.append("</td>");
        body.append("</tr></table></body></html>");

        final JTextPane text = new JTextPane();
        text.setMargin(new Insets(0, 0, 0, 0));
        text.setContentType("text/html");
        text.setText(body.toString());
        text.setFont(new Font(DIALOG, PLAIN, 12));
        text.setBackground(new Color(255, 255, 255, 0));
        text.setEditable(false);
        text.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == ACTIVATED && isDesktopSupported()) {
                    final Desktop desktop = getDesktop();
                    if (desktop.isSupported(BROWSE)) {
                        try {
                            desktop.browse(e.getURL().toURI());
                        } catch (IOException | URISyntaxException exception) {
                            LOG.error("Could not open " + e.getURL() + " because of an exception.", exception);
                        }
                    } else {
                        LOG.error("Could not open " + e.getURL() + " because browse is not supported by desktop.");
                    }
                }
                repaint();
            }
        });
        text.addMouseListener(new MouseListener() {
            @Override public void mouseClicked(MouseEvent e) { repaint(); }
            @Override public void mousePressed(MouseEvent e) { repaint(); }
            @Override public void mouseReleased(MouseEvent e) { repaint(); }
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e) { repaint(); }
        });
        add(text, new CC().spanX(2).growX().minWidth("10px"));
    }


    protected void createButtonBar(@Nonnull Resources resources) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JButton okButton = new JButton(resources.getString("ok"));
        okButton.addActionListener(getDisposeActionListener());
        panel.add(okButton);
        add(panel, new CC().alignX("right"));
    }

    @Nonnull
    public static ActionListener showAboutDialogListener(@Nonnull final Resources resources) {
        return new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
            new AboutDialog(resources).activate();
        }};
    }
}
