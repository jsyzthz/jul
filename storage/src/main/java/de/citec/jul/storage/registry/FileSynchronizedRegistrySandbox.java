/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.storage.registry.plugin.FileRegistryPlugin;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <MAP>
 * @param <R>
 */
public class FileSynchronizedRegistrySandbox<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends FileSynchronizedRegistryInterface<KEY, VALUE, R>> extends RegistrySandbox<KEY, VALUE, MAP, R, FileRegistryPlugin> implements FileSynchronizedRegistryInterface<KEY, VALUE, R> {

    public FileSynchronizedRegistrySandbox(final MAP entryMap) throws InstantiationException {
        super(entryMap);
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
    }

    @Override
    public void saveRegistry() throws CouldNotPerformException {
    }
}