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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.Runtime.getRuntime;
import static org.echocat.jomon.runtime.Log4JUtils.configureRuntime;
import static org.echocat.jomon.runtime.LookAndFeelUtils.tryToSetSystemLookAndFeel;

public class Boot {

    private static final Logger LOG = LoggerFactory.getLogger(Boot.class);
    private static final Resources RESOURCES = new Resources(Boot.class);

    public static void main(String[] args) throws Exception {
        tryToSetSystemLookAndFeel();
        final Configuration configuration = new Configuration();
        configuration.load();
        final Velma velma = start(RESOURCES, configuration);
        getRuntime().addShutdownHook(new Thread("destroyer") { @Override public void run() {
            Boot.stop(velma, RESOURCES);
        }});
    }

    @Nonnull
    private static Velma start(@Nonnull Resources resources, @Nonnull Configuration configuration) throws Exception {
        configureLog4j();
        LOG.info("Starting " + resources.getFullApplicationName() + "...");
        final Velma velma;
        boolean success = false;
        try {
            velma = new Velma(resources, configuration);
            success = true;
        } finally {
            if (success) {
                LOG.info("Starting " + resources.getFullApplicationName() + "... DONE!");
            } else {
                LOG.error("Starting " + resources.getFullApplicationName() + "... FAILED! (See following messages for cause)");
            }
        }
        return velma;
    }

    private static void stop(@Nonnull Velma velma, @Nonnull Resources resources) {
        try {
            LOG.info("Stopping " + resources.getFullApplicationName() + "...");
            velma.close();
        } catch (Exception e) {
            LOG.error("Stopping " + resources.getFullApplicationName() + "... FAILED!", e);
        }
    }

    private static void configureLog4j() {
        configureRuntime(Boot.class.getResource("log4j.xml"));
    }

    private Boot() {}
}
