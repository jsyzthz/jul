package org.openbase.jul.extension.openhab.binding.transform;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.exception.CouldNotTransformException;
import rst.vision.HSVColorType.HSVColor;
import rst.homeautomation.openhab.HSBType;

/**
 *
 * @author thuxohl
 */
public class HSVColorTransformer {

    public static HSVColor transform(HSBType.HSB color) throws CouldNotTransformException {
        try {
            return HSVColor.newBuilder().setHue(color.getHue()).setSaturation(color.getSaturation()).setValue(color.getBrightness()).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSBType.HSB.class.getName() + " to " + HSBType.HSB.class.getName() + "!", ex);
        }
    }

    public static HSBType.HSB transform(HSVColor color) throws CouldNotTransformException {
        try {
            return HSBType.HSB.newBuilder().setHue(color.getHue()).setSaturation(color.getSaturation()).setBrightness(color.getValue()).build();
        } catch (Exception ex) {
            throw new CouldNotTransformException("Could not transform " + HSVColor.class.getName() + " to " + HSBType.class.getName() + "!", ex);
        }
    }
}