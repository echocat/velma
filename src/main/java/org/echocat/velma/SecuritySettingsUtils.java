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

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;
import org.sonatype.plexus.components.sec.dispatcher.model.io.xpp3.SecurityConfigurationXpp3Writer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.Random;

import static org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION;

public class SecuritySettingsUtils {

    private static final Random RANDOM = new SecureRandom();

    public static void writeSettingsTo(@Nonnull SettingsSecurity settings, @Nonnull Writer writer) throws IOException {
        new SecurityConfigurationXpp3Writer().write(writer, settings);
    }

    public static void writeSettingsWithMasterPasswordTo(@Nonnull Writer writer, @Nonnull String masterPassword) throws IOException {
        writeSettingsWithEncryptedMasterPasswordTo(writer, encryptMasterPassword(masterPassword));
    }

    public static void writeSettingsWithEncryptedMasterPasswordTo(@Nonnull Writer writer, @Nonnull String encryptedMasterPassword) throws IOException {
        final SettingsSecurity settings = new SettingsSecurity();
        settings.setMaster(encryptedMasterPassword);
        writeSettingsTo(settings, writer);
    }

    @Nonnull
    public static String encryptMasterPassword(@Nonnull String password) {
        return encryptPassword(password, SYSTEM_PROPERTY_SEC_LOCATION);
    }
    
    @Nonnull
    public static String encryptPassword(@Nonnull String password, @Nonnull String passPhrase) {
        try {
            return getCipher().encryptAndDecorate(password, passPhrase);
        } catch (PlexusCipherException e) {
            throw new RuntimeException("Could not create the password.", e);
        }
    }

    @Nonnull
    public static String decryptMasterPassword(@Nonnull String password) {
        return decryptPassword(password, SYSTEM_PROPERTY_SEC_LOCATION);
    }

    @Nonnull
    public static String decryptPassword(@Nonnull String password, @Nonnull String passPhrase) {
        final DefaultPlexusCipher cipher = getCipher();
        try {
            return cipher.decryptDecorated(password, passPhrase);
        } catch (PlexusCipherException e) {
            throw new SecurityException("Could not decrypt the password.", e);
        }
    }

    @Nonnull
    private static DefaultPlexusCipher getCipher() {
        final DefaultPlexusCipher cipher;
        try {
            cipher = new DefaultPlexusCipher();
        } catch (PlexusCipherException e) {
            throw new RuntimeException("Could not create cipher.", e);
        }
        return cipher;
    }

    private SecuritySettingsUtils() {}

    @Nonnull
    public static String createFakeMasterPassword() {
        final StringBuilder sb = new StringBuilder();
        final int length = RANDOM.nextInt(249) + 1;
        for (int i = 0; i<length ; i++) {
            //noinspection DuplicateCondition
            if (RANDOM.nextBoolean()) {
                sb.append((char)('A' + RANDOM.nextInt('Z' - 'A')));
            } else //noinspection DuplicateCondition
                if (RANDOM.nextBoolean()) {
                sb.append((char)('a' + RANDOM.nextInt('z' - 'a')));
            } else {
                sb.append((char)('0' + RANDOM.nextInt('9' - '0')));
            }
        }
        return sb.toString();
    }
}
