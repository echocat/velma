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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.echocat.velma.PasswordRequest.ResponseType.DEFAULT_RESPONSE_TYPES;

public abstract class PasswordRequest {
    
    public enum ResponseType {
        password,
        empty,
        fakePassword;

        public static final ResponseType[] DEFAULT_RESPONSE_TYPES = new ResponseType[]{ResponseType.password, ResponseType.empty};
    }

    public enum CacheType {
        cacheable,
        notCachable
    }
    
    private final CacheType _cacheType;
    private final Set<ResponseType> _responseTypes;

    public PasswordRequest(@Nonnull CacheType cacheType, @Nullable ResponseType... responseTypes) {
        _cacheType = cacheType;
        _responseTypes = new HashSet<>(asList(responseTypes != null && responseTypes.length > 0 ? responseTypes : DEFAULT_RESPONSE_TYPES));
    }

    @Nonnull
    public CacheType getCacheType() {
        return _cacheType;
    }

    @Nonnull
    public Set<ResponseType> getResponseTypes() {
        return _responseTypes;
    }

    @Nonnull
    public abstract String formatRemoteAsHtml(@Nonnull Resources resources);

}
