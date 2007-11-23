/*
 * @(#)  2007/11/23
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

import javax.swing.filechooser.FileFilter;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DODDLEProjectFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        return f.getName().toLowerCase().endsWith(".ddl");
    }

    @Override
    public String getDescription() {
        return Translator.getTerm("DODDLEProjectFileFilter");
    }
}
