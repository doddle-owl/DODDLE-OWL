/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class VisualizeAction extends AbstractAction {

    private DODDLEPlugin doddlePlugin;

    public VisualizeAction(String title, DODDLEPlugin dp) {
        super(title);
        doddlePlugin = dp;
    }

    /**
     * MR�R�Ƃ̂������Ǘ�����A�N�V�������Z�b�g����
     */
    public void actionPerformed(ActionEvent e) {
        if (doddlePlugin != null) {
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            ConstructConceptTreePanel constructConceptTreePanel = currentProject.getConstructConceptTreePanel();
            Model ontology = JenaModelMaker.makeClassModel(constructConceptTreePanel.getTreeModelRoot(),
                    ModelFactory.createDefaultModel());
            doddlePlugin.replaceRDFSModel(ontology);
        }
    }
}
