package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author shigeta
 * 
 * 2004-12-06 modified by takeshi morita
 * 
 */
public class ConceptDefinitionPanel extends JPanel implements ListSelectionListener {

    private Map<String, Set<Concept>> wordCorrespondConceptSetMap;
    private Map<String, Concept> complexWordConceptMap;
    private Map<String, Set<Concept>> wordConceptSetMap;

    public String corpusString;
    public List<String> inputWordList;

    private JList inputConceptJList;
    private ConceptDefinitionResultPanel resultPanel;
    private ConceptDefinitionAlgorithmPanel algorithmPanel;
    private ConceptDefinitionResultPanel.ConceptDefinitionPanel conceptDefinitionPanel;

    private DODDLEProject doddleProject;
    private DocumentSelectionPanel docSelectionPanel;
    private DisambiguationPanel disambiguationPanel;

    public void setInputConceptJList() {
        inputWordList = getInputWordList();
        inputConceptJList.removeAll();
        DefaultListModel listModel = new DefaultListModel();
        for (String iw : inputWordList) {
            listModel.addElement(iw);
        }
        inputConceptJList.setModel(listModel);
    }

    public Concept getConcept(String word) {
        Concept c = null;
        if (complexWordConceptMap.get(word) != null) {
            c = complexWordConceptMap.get(word);
            // System.out.println("cid: " + id);
        } else if (wordCorrespondConceptSetMap.get(word) != null) {
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(word);
            c = (Concept) correspondConceptSet.toArray()[0];
            // System.out.println("id: " + id);
        } else {
            Set<Concept> wordConceptSet = wordConceptSetMap.get(word);
            if (wordConceptSet != null) {
                c = (Concept) wordConceptSet.toArray()[0];
            }
        }
        if (c == null) { return null; }
        Concept concept = doddleProject.getConcept(c.getURI());
        if (concept != null) { return concept; }
        if (c.getNameSpace().equals(DODDLE.EDR_URI)) {
            return EDRDic.getEDRConcept(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLE.EDRT_URI)) {
            return EDRDic.getEDRTConcept(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLE.WN_URI)) { return WordNetDic.getWNConcept(c.getLocalName()); }
        return EDRDic.getEDRConcept(c.getLocalName());
    }

    private Resource getResource(Concept c, Model ontology) {
        return ontology.getResource(c.getURI());
    }

