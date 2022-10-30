/*
 * Copyright 2017 by Nextlabs Inc.
 *
 * All rights reserved worldwide.
 * Created on Jan 2017
 * 
 * Author: sduan
 *
 */
package com.nextlabs.plugins.jwtsecret;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.plugin.IDCCHeartbeatServerPlugin;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.heartbeat.IServerHeartbeatManager;
import com.bluejungle.framework.heartbeat.ServerHeartbeatManagerImpl;
import com.bluejungle.framework.utils.SerializationUtils;
import com.nextlabs.destiny.configclient.ConfigClient;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

public class JwtSecretServer implements IDCCHeartbeatServerPlugin {
	private static final Log log = LogFactory.getLog(JwtSecretServer.class);

    private static final CharSequence SECRET_KEY = ConfigClient.get("pbkdf2.encoding.secret.key").toString();
    private static final int ITERATION_COUNT = ConfigClient.get("pbkdf2.encoding.iteration.count").toInt();
    private static final int HASH_WIDTH = ConfigClient.get("pbkdf2.encoding.hash.width").toInt();
    private static final int SALT_LENGTH = ConfigClient.get("pbkdf2.encoding.salt.width").toInt();

    private static final String PBKDF2_ID = "pbkdf2";
	private ClassLoader classLoader = getClass().getClassLoader();
	private static final String USER_TOKEN_ATTRIBUTE_KEY = "jwt_passphrase";
	private static final String CLIENT_ID_COLUMN = "username";
	private static final String CLIENT_PASSWORD_COLUMN = "password";
	private static final String CLIENT_TOKEN_SECRET_COLUMN = "prop_value";
	private static final String USER_TOKEN_SQL =
			"SELECT appuser." + CLIENT_ID_COLUMN + ", appuser." + CLIENT_PASSWORD_COLUMN + ", props." + CLIENT_TOKEN_SECRET_COLUMN + " " +
			"FROM application_user appuser " + 
			"INNER JOIN app_user_properties props " +
			"ON appuser.id = props.user_id " + 
			"WHERE props.prop_key = '" + USER_TOKEN_ATTRIBUTE_KEY + "' " + 
				"AND appuser.status = 'ACTIVE'";
	
	public JwtSecretServer() {
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
        heartbeatMgr.register(com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO.PLUGIN, this);
        heartbeatMgr.register(com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO.PLUGIN, this);
		log.info("Registed JwtSecretServer with component " + component.getComponentName());
	}

	@Override
	public Serializable serviceHeartbeatRequest(String name, String data) {
		if(com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO.PLUGIN.equals(name)) {
			Serializable requestObject = SerializationUtils.unwrapSerialized(data, classLoader);
			if (requestObject instanceof com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO) {
				com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO requestDto = (com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO) requestObject;
				if(log.isDebugEnabled()) {
					log.debug("Recevied JwtSecretRequest from host: " + requestDto.getHostname());
				}
                com.nextlabs.plugins.jwtsecret.dto.JwtSecretResponseDTO responseDto = null;
                try {
                    responseDto = getClientSecretTokensLegacy();
                } catch (JwtSecretGetSecretException e) {
                    log.error("Error retrieve jwt secrets: ", e);
                    // return empty repsonse
                    responseDto = new com.nextlabs.plugins.jwtsecret.dto.JwtSecretResponseDTO();
                }
                return responseDto;
			} else {
				log.warn(String.format("Skip service heartbeat request because recevied data type: %s is not of type: %s",
						requestObject.getClass().getName(), com.nextlabs.plugins.jwtsecret.dto.JwtSecretRequestDTO.class.getName()));
			}
        } else if (com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO.PLUGIN.equals(name)) {
			Serializable requestObject = SerializationUtils.unwrapSerialized(data, classLoader);
			if (requestObject instanceof com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO) {
				com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO requestDto = (com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO) requestObject;
				if(log.isDebugEnabled()) {
					log.debug("Recevied JwtSecretRequest from host: " + requestDto.getHostname());
				}
                com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO responseDto = null;
                try {
                    responseDto = getClientSecretTokens();
                } catch (JwtSecretGetSecretException e) {
                    log.error("Error retrieve jwt secrets: ", e);
                    responseDto = new com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO();
                }
                return responseDto;
			} else {
				log.warn(String.format("Skip service heartbeat request because recevied data type: %s is not of type: %s",
						requestObject.getClass().getName(), com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO.class.getName()));
			}
			
		} else {
			log.warn(String.format("Plugin id: %s doesn't match: %s or %s",
                                   name, com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO.PLUGIN,
                                   com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO.PLUGIN));
		}
        return null;
	}
	
