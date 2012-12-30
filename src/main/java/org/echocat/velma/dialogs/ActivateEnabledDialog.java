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

package org.echocat.velma.dialogs;

import org.echocat.velma.Resources;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.SystemColor.window;

public abstract class ActivateEnabledDialog extends JDialog {

    private final ActionListener _disposeActionListener = new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
        dispose();
    }};

    private final Resources _resources;

    protected ActivateEnabledDialog(@Nonnull Resources resources, @Nonnull String titleKey) {
        _resources = resources;

        setTitle(resources.getString(titleKey));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        registerRootKeyStrokes();
        registerRootListeners();
        setLayout(createLayout());
        setIconImages(resources.getIcons());
        setModalityType(APPLICATION_MODAL);
        setBackground(window);
        setAlwaysOnTop(true);

    }

    public void activate() {
        pack();
        setLocationRelativeTo(getRootPane());
        pack();
        setLocationRelativeTo(getRootPane());
        setVisible(true);
    }

    @Nonnull
    protected abstract LayoutManager createLayout();

    protected void registerRootKeyStrokes() {
        final KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        final InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(enterStroke, "ENTER");
        inputMap.put(escapeStroke, "ESCAPE");
        getRootPane().getActionMap().put("ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
    }

    protected void registerRootListeners() {
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) {
            cancel();
        }});
    }

    public void finish() {
        dispose();
    }

    public void cancel() {
        dispose();
    }

    @Nonnull
    protected Resources getResources() {
        return _resources;
    }

    @Nonnull
    protected ActionListener getDisposeActionListener() {
        return _disposeActionListener;
    }


}
