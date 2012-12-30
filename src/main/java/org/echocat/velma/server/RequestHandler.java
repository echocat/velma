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

import org.echocat.velma.PasswordRequest;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

import static org.echocat.velma.PasswordRequest.CacheType.cacheable;
import static org.echocat.velma.PasswordRequest.ResponseType.fakePassword;
import static org.echocat.velma.PasswordRequest.ResponseType.password;

public class RequestHandler extends AbstractHandler {

    private final Location _location;
    private final BodyCreator _bodyCreator;

    public RequestHandler(@Nonnull Location location, @Nonnull BodyCreator bodyCreator) {
        _location = location;
        _bodyCreator = bodyCreator;
    }

    @Override
    public void handle(@Nonnull String target, @Nonnull Request baseRequest, @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) throws IOException, ServletException {
        final PasswordRequest passwordRequest = toPasswordRequest(request);
        response.setContentType("text/xml");
        createBody(passwordRequest, target, response.getWriter());
        baseRequest.setHandled(true);
    }

    private void createBody(@Nonnull PasswordRequest passwordRequest, @Nonnull String target, @Nonnull Writer writer) throws IOException {
        if (_location.equals(target)) {
            _bodyCreator.createRealBody(passwordRequest, writer);
        } else {
            _bodyCreator.createFakeBody(writer);
        }
    }

    @Nonnull
    private PasswordRequest toPasswordRequest(@Nonnull HttpServletRequest request) {
        return new PasswordRequest(request.getHeader("User-Agent"), cacheable, password, fakePassword);
    }
}

