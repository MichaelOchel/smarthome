/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.digitalstrom.internal.providers;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

/**
 * The {@link BaseDsI18n} provides the internationalization service in form of the {@link I18nProvider} of the
 * digitalSTROM-Bindings. So this class can be implement e.g. by provider implementations like the
 * {@link org.eclipse.smarthome.core.thing.type.ChannelTypeProvider}.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public abstract class BaseDsI18n {

    public final static String LABEL_ID = "label";
    public final static String DESC_ID = "desc";
    public final static String SEPERATOR = "_";

    private I18nProvider i18n = null;
    private Bundle bundle = null;

    /**
     * Initializes the {@link BaseDsI18n}.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
        init();
    }

    /**
     * Will be call after the {@link BaseDsI18n} is initialized and can be overridden by subclasses to handle some
     * initial jobs.
     */
    protected void init() {
        // Can be overridden by subclasses
    }

    /**
     * Disposes the {@link BaseDsI18n}.
     *
     * @param componentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    /**
     * Sets the {@link I18nProvider} at the {@link BaseDsI18n}.
     *
     * @param i18n
     */
    protected void setI18nProvider(I18nProvider i18n) {
        this.i18n = i18n;
    };

    /**
     * Unsets the {@link I18nProvider} at the {@link BaseDsI18n}.
     *
     * @param i18n
     */
    protected void unsetI18nProvider(I18nProvider i18n) {
        this.i18n = null;
    };

    /**
     * Returns the internationalized text in the language of the {@link Locale} of the given key. If the key an does not
     * exist at the internationalization of the {@link Locale} the {@link Locale#ENGLISH} will be used. If the key dose
     * not exists in {@link Locale#ENGLISH}, too, the key will be returned.
     *
     * @param key
     * @param locale
     * @return internationalized text
     */
    protected String getText(String key, Locale locale) {
        return i18n != null ? i18n.getText(bundle, key, i18n.getText(bundle, key, key, Locale.ENGLISH), locale) : key;
    }

    /**
     * Returns the internationalized label in the language of the {@link Locale} of the given key.
     *
     * @param key
     * @param locale
     * @return internationalized label
     * @see #getText(String, Locale)
     */
    protected String getLabelText(String key, Locale locale) {
        return getText(buildIdentifier(key, LABEL_ID), locale);
    }

    /**
     * Returns the internationalized description in the language of the {@link Locale} of the given key.
     *
     * @param key
     * @param locale
     * @return internationalized description
     * @see #getText(String, Locale)
     */
    protected String getDescText(String key, Locale locale) {
        return getText(buildIdentifier(key, DESC_ID), locale);
    }

    /**
     * Builds the key {@link String} through the given {@link Object}s. <br>
     * The key will be build as lower case {@link Object#toString()} + {@link #SEPERATOR} + {@link Object#toString()} +
     * ... , so the result {@link String} will be look like "object1_object2"
     *
     * @param parts
     * @return
     */
    public static String buildIdentifier(Object... parts) {
        return StringUtils.join(parts, SEPERATOR).toLowerCase();
    }
}
