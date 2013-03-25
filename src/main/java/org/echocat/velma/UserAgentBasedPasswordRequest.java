/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Velma, Copyright (c) 2011-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.velma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class UserAgentBasedPasswordRequest extends PasswordRequest {

    @Nullable
    private final String _userAgent;

    public UserAgentBasedPasswordRequest(@Nullable String userAgent, @Nonnull CacheType cacheType, @Nullable ResponseType... responseTypes) {
        super(cacheType, responseTypes);
        _userAgent = userAgent;
    }

    @Nullable
    public String getUserAgent() {
        return _userAgent;
    }

    @Nonnull
    @Override
    public String formatRemoteAsHtml(@Nonnull Resources resources) {
        final String content = getUserAgent() != null ? getUserAgent() : resources.getString("unknownRequester");
        return "<html><body style='font-family:sans-serif; font-size: 1em;'>" + escapeHtml4(content) + "</body></html>";
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || !(o instanceof UserAgentBasedPasswordRequest)) {
            result = false;
        } else {
            final UserAgentBasedPasswordRequest that = (UserAgentBasedPasswordRequest) o;
            final String userAgent = getUserAgent();
            result = userAgent != null ? userAgent.equals(that.getUserAgent()) : that.getUserAgent() == null;
        }
        return result;
    }

    @Override
    public int hashCode() {
        final String userAgent = getUserAgent();
        return userAgent != null ? userAgent.hashCode() : 0;
    }

}
