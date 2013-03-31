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

import org.echocat.velma.support.ExtendedProcess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.echocat.jomon.runtime.codec.Md5Utils.md5Of;

public class ProcessBasedPasswordRequest extends PasswordRequest {

    @Nonnull
    private final ExtendedProcess _process;
    private final byte[] _md5;

    public ProcessBasedPasswordRequest(@Nonnull ExtendedProcess process, @Nonnull CacheType cacheType, @Nullable ResponseType... responseTypes) {
        super(cacheType, responseTypes);
        _process = process;
        _md5 = tryToGetMd5Of(process);
    }

    @Nullable
    protected byte[] tryToGetMd5Of(@Nonnull ExtendedProcess process) {
        final File executable = process.getExecutable();
        return executable != null ? tryToGetMd5Of(executable) : null;
    }

    @Nullable
    protected byte[] tryToGetMd5Of(@Nonnull File file) {
        byte[] result;
        try (final InputStream is = new FileInputStream(file)) {
            result = md5Of(is).asBytes();
        } catch (IOException ignored) {
            result = null;
        }
        return result;
    }

    @Nonnull
    public ExtendedProcess getProcess() {
        return _process;
    }

    @Nullable
    protected byte[] getMd5() {
        return _md5;
    }

    @Nonnull
    @Override
    public String formatRemoteAsHtml(@Nonnull Resources resources) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>nobr { white-space: nowarp; font-family: monospace; }</style></head><body style='font-family: sans-serif; font-size: 1em'>");

        sb.append("<b>").append(resources.getString(_process.isJavaProcess() ? "javaProcess" : "process")).append(":&nbsp;<nobr>#").append(_process.getId()).append("</nobr></b><br/>");
        if (_process.getMainClass() != null) {
            sb.append(resources.getString("mainClass")).append(":&nbsp;<nobr>").append(escapeHtml4(_process.getMainClass())).append("</nobr><br/>");
        }
        sb.append(resources.getString("executable")).append(":&nbsp;<nobr>").append(formatExecutable(resources)).append("</nobr><br/>");
        sb.append(resources.getString("commandLine")).append(":&nbsp;<nobr>").append(formatCommandLine()).append("</nobr>");

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
            final ExtendedProcess thisProcess = getProcess();
            final ProcessBasedPasswordRequest that = (ProcessBasedPasswordRequest) o;
            final String mainClass = thisProcess.getMainClass();
            if (mainClass != null) {
                result = mainClass.equals(that.getProcess().getMainClass());
            } else {
                final byte[] md5 = getMd5();
                if (md5 != null) {
                    result = Arrays.equals(md5, that.getMd5());
                } else {
                    result = thisProcess.equals(that.getProcess());
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int result;
        final ExtendedProcess process = getProcess();
        final String mainClass = process.getMainClass();
        if (mainClass != null) {
            result = mainClass.hashCode();
        } else {
            final byte[] md5 = getMd5();
            if (md5 != null) {
                result = Arrays.hashCode(md5);
            } else {
                result = process.hashCode();
            }
        }
        return result;
    }

}
