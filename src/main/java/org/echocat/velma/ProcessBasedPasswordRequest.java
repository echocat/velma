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

import org.echocat.jomon.process.Process;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class ProcessBasedPasswordRequest extends PasswordRequest {

    @Nonnull
    private final Process _process;

    public ProcessBasedPasswordRequest(@Nonnull Process process, @Nonnull CacheType cacheType, @Nullable ResponseType... responseTypes) {
        super(cacheType, responseTypes);
        _process = process;
    }

    @Nonnull
    public Process getProcess() {
        return _process;
    }

    @Nonnull
    @Override
    public String formatRemoteAsHtml(@Nonnull Resources resources) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>nobr { white-space: nowarp; font-family: monospace; }</style></head><body style='font-family: sans-serif; font-size: 1em'>");
        sb.append(resources.format("processInfo", _process.getId(), formatExecutable(resources), formatCommandLine()));
        sb.append("</body></html>");
        return sb.toString();
    }

    @Nonnull
    protected String formatExecutable(@Nonnull Resources resources) {
        String result;
        try {
            result = escapeHtml4(_process.getExecutable().getCanonicalPath());
        } catch (IOException ignored) {
            result = resources.getString("unknownPlaceholder");
        }
        return result;
    }

    @Nonnull
    protected String formatCommandLine() {
        final StringBuilder sb = new StringBuilder();
        for (String argument : _process.getCommandLine()) {
            if (sb.length() > 0) {
                sb.append("&nbsp;");
            }
            if (argument.contains(" ")) {
                sb.append("&quot;").append(escapeHtml4(argument.replace("\"", "\\\"")).replace(" ", "&nbsp;")).append("&quot;");
            } else {
                sb.append(escapeHtml4(argument));
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (this == o) {
            result = true;
        } else if (o == null || !(o instanceof ProcessBasedPasswordRequest)) {
            result = false;
        } else {
            final ProcessBasedPasswordRequest that = (ProcessBasedPasswordRequest) o;
            result = getProcess().equals(that.getProcess());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return getProcess().hashCode();
    }

}
