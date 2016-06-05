package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import static org.dc.jul.iface.Identifiable.TYPE_FIELD_ID;
import org.dc.jul.pattern.IdentifiableRemote;

/**
 *
 * @author Divine Threepwood
 * @param <M>
 */
public abstract class AbstractIdentifiableRemote<M extends GeneratedMessage> extends RSBRemoteService<M> implements IdentifiableRemote<String, M> {

    public AbstractIdentifiableRemote(final Class<M> dataClass) {
        super(dataClass);
    }

    @Override
    public String getId() throws NotAvailableException {
        try {
            String id = (String) getDataField(TYPE_FIELD_ID);
            if (id.isEmpty()) {
                throw new InvalidStateException("data.id is empty!");
            }
            return id;
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data.id", ex);
        }
    }
}
