/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

/**
 * @author takeshi morita
 */
public class SaveConceptTypicalWordAction extends AbstractAction {

    public SaveConceptTypicalWordAction(String title) {
        super(title);
    }

    public void saveIDTypicalWord(DODDLEProject currentProject, File file) {
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));

            Map uriTypicalWordMap = constructClassPanel.getIDTypicalWordMap();
            uriTypicalWordMap.putAll(constructPropertyPanel.getIDTypicalWordMap());
            StringBuffer buf = new StringBuffer();
            for (Iterator i = uriTypicalWordMap.keySet().iterator(); i.hasNext();) {
                String id = (String) i.next();
                String typicalWord = (String) uriTypicalWordMap.get(id);
                buf.append(id + "\t" + typicalWord + "\n");
            }
            writer.write(buf.toString());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_DIR);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval != JFileChooser.APPROVE_OPTION) { return; }
        DODDLEProject currentProject = DODDLE.getCurrentProject();
        saveIDTypicalWord(currentProject, chooser.getSelectedFile());
    }
}
