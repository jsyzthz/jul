package org.openbase.jul.extension.protobuf.processing;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import com.google.protobuf.Message;
import java.lang.reflect.InvocationTargetException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class GenericMessageProcessor<M extends Message> implements MessageProcessor<Message, M> {

    private static final String NEW_BUILDER_METHOD_NAME = "newBuilder";
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private M.Builder builder;
    private final Class<M> dataClass;

    public GenericMessageProcessor(final Class<M> dataClass) throws InitializationException {
        this.dataClass = dataClass;
        try {
            this.builder = (M.Builder) dataClass.getMethod(NEW_BUILDER_METHOD_NAME).invoke(null);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new InitializationException(this, new CouldNotPerformException("Could not create builder from dataClass[" + dataClass.getSimpleName() + "]!", ex));
        }
    }

    @Override
    public M process(final Message input) throws CouldNotPerformException, InterruptedException {
        if (dataClass.isInstance(input)) {
            return (M) input;
        }

        builder.clear();
        builder.getDescriptorForType().getFields().stream().forEach((fieldDescriptor) -> {
            input.getAllFields().keySet().stream().filter((inputFieldDescriptor) -> (fieldDescriptor.getType().equals(inputFieldDescriptor.getType())
                    && fieldDescriptor.getName().equals(inputFieldDescriptor.getName()))).forEach((inputFieldDescriptor) -> {
                        builder.setField(fieldDescriptor, input.getField(inputFieldDescriptor));
                    });
        });
        return (M) builder.build();
    }

    public Class<M> getDataClass() {
        return dataClass;
    }
}
