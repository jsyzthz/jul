/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.VerificationFailedException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.IdentifiableMessageMap;
import org.dc.jul.extension.protobuf.ProtobufListDiff;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.pattern.Factory;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.Observer;
import org.dc.jul.schedule.RecurrenceEventFilter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 */
public class RegistrySynchronizer<KEY, ENTRY extends Identifiable<KEY>, CONFIG_M extends GeneratedMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Registry<KEY, ENTRY> registry;
    private final Observer<Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>>> remoteChangeObserver;
    private final RecurrenceEventFilter recurrenceSyncFilter;
    private final ProtobufListDiff<KEY, CONFIG_M, CONFIG_MB> entryConfigDiff;
    private final Factory<ENTRY, CONFIG_M> factory;
    protected final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB, ?> remoteRegistry;

    public RegistrySynchronizer(final Registry<KEY, ENTRY> registry, final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB, ?> remoteRegistry, final Factory<ENTRY, CONFIG_M> factory) throws org.dc.jul.exception.InstantiationException {
        try {
            this.registry = registry;
            this.remoteRegistry = remoteRegistry;
            this.entryConfigDiff = new ProtobufListDiff<>();
            this.factory = factory;
            this.recurrenceSyncFilter = new RecurrenceEventFilter(15000) {

                @Override
                public void relay() throws Exception {
                    internalSync();
                }
            };

            this.remoteChangeObserver = (Observable<Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>>> source, Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>> data) -> {
                sync();
            };

        } catch (Exception ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    public void init() throws CouldNotPerformException, InterruptedException {
        this.remoteRegistry.addObserver(remoteChangeObserver);
        try {
            this.internalSync();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial sync failed!", ex), logger, LogLevel.ERROR);
            try {
                if (JPService.getProperty(JPTestMode.class).getValue()) {
                    throw ex;
                }
            } catch (JPServiceException exx) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", exx), logger);
            }
        }
    }

    public void shutdown() {
        this.remoteRegistry.removeObserver(remoteChangeObserver);
        this.recurrenceSyncFilter.cancel();
    }

    private void sync() {
        recurrenceSyncFilter.trigger();
    }

    private synchronized void internalSync() throws CouldNotPerformException, InterruptedException {
        logger.info("Perform registry sync...");

        try {
            entryConfigDiff.diff(remoteRegistry.getMessages());

            MultiException.ExceptionStack removeExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getRemovedMessageMap().getMessages()) {
                try {
                    remove(config);
                } catch (CouldNotPerformException ex) {
                    removeExceptionStack = MultiException.push(this, ex, removeExceptionStack);
                }
            }

            MultiException.ExceptionStack updateExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getUpdatedMessageMap().getMessages()) {
                try {
                    if (verifyConfig(config)) {
                        update(config);
                    } else {
                        remove(config);
                        entryConfigDiff.getOriginMessages().removeMessage(config);
                    }
                } catch (CouldNotPerformException ex) {
                    updateExceptionStack = MultiException.push(this, ex, updateExceptionStack);
                }
            }

            MultiException.ExceptionStack registerExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getNewMessageMap().getMessages()) {
                try {
                    if (verifyConfig(config)) {
                        register(config);
                    }
                } catch (CouldNotPerformException ex) {
                    registerExceptionStack = MultiException.push(this, ex, registerExceptionStack);
                }
            }

            int errorCounter = MultiException.size(removeExceptionStack) + MultiException.size(updateExceptionStack) + MultiException.size(registerExceptionStack);
            logger.info(entryConfigDiff.getChangeCounter() + " registry changes applied. " + errorCounter + " are skipped.");

            // sync origin list.
            IdentifiableMessageMap<KEY, CONFIG_M, CONFIG_MB> newOriginEntryMap = new IdentifiableMessageMap<>();
            for (ENTRY entry : registry.getEntries()) {
                newOriginEntryMap.put(remoteRegistry.get(entry.getId()));
            }
            entryConfigDiff.replaceOriginMap(newOriginEntryMap);

            // build exception cause chain.
            MultiException.ExceptionStack exceptionStack = null;
            try {
                MultiException.checkAndThrow("Could not remove all entries!", removeExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            try {
                MultiException.checkAndThrow("Could not update all entries!", updateExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            try {
                MultiException.checkAndThrow("Could not register all entries!", registerExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            MultiException.checkAndThrow("Could not sync all entries!", exceptionStack);

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Entry registry sync failed!", ex);
        }
    }

    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return registry.register(factory.newInstance(config));
    }

    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return registry.update(factory.newInstance(config));
    }

    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return registry.remove(remoteRegistry.getKey(config));
    }

    /**
     * Method should return true if the given configurations is valid, otherwise false. This default implementation accepts all configurations. To implement a custom verification just overwrite this
     * method.
     *
     * @param config
     * @return
     * @throws org.dc.jul.exception.VerificationFailedException
     */
    public boolean verifyConfig(final CONFIG_M config) throws VerificationFailedException {
        return true;
    }
}