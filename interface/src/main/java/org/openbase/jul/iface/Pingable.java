package org.openbase.jul.iface;

/*
 * #%L
 * JUL Interface
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.annotations.RPCMethod;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Pingable {

    /**
     * Method can be used to calculate connection ping.
     * The given timestamp argument is just returned from the local server to calculate the delay on client side.
     *
     * @param timestemp
     * @return
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    @RPCMethod
    public Future<Long> ping(Long timestemp) throws CouldNotPerformException;
}
