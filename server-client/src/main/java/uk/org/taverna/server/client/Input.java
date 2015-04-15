package uk.org.taverna.server.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.io.IOUtils;
import org.taverna.server.client.wadl.TavernaServer.Root.RunsRunName.Input.InputName;

import uk.org.taverna.server.client.generic.port.InputPort;
import uk.org.taverna.server.client.rest.InputDescription;
import uk.org.taverna.server.client.rest.InputDescription.Value;

public class Input {
	private InputPort port;
	private Run run;
	private InputName handle;

	Input(Run run, InputPort port) {
		this.port = port;
		this.run = run;
		this.handle = run.run.input().inputName(port.getName());
	}

	public String getName() {
		return port.getName();
	}

	public Integer getDepth() {
		return port.getDepth();
	}

	public Character getListSeparator() {
		String sep = handle.getAsInputDescriptionXml().getListDelimiter();
		if (sep == null || sep.isEmpty())
			return null;
		return sep.charAt(0);
	}

	public String getValue() {
		InputDescription idesc = handle.getAsInputDescriptionXml();
		Value v = idesc.getValue();
		if (v != null)
			return v.getValue();
		String name = idesc.getFile().getValue();
		try (InputStream s = new File(run, name).getAsStream()) {
			return IOUtils.toString(s);
		} catch (IOException e) {
			// Can't read from the input; should COMPLAIN but this is really a
			// "can't happen" case.
			return null;
		}
	}

	public void setValue(String value) {
		Value v = new Value();
		v.setValue(value);
		InputDescription idesc = new InputDescription();
		idesc.setValue(v);
		handle.putXmlAsInputDescription(idesc);
	}

	public void setValue(String value, char listSeparator) {
		Value v = new Value();
		v.setValue(value);
		InputDescription idesc = new InputDescription();
		idesc.setValue(v);
		idesc.setListDelimiter(new String(new char[] { listSeparator }));
		handle.putXmlAsInputDescription(idesc);
	}
}
