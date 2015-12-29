/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public interface RegistrySandboxInterface<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>> extends RegistryInterface<KEY, ENTRY, R> {

    public void sync(final MAP map) throws CouldNotPerformException;
    
    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException;
    
    public void removeConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException;
    
    void replaceInternalMap(Map<KEY, ENTRY> map) throws CouldNotPerformException;

    public ENTRY load(final ENTRY entry) throws CouldNotPerformException;
}