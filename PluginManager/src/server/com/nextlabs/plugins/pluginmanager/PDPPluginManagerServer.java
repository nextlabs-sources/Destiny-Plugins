/*
 * Created on Dec 08, 2020
 *
 * All sources, binaries and HTML pages (C) copyright 2020 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 */

package com.nextlabs.plugins.pluginmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.plugin.IDCCHeartbeatServerPlugin;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.heartbeat.IServerHeartbeatManager;
import com.bluejungle.framework.heartbeat.ServerHeartbeatManagerImpl;
import com.bluejungle.framework.utils.SerializationUtils;
import com.nextlabs.destiny.container.shared.pluginmanager.IPDPPluginManager;
import com.nextlabs.destiny.container.shared.pluginmanager.PDPPluginManagementException;
import com.nextlabs.destiny.container.shared.pluginmanager.hibernateimpl.PDPPluginEntity;
import com.nextlabs.destiny.container.shared.pluginmanager.hibernateimpl.PDPPluginFileEntity;
import com.nextlabs.destiny.container.shared.pluginmanager.hibernateimpl.PDPPluginManager;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackage;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackageFile;
import com.nextlabs.plugins.pluginmanager.dto.PluginPackageFileType;
import com.nextlabs.plugins.pluginmanager.dto.PDPPluginManagerRequestDTO;
import com.nextlabs.plugins.pluginmanager.dto.PDPPluginManagerResponseDTO;

public class PDPPluginManagerServer implements IDCCHeartbeatServerPlugin {
	private static final Log log = LogFactory.getLog(PDPPluginManagerServer.class);

	private ClassLoader classLoader = getClass().getClassLoader();
    
    private static final String PLUGIN_FILES_TABLE = "pdp_plugin_files";
    private static final String IS_ACTIVE_COLUMN = "isactive";
    private static final String LAST_UPDATED_COLUMN = "changed_time";
    private static final String PLUGIN_NAME_COLUMN = "plugin_name";
   
    private static final String PLUGIN_FILES_SQL = "SELECT plugin_name, file_type, file_data " +
                                                   "FROM pdp_plugin_files " +
                                                   "WHERE isactive = \'Y\' AND changed_time > {} ";

    private IPDPPluginManager pluginManager;
    
    @Override
    public Serializable serviceHeartbeatRequest(String name, String requestString) {
        if (PDPPluginManagerRequestDTO.PLUGIN_ID.equals(name)) {
            Serializable requestObject = SerializationUtils.unwrapSerialized(requestString, classLoader);

            PDPPluginManagerRequestDTO requestDTO = (PDPPluginManagerRequestDTO)requestObject;
            if(log.isDebugEnabled()) {
                log.debug("Received PDPPluginManagerRequestDTO");
            }

            Date lastUpdate = new Date(requestDTO.getLastUpdate());

            try {
                Collection<PDPPluginEntity> modifiedPlugins = pluginManager.getModifiedPlugins(lastUpdate);
                Collection<PDPPluginEntity> deletedPlugins = pluginManager.getDeletedPlugins(lastUpdate);

                if ((modifiedPlugins == null || modifiedPlugins.size() == 0) &&
                    (deletedPlugins == null || deletedPlugins.size() == 0)) {
                    log.debug("No pdp plugins added since " + lastUpdate);
                    return null;
                }

                return buildResponse(requestDTO.getLastUpdate(), modifiedPlugins, deletedPlugins);
            } catch (PDPPluginManagementException pme) {
                log.error("Exception from plugin management code", pme);
            }
            
            return null;
        } else {
            log.warn(String.format("Plugin id: %s doesn't match: %s", name, PDPPluginManagerRequestDTO.PLUGIN_ID));
        }

        return null;
    }

    private PDPPluginManagerResponseDTO buildResponse(long ts,
                                                     Collection<PDPPluginEntity> modifiedPlugins,
                                                     Collection<PDPPluginEntity> deletedPlugins) {
        PDPPluginManagerResponseDTO responseDTO = new PDPPluginManagerResponseDTO();

        responseDTO.setTimestamp(ts);
        
        if (modifiedPlugins != null) {
            for (PDPPluginEntity entity: modifiedPlugins) {
                log.info("Modifying/creating pdp plugin " + entity.getName());
                responseDTO.getNewOrUpdatedPlugins().add(convertEntityToPackage(entity));
            }
        }

        if (deletedPlugins != null) {
            for (PDPPluginEntity entity: deletedPlugins) {
                log.info("Deleting pdp plugin " + entity.getName());
                responseDTO.getDeletedPlugins().add(convertEntityToPackage(entity).getName());
            }
        }

        return responseDTO;
    }
    
    private PluginPackage convertEntityToPackage(PDPPluginEntity entity) {
        PluginPackage pluginPackage = new PluginPackage();

        pluginPackage.setName(entity.getName());

        for (Object pf : entity.getPluginFiles()) {
            PDPPluginFileEntity fileEntity = (PDPPluginFileEntity)pf;

            pluginPackage.getPackageFiles().add(convertEntityToFile(fileEntity));
        }

        return pluginPackage;
    }

