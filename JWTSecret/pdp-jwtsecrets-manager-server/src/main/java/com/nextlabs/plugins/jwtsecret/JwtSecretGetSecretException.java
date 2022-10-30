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

public class JwtSecretGetSecretException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8013370702452800554L;
	
	/**
     * Constructor
     * 
     * @param arg0
     */
    public JwtSecretGetSecretException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public JwtSecretGetSecretException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public JwtSecretGetSecretException(Throwable arg0) {
        super(arg0);
    }
    
}
