package org.openbase.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;
import org.openbase.jps.preset.JPInitialize;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
@Deprecated
public class JPInitializeDB extends AbstractJPBoolean {

    public final static String[] COMMAND_IDENTIFIERS = {"--init-db"};

    public JPInitializeDB() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * returns true if JPS is in test mode or JPResetDB is enabled.
     * @return
     */
    @Override
    protected Boolean getPropertyDefaultValue() {
        try {
            return JPService.getProperty(JPInitialize.class).getValue() || JPService.getProperty(JPResetDB.class).getValue();
        } catch (JPNotAvailableException ex) {
            JPService.printError("Could not load default value!", ex);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Initialize a new instance of the internal database.";
    }
}
