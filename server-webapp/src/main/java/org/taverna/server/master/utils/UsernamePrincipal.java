/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.utils;

import java.io.Serializable;
import java.security.Principal;

import javax.servlet.ServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A simple serializable principal that just records the name.
 * 
 * @author Donal Fellows
 */
public class UsernamePrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 2703493248562435L;

	public UsernamePrincipal(Object principal) {
		if (principal instanceof Authentication)
			principal = ((Authentication) principal).getPrincipal();
		if (principal instanceof Principal)
			this.name = ((Principal) principal).getName();
		else if (principal instanceof String)
			this.name = (String) principal;
		else if (principal instanceof UserDetails)
			this.name = ((UserDetails) principal).getUsername();
		else
			this.name = principal.toString();
	}

	public UsernamePrincipal(Object principal, ServletRequest originatingMessage) {
		this(principal);
		if (originatingMessage != null)
			ip = originatingMessage.getRemoteAddr();
	}

	private String name;
	private String ip;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		if (ip == null)
			return "Principal<" + name + ">";
		else
			return "Principal<" + name + "> (" + ip + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Principal) {
			Principal p = (Principal) o;
			return name.equals(p.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
