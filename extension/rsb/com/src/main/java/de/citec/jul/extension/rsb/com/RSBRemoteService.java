/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBListenerInterface;
import de.citec.jul.extension.rsb.iface.RSBRemoteServerInterface;
import com.google.protobuf.Descriptors;
import de.citec.jul.extension.rsb.scope.ScopeProvider;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import static de.citec.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import de.citec.jul.schedule.WatchDog;
import java.lang.reflect.ParameterizedType;
import rsb.Event;
import rsb.Handler;
import rsb.Scope;
import rsb.patterns.Future;

/**
 *
 * @author mpohling
 * @param <M>
 */
public abstract class RSBRemoteService<M extends GeneratedMessage> extends Observable<M> {

    private RSBListenerInterface listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private final Handler mainHandler;
    private RSBRemoteServerInterface remoteServer;

    protected Scope scope;
    private M data;
    private boolean initialized;

    public RSBRemoteService() {
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
    }

    public void init(final String label, final ScopeProvider location) throws InitializationException {
        try {
            init(generateScope(label, detectMessageClass(), location));
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public synchronized void init(final Scope scope) throws InitializationException {

        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            if (initialized) {
                logger.warn("Skip initialization because " + this + " already initialized!");
                return;
            }

            this.scope = new Scope(scope.toString().toLowerCase());
            logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + this.scope + ".");

            initListener(this.scope);
            initRemoteServer(this.scope);

            try {
                addHandler(mainHandler, true);
            } catch (InterruptedException ex) {
                logger.warn("Could not register main handler!", ex);
            }
            initialized = true;
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private void initListener(final Scope scope) throws CouldNotPerformException {
        try {
            this.listener = RSBFactory.getInstance().createSynchronizedListener(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS));
            this.listenerWatchDog = new WatchDog(listener, "RSBListener[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS) + "]");
        } catch (InstantiationException ex) {
            throw new CouldNotPerformException("Could not create Listener on scope [" + scope + "]!", ex);
        }
    }

    private void initRemoteServer(final Scope scope) throws CouldNotPerformException {
        try {
            this.remoteServer = RSBFactory.getInstance().createSynchronizedRemoteServer(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL));
            this.remoteServerWatchDog = new WatchDog(remoteServer, "RSBRemoteServer[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL) + "]");
            this.listenerWatchDog.addObserver(new Observer<WatchDog.ServiceState>() {

                @Override
                public void update(Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data) throws Exception {
                    if (data == WatchDog.ServiceState.Running) {

                        // Sync data after service start.
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    remoteServerWatchDog.waitForActivation();
                                    sync();
                                } catch (InterruptedException | CouldNotPerformException ex) {
                                    ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not trigger data sync!", ex));
                                }
                            }
                        }.start();
                    }
                }
            });
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not create RemoteServer on scope [" + scope + "]!", ex);
        }
    }

    public void addHandler(final Handler handler, final boolean wait) throws InterruptedException, CouldNotPerformException {
        try {
            listener.addHandler(handler, wait);
        } catch (InterruptedException ex) {
            throw new CouldNotPerformException("Could not register Handler!", ex);
        }
    }

    public void activate() throws InterruptedException, CouldNotPerformException {
        if (!initialized) {
            throw new InvalidStateException("Skip activation because " + this + " is not initialized!");
        }
        activateListener();
        activateRemoteServer();
    }

    public void deactivate() throws InterruptedException, CouldNotPerformException {
        try {
            if (!initialized) {
                throw new InvalidStateException("Skip deactivation because " + this + " is not initialized!");
            }
            deactivateListener();
            deactivateRemoteServer();
        } catch (InvalidStateException | InterruptedException ex) {
            throw new CouldNotPerformException("Could not deactivate " + getClass().getSimpleName() + "!", ex);
        }
    }

    private void activateListener() throws InterruptedException {
        listenerWatchDog.activate();
    }

    private void deactivateListener() throws InterruptedException {
        listenerWatchDog.deactivate();
    }

    public boolean isConnected() {
        return listenerWatchDog.isActive() && listener.isActive() && remoteServerWatchDog.isActive() && remoteServer.isActive();
    }

    private void activateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.activate();
    }

    private void deactivateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.deactivate();
    }

    public Object callMethod(String methodName) throws CouldNotPerformException {
        return callMethod(methodName, null);
    }

    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException {
        return callMethodAsync(methodName, null);
    }

    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException {

        if (!initialized) {
            throw new CouldNotPerformException("Skip invocation of Method[" + methodName + "] because " + this + " is not initialized!");
        }

        try {
            logger.info("Calling method [" + methodName + "(" + type + ")] on scope: " + remoteServer.getScope().toString());
            return remoteServer.call(methodName, type);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }

    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException {

        if (!initialized) {
            throw new CouldNotPerformException("Skip invocation of Method[" + methodName + "] because " + this + " is not initialized!");
        }
        try {
            logger.info("Calling method [" + methodName + "(" + type + ")] on scope: " + remoteServer.getScope().toString());
            return remoteServer.callAsync(methodName, type);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }
    
    protected void sync() throws CouldNotPerformException {
        callMethodAsync(RPC_REQUEST_STATUS);
    }   

    public M requestStatus() throws CouldNotPerformException {
        try {
            logger.debug("requestStatus updated.");
            return (M) callMethod(RPC_REQUEST_STATUS);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not request the current status.", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (CouldNotPerformException | InterruptedException ex) {
            logger.error("Could not deactivate remote service!", ex);
        }
        super.shutdown();
    }

    public M getData() throws CouldNotPerformException {
        try {
            if (data == null) {
                return requestStatus();
            }
            return data;
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data", ex);
        }
    }

    public Class detectMessageClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }

    public static Scope generateScope(final String label, final Class typeClass, final ScopeProvider location) throws CouldNotPerformException {
        try {
            return location.getScope().concat(new Scope(Scope.COMPONENT_SEPARATOR + typeClass.getSimpleName())).concat(new Scope(Scope.COMPONENT_SEPARATOR + label));

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
    }

    protected final Object getField(String name) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = getData().getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + getData().getClass().getName());
            }
            return getData().getField(findFieldByName);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not return value of field [" + name + "] for " + this, ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[scope:" + scope + "]";
    }

    public abstract void notifyUpdated(M data) throws CouldNotPerformException;

    private class InternalUpdateHandler implements Handler {

        @Override
        public void internalNotify(Event event) {
            logger.debug("Internal notification: " + event.toString());
            try {
                data = (M) event.getData();
                logger.info("Data update for " + this);
                notifyUpdated(data);
                notifyObservers(data);
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not notify data update! Given Datatype[" + event.getData().getClass().getName() + "] is not compatible with " + this.getClass().getName() + "]!", ex));
            }
        }
    }
}

// TODO mpohling: Config for test setup.
//        ParticipantConfig config = rsb.Factory.getInstance().getDefaultParticipantConfig();
//        
//        for (Map.Entry<String,TransportConfig> transport : config.getTransports().entrySet()) {
//            transport.getValue().setEnabled(false);
//        }
//        config.getTransports().get("inmemory").setEnabled(true);
//        Informer informer = rsb.Factory.getInstance().createInformer(new Scope("/"), config);
