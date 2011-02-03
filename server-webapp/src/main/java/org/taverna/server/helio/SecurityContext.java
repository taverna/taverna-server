package org.taverna.server.helio;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;

import javax.servlet.ServletContext;

import org.taverna.server.master.localworker.RemoteRunDelegate;
import org.taverna.server.master.localworker.SecurityContextDelegate;

/**
 * A security context that's adapted for HELIO.
 * @author dkf
 *
 */
class SecurityContext extends SecurityContextDelegate {
	private KeyStore keystore;
	SecurityContext(RemoteRunDelegate run, Principal owner,
			SecurityContextDelegate.Factory factory) throws GeneralSecurityException {
		super(run, owner, factory);
		keystore = super.getInitialKeyStore();
	}

	/**
	 * A security context factory that's adapted for HELIO.
	 * @author dkf
	 *
	 */
	public static class Factory extends SecurityContextDelegate.Factory {
		@Override
		public SecurityContextDelegate create(RemoteRunDelegate run,
				Principal owner) throws GeneralSecurityException {
			return new SecurityContext(run, owner, this);
		}
	}

	@Override
	public void initializeSecurityFromContext(ServletContext context) throws Exception {
		super.initializeSecurityFromContext(context);
		// FIXME transfer proxy certificate from context to keystore
	}

	@Override
	protected KeyStore getInitialKeyStore() throws GeneralSecurityException {
		return keystore;
	}
}
