/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SaveProjectAsAction extends SaveProjectAction {

    public SaveProjectAsAction(String title, DODDLE ddl) {
        super(title, Utils.getImageIcon("page_save.png"), ddl);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        if (currentProject == null) { return; }
        JFileChooser fileChooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File file) {
              if(file.isDirectory()) return true;
              return file.getName().toLowerCase().endsWith(".ddl");
            }
            public String getDescription() {
              return Translator.getTerm("DODDLEProjectFileFilter");
            }
        });
        int retval = fileChooser.showSaveDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        File saveFile = fileChooser.getSelectedFile();
        if (!saveFile.getName().endsWith(".ddl")) {
            saveFile = new File(saveFile.getAbsolutePath()+".ddl");
        }
        saveProject(saveFile, currentProject);
    }
}
