/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.w3c.dom.*;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author takeshi morita
 */
public class LoadOntologyAction extends AbstractAction {

    private String conversionType;
    public static final String OWL_ONTOLOGY = "OWL";
    public static final String FREEMIND_ONTOLOGY = "FREEMIND";

    public LoadOntologyAction(String title, String type) {
        super(title);
        conversionType = type;
    }

    public void loadFreeMindOntology(DODDLEProject currentProject, File file) {
        InputConceptSelectionPanel inputConceptSelectionPanel = currentProject.getInputConceptSelectionPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) { return; }
        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(inputConceptSelectionPanel.getInputConceptSet());

        Element docElement = FreeMindModelMaker.getDocumentElement(file);
        Element rootNode = null;
        Element nounRootNode = null;
        Element verbRootNode = null;
        NodeList rootNodeList = docElement.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
            }
        }
        rootNodeList = rootNode.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
                if (rootNode.getAttribute("ID").equals(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI)) {
                    nounRootNode = rootNode;
                } else if (rootNode.getAttribute("ID").equals(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI)) {
                    verbRootNode = rootNode;
                }
            }
        }

        Concept rootNounConcept = new VerbConcept(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI, "");
        rootNounConcept.addLabel(new DODDLELiteral("ja", "名詞的概念 (Is-a)"));
        rootNounConcept.addLabel(new DODDLELiteral("en", "Is-a Root Class"));
        ConceptTreeNode rootTreeNode = new ConceptTreeNode(rootNounConcept, DODDLE.getCurrentProject());
        FreeMindModelMaker.setConceptTreeModel(rootTreeNode, nounRootNode);

        currentProject.initUserIDCount();
        DefaultTreeModel treeModel = new DefaultTreeModel(rootTreeNode);
        treeModel = constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();

        currentProject.setUserIDCount(currentProject.getUserIDCount());
        VerbConcept rootVerbConcept = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI, "");
        rootVerbConcept.addLabel(new DODDLELiteral("ja", "動詞的概念"));
        rootVerbConcept.addLabel(new DODDLELiteral("en", "Root Property"));
        rootTreeNode = new ConceptTreeNode(rootVerbConcept, DODDLE.getCurrentProject());
        FreeMindModelMaker.setConceptTreeModel(rootTreeNode, verbRootNode);
        treeModel = new DefaultTreeModel(rootTreeNode);
        treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        expandTrees(currentProject);
    }
    
    
    public void loadOWLOntology(DODDLEProject currentProject, File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Model model = ModelFactory.createDefaultModel();
            model.read(reader, DODDLEConstants.BASE_URI, "RDF/XML");
            loadOWLOntology(currentProject, model);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
    }

    public void loadOWLOntology(DODDLEProject currentProject, Model model) {
        InputConceptSelectionPanel inputConceptSelectionPanel = currentProject.getInputConceptSelectionPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(inputConceptSelectionPanel.getInputConceptSet());
        
        currentProject.initUserIDCount();
        TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI),
                ConceptTreePanel.CLASS_ISA_TREE);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        treeModel = constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructClassPanel.setConceptDriftManagementResult();
        treeModel.reload();
        
        rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI),
                ConceptTreePanel.CLASS_HASA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        constructClassPanel.setHasaTreeModel(treeModel);
        constructClassPanel.setVisibleHasaTree(true);
        treeModel.reload();
        
        currentProject.setUserIDCount(currentProject.getUserIDCount());
        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI),
                ConceptTreePanel.PROPERTY_ISA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        treeModel = constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        
        rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI),
                ConceptTreePanel.PROPERTY_HASA_TREE);
        treeModel = new DefaultTreeModel(rootNode);
        constructPropertyPanel.setHasaTreeModel(treeModel);
        constructPropertyPanel.setVisibleHasaTree(true);
        treeModel.reload();
        
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
        expandTrees(currentProject);
    }
    
    public void loadOWLOntology(int projectID, IDBConnection con, DODDLEProject project) {
        ModelMaker maker = ModelFactory.createModelRDBMaker(con);
        Model model = maker.openModel("DODDLE Project "+projectID);
        loadOWLOntology(project, model);
        model.close();
    }

    private void expandTrees(DODDLEProject currentProject) {
        currentProject.getConstructClassPanel().expandIsaTree();
        currentProject.getConstructClassPanel().expandHasaTree();
        currentProject.getConstructPropertyPanel().expandIsaTree();
        currentProject.getConstructPropertyPanel().expandHasaTree();
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLEConstants.PROJECT_HOME);
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File file) {
              if(file.isDirectory()) return true;
              return file.getName().toLowerCase().endsWith(".owl");
            }
            public String getDescription() {
              return Translator.getTerm("OWLFileFilter");
            }
        });
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            if (conversionType.equals(OWL_ONTOLOGY)) {
                loadOWLOntology(currentProject, chooser.getSelectedFile());
                DODDLE.STATUS_BAR.setText(Translator.getTerm("OpenOWLOntologyAction"));
            } else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
                loadFreeMindOntology(currentProject, chooser.getSelectedFile());
                DODDLE.STATUS_BAR.setText(Translator.getTerm("OpenFreeMindOntologyAction"));
            }
        }
    }
}
