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
import org.echocat.jomon.runtime.util.Duration;
import org.echocat.velma.Configuration;
import org.echocat.velma.Password;
import org.echocat.velma.PasswordRequest;
import org.echocat.velma.PasswordRequest.ResponseType;
import org.echocat.velma.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.awt.Color.red;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.echocat.velma.DurationUtils.DEFAULT_DURATIONS;
import static org.echocat.velma.DurationUtils.formatDuration;
import static org.echocat.velma.PasswordRequest.CacheType.cacheable;
import static org.echocat.velma.PasswordRequest.ResponseType.*;
import static org.echocat.velma.SecuritySettingsUtils.createFakeMasterPassword;
import static org.echocat.velma.SecuritySettingsUtils.encryptMasterPassword;

public class RequestMasterPasswordDialog extends ActivateEnabledDialog {

    private static final Logger LOG = LoggerFactory.getLogger(RequestMasterPasswordDialog.class);

    private final JPasswordField _passwordField;
    private final JComboBox<Duration> _durationBox;
    private final Configuration _configuration;
    private final PasswordRequest _request;
    
    private volatile Password _password;

    public RequestMasterPasswordDialog(@Nonnull Resources resources, @Nonnull Configuration configuration, @Nonnull PasswordRequest request) {
        super(resources, "passwordRequestTitle");
        _configuration = configuration;
        _request = request;

        createIntroduction(resources);
        createRequesterHint(resources, request);
        _passwordField = createPasswordField(resources);
        _durationBox = request.getCacheType() == cacheable ? createExpireSelect(resources) : null;
        createButtonBar(resources);
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
        text.setText("<html><body style='font-family: sans; font-size: 1em'>" + resources.getString("passwordRequestIntroduction") + "</body></html>");
        text.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        text.setBackground(SystemColor.control);
        text.setEditable(false);
        add(text, new CC().spanX(2).growX().minWidth("10px"));
    }

    private void createRequesterHint(@Nonnull Resources resources, @Nonnull PasswordRequest request) {
        final String userAgent = request.getUserAgent();
        add(new JLabel(resources.getString("requestedBy") + ":"), new CC().alignX("right"));
        final JTextField requestByField = new JTextField(userAgent != null ? userAgent : resources.getString("unknownRequester"));
        requestByField.setEditable(false);
        add(requestByField, new CC().minWidth("40px"));
    }

    private JPasswordField createPasswordField(@Nonnull Resources resources) {
        add(new JLabel(resources.getString("password") + ":"), new CC().alignX("right"));

        final JPasswordField passwordField = new JPasswordField();
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (_durationBox != null) {
                    final int index = _durationBox.getSelectedIndex();
                    if (e.getKeyCode() == 38) {
                        if (index > 0) {
                            _durationBox.setSelectedIndex(index - 1);
                        }
                    } else if (e.getKeyCode() == 40) {
                        if (index + 1 < _durationBox.getItemCount()) {
                            _durationBox.setSelectedIndex(index + 1);
                        }
                    } else if (e.getKeyCode() == 36 || e.getKeyCode() == 33) {
                        if (index > 0) {
                            _durationBox.setSelectedIndex(0);
                        }
                    } else if (e.getKeyCode() == 35 || e.getKeyCode() == 34) {
                        if (index + 1 < _durationBox.getItemCount()) {
                            _durationBox.setSelectedIndex(_durationBox.getItemCount() - 1);
                        }
                    }
                }
            }
        });

        add(passwordField, new CC().minWidth("40px"));
        return passwordField;
    }

    private JComboBox<Duration> createExpireSelect(@Nonnull final Resources resources) {
        add(new JLabel(resources.getString("willExpireIn") + ":"), new CC().alignX("right"));
        final List<Duration> durations = new ArrayList<>(DEFAULT_DURATIONS);
        final Duration defaultExpireAfter = _configuration.getDefaultExpireAfter();
        if (!durations.contains(defaultExpireAfter)) {
            durations.add(defaultExpireAfter);
        }
        Collections.sort(durations);
        final JComboBox<Duration> box = new JComboBox<>(durations.toArray(new Duration[durations.size()]));
        box.setSelectedItem(defaultExpireAfter);
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                final JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                result.setText(value instanceof Duration && ((Duration) value).isGreaterThan(0) ? formatDuration(resources.getResourceBundle(), (Duration) value) : resources.getString("expireInstantly"));
                return result;
            }
        });
        add(box, new CC().minWidth("40px"));
        return box;
    }

    private void createButtonBar(@Nonnull Resources resources) {
        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JButton okButton = new JButton(resources.getString("ok"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });
        panel.add(okButton);

        final Set<ResponseType> responseTypes = _request.getResponseTypes();
        if (responseTypes.contains(fakePassword)) {
            final JButton fakeButton = new JButton(resources.getString("fakeResponse"));
            fakeButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
                cancel();
            }});
            panel.add(fakeButton);
        }

        if (responseTypes.contains(empty)) {
            final JButton cancelButton = new JButton(resources.getString("cancel"));
            cancelButton.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {
                cancel();
            }});
            panel.add(cancelButton);
        }

        add(panel, new CC().spanX(2).alignX("right"));
    }

    @Nullable
    public Password request() {
        pack();
        setLocationRelativeTo(getRootPane());
        pack();
        setLocationRelativeTo(getRootPane());
        _passwordField.requestFocusInWindow();
        setVisible(true);
        final Password result;
        final Set<ResponseType> responseTypes = _request.getResponseTypes();
        if (_password != null && responseTypes.contains(password)) {
            result = _password;
        } else if (_password == null && responseTypes.contains(fakePassword)) {
            result = createFakePassword();
        } else if (_password == null && responseTypes.contains(empty)) {
            result = null;
        } else {
            throw new IllegalStateException("Could not select a password response for " + responseTypes + " with " + (_password != null ? "a" : "no") + " valid password.");
        }
        return result;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (_request.getCacheType() == cacheable) {
            _configuration.setDefaultExpireAfter(getDuration());
            try {
                _configuration.save();
            } catch (IOException e) {
                LOG.warn("Could not save configuration.", e);
            }
        }
    }

    @Override
    public void finish() {
        final Duration duration = getDuration();
        final String password = getPassword();
        if (_configuration.isProvidedMasterPasswordValid(password)) {
            _password = new Password(encryptMasterPassword(password), duration);
            super.finish();
        } else {
            _passwordField.setForeground(red);
            _passwordField.setSelectedTextColor(red);
            showMessageDialog(this, getResources().getString("invalidPasswordMessage"), getResources().getString("invalidPassword"), ERROR_MESSAGE);
            _passwordField.setSelectionStart(0);
            _passwordField.setSelectionEnd(_passwordField.getPassword().length);
            _passwordField.requestFocus();
        }
    }

    @Override
    public void cancel() {
        _password = null;
        super.cancel();
    }

    @Nonnull
    protected Password createFakePassword() {
        final String fakeMasterPassword = createFakeMasterPassword();
        return new Password(encryptMasterPassword(fakeMasterPassword), getDuration());
    }

    @Nonnull
    protected Duration getDuration() {
        final Duration duration = _durationBox != null ? (Duration) _durationBox.getSelectedItem() : null;
        return duration != null ? duration : new Duration(0, SECONDS);
    }

    @Nonnull
    private String getPassword() {
        return new String(_passwordField.getPassword());
    }
}
