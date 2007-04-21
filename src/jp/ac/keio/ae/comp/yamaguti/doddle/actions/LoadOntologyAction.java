/*
 * @(#)  2006/02/08
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.xml.parsers.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.*;

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
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) { return; }
        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(disambiguationPanel.getInputConceptSet());

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
                if (rootNode.getAttribute("ID").equals("http://www.yamaguti.comp.ae.keio.ac.jp/doddle#CLASS_ROOT")) {
                    nounRootNode = rootNode;
                } else if (rootNode.getAttribute("ID")
                        .equals("http://www.yamaguti.comp.ae.keio.ac.jp/doddle#PROP_ROOT")) {
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
        constructClassPanel.setConceptTreeModel(treeModel);
        constructClassPanel.setVisibleIsaTree(true);
        constructClassPanel.checkMultipleInheritance(treeModel);
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
        constructPropertyPanel.setConceptTreeModel(treeModel);
        constructPropertyPanel.setVisibleIsaTree(true);
        constructPropertyPanel.checkMultipleInheritance(treeModel);
        ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
        constructPropertyPanel.setConceptDriftManagementResult();
        treeModel.reload();
        currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
    }

    public void loadOWLOntology(DODDLEProject currentProject, File file) {
        DisambiguationPanel disambiguationPanel = currentProject.getDisambiguationPanel();
        ConstructClassPanel constructClassPanel = currentProject.getConstructClassPanel();
        ConstructPropertyPanel constructPropertyPanel = currentProject.getConstructPropertyPanel();

        if (!file.exists()) { return; }
        constructClassPanel.init();
        constructPropertyPanel.init();
        currentProject.resetURIConceptMap();
        ConceptTreeMaker.getInstance().setInputConceptSet(disambiguationPanel.getInputConceptSet());
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            Model model = ModelFactory.createDefaultModel();
            model.read(reader, DODDLE.BASE_URI, "RDF/XML");
            currentProject.initUserIDCount();
            TreeNode rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_ROOT_URI), ConceptTreePanel.ISA_TREE);
            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
            constructClassPanel.setConceptTreeModel(treeModel);
            constructClassPanel.setVisibleIsaTree(true);
            constructClassPanel.checkMultipleInheritance(treeModel);
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
            ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
            constructClassPanel.setConceptDriftManagementResult();
            treeModel.reload();
            
            rootNode = ConceptTreeMaker.getInstance().getConceptTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_CLASS_HASA_ROOT_URI), ConceptTreePanel.CLASS_HASA_TREE);
            treeModel = new DefaultTreeModel(rootNode);
            constructClassPanel.setHasaTreeModel(treeModel);
            constructClassPanel.setVisibleHasaTree(true);
            treeModel.reload();

            currentProject.setUserIDCount(currentProject.getUserIDCount());
            rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_URI), ConceptTreePanel.ISA_TREE);
            treeModel = new DefaultTreeModel(rootNode);
            constructPropertyPanel.setConceptTreeModel(treeModel);
            constructPropertyPanel.setVisibleIsaTree(true);
            constructPropertyPanel.checkMultipleInheritance(treeModel);
            ConceptTreeMaker.getInstance().conceptDriftManagement(treeModel);
            constructPropertyPanel.setConceptDriftManagementResult();
            treeModel.reload();
            
            rootNode = ConceptTreeMaker.getInstance().getPropertyTreeRoot(currentProject, model,
                    ResourceFactory.createResource(ConceptTreeMaker.DODDLE_PROPERTY_HASA_ROOT_URI), ConceptTreePanel.PROPERTY_HASA_TREE);
            treeModel = new DefaultTreeModel(rootNode);
            constructPropertyPanel.setHasaTreeModel(treeModel);
            constructPropertyPanel.setVisibleHasaTree(true);
            treeModel.reload();
            
            currentProject.setUserIDCount(currentProject.getUserIDCount() + 1);
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

    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(DODDLE.PROJECT_HOME);
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            DODDLEProject currentProject = DODDLE.getCurrentProject();
            if (conversionType.equals(OWL_ONTOLOGY)) {
                loadOWLOntology(currentProject, chooser.getSelectedFile());
                DODDLE.STATUS_BAR.setText("Load OWL Ontology Done");
            } else if (conversionType.equals(FREEMIND_ONTOLOGY)) {
                loadFreeMindOntology(currentProject, chooser.getSelectedFile());
                DODDLE.STATUS_BAR.setText("Load FreeMind Ontology Done");
            }
            currentProject.getConstructClassPanel().expandIsaTree();
            currentProject.getConstructClassPanel().expandHasaTree();
            currentProject.getConstructPropertyPanel().expandIsaTree();
        }
    }
}
