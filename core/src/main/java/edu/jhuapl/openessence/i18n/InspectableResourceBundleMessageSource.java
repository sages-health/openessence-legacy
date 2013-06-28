/*
 * Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
 *                             All rights reserved.
 *
 * This material may be used, modified, or reproduced by or for the U.S.
 * Government pursuant to the rights granted under the clauses at
 * DFARS 252.227-7013/7014 or FAR 52.227-14.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
 * WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
 * LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
 * INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
 * RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
 * LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
 * CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
 * INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
 * FOR LOST PROFITS.
 */

package edu.jhuapl.openessence.i18n;

import edu.jhuapl.openessence.datasource.OeDataSource;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A {@link ReloadableResourceBundleMessageSource} that allows querying for all its messages and base names, among other
 * useful operations.
 */
public class InspectableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource {

    private String[] basenames;
    private ResourceLoader resourceLoader;

    private static final Logger log = LoggerFactory.getLogger(InspectableResourceBundleMessageSource.class);

    /**
     * Returns true if the given message code was found, as defined by {@link #getMessage(String, Object[], Locale)}.
     *
     * @param code message code
     * @return true if the given message code was found
     */
    public boolean hasMessage(String code) {
        try {
            getMessage(code, null, LocaleContextHolder.getLocale());
            return true;
        } catch (NoSuchMessageException e) {
            return false;
        }
    }

    /**
     * Get all messages associated with this message source.
     */
    public Map<String, String> getAllMessages() {
        Map<String, String> messages = new LinkedHashMap<String, String>();

        // add parent messages first so that child can override
        MessageSource parentMessageSource = getParentMessageSource();
        if (parentMessageSource instanceof InspectableResourceBundleMessageSource) {
            InspectableResourceBundleMessageSource parent =
                    (InspectableResourceBundleMessageSource) parentMessageSource;
            messages.putAll(parent.getAllMessages());
        }

        for (String basename : basenames) {
            messages.putAll(getAllMessages(basename));
        }

        return messages;
    }

    private Map<String, String> getAllMessages(String basename) {
        return getAllMessages(basename, LocaleContextHolder.getLocale());
    }

    private Map<String, String> getAllMessages(String basename, Locale locale) {
        Map<String, String> messages = new LinkedHashMap<String, String>();

        for (String filename : calculateAllFilenames(basename, locale)) {
            PropertiesHolder holder = getProperties(filename);
            if (holder.getProperties() != null) {
                for (Object o : holder.getProperties().keySet()) {
                    // since o is actually a String, this is essentially a type-safe cast
                    try {
                        String message = getMessage(o.toString(), null, locale);
                        messages.put(o.toString(), message);
                    } catch (NoSuchMessageException e) {
                        // don't add message if it doesn't exist
                    }
                }
            }
        }

        return messages;
    }

    /**
     * Get a message associated with a given data source. If the message is not associated with the given data source,
     * the global message is returned (if one exists).
     *
     * getMessage is already heavily overloaded, so this is a separate method.
     */
    public String getDataSourceMessage(String code, OeDataSource dataSource) {
        String dsCode = dataSource.getDataSourceId() + "." + code;
        if (hasMessage(dsCode)) {
            return getMessage(dsCode);
        } else {
            return getMessage(code);
        }
    }

    /**
     * Helper method to get a message for a given code using the current locale and a sensible default message.
     */
    public String getMessage(String code) {
        return getMessage(code, code);
    }

    public String getMessage(String code, String defaultMessage) {
        if (code == null) {
            throw new IllegalArgumentException("code cannot be null");
        }

        String message = getMessage(code, null, defaultMessage, LocaleContextHolder.getLocale());
        if (message == null) {
            // prevent NPEs
            return null;
        }

        if (message.isEmpty()) {
            log.warn(
                    "Translation for code \"" + code + "\" is the empty string. Are you sure this is what you wanted?");
            // still return even if empty, since maybe they wanted that
        }

        return message;
    }

    public Collection<Locale> getLocales() throws IOException {
        String basename = basenames[0];
        Resource resource = resourceLoader.getResource(basename + ".properties");
        if (!resource.exists()) {
            return Collections.emptyList();
        }

        File baseFile = resource.getFile();
        final String bundleName = FilenameUtils.getBaseName(baseFile.getPath());
        File[] files = resource.getFile().getParentFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(bundleName + "_") && name.endsWith(".properties");
            }
        });

        List<Locale> locales = new ArrayList<Locale>();
        for (File f : files) {
            String prefix = bundleName + "_";
            String code = f.getName().substring(prefix.length()).replace(".properties", "");

            locales.add(StringUtils.parseLocaleString(code));
        }

        return locales;
    }

    /**
     * <p> Find a supported locale that is the best match to the given locale. For example, if we only support "en" and
     * "fr", then the best match for "en_US" would be "en". </p> The available locales are given by the properties files
     * for the 0th basename. For example, if the 0th element of the basenames array is "file:/home/messages", then this
     * method looks for all properties files in /home that begin with "messages". So if that directory has the files
     * "messages_en.properties" and "messages_fr.properties", then the available locales are "en" and "fr". The "best"
     * locale is given by the order of values returned by {@link #calculateFilenamesForLocale(String, Locale)}.
     *
     * @param locale the locale to match
     * @return best match or default locale if no best match
     */
    public Locale getBestMatchingLocale(Locale locale) {
        if (basenames == null || basenames.length == 0) {
            throw new IllegalStateException("no basenames set");
        }

        List<String> filenames = calculateFilenamesForLocale(basenames[0], locale);
        if (filenames.isEmpty()) {
            throw new IllegalStateException("no filenames found for locale " + locale);
        }

        for (String filename : filenames) {
            // only support properties files since that's all we use
            Resource resource = resourceLoader.getResource(filename + ".properties");
            if (resource.exists()) {
                String prefix = basenames[0] + "_";
                String code = filename.substring(prefix.length());

                return StringUtils.parseLocaleString(code);
            }
        }

        return Locale.getDefault();
    }

    public String[] getBasenames() {
        return basenames;
    }

    /**
     * Make the basenames in {@link ReloadableResourceBundleMessageSource} visible to this class.
     */
    public void setBasenames(String... basenames) {
        this.basenames = basenames.clone();
        super.setBasenames(basenames);
    }

    /**
     * Make the {@code ResourceLoader} in {@link ReloadableResourceBundleMessageSource} visible to this class.
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        super.setResourceLoader(resourceLoader);
    }

}
