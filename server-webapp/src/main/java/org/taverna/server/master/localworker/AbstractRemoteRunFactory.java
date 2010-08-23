package org.taverna.server.master.localworker;

import static java.lang.System.getSecurityManager;
import static java.lang.System.setProperty;
import static java.lang.System.setSecurityManager;
import static java.rmi.registry.LocateRegistry.createRegistry;
import static java.rmi.registry.Registry.REGISTRY_PORT;
import static java.util.Collections.emptyList;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.master.common.Workflow;
import org.taverna.server.master.exceptions.NoCreateException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.factories.ListenerFactory;
import org.taverna.server.master.factories.RunFactory;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.localworker.LocalWorkerState.PerRunCallback;

/**
 * Bridge to remote runs via RMI.
 * 
 * @author Donal Fellows
 */
@ManagedResource(objectName = "Taverna:group=Server,name=Factory", description = "The factory for runs")
public abstract class AbstractRemoteRunFactory implements ListenerFactory,
		RunFactory {
	static final Log log = LogFactory.getLog("Taverna.Server.LocalWorker");

	/**
	 * @return A handle to the current RMI registry.
	 */
	protected Registry getRegistry() {
		try {
			if (registry != null) {
				registry.list();
				return registry;
			}
		} catch (RemoteException e) {
			log.warn("non-functioning existing registry handle", e);
			registry = null;
		}
		try {
			registry = LocateRegistry.getRegistry(getRegistryHost(),
					getRegistryPort());
			registry.list();
			return registry;
		} catch (RemoteException e) {
			log.warn("Failed to get working RMI registry handle.");
			registry = null;
			log.warn("Will build new registry, "
					+ "but service restart ability is at risk.");
			try {
				registry = LocateRegistry.createRegistry(getRegistryPort());
				registry.list();
				return registry;
			} catch (RemoteException e2) {
				log.error(
						"failed to create local working RMI registry on port "
								+ getRegistryPort(), e2);
				log.info("original connection exception", e);
			}
		}
		try {
			registry = LocateRegistry.createRegistry(REGISTRY_PORT);
			registry.list();
			return registry;
		} catch (RemoteException e) {
			log.fatal(
					"totally failed to get registry handle, even on fallback!",
					e);
			registry = null;
			throw new RuntimeException("No RMI Registry Available");
		}
	}

	private Registry registry;
	/**
	 * The name of the resource that describes the default security policy to
	 * install.
	 */
	public static final String SECURITY_POLICY_FILE = "security.policy";
	public LocalWorkerState state;

	@ManagedAttribute(description = "The host holding the RMI registry to communicate via.")
	public String getRegistryHost() {
		return state.getRegistryHost();
	}

	@ManagedAttribute(description = "The host holding the RMI registry to communicate via.")
	public void setRegistryHost(String host) {
		boolean rebuild = false;
		if (host == null || host.isEmpty()) {
			host = null;
			rebuild = (state.getRegistryHost() != null);
		} else {
			rebuild = !host.equals(state.getRegistryHost());
		}
		state.setRegistryHost(host);
		if (rebuild) {
			registry = null;
		}
	}

	@ManagedAttribute(description = "The port number of the RMI registry. Should not normally be set.")
	public int getRegistryPort() {
		return state.getRegistryPort();
	}

	@ManagedAttribute(description = "The port number of the RMI registry. Should not normally be set.")
	public void setRegistryPort(int port) {
		if (port != state.getRegistryPort())
			registry = null;
		state.setRegistryPort(port);
	}

	static {
		if (getSecurityManager() == null) {
			setProperty("java.security.policy", AbstractRemoteRunFactory.class
					.getClassLoader().getResource(SECURITY_POLICY_FILE)
					.toExternalForm());
			setSecurityManager(new RMISecurityManager());
		}
	}

	/**
	 * Set up the run expiry management engine.
	 */
	public AbstractRemoteRunFactory() {
		try {
			registry = LocateRegistry.getRegistry();
			registry.list();
		} catch (RemoteException e) {
			log.warn("Failed to get working RMI registry handle.");
			log
					.warn("Will build new registry, but service restart ability is at risk.");
			try {
				registry = createRegistry(REGISTRY_PORT);
				registry.list();
			} catch (RemoteException e2) {
				log.error("failed to create working RMI registry", e2);
				log.info("original connection exception", e);
			}
		}
	}

	@Override
	public List<String> getSupportedListenerTypes() {
		final Holder<List<String>> types = new Holder<List<String>>();
		types.value = emptyList();
		try {
			state.iterateOverRuns(new PerRunCallback<RemoteException>() {
				@Override
				public void doit(String name, TavernaRun run)
						throws RemoteException {
					types.value = ((RemoteRunDelegate) run).run
							.getListenerTypes();
				}
			});
			log.warn("failed to get list of listener types; no runs");
		} catch (RemoteException e) {
			log.warn("failed to get list of listener types", e);
		}
		return types.value;
	}

	@Override
	public Listener makeListener(TavernaRun run, String listenerType,
			String configuration) throws NoListenerException {
		try {
			return new RemoteRunDelegate.ListenerDelegate(
					((RemoteRunDelegate) run).run.makeListener(listenerType,
							configuration));
		} catch (RemoteException e) {
			throw new NoListenerException("failed to make listener", e);
		}
	}

	@Override
	public TavernaRun create(Principal creator, Workflow workflow)
			throws NoCreateException {
		try {
			Date now = new Date();
			RemoteSingleRun rsr = getRealRun(creator, workflow);
			return new RemoteRunDelegate(now, creator, workflow, rsr, state
					.getDefaultLifetime());
		} catch (NoCreateException e) {
			log.warn("failed to build run instance", e);
			throw e;
		} catch (Exception e) {
			log.warn("failed to build run instance", e);
			throw new NoCreateException("failed to build run instance", e);
		}
	}

	/**
	 * Gets the RMI connector for a new run.
	 * 
	 * @param creator
	 *            Who is creating the workflow run.
	 * @param workflow
	 *            What workflow are they instantiating.
	 * @return The remote interface to the run.
	 * @throws Exception
	 *             Just about anything can go wrong...
	 */
	protected abstract RemoteSingleRun getRealRun(Principal creator,
			Workflow workflow) throws Exception;

	/** @return The names of the current runs. */
	@ManagedAttribute(description = "The names of the current runs.", currencyTimeLimit = 5)
	public String[] getCurrentRunNames() {
		final List<String> names = new ArrayList<String>();
		state.iterateOverRuns(new PerRunCallback<RuntimeException>() {
			@Override
			public void doit(String name, TavernaRun run) {
				names.add(name);
			}
		});
		return names.toArray(new String[0]);
	}

	@ManagedAttribute(description = "The maximum number of simultaneous runs supported by the server.", currencyTimeLimit = 300)
	public int getMaxRuns() {
		return state.getMaxRuns();
	}

	@ManagedAttribute(description = "The maximum number of simultaneous runs supported by the server.")
	public void setMaxRuns(int maxRuns) {
		state.setMaxRuns(maxRuns);
	}

	/** @return How many minutes should a workflow live by default? */
	@ManagedAttribute(description = "How many minutes should a workflow live by default?", currencyTimeLimit = 300)
	public int getDefaultLifetime() {
		return state.getDefaultLifetime();
	}

	/**
	 * Set how long a workflow should live by default.
	 * 
	 * @param defaultLifetime
	 *            Default lifetime, in minutes.
	 */
	@ManagedAttribute
	public void setDefaultLifetime(int defaultLifetime) {
		state.setDefaultLifetime(defaultLifetime);
	}
}
