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
import org.echocat.jomon.process.ProcessRepository;
import org.echocat.jomon.runtime.iterators.CloseableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvmstat.monitor.VmIdentifier;
import sun.jvmstat.perfdata.monitor.protocol.local.LocalMonitoredVm;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.jomon.process.ProcessQuery.query;
import static org.echocat.jomon.runtime.Log4JUtils.configureRuntime;
import static sun.jvmstat.monitor.MonitoredVmUtil.commandLine;
import static sun.jvmstat.monitor.MonitoredVmUtil.mainClass;

public class Test {

    private static final Logger LOG = LoggerFactory.getLogger(Test.class);

    public static void main(String[] args) throws Exception {
        configureRuntime(Test.class.getResource("log4j.xml"));
        try (final ProcessRepository repository = ProcessRepository.getInstance()) {
            try (final CloseableIterator<Process> i = repository.findBy(query())) {
                while (i.hasNext()) {
                    final Process process = i.next();
                    //LOG.info("Check #" + process.getId() + "...");
                    final File executable = process.getExecutable();
                    try {
                        final String mainClass = getMainClassOf(process);
                        LOG.info("SUCCESS: ###" + process.getId() + " " + executable + ": " + mainClass);
                    } catch (Exception | InternalError e) {
                        if (executable != null && executable.getName().contains("idea")) {
                            LOG.info("#" + process.getId() + " " + executable + ": " + e.getClass().getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("UseOfSunClasses")
    @Nullable
    private static String getMainClassOf(@Nonnull Process process) throws Exception {
        final VmIdentifier vmId = new VmIdentifier(Long.toString(process.getId()));
        final LocalMonitoredVm lmVm = new LocalMonitoredVm(vmId, 1000);
        try {
            final File executable = process.getExecutable();
            final String result;
            if (executable != null) {
                final String commandLine = commandLine(lmVm).toLowerCase();
                if (!commandLine.startsWith(executable.getPath().toLowerCase())
                    && !commandLine.startsWith(executable.getAbsolutePath().toLowerCase())
                    && !commandLine.startsWith(executable.getCanonicalPath().toLowerCase())) {
                    result = mainClass(lmVm, true);
                } else {
                    result = null;
                }
            } else {
                result = mainClass(lmVm, false);
            }
            return isEmpty(result) ? null : result.replace('/', '.');
        } finally {
            lmVm.detach();
        }
    }
}
