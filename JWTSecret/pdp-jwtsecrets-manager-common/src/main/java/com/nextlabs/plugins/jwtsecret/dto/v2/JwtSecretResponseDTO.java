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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtSecretResponseDTO implements Externalizable {
    private static final long serialVersionUID = 5031710801104782394L;
    
    private long timestamp;
    private CharSequence secret;
    private int hashWidth;
    private int iterations;
	private int jwtSecretSize;
    private int saltLength;
    
	private List<JwtSecretDTO> jwtSecrets;
	
	public JwtSecretResponseDTO() {
		jwtSecretSize = 0;
		jwtSecrets = new ArrayList<JwtSecretDTO>();
	}
	
	public JwtSecretResponseDTO(List<JwtSecretDTO> jwtSecretDTOs) {
		jwtSecretSize = jwtSecretDTOs.size();
		this.jwtSecrets = jwtSecretDTOs;
	}
	
	public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public CharSequence getSecret() {
        return secret;
    }

    public void setSecret(CharSequence secret) {
        this.secret = secret;
    }

    public int getHashWidth() {
        return hashWidth;
    }

    public void setHashWidth(int hashWidth) {
        this.hashWidth = hashWidth;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getSaltLength() {
        return saltLength;
    }

    public void setSaltLength(int saltLength) {
        this.saltLength = saltLength;
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
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(timestamp);
        out.writeObject(secret);
        out.writeInt(hashWidth);
        out.writeInt(iterations);
		out.writeInt(jwtSecretSize);
		out.writeInt(saltLength);
		for(JwtSecretDTO jwtSecret: jwtSecrets) {
			out.writeObject(jwtSecret);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        timestamp = in.readLong();
        secret = (CharSequence)in.readObject();
        hashWidth = in.readInt();
        iterations = in.readInt();
		jwtSecretSize = in.readInt();
        saltLength = in.readInt();
		for(int i = 0; i < jwtSecretSize; i++) {
			jwtSecrets.add((JwtSecretDTO) in.readObject());
		}
	}
	
}
