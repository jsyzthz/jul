/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.iface;

import de.citec.jul.exception.CouldNotPerformException;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public interface Changeable {

    public void notifyChange() throws CouldNotPerformException;
}