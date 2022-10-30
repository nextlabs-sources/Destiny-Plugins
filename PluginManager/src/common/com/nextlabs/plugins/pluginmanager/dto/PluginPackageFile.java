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

public class PluginPackageFile implements Externalizable {
    private String name;
    private PluginPackageFileType type;
    private byte[] contents;

    public PluginPackageFile() {
    }

    public PluginPackageFile(String name, PluginPackageFileType type, byte[] contents) {
        this.name = name;
        this.type = type;
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PluginPackageFileType getType() {
        return type;
    }

    public void setType(PluginPackageFileType type) {
        this.type = type;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeUTF(type.getName());
        out.writeObject(contents);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        type = PluginPackageFileType.getPluginPackageFileType(in.readUTF());
        contents = (byte[])in.readObject();
    }
}
