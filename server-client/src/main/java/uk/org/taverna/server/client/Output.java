package uk.org.taverna.server.client;

import uk.org.taverna.server.client.generic.port.ErrorValue;
import uk.org.taverna.server.client.generic.port.LeafValue;
import uk.org.taverna.server.client.generic.port.ListValue;
import uk.org.taverna.server.client.generic.port.OutputPort;

public class Output {
	private OutputPort port;
	private Run run;

	Output(Run run, OutputPort port) {
		this.port = port;
		this.run = run;
	}

	public String getName() {
		return port.getName();
	}

	public int getDepth() {
		return port.getDepth();
	}

	public Value getValue(int... coordinates) {
		if (coordinates.length == 0) {
			if (port.getError() != null)
				return new Error(port.getError());
			return new Value(port.getValue());
		}
		ListValue l = port.getList();
		uk.org.taverna.server.client.generic.port.Value v = null;
		for (int idx : coordinates) {
			v = l.getValueOrListOrError().get(idx);
			if (v instanceof ListValue)
				l = (ListValue) v;
		}
		return new Value((LeafValue) v);
	}

	public int getListLength(int... coordinates) {
		ListValue l = port.getList();
		for (int idx : coordinates)
			l = (ListValue) l.getValueOrListOrError().get(idx);
		return l.getLength();
	}

	public class Error extends Value {
		private ErrorValue error;

		Error(ErrorValue error) {
			super(null);
			this.error = error;
		}

		@Override
		public String getContentType() {
			return "text/x-taverna-error-trace+plain";
		}

		@Override
		public Long getSize() {
			return error.getErrorByteLength();
		}

		@Override
		public File getContent() {
			return new File(run, error.getErrorFile());
		}
	}

	public class Value {
		LeafValue value;

		Value(LeafValue v) {
			value = v;
		}

		public String getContentType() {
			return value.getContentType();
		}

		public Long getSize() {
			return value.getContentByteLength();
		}

		public File getContent() {
			return new File(run, value.getContentFile());
		}
	}
}
