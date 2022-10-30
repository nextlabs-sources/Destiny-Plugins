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

public class PDPPluginManagerRequestDTO implements Externalizable {
    public static final String PLUGIN_ID = "PDP Plugin Manager";
    
    private long lastUpdate;

    public PDPPluginManagerRequestDTO() {
    }

    public PDPPluginManagerRequestDTO(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(lastUpdate);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        lastUpdate = in.readLong();
    }
}
