/*
 * @(#)  2006/04/07
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;
import java.util.*;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 */
public class ConstructTreeAction extends AbstractAction {

    private boolean isNounAndVerbTree;
    private Map<String, Concept> wordConceptMap; // 入力単語と適切に対応するIDのマッピング
    private Map<DefaultMutableTreeNode, String> abstractNodeLabelMap;
    private Map<InputWordModel, ConstructTreeOption> complexConstructTreeOptionMap;
    private DisambiguationPanel disambiguationPanel;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;

    private DefaultListModel undefinedWordListModel;
    private UndefinedWordListPanel undefinedWordListPanel;

    private DODDLEProject project;

    public ConstructTreeAction(String title, boolean t, DODDLEProject p) {
        super(title);
        isNounAndVerbTree = t;
        project = p;
        disambiguationPanel = p.getDisambiguationPanel();
        constructClassPanel = p.getConstructClassPanel();
        constructPropertyPanel = p.getConstructPropertyPanel();
        wordConceptMap = disambiguationPanel.getWordConceptMap();
        undefinedWordListPanel = disambiguationPanel.getUndefinedWordListPanel();
        complexConstructTreeOptionMap = disambiguationPanel.getComplexConstructTreeOptionMap();
    }

    public void actionPerformed(ActionEvent e) {
        if (isNounAndVerbTree) {
            OptionDialog.setNounAndVerbConceptHiearchy();
        } else {
            OptionDialog.setNounConceptHiearchy();
        }
        constructTree();
    }

