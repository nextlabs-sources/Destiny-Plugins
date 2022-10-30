/*
 * Created on Jul 13, 2021
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

public class JwtSecretRequestDTO implements Externalizable {
    private static final long serialVersionUID = 3610315092828806063L;

	public static final String PLUGIN = "JwtSecretPluginV2";
	
	/**
	 * The host making the request
	 */
	private String hostname;
    /*
     * Time of last request
     */
    private long timestamp;
    
	public JwtSecretRequestDTO() {
		this("localhost", 0L);
	}
	
	public JwtSecretRequestDTO(String hostname, long timestamp) {
		this.hostname = hostname;
        this.timestamp = timestamp;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(hostname);
		out.writeLong(timestamp);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		hostname = in.readUTF();
        timestamp = in.readLong();
	}

}
