/**
 * 
 */
package org.taverna.server.master.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.taverna.server.master.TavernaServerImpl.log;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.taverna.server.master.exceptions.NoDirectoryEntryException;

@Provider
public class NoDirectoryEntryHandler extends HandlerCore implements
		ExceptionMapper<NoDirectoryEntryException> {
	@Override
	public Response toResponse(NoDirectoryEntryException exn) {
		if (managementModel.getLogOutgoingExceptions())
			log.info("converting exception to response", exn);
		return Response.status(NOT_FOUND).type(TEXT_PLAIN_TYPE).entity(
				exn.getMessage()).build();
	}
}