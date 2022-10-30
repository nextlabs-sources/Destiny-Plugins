/*
 * Created on Jul 12, 2021
 *
 * All sources, binaries and HTML pages (C) copyright 2021 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 */

package com.nextlabs.plugins.jwtsecret.dto.v2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JwtSecretDTO implements Externalizable {
	private static final long serialVersionUID = 8320010709014039362L;
    
	private String clientId;
	private byte[] clientPassword;
	private String jwtSecret;
	
	public JwtSecretDTO() {
	}
	
	public JwtSecretDTO(String clientId, byte[] clientPassword, String jwtSecret) {
		this.clientId = clientId;
		this.clientPassword = clientPassword;
		this.jwtSecret = jwtSecret;
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

    public byte[] getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(byte[] clientPassword) {
        this.clientPassword = clientPassword;
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
		out.writeObject(clientPassword);
		out.writeUTF(jwtSecret);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clientId = in.readUTF();
		clientPassword = (byte[])in.readObject();
		jwtSecret = in.readUTF();
	}

}
