/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.iface;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 * @param <ID>
 */
public interface Identifiable<ID> {
    
    public String FIELD_ID = "id";
    
	public ID getId();
}