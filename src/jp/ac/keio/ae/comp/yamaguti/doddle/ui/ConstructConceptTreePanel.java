package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

/*
 * 2005/03/01
 *  
 */

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 * 
 */
public class ConstructConceptTreePanel extends JPanel implements ComplexConceptTreeInterface {

    private UndefinedWordListPanel undefinedWordListPanel;
    private ConceptTreePanel conceptTreePanel;
    private ConceptInformationPanel conceptInfoPanel;

    private ConceptDriftManagementPanel conceptDriftManagementPanel;
    private ConceptTreeMaker treeMaker = ConceptTreeMaker.getInstance();

    private DODDLEProject project;

    public void loadTrimmedResultAnalysis(DODDLEProject project, File file) {
        if (!file.exists()) { return; }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            String line = "";
            Map<String, List<List<Concept>>> idTrimmedConceptListMap = new HashMap<String, List<List<Concept>>>();
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split("\\|");
                String[] concepts = lines[0].split(",");
                List<List<Concept>> trimmedConceptList = new ArrayList<List<Concept>>();
                for (int i = 1; i < lines.length; i++) {
                    String[] conceptStrs = lines[i].split(",");
                    List<Concept> list = new ArrayList<Concept>();
                    for (int j = 0; j < conceptStrs.length; j++) {
                        list.add(DODDLEDic.getConcept(conceptStrs[j]));
                    }
                    trimmedConceptList.add(list);
                }
                idTrimmedConceptListMap.put(concepts[0] + concepts[1], trimmedConceptList);
            }
            TreeModel treeModel = conceptTreePanel.getConceptTree().getModel();
            ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
            loadTrimmedResultAnalysis(rootNode, idTrimmedConceptListMap);
            conceptDriftManagementPanel.setTRADefaultValue();
            reader.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void loadTrimmedResultAnalysis(ConceptTreeNode node,
            Map<String, List<List<Concept>>> idTrimmedConceptListMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            String id = childNode.getConcept().getIdentity() + node.getConcept().getIdentity();
            List<List<Concept>> trimmedConceptList = idTrimmedConceptListMap.get(id);
            if (trimmedConceptList != null && 0 < trimmedConceptList.size()) {
                childNode.setTrimmedConceptList(trimmedConceptList);
                conceptDriftManagementPanel.addTRANode(childNode);
            }
            loadTrimmedResultAnalysis(childNode, idTrimmedConceptListMap);
        }
    }

    public ConceptDriftManagementPanel getConceptDriftManagementPanel() {
        return conceptDriftManagementPanel;
    }

    public void setUndefinedWordListModel(ListModel model) {
        undefinedWordListPanel.setUndefinedWordListModel(model);
        repaint();
    }

    public Map getIDTypicalWordMap() {
        return conceptTreePanel.getConceptTypicalWordMap();
    }

    public void loadIDTypicalWord(Map idTypicalWordMap) {
        conceptTreePanel.loadConceptTypicalWord(idTypicalWordMap);
    }

    public TreeModel getConceptTreeModel() {
        return conceptTreePanel.getConceptTree().getModel();
    }

    public JTree getConceptTree() {
        return conceptTreePanel.getConceptTree();
    }

    public ConstructConceptTreePanel(DODDLEProject p) {
        project = p;
        undefinedWordListPanel = new UndefinedWordListPanel();
        conceptTreePanel = new ConceptTreePanel(Translator.getString("ClassTreePanel.ConceptTree"),
                undefinedWordListPanel, p);
        conceptDriftManagementPanel = new ConceptDriftManagementPanel(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
                conceptTreePanel.getConceptTree(), project);

        conceptInfoPanel = new ConceptInformationPanel(conceptTreePanel.getConceptTree(), new ConceptTreeCellRenderer(
                ConceptTreeCellRenderer.NOUN_CONCEPT_TREE));
        JSplitPane eastSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conceptInfoPanel,
                conceptDriftManagementPanel);
        eastSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        eastSplitPane.setOneTouchExpandable(true);

        JSplitPane westPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, undefinedWordListPanel, conceptTreePanel);
        westPane.setOneTouchExpandable(true);
        westPane.setDividerSize(10);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPane, eastSplitPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        this.setLayout(new BorderLayout());
        this.add(splitPane, BorderLayout.CENTER);
    }

    public TreeModel getDefaultConceptTreeModel(Set pathSet) {
        return treeMaker.getDefaultConceptTreeModel(pathSet, project);
    }

    public void addJPWord(String identity, String word) {
        conceptTreePanel.addJPWord(identity, word);
    }

    public void addSubConcept(String identity, String word) {
        conceptTreePanel.addSubConcept(identity, word);
    }

    private double addedAbstractComplexConceptCnt;
    private double averageAbstracComplexConceptGroupSiblingConceptCnt;

    public double getAddedAbstractComplexConceptCnt() {
        return addedAbstractComplexConceptCnt;
    }

    public double getAverageAbstracComplexConceptGroupSiblingConceptCnt() {
        return averageAbstracComplexConceptGroupSiblingConceptCnt;
    }

    public void addComplexWordConcept(Map matchedWordIDMap, TreeNode rootNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTree().getModel();
        ConceptTreeNode conceptTreeRootNode = (ConceptTreeNode) model.getRoot();
        conceptTreePanel.addComplexWordConcept(matchedWordIDMap, rootNode, conceptTreeRootNode);
        addedAbstractComplexConceptCnt = conceptTreePanel.getAbstractNodeCnt();
        DODDLE.getLogger().log(Level.INFO, "追加した抽象中間クラス数: " + addedAbstractComplexConceptCnt);
        if (addedAbstractComplexConceptCnt == 0) {
            averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        } else {
            averageAbstracComplexConceptGroupSiblingConceptCnt = conceptTreePanel
                    .getTotalAbstractNodeGroupSiblingNodeCnt()
                    / addedAbstractComplexConceptCnt;
        }
        DODDLE.getLogger().log(Level.INFO,
                "抽象中間クラスの平均兄弟クラスグループ化数: " + averageAbstracComplexConceptGroupSiblingConceptCnt);
    }

    public void init() {
        addedAbstractComplexConceptCnt = 0;
        averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        ConceptTreeMaker.getInstance().init();
        conceptTreePanel.getConceptTree().setModel(new DefaultTreeModel(null));
    }

    private int beforeTrimmingConceptNum;

    public int getBeforeTrimmingConceptNum() {
        return beforeTrimmingConceptNum;
    }

    private int addedSINNum;

    public int getAddedSINNum() {
        return addedSINNum;
    }

    private int trimmedConceptNum;

    public int getTrimmedConceptNum() {
        return trimmedConceptNum;
    }

    public int getAfterTrimmingConceptNum() {
        return beforeTrimmingConceptNum - trimmedConceptNum;
    }

    public TreeModel getTreeModel(Set<Concept> conceptSet) {
        Set pathSet = treeMaker.getPathList(conceptSet);
        trimmedConceptNum = 0;
        TreeModel model = treeMaker.getTrimmedTreeModel(pathSet, project);
        trimmedConceptNum = treeMaker.getTrimmedConceptNum();
        beforeTrimmingConceptNum = treeMaker.getBeforeTrimmingConceptNum();
        addedSINNum = beforeTrimmingConceptNum - conceptSet.size();
        DODDLE.getLogger().log(Level.INFO, "クラス階層構築における追加SIN数: " + addedSINNum);
        DODDLE.getLogger().log(Level.INFO, "剪定前クラス数: " + beforeTrimmingConceptNum);
        DODDLE.getLogger().log(Level.INFO, "剪定クラス数: " + trimmedConceptNum);
        DODDLE.getLogger().log(Level.INFO, "剪定後クラス数: " + getAfterTrimmingConceptNum());
        setConceptDriftManagementResult();
        conceptTreePanel.checkAllMultipleInheritanceNode(model);
        return model;
    }

    public void setConceptDriftManagementResult() {
        conceptDriftManagementPanel.setConceptDriftManagementResult();
    }

    public void setTreeModel(TreeModel model) {
        conceptTreePanel.getConceptTree().setModel(model);
    }

    public void checkMultipleInheritance(TreeModel model) {
        conceptTreePanel.checkAllMultipleInheritanceNode(model);
    }

    public int getAllConceptCnt() {
        return Utils.getAllConcept(conceptTreePanel.getConceptTree().getModel()).size();
    }

    public double getChildCntAverage() {
        return Utils.getChildCntAverage(conceptTreePanel.getConceptTree().getModel());
    }

    public Set getAllConceptID() {
        return conceptTreePanel.getAllConceptID();
    }

    public ConceptTreeNode getTreeModelRoot() {
        JTree conceptTree = conceptTreePanel.getConceptTree();
        if (conceptTree.getModel().getRoot() instanceof ConceptTreeNode) { return (ConceptTreeNode) conceptTree
                .getModel().getRoot(); }
        return null;
    }

    public void expandTree() {
        JTree conceptTree = conceptTreePanel.getConceptTree();
        for (int i = 0; i < conceptTree.getRowCount(); i++) {
            conceptTree.expandPath(conceptTree.getPathForRow(i));
        }
    }

    public Map<String, Concept> getComplexWordConceptMap() {
        return conceptTreePanel.getComplexWordConceptMap();
    }

    public Set getSupConceptSet(String id) {
        return conceptTreePanel.getSupConceptSet(id);
    }

    public void setVisibleConceptTree(boolean isVisible) {
        conceptTreePanel.getConceptTree().setVisible(isVisible);
    }
}