    public void constructTree() {

        new Thread() {

            private boolean isExistNode(TreeNode node, TreeNode childNode, String word, Concept ic) {
                return childNode.toString().equals(word)
                        || (node.getParent() == null && childNode.toString().equals(ic.getIdentity()));
            }

            private void addComplexWordNode(int len, InputWordModel iwModel, TreeNode node) {
                if (len == iwModel.getComplexWordLength()) { return; }
                List wordList = iwModel.getWordList();
                StringBuffer buf = new StringBuffer();
                for (int i = wordList.size() - len - 1; i < wordList.size(); i++) {
                    buf.append(wordList.get(i));
                }
                String word = buf.toString();
                // wordの長さが照合単語以上の長さのときに複合語の階層化を行う
                if (iwModel.getMatchedWord().length() <= word.length()) {
                    Concept ic = wordConceptMap.get(iwModel.getWord());
                    for (int i = 0; i < node.getChildCount(); i++) {
                        TreeNode childNode = node.getChildAt(i);
                        if (isExistNode(node, childNode, word, ic)) {
                            addComplexWordNode(len + 1, iwModel, childNode);
                            return;
                        }
                    }
                    DefaultMutableTreeNode childNode = null;
                    if (word.equals(iwModel.getMatchedWord())) {
                        childNode = new DefaultMutableTreeNode(ic.getIdentity());
                    } else {
                        childNode = new DefaultMutableTreeNode(word);
                    }
                    ((DefaultMutableTreeNode) node).add(childNode);
                    addComplexWordNode(len + 1, iwModel, childNode);
                } else {
                    // System.out.println("照合単語: " +
                    // iwModel.getMatchedWord() + " => 短すぎる単語: " + word);
                    addComplexWordNode(len + 1, iwModel, node);
                }
            }

            private boolean hasONEComplexWordChild(TreeNode node) {
                int complexWordChildNum = 0;
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    if (childNode.getUserObject() instanceof String) {
                        complexWordChildNum += 1;
                        if (1 < complexWordChildNum) { return false; }
                    }
                }
                return complexWordChildNum == 1;
            }

            private void trimComplexWordNode(DefaultMutableTreeNode node) {
                Set<String> sameNodeSet = new HashSet<String>();
                Set<DefaultMutableTreeNode> addNodeSet = new HashSet<DefaultMutableTreeNode>();
                Set<DefaultMutableTreeNode> removeNodeSet = new HashSet<DefaultMutableTreeNode>();
                Set<String> reconstructNodeSet = new HashSet<String>();
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    if (trimNotInputComplexWord(node, childNode)) {
                        // trimmingされた場合，最初から処理をしなおす
                        i = -1; // 0からはじめるには，-1にする必要あり
                        continue;
                    }
                    extractMoveComplexWordNodeSet(sameNodeSet, addNodeSet, removeNodeSet, childNode);
                    extractReconstructedNodeSet(reconstructNodeSet, childNode);
                }
                moveComplexWordNodeSet(node, addNodeSet, removeNodeSet, reconstructNodeSet);
                // 兄弟概念をすべて処理した後に，子ノードの処理に移る
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    trimComplexWordNode(childNode);
                }
            }

            /**
             * @param node
             * @param addNodeSet
             * @param removeNodeSet
             * @param reconstructNodeSet
             */
            private void moveComplexWordNodeSet(DefaultMutableTreeNode node, Set<DefaultMutableTreeNode> addNodeSet,
                    Set<DefaultMutableTreeNode> removeNodeSet, Set<String> reconstructNodeSet) {
                // 子ノードを一つしかもたない抽象ノードの子ノードをnodeに追加
                for (Iterator i = addNodeSet.iterator(); i.hasNext();) {
                    DefaultMutableTreeNode addNode = (DefaultMutableTreeNode) i.next();
                    node.add(addNode);
                }
                // 子ノードを一つしかもたない抽象ノードを削除
                for (Iterator i = removeNodeSet.iterator(); i.hasNext();) {
                    DefaultMutableTreeNode removeNode = (DefaultMutableTreeNode) i.next();
                    node.remove(removeNode);
                }
                // 同一レベルに再構成(抽象ノードに追加)されたノードが含まれている場合には削除
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    if (reconstructNodeSet.contains(childNode.toString())) {
                        node.remove(childNode);
                        i = -1; // 0からはじめるには，-1にする必要あり
                        continue;
                    }
                }
            }

            /**
             * @param reconstructNodeSet
             * @param childNode
             */
            private void extractReconstructedNodeSet(Set<String> reconstructNodeSet, DefaultMutableTreeNode childNode) {
                // ２つ以上子ノード(複合語)を持つ中間ノードに追加されたノードをreconstructNodeSetに保存
                // 現状では，複合語以外のノードも含めて２つ以上でもよくなっているので，
                // 単純にchildcountで処理するのではなくて，複合語かどうかをチェックしてから処理する
                // ようにする．
                if (childNode.getUserObject() instanceof Concept && !hasONEComplexWordChild(childNode)) {
                    for (int j = 0; j < childNode.getChildCount(); j++) {
                        DefaultMutableTreeNode reconstructNode = (DefaultMutableTreeNode) childNode.getChildAt(j);
                        reconstructNodeSet.add(reconstructNode.toString());
                    }
                }
            }

            /**
             * @param addNodeSet
             * @param removeNodeSet
             * @param childNode
             */
            private void extractMoveComplexWordNodeSet(Set<String> sameNodeSet, Set<DefaultMutableTreeNode> addNodeSet,
                    Set<DefaultMutableTreeNode> removeNodeSet, DefaultMutableTreeNode childNode) {
                if (childNode.getUserObject() instanceof Concept && hasONEComplexWordChild(childNode)) {
                    for (int i = 0; i < childNode.getChildCount(); i++) {
                        DefaultMutableTreeNode grandChildNode = (DefaultMutableTreeNode) childNode.getChildAt(i);
                        if (grandChildNode.getUserObject() instanceof String) {
                            removeNodeSet.add(childNode);
                            if (!sameNodeSet.contains(grandChildNode.toString())) {
                                DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(grandChildNode.toString());
                                // この場合移動すべきか，コピーすべきかを考える
                                // for (int j = 0; j <
                                // grandChildNode.getChildCount(); j++) {
                                // addNode.add((DefaultMutableTreeNode)
                                // grandChildNode.getChildAt(j));
                                // }
                                deepCloneTreeNode(grandChildNode, addNode);
                                addNodeSet.add(addNode);
                                sameNodeSet.add(grandChildNode.toString());
                            }
                        }
                    }
                }
            }

            /**
             * 
             * trimmingされたらtrueを返す
             * 
             * @param node
             * @param childNode
             */
            private boolean trimNotInputComplexWord(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode) {
                if (OptionDialog.isTrimNodeWithComplexWordConceptConstruction()
                        && childNode.getUserObject() instanceof String
                        && !complexWordSet.contains(childNode.toString())) {
                    DefaultMutableTreeNode[] grandChildNodeList = new DefaultMutableTreeNode[childNode.getChildCount()];
                    for (int i = 0; i < childNode.getChildCount(); i++) {
                        grandChildNodeList[i] = (DefaultMutableTreeNode) childNode.getChildAt(i);
                    }
                    for (int i = 0; i < grandChildNodeList.length; i++) {
                        // (注意)
                        // childNodeからgrandChildNodeを削除して，nodeに追加する
                        // 直接追加してしまうと，childNodeの全子要素を扱えないので，
                        // 配列に格納してから，nodeに追加している．
                        node.add(grandChildNodeList[i]);
                    }
                    node.remove(childNode);
                    return true;
                }
                return false;
            }

            private Set<String> complexWordSet; // 入力語彙に含まれない複合語を削除するさいに参照

            private boolean isInputConcept(Concept c, Set<Concept> conceptSet) {
                for (Concept ic : conceptSet) {
                    if (ic.getIdentity().equals(c.getIdentity())) { return true; }
                }
                return false;
            }

            private void setComplexConcept(ComplexConceptTreeInterface ccTreeInterface, Set<Concept> conceptSet) {
                complexWordSet = new HashSet<String>();
                Map<String, String> matchedWordIDMap = new HashMap<String, String>();
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
                for (Iterator i = complexConstructTreeOptionMap.keySet().iterator(); i.hasNext();) {
                    InputWordModel iwModel = (InputWordModel) i.next();
                    complexWordSet.add(iwModel.getWord());
                    complexWordSet.add(iwModel.getMatchedWord());
                    ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                    matchedWordIDMap.put(iwModel.getMatchedWord(), ctOption.getConcept().getId());
                    if (!isInputConcept(ctOption.getConcept(), conceptSet)) {
                        continue;
                    }
                    if (ctOption.getOption().equals("SAME")) {
                        ccTreeInterface.addJPWord(ctOption.getConcept().getIdentity(), iwModel.getWord());
                    } else if (ctOption.getOption().equals("SUB")) {
                        addComplexWordNode(0, iwModel, rootNode);
                    }
                }
                DODDLE.getLogger().log(Level.DEBUG, "複合語階層構築");
                if (OptionDialog.isAddAbstractInternalComplexWordConcept()) {
                    addAbstractTreeNode(rootNode);
                    DODDLE.getLogger().log(Level.DEBUG, "抽象中間概念を追加");
                }
                // printDebugTree(rootNode, "before trimming");
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                    trimComplexWordNode(childNode);
                }
                DODDLE.getLogger().log(Level.DEBUG, "複合語の剪定");
                // printDebugTree(rootNode, "add abstract node");
                complexWordSet.clear();
                ccTreeInterface.addComplexWordConcept(matchedWordIDMap, abstractNodeLabelMap, rootNode);
            }

            private int countNode(int cnt, TreeNode node) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    TreeNode childNode = node.getChildAt(i);
                    cnt += countNode(cnt, childNode);
                }
                return node.getChildCount();
            }

            /**
             * @param rootNode
             */
            private void addAbstractTreeNode(DefaultMutableTreeNode rootNode) {
                nodeRemoveNodeSetMap = new HashMap();
                abstractNodeLabelMap = new HashMap<DefaultMutableTreeNode, String>();
                tmpcnt = 0;
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    DODDLE.getLogger().log(Level.DEBUG,
                            rootNode.getChildAt(i) + ": " + (i + 1) + "/" + rootNode.getChildCount());
                    reconstructComplexTree(1, (DefaultMutableTreeNode) rootNode.getChildAt(i));
                    // 多重継承している場合もあるので，一度クローンを抽象ノードに挿入した後に，
                    // 親ノードから削除する．
                    for (Iterator j = nodeRemoveNodeSetMap.entrySet().iterator(); j.hasNext();) {
                        Entry entry = (Entry) j.next();
                        DefaultMutableTreeNode supNode = (DefaultMutableTreeNode) entry.getKey();
                        Set removeNodeSet = (Set) entry.getValue();
                        for (Iterator k = removeNodeSet.iterator(); k.hasNext();) {
                            supNode.remove((DefaultMutableTreeNode) k.next());
                        }
                    }
                    nodeRemoveNodeSetMap.clear();
                }
            }

            /**
             * @param rootNode
             */
            private void printDebugTree(DefaultMutableTreeNode rootNode, String title) {
                JFrame frame = new JFrame();
                frame.setTitle(title);
                JTree debugTree = new JTree(new DefaultTreeModel(rootNode));
                frame.getContentPane().add(new JScrollPane(debugTree));
                frame.setSize(800, 600);
                frame.setVisible(true);
            }

            private Map nodeRemoveNodeSetMap;
            private int tmpcnt;

            /**
             * 接頭語で複合語階層を再構成する
             * 
             * d: デバッグ用．再帰の深さをはかるため．
             */
            private void reconstructComplexTree(int d, DefaultMutableTreeNode node) {
                if (node.getChildCount() == 0) { return; }
                // System.out.println(node + ": " + d);
                Map abstractConceptTreeNodeMap = new HashMap();
                // nodeの最後に抽象ノードが追加されていくが，
                // node.getChildCountも増加するためiの値を変更しなくても問題はない
                // 抽象ノードのみが最後に追加されていくため，それ以外のノードに関してはすべて処理される
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    // 抽象ノードを上位に持つ複合語は処理しない
                    if (node.getUserObject() instanceof String && childNode.getUserObject() instanceof String) {
                        String complexWord = childNode.toString();
                        // InputWordModel iwModel =
                        // disambiguationPanel.getInputModule().makeInputWordModel(complexWord);
                        InputWordModel iwModel = disambiguationPanel.makeInputWordModel(complexWord);
                        String word = iwModel.getTopWord();
                        Concept c = wordConceptMap.get(word);
                        tmpcnt++;
                        if (c != null && 1 < iwModel.getWordList().size()) {
                            Set supConceptSet = getSupConceptSet(c.getId());
                            for (Iterator j = supConceptSet.iterator(); j.hasNext();) {
                                Concept supConcept = (Concept) j.next();
                                reconstructComplexNode(node, abstractConceptTreeNodeMap, childNode, supConcept, iwModel);
                            }
                        }
                    }
                }
                // 兄弟ノードをすべて処理した後に，子ノードの処理に移る
                for (int i = 0; i < node.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                    reconstructComplexTree(++d, childNode);
                }
            }

            private Set getSupConceptSet(String id) {
                Set supConceptSet = null;
                supConceptSet = constructClassPanel.getSupConceptSet(id);
                supConceptSet.addAll(constructPropertyPanel.getSupConceptSet(id));
                return supConceptSet;
            }

            /**
             * @param node
             * @param abstractConceptTreeNodeMap
             * @param childNode
             * @param supConcept
             */
            private void reconstructComplexNode(DefaultMutableTreeNode node, Map abstractConceptTreeNodeMap,
                    DefaultMutableTreeNode childNode, Concept supConcept, InputWordModel iwModel) {
                DefaultMutableTreeNode abstractNode = getAbstractNode(node, abstractConceptTreeNodeMap, supConcept,
                        iwModel);
                // System.out.println("語頭の上位概念: " + supConcept.getWord());
                // System.out.println("複合語の上位概念: " + node.toString());
                insertNode(childNode, abstractNode);
                setRemoveNode(node, childNode);
            }

            /**
             * @param node
             * @param childNode
             */
            private void setRemoveNode(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode) {
                if (nodeRemoveNodeSetMap.get(node) != null) {
                    Set removeNodeSet = (Set) nodeRemoveNodeSetMap.get(node);
                    removeNodeSet.add(childNode);
                    nodeRemoveNodeSetMap.put(node, removeNodeSet);
                } else {
                    Set removeNodeSet = new HashSet();
                    removeNodeSet.add(childNode);
                    nodeRemoveNodeSetMap.put(node, removeNodeSet);
                }
            }

            /**
             * 
             */
            private void insertNode(DefaultMutableTreeNode childNode, DefaultMutableTreeNode abstractNode) {
                DefaultMutableTreeNode insertNode = new DefaultMutableTreeNode(childNode.toString());
                deepCloneTreeNode(childNode, insertNode); // 多重継承している場合があるので，クローンを挿入する
                abstractNode.add(insertNode);
            }

            /*
             * TreeNodeの深いコピーを行う． orgNodeをinsertNodeにコピーする
             */
            private void deepCloneTreeNode(DefaultMutableTreeNode orgNode, DefaultMutableTreeNode insertNode) {
                for (int i = 0; i < orgNode.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) orgNode.getChildAt(i);
                    DefaultMutableTreeNode childNodeClone = new DefaultMutableTreeNode(childNode.getUserObject());
                    insertNode.add(childNodeClone);
                    deepCloneTreeNode(childNode, childNodeClone);
                }
            }

            /**
             * @param node
             * @param abstractConceptTreeNodeMap
             * @param supConcept
             * @return
             */
            private DefaultMutableTreeNode getAbstractNode(DefaultMutableTreeNode node, Map abstractConceptTreeNodeMap,
                    Concept supConcept, InputWordModel iwModel) {
                DefaultMutableTreeNode abstractNode = null;
                if (abstractConceptTreeNodeMap.get(supConcept) != null) {
                    abstractNode = (DefaultMutableTreeNode) abstractConceptTreeNodeMap.get(supConcept);
                } else {
                    abstractNode = new DefaultMutableTreeNode(supConcept);
                    abstractNodeLabelMap.put(abstractNode, supConcept.getWord() + iwModel.getWordWithoutTopWord());
                    // 抽象ノードはnodeの最後に追加する
                    // nodeの先頭に挿入してしまうと，nodeの２番目の要素が現在処理している子ノードとなってしまい，
                    // 重複した処理を行うことになってしまう．また，抽象ノードの数だけ子ノードが処理されない．
                    node.add(abstractNode);
                    abstractConceptTreeNodeMap.put(supConcept, abstractNode);
                }
                return abstractNode;
            }

            private TreeModel makeClassTreeModel(Set<Concept> nounConceptSet) {
                constructClassPanel.init();
                TreeModel classTreeModel = constructClassPanel.getTreeModel(nounConceptSet);
                constructClassPanel.setTreeModel(classTreeModel);
                constructClassPanel.setUndefinedWordListModel(undefinedWordListModel);
                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isConstructComplexWordTree()) {
                    setComplexConcept(constructClassPanel, nounConceptSet);
                }
                DODDLE.STATUS_BAR.addValue();
                return classTreeModel;
            }

            private TreeModel makePropertyTreeModel(Set<Concept> verbConceptSet) {
                constructPropertyPanel.init();
                TreeModel propertyTreeModel = constructPropertyPanel.getTreeModel(
                        constructClassPanel.getAllConceptID(), verbConceptSet);
                constructPropertyPanel.setTreeModel(propertyTreeModel);
                constructPropertyPanel.setUndefinedWordListModel(undefinedWordListModel);

                DODDLE.STATUS_BAR.addValue();
                constructPropertyPanel.removeNounNode(); // 動詞的概念階層から名詞的概念を削除
                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isConstructComplexWordTree()) {
                    setComplexConcept(constructPropertyPanel, verbConceptSet);
                }

                return propertyTreeModel;
            }

            /*
             * 「該当なし」とされた概念を辞書載っていない単語リストに追加
             */
            private void setUndefinedWordSet() {
                for (Iterator i = wordConceptMap.entrySet().iterator(); i.hasNext();) {
                    Entry entry = (Entry) i.next();
                    String word = (String) entry.getKey();
                    Concept c = (Concept) entry.getValue();
                    if (c.equals(DisambiguationPanel.nullConcept)) {
                        undefinedWordListModel.addElement(word);
                    }
                }
            }

            public void run() {
                constructClassPanel.setVisibleConceptTree(false);
                constructPropertyPanel.setVisibleConceptTree(false);
                DODDLE.STATUS_BAR.setLastMessage("階層構築完了");
                DODDLE.STATUS_BAR.initNormal(9);
                DODDLE.STATUS_BAR.startTime();
                project.resetIDConceptMap();

                undefinedWordListModel = new DefaultListModel();
                setUndefinedWordSet();

                if (DODDLE.IS_USING_DB) {
                    disambiguationPanel.setInputConceptSetWithDB();
                } else {
                    disambiguationPanel.setInputConceptSet();
                }
                Set<Concept> inputConceptSet = disambiguationPanel.getInputConceptSet(); // 入力概念のセット

                DODDLE.getLogger().log(Level.INFO, "完全照合 単語数: " + disambiguationPanel.getPerfectMatchedWordCnt());
                DODDLE.getLogger().log(Level.INFO, "部分照合 単語数: " + disambiguationPanel.getPartialMatchedWordCnt());
                DODDLE.getLogger().log(Level.INFO, "入力語彙数: " + (disambiguationPanel.getMatchedWordCnt()));
                DODDLE.getLogger().log(Level.INFO, "入力概念数: " + inputConceptSet.size());
                DODDLE.STATUS_BAR.addValue();
                project.initUserIDCount();

                for (int i = 0; i < undefinedWordListPanel.getModel().getSize(); i++) {
                    undefinedWordListModel.addElement(undefinedWordListPanel.getModel().getElementAt(i));
                }

                DODDLE.STATUS_BAR.addValue();
                if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
                    ConceptDefinition conceptDefinition = ConceptDefinition.getInstance();
                    Set<Concept> inputVerbConceptSet = conceptDefinition.getVerbConceptSet(inputConceptSet);
                    Set<Concept> inputNounConceptSet = new HashSet<Concept>(inputConceptSet);
                    inputNounConceptSet.removeAll(inputVerbConceptSet);

                    DODDLE.getLogger().log(Level.INFO, "入力名詞的概念数: " + inputNounConceptSet.size());
                    DODDLE.STATUS_BAR.addValue();
                    constructClassPanel.setTreeModel(makeClassTreeModel(inputNounConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                    DODDLE.getLogger().log(Level.INFO, "入力動詞的概念数: " + inputVerbConceptSet.size());
                    constructPropertyPanel.setTreeModel(makePropertyTreeModel(inputVerbConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                } else {
                    Set<Concept> inputNounConceptSet = new HashSet<Concept>(inputConceptSet);
                    DODDLE.getLogger().log(Level.INFO, "入力名詞的概念数: " + inputNounConceptSet.size());
                    constructClassPanel.setTreeModel(makeClassTreeModel(inputNounConceptSet));
                    DODDLE.STATUS_BAR.addValue();
                    constructPropertyPanel.setTreeModel(makePropertyTreeModel(new HashSet<Concept>()));
                    DODDLE.STATUS_BAR.addValue();
                }

                constructClassPanel.expandTree();
                DODDLE.STATUS_BAR.addValue();
                constructPropertyPanel.expandTree();
                DODDLE.STATUS_BAR.addValue();
                constructClassPanel.setVisibleConceptTree(true);
                constructPropertyPanel.setVisibleConceptTree(true);
                DODDLE.setSelectedIndex(DODDLE.TAXONOMIC_PANEL);
                DODDLE.STATUS_BAR.addValue();
                DODDLE.STATUS_BAR.hideProgressBar();
            }
        }.start();
    }
}
