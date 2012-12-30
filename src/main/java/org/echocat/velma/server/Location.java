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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.UUID.randomUUID;

public class Location {
    
    private final String _value;

    public Location() {
        this("/" + randomUUID().toString() + "/settings-security.xml");
    }
    
    public Location(@Nonnull String value) {
        _value = value;
    }

    @Nonnull
    public URL toUrl(@Nonnull InetAddress address, @Nonnegative int port) throws MalformedURLException {
        return new URL("http", address.getHostName(), port, _value);
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o instanceof String) {
            result = _value.equals(o);
        } else if (o instanceof Location) {
            result = _value.equals(((Location)o)._value);
        } else {
            result = false;
        }
        return result;
    }
    
    public boolean equals(String string) {
        return equals((Object)string);
    }

    @Override
    public int hashCode() {
        return _value.hashCode();
    }

    @Override
    public String toString() {
        return _value;
    }
}
