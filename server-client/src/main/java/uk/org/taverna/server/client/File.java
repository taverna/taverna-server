package uk.org.taverna.server.client;

import static java.io.File.createTempFile;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.tika.mime.MimeTypes.getDefaultMimeTypes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.tika.mime.MimeTypeException;

import uk.org.taverna.server.client.TavernaServer.ClientException;
import uk.org.taverna.server.client.TavernaServer.ServerException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class File extends DirEntry {
	File(Run run, String path) {
		super(run, path);
	}

	public InputStream getAsStream() {
		return path3.getAsOctetStream(InputStream.class);
	}

	public byte[] get() {
		return path3.getAsOctetStream(byte[].class);
	}

	public String get(Charset encoding) {
		return new String(path3.getAsOctetStream(byte[].class), encoding);
	}

	public java.io.File getAsFile() throws ClientHandlerException,
			UniformInterfaceException, IOException, MimeTypeException,
			ClientException, ServerException {
		ClientResponse cr = path3.getAsOctetStream(ClientResponse.class);
		checkError(cr);
		String[] bits = localName().split("[.]");
		String ext = getDefaultMimeTypes().forName(
				cr.getHeaders().getFirst("Content-Type")).getExtension();
		if (ext == null)
			ext = bits[bits.length - 1];
		java.io.File tmp = createTempFile(bits[0], ext);
		try (OutputStream os = new FileOutputStream(tmp);
				InputStream is = cr.getEntity(InputStream.class)) {
			copy(is, os);
		}
		return tmp;
	}

	public void setContents(byte[] newContents) throws ClientException,
			ServerException {
		checkError(path1.putOctetStreamAsXml(newContents, ClientResponse.class));
	}

	public void setContents(String newContents) throws ClientException,
			ServerException {
		checkError(path1.putOctetStreamAsXml(newContents, ClientResponse.class));
	}

	public void setContents(String newContents, Charset encoding)
			throws ClientException, ServerException {
		checkError(path1.putOctetStreamAsXml(newContents.getBytes(encoding),
				ClientResponse.class));
	}

	public void setContents(InputStream newContents) throws ClientException,
			ServerException {
		checkError(path1.putOctetStreamAsXml(newContents, ClientResponse.class));
	}

	public void setContents(java.io.File newContents) throws IOException,
			ClientException, ServerException {
		checkError(path1.putOctetStreamAsXml(
				entity(newContents, APPLICATION_OCTET_STREAM_TYPE),
				ClientResponse.class));
	}
}