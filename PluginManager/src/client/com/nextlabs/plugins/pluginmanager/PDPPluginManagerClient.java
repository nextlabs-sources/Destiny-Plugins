/*
 * Created on Nov 17, 2020
 *
 * All sources, binaries and HTML pages (C) copyright 2020 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 */

package com.nextlabs.plugins.pluginmanager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.domain.destiny.serviceprovider.IHeartbeatServiceProvider;
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderException;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderManager;
import com.nextlabs.pf.domain.destiny.serviceprovider.IConfigurableServiceProvider;
import com.nextlabs.pf.domain.destiny.serviceprovider.ServiceProviderFileType;
import com.nextlabs.plugins.pluginmanager.dto.PDPPluginManagerRequestDTO;
import com.nextlabs.plugins.pluginmanager.dto.PDPPluginManagerResponseDTO;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackage;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackageFile;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackageFileType;

public class PDPPluginManagerClient implements IHeartbeatServiceProvider, IConfigurableServiceProvider {
	private static final Log log = LogFactory.getLog(PDPPluginManagerClient.class);
	private ClassLoader classLoader = getClass().getClassLoader();
    private Properties properties;
    private IServiceProviderManager serviceProviderManager;
    private Map<PluginPackageFileType, ServiceProviderFileType> pluginTypeMap = new HashMap<>();
    private String storedUpdateTimeFileName;
    private long lastUpdateTime = 0l;
    
    @Override
    public Serializable prepareRequest(String id) {
        if (PDPPluginManagerRequestDTO.PLUGIN_ID.equals(id)) {
            return new PDPPluginManagerRequestDTO(lastUpdateTime);
        } else {
            log.warn("Plugin id " + id + " doesn't match " + PDPPluginManagerRequestDTO.PLUGIN_ID);
        }

        return null;
    }

    @Override
    public void processResponse(String id, String data) {
        if (PDPPluginManagerRequestDTO.PLUGIN_ID.equals(id)) {
            Serializable responseObject = SerializationUtils.unwrapSerialized(data, classLoader);

            if (responseObject != null) {
                if (responseObject instanceof PDPPluginManagerResponseDTO) {
                    PDPPluginManagerResponseDTO responseDTO = (PDPPluginManagerResponseDTO)responseObject;
                    
                    for (PluginPackage pkg : responseDTO.getNewOrUpdatedPlugins()) {
                        try {
                            extractAndRestartPlugin(pkg);
                        } catch (IOException ioe) {
                            log.warn("Unable to extract plug-in details for " + pkg.getName());
                        }
                    }
                    
                    for (String pluginToDelete : responseDTO.getDeletedPlugins()) {
                        getServiceProviderManager().removeServiceProvider(pluginToDelete);
                    }
                    
                    saveUpdateTime(responseDTO.getTimestamp());
                } else {
                    log.error("PDPPluginManager received response of unexpected class " +
                              responseObject.getClass().getName());
                }
            }
        } else {
            log.warn("Plugin id " + id + " doesn't match " + PDPPluginManagerRequestDTO.PLUGIN_ID);
        }
    }

    private void extractAndRestartPlugin(PluginPackage pkg) throws IOException {
        String propertiesPath = null;
        
        // Extract each of the files and write to the appropriate location
        for (PluginPackageFile packageFile : pkg.getPackageFiles()) {
            getServiceProviderManager().addServiceProviderFile(pkg.getName(),
                                                               pluginTypeMap.get(packageFile.getType()),
                                                               packageFile.getName(),
                                                               packageFile.getContents());
        }

        try {
            getServiceProviderManager().restartServiceProvider(pkg.getName());
        } catch (ServiceProviderException spe) {
            log.warn("Unable to restart plugin " + pkg.getName(), spe);
        }
            
    }

    private long loadUpdateTime() {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(storedUpdateTimeFileName))) {
            lastUpdateTime = dis.readLong();
        } catch (IOException e) {
            // This might not be an error. When we first install the file won't exist
            log.warn("Unable to open file " + storedUpdateTimeFileName);
        }
        
        return lastUpdateTime;
    }
    
    private void saveUpdateTime(long newUpdateTime) {
        if (newUpdateTime != 0) {
            lastUpdateTime = newUpdateTime;
        }
        
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(storedUpdateTimeFileName))) {
            dos.writeLong(lastUpdateTime);
        } catch (IOException e) {
            // This is a problem. If we can't persist the update time
            // then when we restart we'll request the plugins all over
            // again
            log.error("Unable to write to file " + storedUpdateTimeFileName, e);
        }
    }
    
    @Override
    public void init() throws Exception {
        pluginTypeMap.put(PluginPackageFileType.MAIN_JAR, ServiceProviderFileType.MAIN_JAR);
        pluginTypeMap.put(PluginPackageFileType.SUPPORTING_JAR, ServiceProviderFileType.SUPPORTING_JAR);
        pluginTypeMap.put(PluginPackageFileType.PROPERTIES_FILE, ServiceProviderFileType.PROPERTIES_FILE);
        pluginTypeMap.put(PluginPackageFileType.MISC_FILE, ServiceProviderFileType.MISC_FILE);

        String rootDir = System.getProperty("dpc.install.home");

        if (rootDir == null) {
            rootDir = ".";
        }
        
        storedUpdateTimeFileName = rootDir + "/jservice/plugin.update.time";
        loadUpdateTime();
    }

    private synchronized IServiceProviderManager getServiceProviderManager() {
        // This can't be done in init(), because this plug-in is
        // initialized by ServiceProviderManager as part of its
        // initialization and this would cause an infinite loop
        if (serviceProviderManager == null) {
            serviceProviderManager = ComponentManagerFactory.getComponentManager().getComponent(ServiceProviderManager.COMP_INFO);
        }

        return serviceProviderManager;
    }
    
    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}





