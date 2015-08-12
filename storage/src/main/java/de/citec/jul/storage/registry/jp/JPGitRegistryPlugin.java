/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.jp;

import de.citec.jps.preset.AbstractJPBoolean;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class JPGitRegistryPlugin extends AbstractJPBoolean {

    public static final String[] COMMAND_IDENTIFIERS = {"--git-support"};

    public JPGitRegistryPlugin() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "Activates the git registry plugin to support database versioning.";
    }

}