	private com.nextlabs.plugins.jwtsecret.dto.JwtSecretResponseDTO getClientSecretTokensLegacy() throws JwtSecretGetSecretException {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        
        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for getting data from database");
        }
        
        Session hSession = null;
        Statement stmt = null;
        com.nextlabs.plugins.jwtsecret.dto.JwtSecretResponseDTO ret = new com.nextlabs.plugins.jwtsecret.dto.JwtSecretResponseDTO();
        try {
        	hSession = dataSource.getCountedSession();
        	Connection conn = hSession.connection();
        	stmt = conn.createStatement();
        	ResultSet rs = stmt.executeQuery(USER_TOKEN_SQL);
        	List<com.nextlabs.plugins.jwtsecret.dto.JwtSecretDTO> jwtSecretDTOs = new ArrayList<>();
        	while(rs.next()) {
        		String clientId = rs.getString(CLIENT_ID_COLUMN);
        		String clientTokenSecret = rs.getString(CLIENT_TOKEN_SECRET_COLUMN);
        		jwtSecretDTOs.add(new com.nextlabs.plugins.jwtsecret.dto.JwtSecretDTO(clientId, clientTokenSecret));
        	}
        	ret.setJwtSecrets(jwtSecretDTOs);
        }catch (SQLException e ) {
            throw new JwtSecretGetSecretException("SQL exception: ", e);
        } catch (HibernateException e) {
			throw new JwtSecretGetSecretException("Hibernate exception: ", e);
		} finally {
            if (stmt != null) {
	            try {
					stmt.close();
				} catch (SQLException e) {
					log.warn("Error close SQL statement", e);
				}
            }
            HibernateUtils.closeSession(dataSource, log);
        }
        return ret;
	}
                       
                       
    private com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO getClientSecretTokens() throws JwtSecretGetSecretException {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        
        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for getting data from database");
        }
        
        Session hSession = null;
        Statement stmt = null;
        com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO ret = new com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO();
        ret.setTimestamp(System.currentTimeMillis());
        ret.setSecret(SECRET_KEY);
        ret.setHashWidth(HASH_WIDTH);
        ret.setIterations(ITERATION_COUNT);
        ret.setSaltLength(SALT_LENGTH);
        try {
        	hSession = dataSource.getCountedSession();
        	Connection conn = hSession.connection();
        	stmt = conn.createStatement();
        	ResultSet rs = stmt.executeQuery(USER_TOKEN_SQL);
        	List<com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretDTO> jwtSecretDTOs = new ArrayList<>();
        	while(rs.next()) {
        		String clientId = rs.getString(CLIENT_ID_COLUMN);
        		byte[] clientPassword = rs.getBytes(CLIENT_PASSWORD_COLUMN);
        		String clientTokenSecret = rs.getString(CLIENT_TOKEN_SECRET_COLUMN);
        		jwtSecretDTOs.add(new com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretDTO(clientId, clientPassword, clientTokenSecret));
        	}
        	ret.setJwtSecrets(jwtSecretDTOs);
        }catch (SQLException e ) {
            throw new JwtSecretGetSecretException("SQL exception: ", e);
        } catch (HibernateException e) {
			throw new JwtSecretGetSecretException("Hibernate exception: ", e);
		} finally {
            if (stmt != null) {
	            try {
					stmt.close();
				} catch (SQLException e) {
					log.warn("Error close SQL statement", e);
				}
            }
            HibernateUtils.closeSession(dataSource, log);
        }
        return ret;
	}
}
