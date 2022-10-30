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

public class JwtSecretRequestDTO implements Externalizable {
	public static final String PLUGIN = "JwtSecretPlugin";
	
	/**
	 * The host making the request
	 */
	private String hostname;
	
	public JwtSecretRequestDTO() {
		this("localhost");
	}
	
	public JwtSecretRequestDTO(String hostname) {
		this.hostname = hostname;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(hostname);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		hostname = in.readUTF();
	}

}
