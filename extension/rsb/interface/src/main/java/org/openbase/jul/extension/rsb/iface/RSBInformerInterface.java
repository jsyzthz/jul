package org.openbase.jul.extension.rsb.iface;

/*
 * #%L
 * JUL Extension RSB Interface
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import rsb.Event;

/**
 *
 * @author Divine Threepwood
 * @param <DataType>
 */
public interface RSBInformerInterface<DataType extends Object> extends RSBParticipantInterface {

    /**
     * Send an {@link Event} to all subscribed participants.
     *
     * @param event the event to send
     * @return modified event with set timing information
     * @throws CouldNotPerformException error sending event
     * @throws java.lang.InterruptedException
     */
    public Event publish(final Event event) throws CouldNotPerformException, InterruptedException;

    /**
     * Send data (of type T) to all subscribed participants.
     *
     * @param data data to send with default setting from the informer
     * @return generated event
     * @throws CouldNotPerformException
     * @throws java.lang.InterruptedException
     */
    public Event publish(final DataType data) throws CouldNotPerformException, InterruptedException;

    /**
     * Returns the class describing the type of data sent by this informer.
     *
     * @return class
     * @throws org.openbase.jul.exception.NotAvailableException
     */
    public Class<?> getTypeInfo() throws NotAvailableException;

    /**
     * Set the class object describing the type of data sent by this informer.
     *
     * @param typeInfo a {@link Class} instance describing the sent data
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public void setTypeInfo(final Class<DataType> typeInfo) throws CouldNotPerformException;

}