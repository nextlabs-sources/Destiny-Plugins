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

public class JwtSecretDTO implements Externalizable {
	
	private String clientId;
	private String jwtSecret;
	
	public JwtSecretDTO() {
	}
	
	public JwtSecretDTO(String clientId, String jwtSecret) {
		this.clientId = clientId;
		this.jwtSecret = jwtSecret;
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(clientId);
		out.writeUTF(jwtSecret);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientId = in.readUTF();
		jwtSecret = in.readUTF();
	}

}
