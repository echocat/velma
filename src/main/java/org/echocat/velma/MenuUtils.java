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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.System.exit;

public class MenuUtils {

    @Nonnull
    public static PopupMenu popupMenu(@Nullable MenuItem... items) {
        final PopupMenu menu = new PopupMenu();
        if (items != null) {
            for (MenuItem item : items) {
                menu.add(item);
            }
        }
        return menu;
    }

    @Nonnull
    public static MenuItem menuItem(@Nonnull Resources resources, @Nonnull String titleKey, @Nonnull ActionListener listener) {
        final MenuItem item = new MenuItem(resources.getString(titleKey));
        item.addActionListener(listener);
        return item;
    }

    @Nonnull
    public static ActionListener exitApplicationListener() {
        return new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
            //noinspection CallToSystemExit
            exit(0);
        }};
    }

    private MenuUtils() {}

}
