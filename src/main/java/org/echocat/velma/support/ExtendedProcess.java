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

package org.echocat.velma.support;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public interface ExtendedProcess extends org.echocat.jomon.process.Process {

    public boolean isJavaProcess();

    @Nullable
    public String getMainClass();

    public static class Impl implements ExtendedProcess {

        @Nonnull
        private final org.echocat.jomon.process.Process _delegate;
        private final boolean _javaProcess;
        @Nullable
        private final String _mainClass;

        public Impl(@Nonnull org.echocat.jomon.process.Process delegate, boolean isJavaProcess, @Nullable String mainClass) {
            _delegate = delegate;
            _javaProcess = isJavaProcess;
            _mainClass = mainClass;
        }

        @Override
        public boolean isJavaProcess() {
            return _javaProcess;
        }

        @Nullable
        @Override
        public String getMainClass() {
            return _mainClass;
        }

        @Override
        public long getId() {
            return _delegate.getId();
        }

        @Override
        @Nullable
        public File getExecutable() {
            return _delegate.getExecutable();
        }

        @Override
        @Nullable
        public String[] getCommandLine() {
            return _delegate.getCommandLine();
        }

        @Override
        public boolean isPathCaseSensitive() {
            return _delegate.isPathCaseSensitive();
        }

        @Override
        public int hashCode() {
            return _delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof org.echocat.jomon.process.Process && _delegate.equals(obj));
        }

        @Override
        public String toString() {
            return _delegate.toString();
        }
    }

}
