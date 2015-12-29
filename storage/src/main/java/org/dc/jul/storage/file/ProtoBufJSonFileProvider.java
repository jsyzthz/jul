/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.file;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Identifiable;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ProtoBufJSonFileProvider implements FileProvider<Identifiable<String>> {

    public static final String FILE_TYPE = "json";
    public static final String FILE_SUFFIX = "." + FILE_TYPE;

    @Override
    public String getFileName(Identifiable<String> context) throws CouldNotPerformException {
        try {
            if(context == null) {
                throw new NotAvailableException("context");
            }
            
            return convertIntoValidFileName(context.getId().replaceAll("/", "_")) + FILE_SUFFIX;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate file name!", ex);
        }
    }

    @Override
    public String getFileType() {
        return FILE_TYPE;
    }

    @Override
    public FileFilter getFileFilter() {
        return new FileFileFilter() {
            @Override
            public boolean accept(File file) {
                if (file == null) {
                    return false;
                }
                return (!file.isHidden()) && file.isFile() && file.getName().toLowerCase().endsWith(FILE_SUFFIX);
            }
        };
    }

    public String convertIntoValidFileName(final String filename) {
        return filename.replaceAll("[^0-9a-zA-Z-äöüÄÖÜéàèÉÈßÄ\\.\\-\\_\\[\\]\\#\\$]+", "_");
    }
}