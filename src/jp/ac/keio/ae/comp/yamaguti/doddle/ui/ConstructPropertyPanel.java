package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

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

/*
 * @(#)  2005/07/17
 *
 */

/**
 * @author takeshi morita
 */
public class ConstructPropertyPanel extends ConstructConceptTreePanel {

    private int afterTrimmingConceptCnt;
    private EDRConceptDefinitionPanel edrConceptDefinitionPanel;

    public int getAfterTrimmingConceptCnt() {
        return afterTrimmingConceptCnt;
    }

    public boolean isConceptContains(Concept c) {
        return conceptTreePanel.isConceptContains(c);
    }

    public ConstructPropertyPanel(DODDLEProject p) {
        project = p;
        undefinedWordListPanel = new UndefinedWordListPanel();
        conceptTreePanel = new ConceptTreePanel(Translator.getString("PropertyTreePanel.ConceptTree"),
                undefinedWordListPanel, project);
        edrConceptDefinitionPanel = new EDRConceptDefinitionPanel(p);
        conceptDriftManagementPanel = new ConceptDriftManagementPanel(ConceptTreeCellRenderer.VERB_CONCEPT_TREE,
                conceptTreePanel.getConceptTree(), project);
        JTabbedPane tab = new JTabbedPane();
        tab.add(edrConceptDefinitionPanel, Translator.getString("PropertyTreePanel.ConceptDefinition"));
        tab.add(conceptDriftManagementPanel, Translator.getString("ConceptTreePanel.ConceptDriftManagement"));

        conceptInfoPanel = new ConceptInformationPanel(conceptTreePanel.getConceptTree(), new ConceptTreeCellRenderer(
                ConceptTreeCellRenderer.VERB_CONCEPT_TREE), edrConceptDefinitionPanel, conceptDriftManagementPanel);
        JSplitPane eastSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, conceptInfoPanel, tab);
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

    public void addComplexWordConcept(Map matchedWordIDMap, Map abstractNodeLabelMap, TreeNode rootNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTreePanel.getConceptTree().getModel();
        ConceptTreeNode conceptTreeRootNode = (ConceptTreeNode) model.getRoot();
        conceptTreePanel.addComplexWordConcept(matchedWordIDMap, rootNode, conceptTreeRootNode, abstractNodeLabelMap);
        DODDLE.getLogger().log(Level.INFO, "追加した抽象中間ノード数: " + conceptTreePanel.getAbstractNodeCnt());
        addedAbstractComplexConceptCnt = conceptTreePanel.getAbstractConceptCnt();
        DODDLE.getLogger().log(Level.DEBUG, "追加した抽象中間プロパティ数: " + addedAbstractComplexConceptCnt);
        if (addedAbstractComplexConceptCnt == 0) {
            averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        } else {
            averageAbstracComplexConceptGroupSiblingConceptCnt = conceptTreePanel
                    .getTotalAbstractNodeGroupSiblingNodeCnt()
                    / addedAbstractComplexConceptCnt;
        }
        DODDLE.getLogger().log(Level.INFO,
                "抽象中間プロパティの平均兄弟プロパティグループ化数: " + averageAbstracComplexConceptGroupSiblingConceptCnt);
    }

    public void init() {
        addedAbstractComplexConceptCnt = 0;
        averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        treeMaker.init();
        conceptTreePanel.getConceptTree().setModel(new DefaultTreeModel(null));
        edrConceptDefinitionPanel.init();
    }

    private void removeNounConceptPath(Set<List<Concept>> pathSet) {
        Set<List<Concept>> removeSet = new HashSet<List<Concept>>();
        for (List<Concept> path : pathSet) {
            Concept c = path.get(2);
            if (!(c.getId().equals("30f801") || c.getId().equals("30f83e"))) {
                removeSet.add(path);
                // System.out.println("path: " + path);
            }
        }
        pathSet.removeAll(removeSet);
    }

    public TreeModel getTreeModel(Set nounIDSet, Set<Concept> verbConceptSet) {
        Set<List<Concept>> pathSet = treeMaker.getPathList(verbConceptSet);
        removeNounConceptPath(pathSet); // 移動と行為以外のパスを削除する
        trimmedConceptNum = 0;
        TreeModel propertyTreeModel = treeMaker.getTrimmedTreeModel(pathSet, project);
        trimmedConceptNum = treeMaker.getTrimmedConceptNum();
        beforeTrimmingConceptNum = treeMaker.getBeforeTrimmingConceptNum();
        if (beforeTrimmingConceptNum != 1) {
            // 名詞的概念を削除する分．
            // beforeTrimmingConceptNumが１の場合は，名詞的概念は削除されない．
            trimmedConceptNum += 1;
        }
        addedSINNum = beforeTrimmingConceptNum - verbConceptSet.size();
        DODDLE.getLogger().log(Level.INFO, "プロパティ階層構築における追加SIN数: " + addedSINNum);
        DODDLE.getLogger().log(Level.INFO, "剪定前プロパティ数: " + beforeTrimmingConceptNum);
        DODDLE.getLogger().log(Level.INFO, "剪定プロパティ数: " + trimmedConceptNum);
        ConceptTreeNode rootNode = (ConceptTreeNode) propertyTreeModel.getRoot();
        VerbConcept propRoot = new VerbConcept(ConceptTreeMaker.DODDLE_PROPERTY_ROOT_ID, "動詞的概念");
        propRoot.addEnWord("Root Property");
        propRoot.setPrefix(DODDLE.BASE_PREFIX);
        rootNode.setConcept(propRoot);
        rootNode.setIsUserConcept(true);
        setRegion(propertyTreeModel, nounIDSet);
        setConceptDriftManagementResult();
        conceptTreePanel.checkAllMultipleInheritanceNode(propertyTreeModel);
        return propertyTreeModel;
    }

