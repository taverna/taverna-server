package uk.org.taverna.server.client;

import static java.io.File.createTempFile;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

import uk.org.taverna.server.client.TavernaServer.ClientException;
import uk.org.taverna.server.client.TavernaServer.ServerException;
import uk.org.taverna.server.client.generic.DirectoryEntry;
import uk.org.taverna.server.client.generic.DirectoryReference;
import uk.org.taverna.server.client.generic.FileReference;
import uk.org.taverna.server.client.rest.DirectoryContents;
import uk.org.taverna.server.client.rest.MakeDirectory;
import uk.org.taverna.server.client.rest.UploadFile;

import com.sun.jersey.api.client.ClientResponse;

public class Directory extends DirEntry {
	Directory(Run run) {
		super(run, "");
	}

	Directory(Run run, String path) {
		super(run, path);
	}

	public List<DirEntry> list() {
		List<DirEntry> result = new ArrayList<>();
		for (DirectoryEntry de : path3.getAsXml(DirectoryContents.class)
				.getDirOrFile())
			if (de instanceof DirectoryReference)
				result.add(new Directory(run, de.getValue()));
			else if (de instanceof FileReference)
				result.add(new File(run, de.getValue()));
		return result;
	}

	public File createFile(String name, byte[] content) throws ClientException,
			ServerException {
		UploadFile uf = new UploadFile();
		uf.setName(name);
		uf.setValue(content);
		return new File(run, path(path1.putAsXml(uf, ClientResponse.class)));
	}

	public File createFile(String name, java.io.File content)
			throws ClientException, ServerException {
		return new File(run,
				path(new File(run, concat(name)).path1.putOctetStreamAsXml(
						entity(content, APPLICATION_OCTET_STREAM_TYPE),
						ClientResponse.class)));
	}

	public File createFile(String name, URI source) throws ClientException,
			ServerException {
		return new File(run,
				path(new File(run, concat(name)).path1.postTextUriListAsXml(
						source.toString(), ClientResponse.class)));
	}

	public Directory createDirectory(String name) throws ClientException,
			ServerException {
		MakeDirectory mkdir = new MakeDirectory();
		mkdir.setName(name);
		return new Directory(run, path(path1.putAsXml(mkdir,
				ClientResponse.class)));
	}

	public byte[] getZippedContents() {
		return path3.getAsZip(byte[].class);
	}

	public ZipFile getZip() throws IOException {
		byte[] contents = getZippedContents();
		java.io.File tmp = createTempFile(localName(), ".zip");
		try (OutputStream os = new FileOutputStream(tmp)) {
			os.write(contents);
		}
		return new ZipFile(tmp);
	}
}