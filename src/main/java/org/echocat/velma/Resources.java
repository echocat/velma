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

import org.echocat.jomon.runtime.ManifestInformationFactory;
import org.echocat.jomon.runtime.i18n.RecursiveResourceBundleFactory;
import org.echocat.jomon.runtime.i18n.ResourceBundles;
import org.echocat.jomon.runtime.i18n.ResourceBundlesFactory;
import org.echocat.velma.support.ProcessDetector;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.unmodifiableList;
import static java.util.Locale.getDefault;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public class Resources {

    public static final String FALLBACK_APPLICATION_NAME = "echocat Velma";

    private final ProcessDetector _processDetector = new ProcessDetector();
    private final ManifestInformationFactory _manifestInformationFactory;
    private final String _applicationName;
    private final String _version;
    private final ResourceBundles _resourceBundles;
    private final List<? extends Image> _icons;
    private final List<URL> _iconUrls;

    public Resources(@Nonnull Class<?> inRelationTo) {
        _manifestInformationFactory = loadManifestInformationFactory(inRelationTo);
        _applicationName = loadApplicationNameBy(_manifestInformationFactory);
        _version = tryLoadVersionBy(_manifestInformationFactory);
        _resourceBundles = loadBundles(inRelationTo);
        _iconUrls = loadIconUrls(inRelationTo);
        _icons = loadIcons(_iconUrls);

    }

    @Nonnull
    protected ManifestInformationFactory loadManifestInformationFactory(@Nonnull Class<?> inRelationTo) {
        return new ManifestInformationFactory(inRelationTo);
    }

    @Nonnull
    protected String loadApplicationNameBy(@Nonnull ManifestInformationFactory manifestInformationFactory) {
        final String titleFromManifest = manifestInformationFactory.getImplementationTitle();
        return (titleFromManifest != null ? titleFromManifest : FALLBACK_APPLICATION_NAME);
    }

    @Nullable
    protected String tryLoadVersionBy(@Nonnull ManifestInformationFactory manifestInformationFactory) {
        return manifestInformationFactory.getImplementationVersion();
    }

    @Nonnull
    protected ResourceBundles loadBundles(@Nonnull Class<?> inRelationTo) {
        final ResourceBundlesFactory factory = new ResourceBundlesFactory(new RecursiveResourceBundleFactory());
        factory.setLocales(newHashSet(
            null,
            Locale.GERMAN
        ));
        return factory.getFor(inRelationTo);
    }

    @Nonnull
    protected List<URL> loadIconUrls(@Nonnull Class<?> inRelationTo) {
        final List<URL> result = new ArrayList<>();
        for (int size = 1; size <= 256; size++) {
            final URL resource = inRelationTo.getResource("icon." + size + "x" + size + ".png");
            if (resource != null) {
                result.add(resource);
            }
        }
        return unmodifiableList(result);
    }

    @Nonnull
    protected List<? extends Image> loadIcons(@Nonnull List<URL> iconUrls) {
        final List<Image> result = new ArrayList<>();
        for (URL iconUrl : iconUrls) {
            final ImageIcon image = new ImageIcon(iconUrl);
            if (image.getIconHeight() >= 0) {
                result.add(image.getImage());
            }
        }
        return unmodifiableList(result);
    }

    @Nonnull
    public ResourceBundle getResourceBundle() {
        return _resourceBundles.getBundle(getDefault());
    }

    @Nonnull
    public List<? extends Image> getIcons() {
        return _icons;
    }

    @Nonnull
    public Image getIcon(@Nonnegative int size) {
        Image result = null;
        for (Image icon : _icons) {
            if (icon.getHeight(null) == size) {
                result = icon;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Could not find an image for size " + size + "x" + size + ".");
        }
        return result;
    }

    @Nonnull
    public List<URL> getIconUrls() {
        return _iconUrls;
    }

    @Nonnull
    public URL getIconUrl(@Nonnegative int size) {
        URL result = null;
        for (URL iconUrl : _iconUrls) {
            if (iconUrl.toExternalForm().endsWith(size + ".png")) {
                result = iconUrl;
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Could not find an image for size " + size + "x" + size + ".");
        }
        return result;
    }

    @Nonnull
    public String getString(@Nonnull String key) throws MissingResourceException {
        return _resourceBundles.get(getDefault(), key);
    }

    @Nonnull
    public String format(@Nonnull String key, @Nullable Object... arguments) throws MissingResourceException {
        return MessageFormat.format(getString(key), arguments);
    }

    @Nonnull
    public String formatEscaped(@Nonnull String key, @Nullable Object... arguments) throws MissingResourceException {
        return escapeHtml4(format(key, arguments));
    }

    @Nonnull
    public String getApplicationName() {
        return _applicationName;
    }

    @Nullable
    public String getVersion() {
        return _version;
    }

    @Nonnull
    public String getFullApplicationName() {
        return _applicationName + (_version != null ? " " + _version : "");
    }

    @Nonnull
    public ProcessDetector getProcessDetector() {
        return _processDetector;
    }
}
