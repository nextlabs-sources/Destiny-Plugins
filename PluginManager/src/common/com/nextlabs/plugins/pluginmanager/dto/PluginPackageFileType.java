/*
 * Created on Nov 16, 2020
 *
 * All sources, binaries and HTML pages (C) copyright 2020 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 */

package com.nextlabs.plugins.pluginmanager.dto;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import com.bluejungle.framework.patterns.EnumBase;

public class PluginPackageFileType extends EnumBase {
    public static final PluginPackageFileType MAIN_JAR = new PluginPackageFileType("main");
    public static final PluginPackageFileType SUPPORTING_JAR = new PluginPackageFileType("supporting");
    public static final PluginPackageFileType PROPERTIES_FILE = new PluginPackageFileType("properties");
    public static final PluginPackageFileType MISC_FILE = new PluginPackageFileType("misc");

    private PluginPackageFileType(String name) {
        super(name);
    }

    public static PluginPackageFileType getPluginPackageFileType(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        return getElement(name, PluginPackageFileType.class);
    }
}

