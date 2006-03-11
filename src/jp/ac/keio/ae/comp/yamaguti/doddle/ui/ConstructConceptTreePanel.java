package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

/*
 * 2005/03/01
 *  
 */

import java.awt.*;
import java.util.*;

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
    private ConceptDescriptionPanel conceptDescriptionPanel;

    private ConceptDriftManagementPanel controlPanel;
    private ConceptTreeMaker treeMaker = ConceptTreeMaker.getInstance();

    private DODDLEProject project;

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
        conceptTreePanel = new ConceptTreePanel(Translator.getString("ClassTreePanel.ConceptTree"), undefinedWordListPanel, p);
        controlPanel = new ConceptDriftManagementPanel(conceptTreePanel.getConceptTree());

        conceptDescriptionPanel = new ConceptDescriptionPanel(conceptTreePanel.getConceptTree(),
                new ConceptTreeCellRenderer(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE));
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(conceptDescriptionPanel, BorderLayout.CENTER);
        eastPanel.add(controlPanel, BorderLayout.SOUTH);

        JSplitPane westPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, undefinedWordListPanel, conceptTreePanel);
        westPane.setOneTouchExpandable(true);
        westPane.setDividerSize(10);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westPane, eastPanel);
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
        controlPanel.setDefaultValue();
        conceptTreePanel.checkAllMultipleInheritanceNode(model);
        return model;
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