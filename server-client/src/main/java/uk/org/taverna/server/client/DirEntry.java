package uk.org.taverna.server.client;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

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

	// This is so AWFUL! This is all just so that we can force the path to be
	// built without encoding the slashes in it.
	private void hackTheUB(Object path) throws IllegalAccessException,
			NoSuchFieldException {
		class HackedUriBuilder extends JerseyUriBuilder {
			@Override
			public URI buildFromMap(Map<String, ?> values) {
				return buildFromMap(values, false);
			}

			@Override
			public JerseyUriBuilder clone() {
				HackedUriBuilder ub = new HackedUriBuilder();
				try {
					ub.copyFields(this);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("failed to clone UriBuilder", e);
				}
				return ub;
			}

			void copyFields(JerseyUriBuilder source)
					throws IllegalAccessException {
				for (Field field : JerseyUriBuilder.class.getDeclaredFields()) {
					field.setAccessible(true);
					field.set(this, field.get(source));
				}
			}
		}
		HackedUriBuilder ub = new HackedUriBuilder();
		Field uriBuilderField = path.getClass().getDeclaredField("_uriBuilder");
		uriBuilderField.setAccessible(true);
		ub.copyFields((JerseyUriBuilder) uriBuilderField.get(path));
		uriBuilderField.set(path, ub);
	}

	protected DirEntry(Run run, String path) {
		this.run = run;
		this.path = trim(path);
		path1 = run.run.wd().path(this.path);
		path2 = run.run.wd().path2(this.path);
		path3 = run.run.wd().path3(this.path);
		try {
			hackTheUB(path1);
			hackTheUB(path2);
			hackTheUB(path3);
		} catch (NoSuchFieldException | IllegalAccessException e) {
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