    public Model addConceptDefinition(Model ontology) {
        for (int i = 0; i < resultPanel.getRelationCount(); i++) {
            Object[] relation = resultPanel.getRelation(i);
            String domainWord = (String) relation[1];
            String rangeWord = (String) relation[3];
            Concept property = (Concept) relation[2];
            Concept domainConcept = getConcept(domainWord);
            Concept rangeConcept = getConcept(rangeWord);

            // System.out.println("r: "+property+"d: "+domainConcept + "r:
            // "+rangeConcept);

            if (property.getLocalName().equals("DID0")) { // agent
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.domain, getResource(rangeConcept, ontology));
            } else if (property.getLocalName().equals("DID1")) {// object
                ontology.add(getResource(domainConcept, ontology), RDF.type, OWL.ObjectProperty);
                ontology.add(getResource(domainConcept, ontology), RDFS.range, getResource(rangeConcept, ontology));
            } else {
                ontology.add(getResource(property, ontology), RDFS.domain, getResource(domainConcept, ontology));
                ontology.add(getResource(property, ontology), RDFS.range, getResource(rangeConcept, ontology));
            }
        }
        return ontology;
    }

    public ConceptDefinitionPanel(DODDLEProject project) {
        doddleProject = project;
        docSelectionPanel = project.getDocumentSelectionPanel();
        disambiguationPanel = project.getDisambiguationPanel();

        inputConceptJList = new JList(new DefaultListModel());
        inputConceptJList.addListSelectionListener(this);

        algorithmPanel = new ConceptDefinitionAlgorithmPanel(inputConceptJList, doddleProject);
        resultPanel = new ConceptDefinitionResultPanel(inputConceptJList, algorithmPanel, doddleProject);
        conceptDefinitionPanel = resultPanel.getDefinePanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, algorithmPanel, resultPanel);
        splitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        splitPane.setOneTouchExpandable(true);
        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);
        setTableAction();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (inputConceptJList.getSelectedValue() != null) {
            String selectedInputConcept = inputConceptJList.getSelectedValue().toString();
            resultPanel.calcWSandARValue(selectedInputConcept);
        }
    }

    public void loadConceptDefinition(Set acceptedSet, Set wrongSet) {
        resultPanel.loadConceptDefinition(acceptedSet, wrongSet);
    }

    private void setTableAction() {
        resultPanel.getWordSpaceSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    // System.out.println(lsm.getMinSelectionIndex());
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getWSTableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                }
            }
        });

        resultPanel.getAprioriSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getARTableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                    // System.out.println("-----" + selectedRow);
                }
            }
        });

        resultPanel.getWASelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    // String c1 = comboBox.getSelectedItem().toString();
                    String c1 = inputConceptJList.getSelectedValue().toString();
                    String c2 = resultPanel.getWATableRowConceptName(selectedRow);
                    conceptDefinitionPanel.setCText(c1, c2);
                    // System.out.println("-----" + selectedRow);
                }
            }
        });

    }

    public int getWSResultTableSelectedRow() {
        return resultPanel.getWSTableSelectedRow();
    }

    public int getAprioriResultTableSelectedRow() {
        return resultPanel.getARTableSelectedRow();
    }

    public int getWAResultTableSelectedRow() {
        return resultPanel.getWATableSelectedRow();
    }

    public DefaultTableModel getWSResultTableModel() {
        return resultPanel.getWResultTableModel();
    }

    public DefaultTableModel getAprioriResultTableModel() {
        return resultPanel.getAResultTableModel();
    }

    public DefaultTableModel getWAResultTableModel() {
        return resultPanel.getWAResultTableModel();
    }

    public void saveWordSpaceResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.WORDSPACE);
    }

    public void saveAprioriResult(File dir) {
        algorithmPanel.saveResult(dir, ConceptDefinitionAlgorithmPanel.APRIORI);
    }

    public void saveConeptDefinitionParameters(File file) {
        algorithmPanel.saveConceptDefinitionParameters(file);
    }

    public void saveConceptDefinition(File file) {
        resultPanel.saveConceptDefinition(file);
    }

    public void saveWrongPairSet(File file) {
        resultPanel.saveWrongPairSet(file);
    }

    public void setInputConceptSet() {
        algorithmPanel.setInputConcept();
    }

    public void setInputDocList() {
        resultPanel.setInputDocList();
    }

    public ConceptPair getPair(String str, List list) {
        for (int i = 0; i < list.size(); i++) {
            if (((ConceptPair) list.get(i)).getCombinationToString().equals(str)) { return (ConceptPair) list.get(i); }
        }
        return null;
    }

    public boolean contains(List list, ConceptPair pair) {
        for (int i = 0; i < list.size(); i++) {
            if (pair.isSameCombination((ConceptPair) list.get(i))) { return true; }
        }
        return false;
    }

    public List makeValidList(List list) {
        List returnList = new ArrayList();
        List resultA = (ArrayList) list.get(0);
        boolean flag = false;
        for (int j = 0; j < resultA.size(); j++) {
            ConceptPair pair = (ConceptPair) resultA.get(j);
            for (int i = 1; i < list.size(); i++) {
                List resultB = (List) list.get(i);
                flag = contains(resultB, pair);
            }
            if (flag) {
                returnList.add(pair.getCombinationToString());
            }
        }
        return returnList;
    }

    public ConceptPair getSameCombination(ConceptPair pair, List list) {
        for (int i = 0; i < list.size(); i++) {
            ConceptPair item = (ConceptPair) list.get(i);
            if (item.isSameCombination(pair)) { return item; }
        }
        return null;
    }

    public Set<Document> getDocSet() {
        return docSelectionPanel.getDocSet();
    }

    private boolean isVerbConcept(Concept c) {
        if (c.getLocalName().indexOf("UID") != -1) {
            if (doddleProject.getConstructPropertyPanel().isConceptContains(c)) { return true; }
            return false;
        }
        Set<List<Concept>> pathSet = null;
        if (c.getNameSpace().equals(DODDLE.EDR_URI)) {
            pathSet = EDRTree.getEDRTree().getPathToRootSet(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLE.EDRT_URI)) {
            pathSet = EDRTree.getEDRTTree().getPathToRootSet(c.getLocalName());
        } else if (c.getNameSpace().equals(DODDLE.WN_URI)) {
            pathSet = WordNetDic.getPathToRootSet(new Long(c.getLocalName()));
        }
        for (List<Concept> path : pathSet) {
            if (path.size() == 1) { return false; }
            Concept upperConcept = path.get(1); // 事象概念の下位に移動と行為があるため，１とする
            // 移動または行為の下位概念の場合は，動詞と見なす．
            if (upperConcept.getLocalName().equals("ID30f83e") || upperConcept.getLocalName().equals("ID30f801")) { return true; }
        }
        return false;
    }

    public List<String> getInputWordList() {
        List<String> inputWordList = new ArrayList<String>();
        wordCorrespondConceptSetMap = disambiguationPanel.getWordCorrespondConceptSetMap();
        if (wordCorrespondConceptSetMap != null) {
            wordConceptSetMap = disambiguationPanel.getWordConceptSetMap();
            complexWordConceptMap = doddleProject.getConstructClassPanel().getComplexWordConceptMap();
            Set<InputWordModel> inputWordModelSet = disambiguationPanel.getInputWordModelSet();
            for (InputWordModel iwModel : inputWordModelSet) {
                String word = iwModel.getWord();
                Concept c = getConcept(word);
                // 複合語の場合，概念階層構築後でなければ，Conceptはnullとなる
                if (c != null && !isVerbConcept(c)) {
                    inputWordList.add(word);
                }
            }
            Collections.sort(inputWordList);
        }
        return inputWordList;
    }

    public Set<String> getComplexWordSet() {
        Set<String> complexWordSet = new HashSet<String>();
        Set<String> wordSet = wordConceptSetMap.keySet();
        for (String w : wordSet) {
            if (w.indexOf(" ") != -1) {
                complexWordSet.add(w);
            }
        }
        Set<String> partialMatchedWordSet = complexWordConceptMap.keySet();
        if (partialMatchedWordSet != null) {
            complexWordSet.addAll(partialMatchedWordSet);
        }
        return complexWordSet;
    }
}