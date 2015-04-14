package uk.org.taverna.server.client;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.taverna.server.client.wadl.TavernaServer.Root.RunsRunName.Wd.Path;
import org.taverna.server.client.wadl.TavernaServer.Root.RunsRunName.Wd.Path2;
import org.taverna.server.client.wadl.TavernaServer.Root.RunsRunName.Wd.Path3;

import uk.org.taverna.server.client.TavernaServer.ClientException;
import uk.org.taverna.server.client.TavernaServer.ServerException;

import com.sun.jersey.api.client.ClientResponse;

public abstract class DirEntry extends Connected {
	final String path;
	final Run run;
	final Path path1;
	final Path2 path2;
	final Path3 path3;

	private static String trim(String path) {
		return path.replaceFirst("/+$", "").replaceFirst("^/+", "");
	}

	protected DirEntry(Run run, String path) {
		this.run = run;
		this.path = trim(path);
		path1 = run.run.wd().path(this.path);
		path2 = run.run.wd().path2(this.path);
		path3 = run.run.wd().path3(this.path);
		UriBuilder ub = new JerseyUriBuilder() {
			@Override
			public URI buildFromMap(Map<String, ?> values) {
				return buildFromMap(values, false);
			}
		};
		try {
			Field f;
			f = path1.getClass().getDeclaredField("_uriBuilder");
			f.setAccessible(true);
			f.set(path1, ub);
			f = path2.getClass().getDeclaredField("_uriBuilder");
			f.setAccessible(true);
			f.set(path2, ub);
			f = path3.getClass().getDeclaredField("_uriBuilder");
			f.setAccessible(true);
			f.set(path3, ub);
		} catch (SecurityException | NoSuchFieldException
				| IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException("failed to install UriBuilder", e);
		}
	}

	public void delete() throws ClientException, ServerException {
		checkError(path2.deleteAsXml(ClientResponse.class));
	}

	String path(ClientResponse response) throws ClientException,
			ServerException {
		checkError(response);
		String[] bits = response.getLocation().getPath().split("/");
		return concat(bits[bits.length - 1]);
	}

	String localName() {
		String[] bits = path.split("/");
		return bits[bits.length - 1];
	}

	public String getName() {
		return localName();
	}

	public String getPath() {
		return path;
	}

	String concat(String name) {
		return path + "/" + name.split("/", 2)[0];
	}
}