/*
 * Copyright 2017 by Nextlabs Inc.
 *
 * All rights reserved worldwide.
 * Created on Jan 2017
 * 
 * Author: sduan
 *
 */
package com.nextlabs.plugins.jwtsecret.dto;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtSecretResponseDTO implements Externalizable {
	
	private int jwtSecretSize;
	private List<JwtSecretDTO> jwtSecrets;
	
	public JwtSecretResponseDTO() {
		jwtSecretSize = 0;
		jwtSecrets = new ArrayList<JwtSecretDTO>();
	}
	
	public JwtSecretResponseDTO(List<JwtSecretDTO> jwtSecretDTOs) {
		jwtSecretSize = jwtSecretDTOs.size();
		this.jwtSecrets = jwtSecretDTOs;
	}
	
	
	public int getJwtSecretSize() {
		return jwtSecretSize;
	}

	public List<JwtSecretDTO> getJwtSecrets() {
		return jwtSecrets;
	}
	
	public void setJwtSecrets(List<JwtSecretDTO> jwtSecretDTOs) {
		jwtSecretSize = jwtSecretDTOs.size();
		this.jwtSecrets = jwtSecretDTOs;
	}
	
	public Map<String, String> toMap() {
		Map<String, String> ret = new HashMap<String, String>();
		for (JwtSecretDTO secretDto: jwtSecrets) {
			ret.put(secretDto.getClientId(), secretDto.getJwtSecret());
		}
		return ret;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(jwtSecretSize);
		for(JwtSecretDTO jwtSecret: jwtSecrets) {
			out.writeObject(jwtSecret);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		jwtSecretSize = in.readInt();
		for(int i = 0; i < jwtSecretSize; i++) {
			jwtSecrets.add((JwtSecretDTO) in.readObject());
		}
	}
	
}
