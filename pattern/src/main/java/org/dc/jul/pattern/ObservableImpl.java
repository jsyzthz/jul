package org.dc.jul.pattern;

/*
 * #%L
 * JUL Pattern
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.MultiException.ExceptionStack;
import org.dc.jul.exception.NotAvailableException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 * @param <T>
 */
public class ObservableImpl<T> implements Observable<T>{

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservableImpl.class);

    private static final boolean DEFAULT_UNCHANGED_VALUE_FILTER = true;

    private final boolean unchangedValueFilter;
    private final Object LOCK = new Object();
    private final List<Observer<T>> observers;
    private T latestValue;

    public ObservableImpl() {
        this(DEFAULT_UNCHANGED_VALUE_FILTER);
    }

    public ObservableImpl(final boolean unchangedValueFilter) {
        this.observers = new ArrayList<>();
        this.unchangedValueFilter = unchangedValueFilter;
    }

    @Override
    public void addObserver(Observer<T> observer) {
        synchronized (LOCK) {
            if (observers.contains(observer)) {
                LOGGER.warn("Skip observer registration. Observer[" + observer + "] is already registered!");
                return;
            }

            observers.add(observer);
            //TODO mpohling: check if this is usefull and does not interfere with any usage.
//            try {
//                observer.update(this, latestValue);
//            } catch (Exception ex) {
//                ExceptionPrinter.printHistory(new CouldNotPerformException("Initial Observer[" + observer + "] sync failed!", ex), logger, LogLevel.ERROR);
//            }
        }
    }

    @Override
    public void removeObserver(Observer<T> observer) {
        synchronized (LOCK) {
            observers.remove(observer);
        }
    }

    @Override
    public void shutdown() {
        synchronized (LOCK) {
            observers.clear();
        }
    }

    public void notifyObservers(Observable<T> source, T observable) throws MultiException {
        ExceptionStack exceptionStack = null;

        synchronized (LOCK) {
            //TODO mpohling: check why this is not working!
//            if (observable == null) {
//                logger.debug("Skip notification because observable is null!");
//                return;
//            }
//
//            if (unchangedDataFilter && latestValue != null && latestValue.equals(observable) && latestValue.toString().equals(observable.toString())) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(Observable.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                logger.debug("Skip notification because observable has not changed!");
//                return;
//            }

            latestValue = observable;

            for (Observer<T> observer : observers) {
                try {
                    observer.update(source, observable);
                } catch (Exception ex) {
                    exceptionStack = MultiException.push(observer, ex, exceptionStack);
                }
            }
        }
        MultiException.checkAndThrow("Could not notify Data[" + observable + "] to all observer!", exceptionStack);
    }

    public void notifyObservers(T observable) throws MultiException {
        notifyObservers(this, observable);
    }

    @Override
    public T getLatestValue() throws NotAvailableException {
        if (latestValue == null) {
            throw new NotAvailableException("latestvalue");
        }
        return latestValue;
    }
}
