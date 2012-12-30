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

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;

import static org.echocat.velma.SecuritySettingsUtils.decryptPassword;
import static org.echocat.velma.SecuritySettingsUtils.encryptPassword;

public class Configuration {

    private static final String DEFAULT_EXPIRE_AFTER_KEY = "defaultExpireAfter";
    private static final String MASTER_PASSWORD_CHECK_SUM_SEED = "masterPasswordCheckSumSeed";
    private static final String MASTER_PASSWORD_CHECK_SUM = "masterPasswordCheckSum";

    private static final String MAGIC_SEED = "sdfkj3254fdjsjm103jmfdsfs243098dskjs";
    private static final Random RANDOM = new SecureRandom();
    

    private final Properties _properties = new Properties();

    public void setDefaultExpireAfter(@Nullable Duration expireAfter) {
        synchronized (this) {
            _properties.setProperty(DEFAULT_EXPIRE_AFTER_KEY, expireAfter != null ? expireAfter.toPattern() : null);
        }
    }

    @Nullable
    public Duration getDefaultExpireAfter() {
        final String value;
        synchronized (this) {
            value = _properties.getProperty(DEFAULT_EXPIRE_AFTER_KEY, new Duration("30m").toString());
        }
        return value != null ? new Duration(value) : new Duration(0);
    }

    public void setMasterPasswordCheckSum(@Nullable String masterPasswordCheckSum) {
        synchronized (this) {
            _properties.setProperty(MASTER_PASSWORD_CHECK_SUM, masterPasswordCheckSum);
        }
    }

    @Nullable
    public String getMasterPasswordCheckSum() {
        synchronized (this) {
            return _properties.getProperty(MASTER_PASSWORD_CHECK_SUM);
        }
    }

    public void setMasterPasswordCheckSumSeed(@Nullable String masterPasswordCheckSumSeed) {
        synchronized (this) {
            _properties.setProperty(MASTER_PASSWORD_CHECK_SUM_SEED, masterPasswordCheckSumSeed);
        }
    }

    @Nullable
    public String getMasterPasswordCheckSumSeed() {
        synchronized (this) {
            return _properties.getProperty(MASTER_PASSWORD_CHECK_SUM_SEED);
        }
    }

    public void setMasterPassword(@Nonnull String masterPassword) {
        final String checksumSeed = Long.toString(RANDOM.nextLong());
        final String checksum = Integer.toString((checksumSeed + MAGIC_SEED).hashCode());
        synchronized (this) {
            _properties.setProperty(MASTER_PASSWORD_CHECK_SUM_SEED, encryptPassword(checksumSeed, masterPassword));
            _properties.setProperty(MASTER_PASSWORD_CHECK_SUM, encryptPassword(checksum, masterPassword));
            
        }
    }
    
    public boolean hasMasterPassword() {
        return _properties.containsKey(MASTER_PASSWORD_CHECK_SUM_SEED) && _properties.containsKey(MASTER_PASSWORD_CHECK_SUM);
    }

    /**
     * @throws IllegalStateException if there is no master password currently set in this configuration.
     */
    public boolean isProvidedMasterPasswordValid(@Nonnull String masterPassword) throws IllegalStateException {
        if (!hasMasterPassword()) {
            throw new IllegalStateException("There is no masterPassword stored in this configuration.");
        }
        boolean result = false;
        try {
            final String checksumSeed = decryptPassword(_properties.getProperty(MASTER_PASSWORD_CHECK_SUM_SEED), masterPassword);
            final String checksum = decryptPassword(_properties.getProperty(MASTER_PASSWORD_CHECK_SUM), masterPassword);
            final String expectedChecksum = Integer.toString((checksumSeed + MAGIC_SEED).hashCode());
            result = expectedChecksum.equals(checksum);
        } catch (SecurityException ignored) {}
        return result;
    }
    
    public void load() throws IOException {
        final File configFile = getConfigFile();
        if (configFile.isFile()) {
            try (final InputStream is = new FileInputStream(configFile)) {
                try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                    synchronized (this) {
                        _properties.load(reader);
                    }
                }
            }
        }
    }

    public void save() throws IOException {
        final File configFile = getConfigFile();
        final File parentFile = configFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new IOException("Could not create directory of '" + configFile + "'.");
            }
        }
        try (final OutputStream os = new FileOutputStream(configFile)) {
            try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                synchronized (this) {
                    _properties.store(writer, null);
                }
            }
        }
    }

    @Nonnull
    private File getConfigFile() {
        return new File(System.getProperty("user.home"), ".m2/velma.properties");
    }

}
