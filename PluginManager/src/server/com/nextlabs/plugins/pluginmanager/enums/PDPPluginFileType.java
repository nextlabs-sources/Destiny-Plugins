package com.nextlabs.plugins.pluginmanager.enums;

/**
 * List of PIP plugin file types
 *
 * This is a duplicate of com.nextlabs.destiny.console.enums.PDPPluginFileType
 *
 * @author Chok Shah Neng
 */
public enum PDPPluginFileType {

    PROPERTIES, PRIMARY_JAR, THIRD_PARTY_JAR, OTHER;

    public static PDPPluginFileType get(String type) {
        for (PDPPluginFileType t : PDPPluginFileType.values()) {
            if (t.name().equals(type)) {
                return t;
            }
        }

        return PRIMARY_JAR;
    }
}
