/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class VariableProcessor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(VariableProcessor.class);

    public static String resolveVariables(String context, final boolean throwOnError, final VariableProvider... providers) throws MultiException {
        String variableIdentifier, variableValue;
        MultiException.ExceptionStack exceptionStack = null;
        while (!Thread.interrupted()) {

            // Detect variables
            variableIdentifier = StringUtils.substringBetween(context, VariableProvider.VARIABLE_INITIATOR, VariableProvider.VARIABLE_TERMINATOR);
            if (variableIdentifier == null) {
                // Context does not contain any variables.
                break;
            }

            // Resolve detected variable.
            variableValue = "";
            for (VariableProvider provider : providers) {
                try {
                    variableValue = provider.getValue(variableIdentifier);
                    logger.debug("Variable[" + variableIdentifier + "] = Value[" + variableValue + "] resolved by Provider[" + provider.getName() + "].");
                    break;
                } catch (NotAvailableException ex) {
                    exceptionStack = MultiException.push(logger, ex, exceptionStack);
                    variableValue = "";
                }
            }

            // Replace detected variable by it's value in the given context.
            context = StringUtils.replace(context, VariableProvider.VARIABLE_INITIATOR + variableIdentifier + VariableProvider.VARIABLE_TERMINATOR, variableValue);

        }

        try {
            MultiException.checkAndThrow("Could not resolve all variables!", exceptionStack);
        } catch (MultiException ex) {
            if (throwOnError) {
                throw ex;
            } else {
                ExceptionPrinter.printHistory(logger, ex);
            }
        }
        return context;
    }
}