    private PluginPackageFile convertEntityToFile(PDPPluginFileEntity entity) {
        return new PluginPackageFile(entity.getName(),
                                     convertType(entity.getType()),
                                     entity.getContent());
    }
    
    private PluginPackageFileType convertType(String type) {
        if (type.equals("PROPERTIES")) {
            return PluginPackageFileType.PROPERTIES_FILE;
        } else if (type.equals("PRIMARY_JAR")) {
            return PluginPackageFileType.MAIN_JAR;
        } else if(type.equals("THIRD_PARTY_JAR")) {
            return PluginPackageFileType.SUPPORTING_JAR;
        } else if(type.equals("OTHER")) {
            return PluginPackageFileType.MISC_FILE;
        }

        throw new IllegalArgumentException("type not one of PROPERTIES, PRIMARY_JAR, THIRD_PARTY_JAR, or OTHER: " + type);
    }
    
    private PDPPluginManagerResponseDTO testMe() {
        PDPPluginManagerResponseDTO responseDTO = new PDPPluginManagerResponseDTO();
        
        responseDTO.setTimestamp(new Date().getTime());
        
        ArrayList<PluginPackage> newOrUpdatedPlugins = new ArrayList<>();

        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setName("TestPlugin");
        pluginPackage.getPackageFiles().add(new PluginPackageFile("test-plugin.properties", PluginPackageFileType.PROPERTIES_FILE, getBytes("/opt/NextLabs/PolicyServer/server/misc/test-plugin.properties")));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("TestPlugin.jar", PluginPackageFileType.MAIN_JAR, getBytes("/opt/NextLabs/PolicyServer/server/misc/TestPlugin.jar")));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("Extra.jar", PluginPackageFileType.SUPPORTING_JAR, getBytes("/opt/NextLabs/PolicyServer/server/misc/Extra.jar")));

        responseDTO.getNewOrUpdatedPlugins().add(pluginPackage);

        return responseDTO;
    }

    private byte[] getBytes(String fileName) {
        try {
            return Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            log.error("Caught exception", e);
        }

        return new byte[0];
    }
    
    private void testGronk(PDPPluginManagerResponseDTO responseDTO) {
        // Test code start
        PluginPackage pluginPackage = new PluginPackage();
        pluginPackage.setName("PackageA");
        
        pluginPackage.getPackageFiles().add(new PluginPackageFile("foo.jar", PluginPackageFileType.MAIN_JAR, "This is the main jar, foo.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("bar.jar", PluginPackageFileType.SUPPORTING_JAR, "This is a supporting jar, bar.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("baz.jar", PluginPackageFileType.SUPPORTING_JAR, "This is another supporting jar, baz.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("foo.properties", PluginPackageFileType.PROPERTIES_FILE, "This is the foo.properties file".getBytes()));
        responseDTO.getNewOrUpdatedPlugins().add(pluginPackage);
        
        pluginPackage = new PluginPackage();
        pluginPackage.setName("PackageB");
        pluginPackage.getPackageFiles().add(new PluginPackageFile("blort.jar", PluginPackageFileType.MAIN_JAR, "This is the main jar, blort.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("wibble.jar", PluginPackageFileType.SUPPORTING_JAR, "This is a supporting jar, wibble.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("blort.properties", PluginPackageFileType.PROPERTIES_FILE, "This is the blort.properties file".getBytes()));
        responseDTO.getNewOrUpdatedPlugins().add(pluginPackage);
        
        pluginPackage = new PluginPackage();
        pluginPackage.setName("PackageC");
        pluginPackage.getPackageFiles().add(new PluginPackageFile("shouldbedeleted.jar", PluginPackageFileType.MAIN_JAR, "This is the main jar, shouldbedeleted.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("zot.jar", PluginPackageFileType.SUPPORTING_JAR, "This is a supporting jar, zot.jar".getBytes()));
        pluginPackage.getPackageFiles().add(new PluginPackageFile("shouldbedeleted.properties", PluginPackageFileType.PROPERTIES_FILE, "This is the shouldbedeleted.properties file".getBytes()));
        responseDTO.getNewOrUpdatedPlugins().add(pluginPackage);
        
        responseDTO.getDeletedPlugins().add("PackageC");
        // Test code end
    }
    
    @Override
    public void init(IRegisteredDCCComponent component) {
		// Get heartbeat manager
        ComponentInfo<ServerHeartbeatManagerImpl> heartbeatMgrCompInfo = 
        	new ComponentInfo<ServerHeartbeatManagerImpl>(
        		IServerHeartbeatManager.COMP_NAME, 
        		ServerHeartbeatManagerImpl.class, 
        		IServerHeartbeatManager.class, 
        		LifestyleType.SINGLETON_TYPE);

        IServerHeartbeatManager heartbeatMgr = ComponentManagerFactory.getComponentManager().getComponent(heartbeatMgrCompInfo);
        // Register myself to receive heartbeat plugin requests
        heartbeatMgr.register(PDPPluginManagerRequestDTO.PLUGIN_ID, this);
		log.info("Registed PDPPluginManagerServer with component " + component.getComponentName());

        pluginManager = ComponentManagerFactory.getComponentManager().getComponent(PDPPluginManager.class);
    }
}
