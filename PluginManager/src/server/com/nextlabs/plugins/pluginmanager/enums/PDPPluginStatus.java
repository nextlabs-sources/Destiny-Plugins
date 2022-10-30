package com.nextlabs.plugins.pluginmanager.enums;

/**
 * List of PIP plugin statuses
 *
 * This is a duplicate of com.nextlabs.destiny.console.enums.PDPPluginStatus
 *
 * @author Chok Shah Neng
 */
public enum PDPPluginStatus {
    DRAFT, DEPLOYED, INACTIVE, DELETED, UNKNOWN;

    public static PDPPluginStatus get(String status) {
        for (PDPPluginStatus s : PDPPluginStatus.values()) {
            if (s.name().equals(status)) {
                return s;
            }
        }

        return UNKNOWN;
    }
}
