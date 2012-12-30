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

package org.echocat.velma.server;

import org.echocat.velma.Password;
import org.echocat.velma.PasswordRequest;
import org.echocat.velma.PasswordStorage;
import org.echocat.velma.SecuritySettingsUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

import static org.echocat.velma.SecuritySettingsUtils.writeSettingsWithEncryptedMasterPasswordTo;
import static org.echocat.velma.SecuritySettingsUtils.writeSettingsWithMasterPasswordTo;

public class BodyCreator {
    
    private final PasswordStorage _passwordStorage;

    public BodyCreator(@Nonnull PasswordStorage passwordStorage) {
        _passwordStorage = passwordStorage;
    }

    public void createRealBody(@Nonnull PasswordRequest request, @Nonnull Writer writer) throws IOException {
        final Password password = _passwordStorage.getPassword(request);
        writeSettingsWithEncryptedMasterPasswordTo(writer, password.getEncryptedValue());
    }
    
    public void createFakeBody(@Nonnull Writer writer) throws IOException {
        writeSettingsWithMasterPasswordTo(writer, SecuritySettingsUtils.createFakeMasterPassword());
    }

}
