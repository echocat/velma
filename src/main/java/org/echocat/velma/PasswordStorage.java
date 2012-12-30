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

import org.echocat.velma.dialogs.RequestMasterPasswordDialog;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class PasswordStorage {

    private final Map<String, Password> _userAgentToPassword = new HashMap<>();
    private final Resources _resources;
    private final Configuration _configuration;

    public PasswordStorage(@Nonnull Resources resources, @Nonnull Configuration configuration) {
        _resources = resources;
        _configuration = configuration;
    }

    @Nullable
    public Password getPassword(@Nonnull PasswordRequest request) {
        synchronized (this) {
            Password password = _userAgentToPassword.get(request.getUserAgent());
            if (password == null || password.isStillValid()) {
                password = getNewPassword(request);
                _userAgentToPassword.put(request.getUserAgent(), password);
            }
            return password;
        }
    }

    @Nullable
    public Password getNewPassword(@Nonnull PasswordRequest request) {
        synchronized (this) {
            final Password password = requestNewPassword(request);
            _userAgentToPassword.put(request.getUserAgent(), password);
            return password;
        }
    }

    @Nullable
    private Password requestNewPassword(@Nonnull PasswordRequest request) {
        final RequestMasterPasswordDialog dialog = new RequestMasterPasswordDialog(_resources, _configuration, request);
        return dialog.request();
    }

    public void forgetAllPasswordsNow() {
        synchronized (this) {
            _userAgentToPassword.clear();
        }
    }

    @Nonnull
    public static ActionListener forgetAllPasswordsNowListener(@Nonnull final PasswordStorage storage) {
        return new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
            storage.forgetAllPasswordsNow();
        }};
    }
}
