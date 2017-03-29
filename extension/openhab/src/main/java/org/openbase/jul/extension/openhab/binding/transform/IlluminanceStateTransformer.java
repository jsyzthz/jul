package org.openbase.jul.extension.openhab.binding.transform;

import rst.domotic.state.IlluminanceStateType.IlluminanceState;

/*-
 * #%L
 * JUL Extension OpenHAB
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
/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class IlluminanceStateTransformer {

    /**
     * Transform a number to an illuminationState by setting the number as the illuminance.
     *
     * @param value the brightness value
     * @return the corresponding brightness state
     */
    public static IlluminanceState transform(final Double value) {
        IlluminanceState.Builder state = IlluminanceState.newBuilder();
        state.setIlluminance(value);
        return state.build();
    }

    /**
     * Get the illuminance value.
     *
     * @param illuminanceState the state
     * @return the current illuminance
     */
    public static Double transform(IlluminanceState illuminanceState) {
        return illuminanceState.getIlluminance();
    }
}