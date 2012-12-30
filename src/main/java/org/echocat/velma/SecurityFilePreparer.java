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

import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;
import org.sonatype.plexus.components.sec.dispatcher.model.io.xpp3.SecurityConfigurationXpp3Writer;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URL;

import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION;
import static org.sonatype.plexus.components.sec.dispatcher.SecUtil.read;

public class SecurityFilePreparer {

    public void prepare(@Nonnull URL masterPasswordUrl) throws IOException {
        final File securitySettingsFile = resolveSecurityFile();
        checkParentOf(securitySettingsFile);
        createIfNotExists(securitySettingsFile);
        modifyMasterPasswordUrl(securitySettingsFile, masterPasswordUrl);
    }

    @Nonnull
    private File resolveSecurityFile() throws IOException {
        final String location = getProperty(SYSTEM_PROPERTY_SEC_LOCATION, "~/.m2/settings-security.xml");
        final String realLocation = location.charAt(0) == '~' ? getProperty("user.home") + location.substring(1) : location;
        final File securityFile = new File(realLocation);
        if (securityFile.exists() && !securityFile.isFile()) {
            throw new IOException("The file '" + securityFile + "' exists but is not a file.");
        }
        return securityFile;
    }

    private void checkParentOf(@Nonnull File securitySettingsFile) throws IOException {
        final File parent = securitySettingsFile.getParentFile();
        if (parent != null) {
            if (parent.exists()) {
                if (!parent.isDirectory()) {
                    throw new IOException("The parent of file '" + securitySettingsFile + "' is not a directory.");
                }
            } else {
                if (!parent.mkdirs()) {
                    throw new IOException("Could not create directory which will contain '" + securitySettingsFile + "'.");
                }
            }
        }
    }

    private void createIfNotExists(@Nonnull File securitySettingsFile) throws IOException {
        if (!securitySettingsFile.exists()) {
            writeStringToFile(securitySettingsFile, "<settingsSecurity></settingsSecurity>\n");
        }
    }

    private void modifyMasterPasswordUrl(@Nonnull File securitySettingsFile, @Nonnull URL masterPasswordUrl) throws IOException {
        final SettingsSecurity settings = readSecuritySettingsFile(securitySettingsFile);
        modifyMasterPasswordUrl(masterPasswordUrl, settings);
        writeSecuritySettingsFile(securitySettingsFile, settings);
    }

    private void modifyMasterPasswordUrl(@Nonnull URL masterPasswordUrl, @Nonnull SettingsSecurity settings) {
        settings.setRelocation(masterPasswordUrl.toExternalForm());
        settings.setMaster(null);
    }

    @Nonnull
    private SettingsSecurity readSecuritySettingsFile(@Nonnull File securitySettingsFile) throws IOException {
        try {
            return read(securitySettingsFile.getPath(), false);
        } catch (SecDispatcherException e) {
            throw new IOException("Could not read '" + securitySettingsFile + "'", e);
        }
    }

    private void writeSecuritySettingsFile(@Nonnull File securitySettingsFile, @Nonnull SettingsSecurity settings) throws IOException {
        try (final OutputStream os = new FileOutputStream(securitySettingsFile)) {
            try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                new SecurityConfigurationXpp3Writer().write(writer, settings);
            }
        }
    }

}
