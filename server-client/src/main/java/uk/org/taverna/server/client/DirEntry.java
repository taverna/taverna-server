package uk.org.taverna.server.client;

import java.util.Arrays;
import java.util.List;

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

	private static List<String> elements(String path) {
		return Arrays.asList(trim(path).split("/"));
	}

	protected DirEntry(Run run, String path) {
		this.run = run;
		this.path = trim(path);
		List<String> elems = elements(path);
		path1 = run.run.wd().path("");
		path1.setPath(elems);
		path2 = run.run.wd().path2("");
		path2.setPath(elems);
		path3 = run.run.wd().path3("");
		path3.setPath(elems);
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