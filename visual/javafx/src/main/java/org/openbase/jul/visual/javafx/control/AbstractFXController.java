package org.openbase.jul.visual.javafx.control;

/*-
 * #%L
 * JUL Visual JavaFX
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

import javafx.fxml.Initializable;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.visual.javafx.iface.DynamicPane;
import org.openbase.jul.visual.javafx.launch.AbstractFXApplication;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractFXController implements Initializable, DynamicPane {

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(final URL url, final ResourceBundle rb) {
        try {
            initContent();
        } catch(Exception ex) {
            AbstractFXApplication.exit(10, new InitializationException(this, ex));
        }
    }
}