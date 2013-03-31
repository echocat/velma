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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.runtime.reflection.ClassUtils.getPublicMethodOf;

public class MainClassDiscovery {

    private static final Logger LOG = LoggerFactory.getLogger(MainClassDiscovery.class);
    private static final Class<?> MONITORED_VM_CLASS = findClass("sun.jvmstat.monitor.MonitoredVm");

    private static final Class<?> VM_IDENTIFIER_CLASS = MONITORED_VM_CLASS != null ? findClass("sun.jvmstat.monitor.VmIdentifier") : null;
    private static final Constructor<?> VM_IDENTIFIER_CONSTRUCTOR = VM_IDENTIFIER_CLASS != null ? getPublicConstructorOf(VM_IDENTIFIER_CLASS, String.class) : null;

    private static final Class<?> LOCAL_MONITORED_VM_CLASS = VM_IDENTIFIER_CLASS != null ? findClass("sun.jvmstat.perfdata.monitor.protocol.local.LocalMonitoredVm") : null;
    private static final Constructor<?> LOCAL_MONITORED_VM_CONSTRUCTOR = LOCAL_MONITORED_VM_CLASS != null ? getPublicConstructorOf(LOCAL_MONITORED_VM_CLASS, VM_IDENTIFIER_CLASS, int.class) : null;
    private static final Method LOCAL_MONITORED_VM_DETACH_METHOD = LOCAL_MONITORED_VM_CLASS != null ? getPublicMethodOf(LOCAL_MONITORED_VM_CLASS, void.class, "detach") : null;

    private static final Class<?> MONITOR_VM_UTIL_CLASS = LOCAL_MONITORED_VM_CLASS != null ? findClass("sun.jvmstat.monitor.MonitoredVmUtil") : null;
    private static final Method COMMAND_LINE_METHOD = MONITOR_VM_UTIL_CLASS != null ? getPublicStaticMethodOf(MONITOR_VM_UTIL_CLASS, String.class, "commandLine", MONITORED_VM_CLASS) : null;
    private static final Method MAIN_CLASS_METHOD = MONITOR_VM_UTIL_CLASS != null ? getPublicStaticMethodOf(MONITOR_VM_UTIL_CLASS, String.class, "mainClass", MONITORED_VM_CLASS, boolean.class): null;

    @Nullable
    public static String findMainClassOf(@Nonnull org.echocat.jomon.process.Process process) throws Exception {
        final String result;
        if (VM_IDENTIFIER_CONSTRUCTOR != null && LOCAL_MONITORED_VM_CONSTRUCTOR != null && COMMAND_LINE_METHOD != null && MAIN_CLASS_METHOD != null) {
            final Object vmId = VM_IDENTIFIER_CONSTRUCTOR.newInstance(Long.toString(process.getId()));
            final Object lmVm = LOCAL_MONITORED_VM_CONSTRUCTOR.newInstance(vmId, 1000);
            try {
                final File executable = process.getExecutable();
                if (executable != null) {
                    final String commandLine = ((String) COMMAND_LINE_METHOD.invoke(null, lmVm)).toLowerCase();
                    if (!commandLine.startsWith(executable.getPath().toLowerCase())
                        && !commandLine.startsWith(executable.getAbsolutePath().toLowerCase())
                        && !commandLine.startsWith(executable.getCanonicalPath().toLowerCase())) {
                        result = (String) MAIN_CLASS_METHOD.invoke(null, lmVm, true);
                    } else {
                        result = null;
                    }
                } else {
                    result = (String) MAIN_CLASS_METHOD.invoke(null, lmVm, false);
                }
            } finally {
                LOCAL_MONITORED_VM_DETACH_METHOD.invoke(lmVm);
            }
        } else {
            result = null;
        }
        return isEmpty(result) ? null : result.replace('/', '.');
    }


    @Nullable
    protected static Method getPublicStaticMethodOf(@Nonnull Class<?> ofType, @Nonnull Class<?> returnType, @Nonnull String methodName, @Nullable Class<?>... parameterTypes) {
        try {
            final Method method = ofType.getMethod(methodName, parameterTypes);
            final int modifiers = method.getModifiers();
            if (!isStatic(modifiers)) {
                throw new IllegalArgumentException(method + " is not static.");
            }
            if (!isPublic(modifiers)) {
                throw new IllegalArgumentException(method + " is not public.");
            }
            if (!returnType.equals(method.getReturnType())) {
                throw new IllegalArgumentException(method + " does not return " + returnType.getName() + ".");
            }
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find public static " + returnType.getSimpleName() + " " + ofType.getSimpleName() + "." + methodName + "(" + Arrays.toString(parameterTypes) + ").", e);
        }
    }

    @Nullable
    protected static Constructor<?> getPublicConstructorOf(@Nonnull Class<?> ofType, @Nullable Class<?>... parameterTypes) {
        try {
            return ofType.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find constructor " + ofType.getName() + "(" + Arrays.toString(parameterTypes) + ").", e);
        }
    }

    @Nullable
    protected static Class<?> findClass(@Nonnull String name) {
        Class<?> result;
        try {
            result = Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            LOG.info("Could not find class '" + name + "' in classpath. Normally this is part of the tools.jar of an Sun-/Oracle-/OpenJDK." +
                " Put it to classpath to make is possible to discover mainClasses of JavaProcesses. Currently this feature is disabled.");
            result = null;
        }
        return result;
    }

    private MainClassDiscovery() {}


}
