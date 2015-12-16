/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jps.core.JPService;
import de.citec.jps.exception.JPServiceException;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.extension.protobuf.BuilderSyncSetup;
import de.citec.jul.extension.protobuf.IdGenerator;
import de.citec.jul.extension.protobuf.IdentifiableMessage;
import de.citec.jul.extension.protobuf.container.ProtoBufMessageMap;
import de.citec.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import de.citec.jul.extension.protobuf.container.transformer.IdentifiableMessageTransformer;
import de.citec.jul.extension.protobuf.processing.ProtoBufFileProcessor;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.storage.registry.jp.JPGitRegistryPlugin;
import de.citec.jul.storage.registry.plugin.GitRegistryPlugin;
import java.io.File;
import java.util.List;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 * @param <KEY>
 * @param <M> Message
 * @param <MB> Message Builder
 * @param <SIB> Synchronized internal builder
 */
public class ProtoBufFileSynchronizedRegistry<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder<SIB>> extends FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> implements ProtoBufRegistryInterface<KEY, M, MB> {

    private final ProtoBufMessageMap<KEY, M, MB, SIB> protobufMessageMap;
    private final IdGenerator<KEY, M> idGenerator;
    private final Observer<IdentifiableMessage<KEY, M, MB>> observer;
    private final Class<M> messageClass;

    public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final BuilderSyncSetup<SIB> builderSetup, final Descriptors.FieldDescriptor fieldDescriptor, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        this(messageClass, new ProtoBufMessageMap<>(builderSetup, fieldDescriptor), idGenerator, databaseDirectory, fileProvider);
    }

    public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final ProtoBufMessageMap<KEY, M, MB, SIB> internalMap, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        super(internalMap, databaseDirectory, new ProtoBufFileProcessor<IdentifiableMessage<KEY, M, MB>, M, MB>(new IdentifiableMessageTransformer<>(messageClass, idGenerator)), fileProvider);
        try {
            this.idGenerator = idGenerator;
            this.messageClass = messageClass;
            this.protobufMessageMap = internalMap;
            this.observer = (Observable<IdentifiableMessage<KEY, M, MB>> source, IdentifiableMessage<KEY, M, MB> data) -> {
                ProtoBufFileSynchronizedRegistry.this.update(data);
            };
            protobufMessageMap.addObserver(observer);

            try {
                if (JPService.getProperty(JPGitRegistryPlugin.class).getValue()) {
                    registerPlugin(new GitRegistryPlugin(this));
                }
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            // TODO mpohling: got error: corrupted double-linked list: 0x00007f178c2a9200
            // -> may clone each sandbox entry instead of cloning whole collection.
            setupSandbox(new ProtoBufFileSynchronizedRegistrySandbox<KEY, M, MB, SIB>(idGenerator));
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public void shutdown() {
        protobufMessageMap.removeObserver(observer);
        protobufMessageMap.shutdown();
        super.shutdown();
    }

    /**
     * This method activate the version control unit of the underlying registry db. The version check and db upgrade is automatically performed during the registry db loading phrase. The db will be
     * upgraded to the latest db format provided by the given converter package. The converter package should contain only classes implementing the DBVersionConverter interface. To fully support
     * outdated db upgrade make sure that the converter pipeline covers the whole version range!
     *
     * Activate version control before loading the registry. Please provide within the converter package only converter with the naming structure
     * [$(EntryType)_$(VersionN)_To_$(VersionN+1)_DBConverter].
     *
     * Example:
     *
     * converter package myproject.db.converter containing the converter pipeline
     *
     * myproject.db.converter.DeviceConfig_0_To_1_DBConverter.class myproject.db.converter.DeviceConfig_1_To_2_DBConverter.class myproject.db.converter.DeviceConfig_2_To_3_DBConverter.class
     *
     * Would support the db upgrade from version 0 till the latest db version 3.
     *
     * @param converterPackage the package containing all converter which provides db entry updates from the first to the latest db version.
     * @throws CouldNotPerformException in case of an invalid converter pipeline or initialization issues.
     */
    public void activateVersionControl(final Package converterPackage) throws CouldNotPerformException {
        try {
            String entryType;
            try {
                entryType = messageClass.getSimpleName().replace("Type", "");
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not detect entry type!", ex);
            }
            super.activateVersionControl(entryType, converterPackage);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not activate version control!", ex);
        }
    }

    public Class<M> getMessageClass() {
        return messageClass;
    }

    @Override
    public M register(final M message) throws CouldNotPerformException {
        return super.register(new IdentifiableMessage<>(message, idGenerator)).getMessage();
    }

    @Override
    public boolean contains(final M message) throws CouldNotPerformException {
        return contains(new IdentifiableMessage<>(message, idGenerator).getId());
    }

    @Override
    public M update(final M message) throws CouldNotPerformException {
        return update(new IdentifiableMessage<>(message, idGenerator)).getMessage();
    }

    @Override
    public M remove(M locationConfig) throws CouldNotPerformException {
        return remove(new IdentifiableMessage<>(locationConfig, idGenerator)).getMessage();
    }

    @Override
    public M getMessage(final KEY id) throws CouldNotPerformException {
        return get(id).getMessage();
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        return entryMap.getMessages();
    }

    @Override
    public MB getBuilder(KEY key) throws CouldNotPerformException {
        return (MB) getMessage(key).toBuilder();
    }

    @Override
    public IdGenerator<KEY, M> getIdGenerator() {
        return idGenerator;
    }
}
