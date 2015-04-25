Taverna Server Client
=====================

This is a simple Java client library for Taverna Server, built as an OSGi bundle.

It's currently built by using tooling to automatically derive Java classes from the WADL description of a Taverna Server instance. This ensures that the messages sent and the operations supported by the client are exactly correct. The fundamental API is then wrapped up in a higher-level API that is exposed as the public API; the only tooled classes that are exposed are some of the read-only introspection classes.

To use this library, instantiate a `uk.org.taverna.server.client.TavernaServerConnectionFactory` and retrieve a `uk.org.taverna.server.client.TavernaServer` instance from it. You can then use that to create a `uk.org.taverna.server.client.Run` by providing a workflow (in various ways), which is a workflow run. Provide the inputs it requires, set it running, wait for it to finish, retrieve the outputs; all the normal operations are supported.
