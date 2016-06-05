package org.dc.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
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

import com.google.protobuf.GeneratedMessage.Builder;

/**
 *
 * @param <MB>
 */
public class ClosableDataBuilder<MB extends Builder<MB>> implements java.lang.AutoCloseable {

        private final BuilderSyncSetup<MB> builderSetup;

        public ClosableDataBuilder(final BuilderSyncSetup<MB> builderSetup, final Object consumer) {
            this.builderSetup = builderSetup;
            builderSetup.lockWrite(consumer);
        }

        public MB getInternalBuilder() {
            return builderSetup.getBuilder();
        }

        @Override
        public void close() throws Exception {
            builderSetup.unlockWrite();
        }
    }