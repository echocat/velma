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

import org.echocat.jomon.process.ProcessRepository;
import org.echocat.velma.support.ExtendedProcess.Impl;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarNotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.net.InetAddress.getByName;
import static org.echocat.jomon.process.ProcessRepository.getInstance;
import static org.hyperic.jni.ArchLoader.IS_WIN32;
import static org.hyperic.sigar.NetFlags.CONN_CLIENT;
import static org.hyperic.sigar.NetFlags.CONN_TCP;

public class ProcessDetector {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessDetector.class);

    private static final ProcessRepository REPOSITORY = getInstance();

    @Nullable
    public ExtendedProcess findProcessOn(@Nonnull String expectedLocalHostName, @Nonnegative int expectedLocalPort) {
        org.echocat.jomon.process.Process result;
        try {
            result = findProcessWithCheckedOn(expectedLocalHostName, expectedLocalPort);
        } catch (SigarException | UnknownHostException e) {
            LOG.info("Could not get process for " + expectedLocalHostName + ":" + expectedLocalPort + ". Ignoring.", e);
            result = null;
        } catch (Exception e) {
            throw new RuntimeException("Could not get process for " + expectedLocalHostName + ":" + expectedLocalPort + ".", e);
        }
        return result != null ? transformIfPossible(result) : null;
    }

    @Nonnull
    private ExtendedProcess transformIfPossible(@Nonnull org.echocat.jomon.process.Process original) {
        boolean isJava;
        String mainClass;
        try {
            mainClass = MainClassDiscovery.findMainClassOf(original);
            isJava = true;
        } catch (Exception ignored) {
            isJava = false;
            mainClass = null;
        }
        return new Impl(original, isJava, mainClass);
    }

    @Nullable
    protected org.echocat.jomon.process.Process findProcessWithCheckedOn(@Nonnull String expectedLocalHostName, @Nonnegative int expectedLocalPort) throws Exception {
        final InetAddress explectedLocalAddress = getByName(expectedLocalHostName);
        org.echocat.jomon.process.Process result;
        try {
            result = findProcessWithSigarOn(explectedLocalAddress, expectedLocalPort);
        } catch (SigarNotImplementedException e) {
            if (IS_WIN32) {
                result = findProcessWithWin32NetstatOn(explectedLocalAddress, expectedLocalPort);
            } else {
                throw e;
            }
        }
        return result;
    }

    @Nullable
    protected org.echocat.jomon.process.Process findProcessWithSigarOn(@Nonnull InetAddress expectedLocalAddress, @Nonnegative int expectedLocalPort) throws Exception {
        final Sigar sigar = new Sigar();
        final NetConnection[] connections = sigar.getNetConnectionList(CONN_CLIENT | CONN_TCP);
        org.echocat.jomon.process.Process result = null;
        for (NetConnection connection : connections) {
            if (connection.getLocalPort() == expectedLocalPort) {
                try {
                    final InetAddress currentAddress = getByName(connection.getLocalAddress());
                    if (expectedLocalAddress.equals(currentAddress)) {
                        final long pid = sigar.getProcPort(connection.getType(), connection.getLocalPort());
                        if (pid != 0) {
                            result = REPOSITORY.findOneBy(pid);
                        }
                        break;
                    }
                } catch (UnknownHostException ignored) {}
            }
        }
        return result;
    }

    @Nullable
    protected org.echocat.jomon.process.Process findProcessWithWin32NetstatOn(@Nonnull InetAddress expectedLocalAddress, @Nonnegative int expectedLocalPort) throws Exception {
        final Process p = new ProcessBuilder("netstat", "-no", "-p", "tcp").start();
        org.echocat.jomon.process.Process result = null;
        try (final InputStream is = p.getInputStream()) {
            try (Scanner scanner = new Scanner(is)) {
                final Pattern pattern = Pattern.compile("TCP\\s+([^\\s]+):(\\d+)\\s+[^\\s]+\\s+[^\\s]+\\s+(\\d+)");
                while (scanner.findWithinHorizon(pattern, 0) != null) {
                    final MatchResult match = scanner.match();
                    try {
                        final long currentPort = Long.parseLong(match.group(2));
                        if (expectedLocalPort == currentPort) {
                            final InetAddress currentAddress = getByName(match.group(1));
                            if (expectedLocalAddress.equals(currentAddress)) {
                                final long pid = Long.parseLong(match.group(3));
                                result = REPOSITORY.findOneBy(pid);
                                break;
                            }
                        }
                    } catch (UnknownHostException | NumberFormatException ignored) {}
                }
            }
        }
        return result;
    }


}
