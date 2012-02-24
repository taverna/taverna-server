/*
 * Copyright (C) 2012 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.localworker.impl;

/**
 * Miscellaneous constants related to security.
 * 
 * @author Donal Fellows.
 */
public interface SecurityConstants {
	/**
	 * The name of the directory (in the home directory) where security settings
	 * will be written.
	 */
	public static final String SECURITY_DIR_NAME = ".taverna-server-security";

	/** The name of the file that will be the created keystore. */
	public static final String KEYSTORE_FILE = "t2keystore.ubr";

	/** The name of the file that will be the created truststore. */
	public static final String TRUSTSTORE_FILE = "t2truststore.ubr";

	/**
	 * The name of the file that contains the password to unlock the keystore
	 * and truststore.
	 */
	public static final String PASSWORD_FILE = "password.txt";

	/**
	 * Used to instruct the Taverna credential manager to use a non-default
	 * location for user credentials.
	 */
	public static final String CREDENTIAL_MANAGER_DIRECTORY = "-cmdir";
	/**
	 * Used to instruct the Taverna credential manager to take its master
	 * password from standard input.
	 */
	public static final String CREDENTIAL_MANAGER_PASSWORD = "-cmpassword";
}
