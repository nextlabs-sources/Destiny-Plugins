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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.domain.destiny.serviceprovider.IHeartbeatServiceProvider;
import com.nextlabs.oauth2.JwtSecretProvider;
import com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretDTO;
import com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretRequestDTO;
import com.nextlabs.plugins.jwtsecret.dto.v2.JwtSecretResponseDTO;

public class JwtSecretClient implements IHeartbeatServiceProvider {

	private static final Log log = LogFactory.getLog(JwtSecretClient.class);

	private ClassLoader classLoader = getClass().getClassLoader();
	
	private IOSWrapper osWrapper;
	private String hostName;
	
	private JwtSecretProvider jwtSecretProvider;
	
	public JwtSecretClient() {
	}
	
	@Override
	public Serializable prepareRequest(String id) {
		if (JwtSecretRequestDTO.PLUGIN.equals(id)) {
            // Fixme - timestamp
			JwtSecretRequestDTO requestDto = new JwtSecretRequestDTO(hostName, 0);
			return requestDto;
		} else {
			log.warn(String.format("Plugin id: %s doesn't match: %s",
					id, JwtSecretRequestDTO.PLUGIN));
		}
		return null;
	}

	@Override
	public void processResponse(String id, String data) {
		if (JwtSecretRequestDTO.PLUGIN.equals(id)) {
			
			Serializable responseObject = SerializationUtils.unwrapSerialized(data, classLoader);
			if (responseObject instanceof JwtSecretResponseDTO) {
				JwtSecretResponseDTO responseDto = (JwtSecretResponseDTO) responseObject;

				jwtSecretProvider.update(responseDto.getTimestamp(),
                                         responseDto.getSecret(),
                                         responseDto.getIterations(),
                                         responseDto.getHashWidth(),
                                         responseDto.getSaltLength(),
                                         buildProviderMap(responseDto));
				if (log.isDebugEnabled()) {
					log.debug("Updated jwt secrets for Jwt Filter");
				}
			} else {
				log.warn(String.format("Skip process heartbeat response because recevied data type: %s is not of type: %s",
						responseObject.getClass().getName(), JwtSecretResponseDTO.class.getName()));
			}
			
		} else {
			log.warn(String.format("Plugin id: %s doesn't match: %s",
					id, JwtSecretRequestDTO.PLUGIN));
		}
	}

    private Map<String, JwtSecretProvider.JwtUserData> buildProviderMap(JwtSecretResponseDTO response) {
        Map<String, JwtSecretProvider.JwtUserData> providerMap = new HashMap<>();

        for (JwtSecretDTO dto : response.getJwtSecrets()) {
            providerMap.put(dto.getClientId(), new JwtSecretProvider.JwtUserData(dto.getJwtSecret(), dto.getClientPassword()));
        }

        return providerMap;
    }
    
	@Override
	public void init() throws Exception {
		log.info("init() started");
		
		osWrapper = ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
		hostName = osWrapper.getFQDN();
		if(hostName.equals("")) { hostName = "localhost"; }
		
		jwtSecretProvider = JwtSecretProvider.getInstance();
		
		log.info("init() finished");
	}

}
