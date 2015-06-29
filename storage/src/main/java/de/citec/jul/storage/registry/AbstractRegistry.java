/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPReadOnly;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.schedule.SyncObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine Threepwood
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>> extends Observable<Map<KEY, ENTRY>> implements RegistryInterface<KEY, ENTRY, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final SyncObject SYNC = new SyncObject(AbstractRegistry.class);
    protected final MAP entryMap;
    private final List<ConsistencyHandler<KEY, ENTRY, MAP, R>> consistencyHandlerList;

    public AbstractRegistry(final MAP entryMap) throws InstantiationException {
        try {
            this.entryMap = entryMap;
            this.consistencyHandlerList = new ArrayList<>();
            this.checkConsistency();
            this.notifyObservers();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    shutdown();
                }
            }));

        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Register " + entry + "...");
        try {
            checkAccess();
            synchronized (SYNC) {
                if (entryMap.containsKey(entry.getId())) {
                    throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                }
                entryMap.put(entry.getId(), entry);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + "!", ex);
        }
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            try {
                superRemove(entry);
            } catch (Exception exx) {
                ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not remove invalid entry!", exx));
            }
            throw ex;
        }
        notifyObservers();
        return entry;
    }

    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Update " + entry + "...");
        try {
            checkAccess();
            synchronized (SYNC) {
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                // replace
                entryMap.put(entry.getId(), entry);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update " + entry + "!", ex);
        }
        checkConsistency();
        notifyObservers();
        return entry;
    }

    @Override
    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException {
        return superRemove(entry);
    }

    public ENTRY superRemove(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Remove " + entry + "...");
        try {
            checkAccess();
            synchronized (SYNC) {
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                return entryMap.remove(entry.getId());
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove " + entry + "!", ex);
        } finally {
            checkConsistency();
            notifyObservers();
        }
    }

    @Override
    public ENTRY get(final KEY key) throws CouldNotPerformException {
        verifyID(key);
        synchronized (SYNC) {
            if (!entryMap.containsKey(key)) {
                TreeMap<KEY, ENTRY> sortedMap = new TreeMap<>(new Comparator<KEY>() {
                    @Override
                    public int compare(KEY o1, KEY o2) {
                        if (o1 instanceof String && o2 instanceof String) {
                            return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
                        }
                        return ((Comparable<KEY>) o1).compareTo(o2);
                    }
                });
                sortedMap.putAll(entryMap);
                throw new NotAvailableException("Entry[" + key + "]", "Nearest neighbor is [" + sortedMap.floorKey(key) + "] or [" + sortedMap.ceilingKey(key) + "].");
            }
            return entryMap.get(key);
        }
    }

    @Override
    public List<ENTRY> getEntries() {
        synchronized (SYNC) {
            return new ArrayList<>(entryMap.values());
        }
    }

    public int size() {
        synchronized (SYNC) {
            return entryMap.size();
        }
    }

    public boolean isEmpty() {
        synchronized (SYNC) {
            return entryMap.isEmpty();
        }
    }

    @Override
    public boolean contains(final ENTRY entry) throws CouldNotPerformException {
        return contains(entry.getId());
    }

    @Override
    public boolean contains(final KEY key) throws CouldNotPerformException {
        return entryMap.containsKey(verifyID(key));
    }

    @Override
    public void clear() {
        synchronized (SYNC) {
            entryMap.clear();
        }
        notifyObservers();
    }

    protected void replaceInternalMap(final Map<KEY, ENTRY> map) {
        synchronized (SYNC) {
            entryMap.clear();
            entryMap.putAll(map);
        }
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        if (JPService.getProperty(JPReadOnly.class).getValue()) {
            throw new InvalidStateException("ReadOnlyMode is detected!");
        }
    }

    private void notifyObservers() {
        try {
            super.notifyObservers(entryMap);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not notify all observer!", ex));
        }
    }

    protected KEY verifyID(final ENTRY entry) throws VerificationFailedException {
        try {
            return verifyID(entry.getId());
        } catch (CouldNotPerformException ex) {
            throw new VerificationFailedException("Could not verify message!", ex);
        }
    }

    protected KEY verifyID(final KEY id) throws VerificationFailedException {
        if (id == null) {
            throw new VerificationFailedException("Invalid id!", new NotAvailableException("id"));
        }
        return id;
    }

    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException {
        consistencyHandlerList.add(consistencyHandler);
    }

    private boolean consistencyCheckRunning = false;

    public synchronized boolean checkConsistency() throws CouldNotPerformException {

        if (consistencyHandlerList.isEmpty()) {
            logger.debug("Skip consistency check because no handler are registered.");
            return true;
        }

        try {

            boolean modification = false;

            // avoid dublicated consistency check
            if (consistencyCheckRunning) {
                return modification;
            }
            consistencyCheckRunning = true;

            int iterationCounter = 0;
            MultiException.ExceptionStack exceptionStack = null;

            ConsistencyHandler lastActiveConsistencyHandler = null;
            Object lastModifieredEntry = null;

            synchronized (SYNC) {
                while (true) {

                    // handle handler interference
                    if (iterationCounter > consistencyHandlerList.size() * entryMap.size() * 2) {
                        MultiException.checkAndThrow("To many errors occoured during processing!", exceptionStack);
                        throw new InvalidStateException("ConsistencyHandler interference detected!");
                    }

                    if (exceptionStack != null) {
                        exceptionStack.clear();
                    }

                    iterationCounter++;
                    try {
                        for (ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler : consistencyHandlerList) {
                            consistencyHandler.reset();
                            for (ENTRY entry : entryMap.values()) {
                                try {
                                    consistencyHandler.processData(entry.getId(), entry, entryMap, (R) this);
                                } catch (CouldNotPerformException | NullPointerException ex) {
                                    exceptionStack = MultiException.push(consistencyHandler, new VerificationFailedException("Could not verify registry data consistency!", ex), exceptionStack);
                                }
                            }
                        }
                    } catch (EntryModification ex) {

                        // check if consistency handler is looping
//                        System.out.println("#### ch:" + lastActiveConsistencyHandler + " = " + lastModifieredEntry);
                        if (ex.getConsistencyHandler() == lastActiveConsistencyHandler && ex.getEntry().equals(lastModifieredEntry)) {
                            throw new InvalidStateException("ConsistencyHandler[" + lastActiveConsistencyHandler + "] is looping over same Entry[" + lastModifieredEntry + "] more than once!");
                        }
                        lastActiveConsistencyHandler = ex.getConsistencyHandler();
                        lastModifieredEntry = ex.getEntry();

                        // inform about modifivation
                        logger.info("Consistency modification applied: " + ex.getMessage());
                        modification = true;
                        continue;
                    } catch (Throwable ex) {
                        exceptionStack = MultiException.push(this, new InvalidStateException("Fatal error occured during consistency check!", ex), exceptionStack);
                    }

                    if (exceptionStack != null && !exceptionStack.isEmpty()) {
                        continue;
                    }

                    logger.info("Registry consistend.");
                    break;
                }
            }
            consistencyCheckRunning = false;
            return modification;

        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Consistency process aborted!", ex));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        clear();
    }
}
