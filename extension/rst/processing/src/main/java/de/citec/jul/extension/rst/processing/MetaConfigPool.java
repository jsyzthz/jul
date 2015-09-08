/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rst.processing;

import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.processing.VariableProcessor;
import de.citec.jul.processing.VariableProvider;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class MetaConfigPool {

    private Collection<VariableProvider> variableProviderPool;

    public MetaConfigPool(Collection<VariableProvider> variableProviderPool) {
        this.variableProviderPool = new ArrayList<>(variableProviderPool);
    }

    public MetaConfigPool() {
        this.variableProviderPool = new ArrayList<>();
    }

    public void register(VariableProvider provider) {
        variableProviderPool.add(provider);
    }

    public String getValue(String variable) throws NotAvailableException {
        try {
            return VariableProcessor.resolveVariables(VariableProcessor.resolveVariable(variable, variableProviderPool), true, variableProviderPool);
        } catch (MultiException ex) {
            throw new NotAvailableException("Variable[" + variable + "]", ex);
        }
    }
}