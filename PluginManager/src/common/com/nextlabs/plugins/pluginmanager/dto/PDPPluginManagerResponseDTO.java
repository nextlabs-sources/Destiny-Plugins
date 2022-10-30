/*
 * Created on Nov 17, 2020
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
import java.util.ArrayList;
import java.util.List;

public class PDPPluginManagerResponseDTO implements Externalizable {
    private long timestamp;

    private List<PluginPackage> newOrUpdatedPlugins = new ArrayList<>();
    private List<String> deletedPlugins = new ArrayList<>();

    public PDPPluginManagerResponseDTO() {
    }

    public PDPPluginManagerResponseDTO(long timestamp, List<PluginPackage> newOrUpdatedPlugins, List<String> deletedPlugins) {
        this.timestamp = timestamp;

        if (newOrUpdatedPlugins != null) {
            this.newOrUpdatedPlugins = newOrUpdatedPlugins;
        }

        if (deletedPlugins != null) {
            this.deletedPlugins = deletedPlugins;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<PluginPackage> getNewOrUpdatedPlugins() {
        return newOrUpdatedPlugins;
    }

    public void setNewOrUpdatedPlugins(List<PluginPackage> newOrUpdatedPlugins) {
        if (newOrUpdatedPlugins != null) {
            this.newOrUpdatedPlugins = newOrUpdatedPlugins;
        }
    }

    public List<String> getDeletedPlugins() {
        return deletedPlugins;
    }

    public void setDeletedPlugins(List<String> deletedPlugins) {
        if (deletedPlugins != null) {
            this.deletedPlugins = deletedPlugins;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(timestamp);
        
        out.writeLong(newOrUpdatedPlugins.size());
        for (PluginPackage pkg: newOrUpdatedPlugins) {
            out.writeObject(pkg);
        }

        out.writeLong(deletedPlugins.size());
        for (String name : deletedPlugins) {
            out.writeUTF(name);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        timestamp = in.readLong();

        long numNewPlugins = in.readLong();

        newOrUpdatedPlugins = new ArrayList<PluginPackage>();

        for (int i = 0; i < numNewPlugins; i++) {
            newOrUpdatedPlugins.add((PluginPackage)in.readObject());
        }

        long numDeletedPlugins = in.readLong();

        deletedPlugins = new ArrayList<String>();

        for (int i = 0; i < numDeletedPlugins; i++) {
            deletedPlugins.add(in.readUTF());
        }
    }
}
