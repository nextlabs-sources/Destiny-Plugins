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

import java.util.ArrayList;
import java.util.List;

public class PluginPackage implements Externalizable {
    private String name;
    private List<PluginPackageFile> packageFiles = new ArrayList<>();

    public PluginPackage() {
    }

    public PluginPackage(String name, List<PluginPackageFile> packageFiles) {
        this.name = name;

        if (packageFiles != null) {
            this.packageFiles = packageFiles;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PluginPackageFile> getPackageFiles() {
        return packageFiles;
    }

    public void setPackageFiles(List<PluginPackageFile> packageFiles) {
        if (packageFiles != null) {
            this.packageFiles = packageFiles;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeInt(packageFiles.size());

        for (PluginPackageFile pkg : packageFiles) {
            out.writeObject(pkg);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        packageFiles = new ArrayList<PluginPackageFile>();
        int numberPackages = in.readInt();

        packageFiles = new ArrayList<PluginPackageFile>();
        for (int i = 0; i < numberPackages; i++) {
            packageFiles.add((PluginPackageFile)in.readObject());
        }
    }
}
