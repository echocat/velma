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

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.Color.red;
import static java.awt.SystemColor.controlText;
import static java.awt.Toolkit.getDefaultToolkit;
import static org.echocat.velma.PasswordRequest.CacheType.notCachable;
import static org.echocat.velma.SecuritySettingsUtils.decryptMasterPassword;
import static org.echocat.velma.SecuritySettingsUtils.encryptPassword;

public class PasswordEncryptionDialog extends ActivateEnabledDialog {

    private final PasswordStorage _passwordStorage;
    private final JPasswordField _passwordField;
    private final JPasswordField _repeatPasswordField;
    private final JTextField _encryptedPasswordField;
    private final JButton _copyToClipboardAndCloseButton;
    
    private volatile Password _masterPassword;

    public PasswordEncryptionDialog(@Nonnull Resources resources, @Nonnull PasswordStorage passwordStorage) {
        super(resources, "passwordEncryptionTitle");
        _passwordStorage = passwordStorage;

        createIntroduction(resources);
        _passwordField = createPasswordField(resources);
        _repeatPasswordField = createRepeatPasswordField(resources);
        _encryptedPasswordField = createEncryptedPasswordField(resources);
        _copyToClipboardAndCloseButton = createButtonBarAndReturnCopyToClipboardAndCloseButton(resources);
    }

    @Nonnull
    @Override
    protected MigLayout createLayout() {
        return new MigLayout(
            new LC().fillX().wrapAfter(2).width("480px").noCache(),
            new AC().gap("rel").grow().fill(),
            new AC().gap("10")
        );
    }

    private void createIntroduction(@Nonnull Resources resources) {
        final JTextPane text = new JTextPane();
        text.setMargin(new Insets(0, 0, 0, 0));
        text.setContentType("text/html");
        text.setText("<html><body style='font-family: sans; font-size: 1em'>" + resources.getString("passwordEncryptionIntroduction") + "</body></html>");
        text.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        text.setBackground(new Color(255, 255, 255, 0));
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

    private JTextField createEncryptedPasswordField(@Nonnull Resources resources) {
        add(new JLabel(resources.getString("encryptedPassword") + ":"), new CC().alignX("right"));
        final JTextField textField = new JTextField();
        textField.setEditable(false);
        add(textField, new CC().minWidth("40px"));
        return textField;
    }

    private JButton createButtonBarAndReturnCopyToClipboardAndCloseButton(@Nonnull Resources resources) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JButton copyToClipboardAndClose = new JButton(resources.getString("copyToClipboardAndClose"));
        final JButton cancelButton = new JButton(resources.getString("cancel"));

        copyToClipboardAndClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        panel.add(copyToClipboardAndClose);
        panel.add(cancelButton);
        add(panel, new CC().spanX(2).alignX("right"));

        return copyToClipboardAndClose;
    }

    @Override
    public void activate() {
        final String userAgent = getResources().format("xDialog", getTitle());
        _masterPassword = _passwordStorage.getPassword(new UserAgentBasedPasswordRequest(userAgent, notCachable));
        if (_masterPassword != null) {
            _passwordField.requestFocusInWindow();
            checkPasswords();
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
                final String masterPassword = decryptMasterPassword(_masterPassword.getEncryptedValue());
                _copyToClipboardAndCloseButton.setEnabled(true);
                _encryptedPasswordField.setForeground(controlText);
                _encryptedPasswordField.setText(encryptPassword(getPassword(), masterPassword));
            } else {
                _copyToClipboardAndCloseButton.setEnabled(false);
                _encryptedPasswordField.setForeground(red);
                _encryptedPasswordField.setText(getResources().getString("passwordsDoesNotMatch"));
            }
        } else {
            _copyToClipboardAndCloseButton.setEnabled(false);
            _encryptedPasswordField.setForeground(red);
            _encryptedPasswordField.setText(getResources().getString("noPasswordsProvided"));
        }
    }

    @Override
    public void finish() {
        if (areAllPasswordsProvided() && doesPasswordsMatch()) {
            getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(_encryptedPasswordField.getText()), null);
            super.finish();
        }
    }

    private String getPassword() {
        return new String(_passwordField.getPassword());
    }

    private String getRepeatPassword() {
        return new String(_repeatPasswordField.getPassword());
    }

    @Nonnull
    public static ActionListener showEncryptPasswordDialogListener(@Nonnull final Resources resources, @Nonnull final PasswordStorage passwordStorage) {
        return new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
            new PasswordEncryptionDialog(resources, passwordStorage).activate();
        }};
    }
}
