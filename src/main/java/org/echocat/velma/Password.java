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
import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class Password {
    
    private final String _encryptedValue;
    private final Date _validUntil;

    public Password(@Nonnull String encryptedValue, @Nonnull Date validUntil) {
        _encryptedValue = encryptedValue;
        _validUntil = validUntil;
    }

    public Password(@Nonnull String value, @Nonnull Duration validFor) {
        this(value, new Date(validFor.plus(currentTimeMillis()).toMilliSeconds()));
    }

    @Nonnull
    public String getEncryptedValue() {
        return _encryptedValue;
    }

    public boolean isStillValid() {
        return new Date().after(_validUntil);
    }
}
