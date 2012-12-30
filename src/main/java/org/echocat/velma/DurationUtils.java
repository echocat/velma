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

import org.echocat.jomon.runtime.util.Duration;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.echocat.jomon.runtime.StringUtils.addElement;

public class DurationUtils {

    public static final List<Duration> DEFAULT_DURATIONS = unmodifiableList(asList(
        null,
        new Duration("15s"),
        new Duration("30s"),
        new Duration("45s"),
        new Duration("1m"),
        new Duration("15m"),
        new Duration("30m"),
        new Duration("45m"),
        new Duration("1h"),
        new Duration("3h"),
        new Duration("6h"),
        new Duration("12h"),
        new Duration("1d"),
        new Duration("7d")
    ));
    
    @Nonnull
    public static String formatDuration(@Nonnull ResourceBundle resourceBundle, @Nonnull Duration duration) {
        final Map<TimeUnit, Long> unitToValue = duration.toUnitToValue();
        final StringBuilder sb = new StringBuilder();
        for (Entry<TimeUnit, Long> unitAndValue : unitToValue.entrySet()) {
            final String unitPattern = resourceBundle.getString("x" + capitalize(unitAndValue.getKey().toString().toLowerCase()));
            addElement(sb, " ", format(unitPattern, unitAndValue.getValue()));
        }
        return sb.toString();
    }

    @Nonnull
    public static Duration parseFromString(@Nonnull String plain) throws IllegalArgumentException {
        final String[] parts = plain.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("The given string is not a valid string representation of a duration: " + plain);
        }
        final TimeUnit timeUnit;
        try {
            timeUnit = TimeUnit.valueOf(parts[0]);    
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The given string is not a valid string representation of a duration: " + plain, e);
        }
        final Long amount;
        try {
            amount = Long.valueOf(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The given string is not a valid string representation of a duration: " + plain, e);
        }
        return new Duration(amount, timeUnit);
    }
    
    private DurationUtils() {}

}