    /**
     * 
     * 名詞的な概念を削除
     * 
     * @param model
     * @param rootNode
     */
    public void removeNounNode() {
        DefaultTreeModel treeModel = (DefaultTreeModel) conceptTreePanel.getConceptTree().getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        ConceptTreeNode eventNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) rootNode.getChildAt(i);
            if (childNode.getConcept().getId().equals("30f7e4")) { // 事象
                eventNode = childNode;
                // 事象ノードの子ノードは，すべて動詞的概念のルート概念の下位概念に移動
                for (int j = 0; j < eventNode.getChildCount(); j++) {
                    ConceptTreeNode eventChildNode = (ConceptTreeNode) eventNode.getChildAt(j);
                    treeModel.removeNodeFromParent(eventChildNode);
                    treeModel.insertNodeInto(eventChildNode, rootNode, 0);
                }
            }
        }
        if (eventNode != null) {
            treeModel.removeNodeFromParent(eventNode); // これより前で消すと事象の子要素がすべて削除されてしまう
        }
        DODDLE.getLogger().log(Level.INFO, "動詞的概念階層から名詞的概念階層を削除");
        afterTrimmingConceptCnt = Utils.getAllConcept(treeModel).size();
        DODDLE.getLogger().log(Level.INFO, "剪定後プロパティ数: " + afterTrimmingConceptCnt);
    }

    /**
     * 
     * クラス階層に存在しない概念をdomain, rangeから削除
     * 
     * @param regionSet
     * @param nounIDSet
     */
    private void removeSet(Set regionSet, Set nounIDSet) {
        Set removeSet = new HashSet();
        for (Iterator i = regionSet.iterator(); i.hasNext();) {
            String id = (String) i.next();
            if (!nounIDSet.contains(id)) {
                removeSet.add(id);
            }
        }
        regionSet.removeAll(removeSet);
    }

    private Set abstractRegion(Set regionSet, Set nounIDSet) {
        Set abstractRegionSet = new HashSet();
        for (Iterator i = regionSet.iterator(); i.hasNext();) {
            String id = (String) i.next();
            if (nounIDSet.contains(id)) {
                abstractRegionSet.add(id);
            } else {
                String subID = EDRTree.getEDRTree().getSubID(id, nounIDSet);
                if (subID != null) {
                    // System.out.println(InputModule.getEDRConcept(id) + " → "
                    // + InputModule.getEDRConcept(subID));
                    abstractRegionSet.add(subID);
                }
            }
        }
        return abstractRegionSet;
    }

    private void setRegion(TreeNode node, ConceptDefinition conceptDefinition, Set nounIDSet) {
        if (node.getChildCount() == 0) { return; }
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept() instanceof VerbConcept) {
                VerbConcept c = (VerbConcept) childNode.getConcept();

                // System.out.println("concept: " + childNode.getConcept() +
                // "trimmed concept: "
                // + c.getTrimmedConceptSet());
                Set<String> domainSet = conceptDefinition.getIDSet("agent", c.getId(), childNode
                        .getTrimmedConceptList());
                // System.out.println("verb concept: " + c);
                // System.out.println("domain: " + domainSet);
                domainSet = abstractRegion(domainSet, nounIDSet);
                // System.out.println("abstract domain: " + domainSet);
                Set<String> rangeSet = conceptDefinition.getIDSet("object", c.getId(), childNode
                        .getTrimmedConceptList());
                // System.out.println("range: " + rangeSet);
                rangeSet = abstractRegion(rangeSet, nounIDSet);
                // System.out.println("abstract range: " + rangeSet);

                c.addAllDomain(domainSet);
                c.addAllRange(rangeSet);
                childNode.setConcept(c);
            }
            setRegion(childNode, conceptDefinition, nounIDSet);
        }
    }

    private void setRegion(TreeModel propertyTreeModel, Set nounIDSet) {
        TreeNode rootNode = (TreeNode) propertyTreeModel.getRoot();
        setRegion(rootNode, ConceptDefinition.getInstance(), nounIDSet);
    }
}
