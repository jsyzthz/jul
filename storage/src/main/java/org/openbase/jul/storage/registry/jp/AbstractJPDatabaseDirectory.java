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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.preset.AbstractJPDirectory;
import org.openbase.jps.preset.JPHelp;
import org.openbase.jps.tools.FileHandler;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractJPDatabaseDirectory extends AbstractJPDirectory {

    public static FileHandler.ExistenceHandling existenceHandling = FileHandler.ExistenceHandling.Must;
    public static FileHandler.AutoMode autoMode = FileHandler.AutoMode.Off;

    public AbstractJPDatabaseDirectory(String[] commandIdentifier) {
        super(commandIdentifier, existenceHandling, autoMode);
    }

    @Override
    public void validate() throws JPValidationException {

        boolean reinitDetected = false;

        try {
            if (JPService.getProperty(JPInitializeDB.class).getValue()) {
                setAutoCreateMode(FileHandler.AutoMode.On);
                setExistenceHandling(FileHandler.ExistenceHandling.Must);
                reinitDetected = true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (JPService.getProperty(JPResetDB.class).getValue()) {
                setAutoCreateMode(FileHandler.AutoMode.On);
                setExistenceHandling(FileHandler.ExistenceHandling.MustBeNew);
                reinitDetected = true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        if (!getValue().exists() && !reinitDetected) {
            throw new JPValidationException("Could not detect Database["+getValue().getAbsolutePath()+"]! You can use the argument " + JPInitializeDB.COMMAND_IDENTIFIERS[0] + " to initialize a new db enviroment. Use " + JPHelp.COMMAND_IDENTIFIERS[0] + " to get more options.");
        }

        super.validate();
    }
}
