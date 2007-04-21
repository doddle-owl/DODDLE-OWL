/*
 * @(#)  2006/06/04
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class VisualizationPanel extends JPanel implements ActionListener {

    private JButton toMR3Button;
    private JButton toDoddleButton;
    private DODDLEProject currentProject;

    public VisualizationPanel(DODDLEProject project) {
        currentProject = project;
        toMR3Button = new JButton("<html><body>DODDLE → MR<sup>3</sup></body></html>");
        toMR3Button.addActionListener(this);
        toMR3Button.setFont(new Font("Dialog", Font.BOLD, 25));
        toDoddleButton = new JButton("<html><body>MR<sup>3</sup> → DODDLE</body></html>");
        toDoddleButton.setFont(new Font("Dialog", Font.BOLD, 25));
        toDoddleButton.addActionListener(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));
        mainPanel.add(toMR3Button);
        mainPanel.add(toDoddleButton);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.NORTH);
    }

    public void toMR3() {
        DODDLE.STATUS_BAR.setText("Loading DODDLE to MR3");
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();
        ConceptDefinitionPanel conceptDefinitionPanel = currentProject.getConceptDefinitionPanel();
        Model ontology = JenaModelMaker.makeClassModel(constructClassPanel.getIsaTreeModelRoot(), ModelFactory
                .createDefaultModel(), ConceptTreePanel.ISA_TREE);
        JenaModelMaker.makePropertyModel(constructPropertyPanel.getIsaTreeModelRoot(), ontology, ConceptTreePanel.ISA_TREE);
        conceptDefinitionPanel.addConceptDefinition(ontology);
        DODDLE.getDODDLEPlugin().replaceRDFSModel(ontology);
        DODDLE.STATUS_BAR.setText("Loading DODDLE to MR3 Done");
    }

    public void toDODDLE() {
        DODDLE.STATUS_BAR.setText("Loading MR3 to DODDLE");
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        constructClassPanel.getIsaTree().setSelectionRow(0); // 照合結果分析結果を選択した状態で復元すると例外が発生するため
        constructPropertyPanel.getIsaTree().setSelectionRow(0);// 照合結果分析結果を選択した状態で復元すると例外が発生するため

        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(disambiguationPanel.getInputConceptSet());

        Model model = DODDLE.getDODDLEPlugin().getModel();
        currentProject.initUserIDCount();
        TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                ResourceFactory.createResource(DODDLE.BASE_URI + ConceptTreeMaker.DODDLE_CLASS_ROOT_URI),
                ConceptTreePanel.ISA_TREE);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        constructClassPanel.checkMultipleInheritance(treeModel);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount());
        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(DODDLE.BASE_URI + ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI), ConceptTreePanel.ISA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        constructPropertyPanel.checkMultipleInheritance(treeModel);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        DODDLE.STATUS_BAR.setText("Loading MR3 to DODDLE Done");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == toMR3Button) {
            toMR3();
        } else if (e.getSource() == toDoddleButton) {
            toDODDLE();
        }
    }
}
