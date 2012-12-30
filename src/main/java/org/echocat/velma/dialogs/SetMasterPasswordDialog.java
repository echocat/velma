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

import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.echocat.velma.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.echocat.velma.PasswordRequest.CacheType.notCachable;

public class SetMasterPasswordDialog extends ActivateEnabledDialog {

    private static final Logger LOG = LoggerFactory.getLogger(RequestMasterPasswordDialog.class);

    private final Configuration _configuration;
    private final PasswordStorage _passwordStorage;

    private final JPasswordField _passwordField;
    private final JPasswordField _repeatPasswordField;
    private final JButton _saveButton;


    public SetMasterPasswordDialog(@Nonnull Resources resources, @Nullable PasswordStorage passwordStorage, @Nonnull Configuration configuration) {
        super(resources, "setMasterPasswordTitle");
        _passwordStorage = passwordStorage;
        _configuration = configuration;
        createIntroduction(resources, passwordStorage);
        _passwordField = createPasswordField(resources);
        _repeatPasswordField = createRepeatPasswordField(resources);
        _saveButton = createButtonBarAndReturnCopyToClipboardAndCloseButton(resources, passwordStorage);
    }

    @Override
    @Nonnull
    protected MigLayout createLayout() {
        return new MigLayout(
            new LC().fillX().wrapAfter(2).width("480px").noCache(),
            new AC().gap("rel").grow().fill(),
            new AC().gap("10")
        );
    }

    private void createIntroduction(@Nonnull Resources resources, @Nullable PasswordStorage passwordStorage) {
        final JTextPane text = new JTextPane();
        text.setMargin(new Insets(0, 0, 0, 0));
        text.setContentType("text/html");
        final String introduction = resources.getString(passwordStorage != null ? "changeMasterPasswordIntroduction" : "setInitialMasterPasswordIntroduction");
        text.setText("<html><body style='font-family: sans; font-size: 1em'>" + introduction + "</body></html>");
        text.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        text.setBackground(SystemColor.control);
        text.setEditable(false);
        add(text, new CC().spanX(2).growX().minWidth("10px"));
    }

    private JPasswordField createPasswordField(@Nonnull Resources resources) {
        add(new JLabel(resources.getString("password") + ":"), new CC().alignX("right"));

        final JPasswordField passwordField = new JPasswordField();
        passwordField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                checkPasswords();
            }
        });
        add(passwordField, new CC().minWidth("40px"));
        return passwordField;
    }

    private JPasswordField createRepeatPasswordField(@Nonnull Resources resources) {
        add(new JLabel(resources.getString("repeatPassword") + ":"), new CC().alignX("right"));

        final JPasswordField passwordField = new JPasswordField();
        passwordField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                checkPasswords();
            }
        });

        add(passwordField, new CC().minWidth("40px"));
        return passwordField;
    }

    private JButton createButtonBarAndReturnCopyToClipboardAndCloseButton(@Nonnull Resources resources, @Nullable PasswordStorage passwordStorage) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JButton saveButton = new JButton(resources.getString("save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        panel.add(saveButton);

        if (passwordStorage != null) {
            final JButton cancelButton = new JButton(resources.getString("cancel"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });
            panel.add(cancelButton);
        }

        add(panel, new CC().spanX(2).alignX("right"));

        return saveButton;
    }

    @Override
    public void activate() {
        if (_passwordStorage != null) {
            final String userAgent = getResources().format("xDialog", getTitle());
            final Password password = _passwordStorage.getPassword(new PasswordRequest(userAgent, notCachable));
            if (password != null) {
                super.activate();
            }
        } else {
            super.activate();
        }
    }
    
    private boolean doesPasswordsMatch() {
        final String password = getPassword();
        final String repeatPassword = getRepeatPassword();
        return password.equals(repeatPassword);
    }

    private boolean areAllPasswordsProvided() {
        return !getPassword().isEmpty() && !getRepeatPassword().isEmpty();
    }

    private void checkPasswords() {
        if (areAllPasswordsProvided()) {
            if (doesPasswordsMatch()) {
                _saveButton.setEnabled(true);
            } else {
                _saveButton.setEnabled(false);
            }
        } else {
            _saveButton.setEnabled(false);
        }
    }

    @Override
    public void finish() {
        if (areAllPasswordsProvided() && doesPasswordsMatch()) {
            try {
                _configuration.setMasterPassword(getPassword());
                _configuration.save();
            } catch (Exception e) {
                LOG.error("Could not save the configuration. This is fatal and it means that the change of the master password does not work. Currently the old password is active.", e);
            }
            super.finish();
        }
    }

    @Override
    public void cancel() {
        if (_passwordStorage != null) {
            super.cancel();
        }
    }

    private String getPassword() {
        return new String(_passwordField.getPassword());
    }

    private String getRepeatPassword() {
        return new String(_repeatPasswordField.getPassword());
    }

    @Nonnull
    public static ActionListener showMasterPasswordDialogListener(@Nonnull final Resources resources, @Nullable final PasswordStorage passwordStorage, @Nonnull final Configuration configuration) {
        return new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
            new SetMasterPasswordDialog(resources, passwordStorage, configuration).activate();
        }};
    }
}
