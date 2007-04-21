package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
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
public class ConceptTreePanel extends JPanel {
    private DODDLEProject project;

    private JTextField searchConceptField;
    private TitledBorder searchConceptFieldBorder;
    private JButton searchButton;
    private JButton searchPreviousButton;
    private JButton searchNextButton;
    private JCheckBox perfectMatchSearchOptionCheckBox;
    private JCheckBox searchURICheckBox;
    private JList labelLangJList;
    private JList descriptionLangJList;

    private JTree conceptTree;
    private ConceptTreeNode targetConceptTreeNode;

    private AddConceptAction addConceptAction;
    private CopyConceptAction copyConceptAction;
    private CloneConceptAction cloneConceptAction;
    private CutConceptAction cutConceptAction;
    private PasteConceptAction pasteConceptAction;
    private DeleteLinkToUpperConceptAction deleteLinkToUpperConceptAction;
    private DeleteConceptAction deleteConceptAction;
    private AddUndefinedWordListAction addUndefinedWordListAction;
    private MoveUndefinedWordListAction moveUndefinedWordListAction;
    private JCheckBox showPrefixCheckBox;

    private ImageIcon addConceptIcon = Utils.getImageIcon("add_concept.png");
    private ImageIcon cloneConceptIcon = Utils.getImageIcon("clone_concept.png");
    private ImageIcon expandTreeIcon = Utils.getImageIcon("expand_tree.png");
    private ImageIcon addUndefWordIcon = Utils.getImageIcon("add_undef_word.png");
    private ImageIcon undefIcon = Utils.getImageIcon("undef.png");

    private ImageIcon copyIcon = Utils.getImageIcon("copy.gif");
    private ImageIcon cutIcon = Utils.getImageIcon("cut.gif");
    private ImageIcon pasteIcon = Utils.getImageIcon("paste.gif");
    private ImageIcon deleteIcon = Utils.getImageIcon("delete.gif");

    private UndefinedWordListPanel undefinedWordListPanel;
    private ConceptDriftManagementPanel conceptDriftManagementPanel;

    private Map<String, Concept> idConceptMap;
    private Map<Concept, Set<ConceptTreeNode>> conceptSameConceptTreeNodeMap;
    private Map<String, Concept> complexWordConceptMap; // 複合語と対応する概念のマッピング
    private static final int LANG_SIZE = 60;

    private ConceptSelectionDialog conceptSelectionDialog;

    private String treeType;

    private JTree hasaTree;

    public static final String ISA_TREE = "is-a Tree";
    public static final String CLASS_HASA_TREE = "clas has-a Tree";
    public static final String PROPERTY_HASA_TREE = "property has-a Tree";

    public ConceptTreePanel(String title, String type, UndefinedWordListPanel undefPanel, DODDLEProject p) {
        project = p;
        treeType = type;
        if (type == CLASS_HASA_TREE) {
            conceptSelectionDialog = new ConceptSelectionDialog(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
            "Class Is-a Selection Dialog");
        } else if (type == PROPERTY_HASA_TREE) {
            conceptSelectionDialog = new ConceptSelectionDialog(ConceptTreeCellRenderer.VERB_CONCEPT_TREE,
            "Property Is-a Selection Dialog");
        }
        undefinedWordListPanel = undefPanel;
        abstractLabelSet = new HashSet<String>();
        idConceptMap = new HashMap<String, Concept>();
        conceptSameConceptTreeNodeMap = new HashMap<Concept, Set<ConceptTreeNode>>();
        complexWordConceptMap = new HashMap<String, Concept>();
        Action searchAction = new SearchAction();
        searchConceptField = new JTextField(15);
        searchConceptField.addActionListener(searchAction);
        searchConceptFieldBorder = BorderFactory.createTitledBorder(Translator
                .getString("ConceptTreePanel.ConceptSearch")
                + " (0/0)");
        searchConceptField.setBorder(searchConceptFieldBorder);
        searchButton = new JButton(Translator.getString("ConceptTreePanel.Search"));
        searchButton.addActionListener(searchAction);
        searchPreviousButton = new JButton(Translator.getString("ConceptTreePanel.SearchPrev"));
        searchPreviousButton.addActionListener(searchAction);
        searchNextButton = new JButton(Translator.getString("ConceptTreePanel.SearchNext"));
        searchNextButton.addActionListener(searchAction);

        perfectMatchSearchOptionCheckBox = new JCheckBox(Translator.getString("ConceptTreePanel.perfectMatchedSearch"));
        searchURICheckBox = new JCheckBox("URI検索");
        JPanel searchCheckBoxPanel = new JPanel();
        searchCheckBoxPanel.add(perfectMatchSearchOptionCheckBox);
        searchCheckBoxPanel.add(searchURICheckBox);

        labelLangJList = new JList(new String[] { "en", "ja", "ALL", "NULL"});
        labelLangJList.setSelectedValue("ALL", true);
        JScrollPane labelLangJListScroll = new JScrollPane(labelLangJList);
        labelLangJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 70));
        labelLangJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 70));
        labelLangJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Lang") + " ("
                + Translator.getString("Label") + ")"));
        descriptionLangJList = new JList(new String[] { "en", "ja", "ALL", "NULL"});
        JScrollPane descriptionLangJListScroll = new JScrollPane(descriptionLangJList);
        descriptionLangJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Lang") + " ("
                + Translator.getString("Description") + ")"));
        descriptionLangJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 70));
        descriptionLangJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 70));

        JPanel searchRangePanel = new JPanel();
        searchRangePanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ConceptTreePanel.SearchOption")));
        searchRangePanel.setLayout(new GridLayout(1, 2));
        searchRangePanel.add(labelLangJListScroll);
        searchRangePanel.add(descriptionLangJListScroll);

        JPanel searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new BorderLayout());
        searchOptionPanel.add(searchCheckBoxPanel, BorderLayout.NORTH);
        searchOptionPanel.add(searchRangePanel, BorderLayout.CENTER);

        JPanel searchDirectionPanel = new JPanel();
        searchDirectionPanel.setLayout(new GridLayout(1, 2));
        searchDirectionPanel.add(searchPreviousButton);
        searchDirectionPanel.add(searchNextButton);
        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new GridLayout(2, 1));
        searchButtonPanel.add(searchButton);
        searchButtonPanel.add(searchDirectionPanel);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchConceptField, BorderLayout.CENTER);
        searchPanel.add(searchButtonPanel, BorderLayout.EAST);
        searchPanel.add(searchOptionPanel, BorderLayout.SOUTH);

        addConceptAction = new AddConceptAction(Translator.getString("ConceptTreePanel.addConceptAction"));
        copyConceptAction = new CopyConceptAction(Translator.getString("ConceptTreePanel.copyConceptAction"));
        cloneConceptAction = new CloneConceptAction(Translator.getString("ConceptTreePanel.cloneConceptAction"));
        cutConceptAction = new CutConceptAction(Translator.getString("ConceptTreePanel.cutConceptAction"));
        pasteConceptAction = new PasteConceptAction(Translator.getString("ConceptTreePanel.pasteConceptAction"));
        deleteLinkToUpperConceptAction = new DeleteLinkToUpperConceptAction(Translator
                .getString("ConceptTreePanel.deleteLinkToUpperConceptAction"));
        deleteConceptAction = new DeleteConceptAction(Translator.getString("ConceptTreePanel.deleteConceptAction"));
        addUndefinedWordListAction = new AddUndefinedWordListAction(Translator
                .getString("ConceptTreePanel.addUndefinedWordListAction"));
        moveUndefinedWordListAction = new MoveUndefinedWordListAction(Translator
                .getString("ConceptTreePanel.moveUndefinedWordListAction"));
        showPrefixCheckBox = new JCheckBox(Translator.getString("ConceptTreePanel.showPrefix"), false);

        conceptTree = new JTree();
        conceptTree.addMouseListener(new ConceptTreeMouseAdapter());
        conceptTree.setEditable(false);
        conceptTree.setDragEnabled(true);
        new DropTarget(conceptTree, new ConceptTreeDropTargetAdapter());
        conceptTree.setScrollsOnExpand(true);

        JScrollPane conceptTreeScroll = new JScrollPane(conceptTree);
        conceptTreeScroll.setPreferredSize(new Dimension(250, 100));
        conceptTreeScroll.setBorder(BorderFactory.createTitledBorder(title));

        JToolBar toolBar = new JToolBar();
        toolBar.add(addConceptAction).setToolTipText(addConceptAction.getTitle());
        if (treeType == ISA_TREE) {
            toolBar.add(cloneConceptAction).setToolTipText(cloneConceptAction.getTitle());
        }
        toolBar.add(copyConceptAction).setToolTipText(copyConceptAction.getTitle());
        toolBar.add(cutConceptAction).setToolTipText(cutConceptAction.getTitle());
        toolBar.add(pasteConceptAction).setToolTipText(pasteConceptAction.getTitle());
        toolBar.add(deleteLinkToUpperConceptAction).setToolTipText(deleteLinkToUpperConceptAction.getTitle());
        if (treeType == ISA_TREE) {
            toolBar.add(deleteConceptAction).setToolTipText(deleteConceptAction.getTitle());
            toolBar.add(addUndefinedWordListAction).setToolTipText(addUndefinedWordListAction.getTitle());
            toolBar.add(moveUndefinedWordListAction).setToolTipText(moveUndefinedWordListAction.getTitle());
        }
        toolBar.add(new ExpandAllPathAction(Translator.getString("ConceptTreePanel.expandConceptTree")))
                .setToolTipText(Translator.getString("ConceptTreePanel.expandConceptTree"));

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(toolBar, BorderLayout.NORTH);
        treePanel.add(conceptTreeScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(treePanel, BorderLayout.CENTER);
    }

    public void setHasaTree(JTree hasaTree) {
        this.hasaTree = hasaTree;
    }

    public void setConceptDriftManagementPanel(ConceptDriftManagementPanel cdmp) {
        conceptDriftManagementPanel = cdmp;
    }

    public boolean isShowPrefix() {
        return showPrefixCheckBox.isSelected();
    }

    public void loadDescriptions(Map<String, DODDLELiteral> wordDescriptionMap) {
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        setDescriptions(rootNode, wordDescriptionMap);
    }

    public void setDescriptions(ConceptTreeNode treeNode, Map<String, DODDLELiteral> wordDescriptionMap) {
        for (int i = 0; i < treeNode.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) treeNode.getChildAt(i);
            for (String lang : childNode.getLangLabelLiteralListMap().keySet()) {
                for (DODDLELiteral label : childNode.getLangLabelLiteralListMap().get(lang)) {
                    if (wordDescriptionMap.get(label.getString()) != null) {
                        DODDLELiteral description = wordDescriptionMap.get(label.getString());
                        childNode.getConcept().addDescription(description);
                    }
                }
            }
            setDescriptions(childNode, wordDescriptionMap);
        }
    }

    class ConceptTreeMouseAdapter extends MouseAdapter {

        private JPopupMenu popupMenu;

        ConceptTreeMouseAdapter() {
            popupMenu = new JPopupMenu();
            popupMenu.add(addConceptAction);
            if (treeType == ISA_TREE) {
                popupMenu.add(cloneConceptAction);
            }
            popupMenu.add(copyConceptAction);
            popupMenu.add(cutConceptAction);
            popupMenu.add(pasteConceptAction);
            popupMenu.add(deleteLinkToUpperConceptAction);
            if (treeType == ISA_TREE) {
                popupMenu.add(deleteConceptAction);
                popupMenu.add(addUndefinedWordListAction);
                popupMenu.add(moveUndefinedWordListAction);
            }
            popupMenu.add(new ExpandSelectedPathAction(Translator.getString("ConceptTreePanel.expandConceptTree")));
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                if (!(conceptTree.getSelectionCount() == 1)) { return; }
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public JTree getConceptTree() {
        return conceptTree;
    }

    public boolean isConceptContains(Concept c) {
        if (!(conceptTree.getModel().getRoot() instanceof ConceptTreeNode)) { return false; }
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        return isConceptContains(c, rootNode);
    }

    private boolean isConceptContains(Concept c, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (c == childNode.getConcept()) { return true; }
            return isConceptContains(c, childNode);
        }
        return false;
    }

    class SearchAction extends AbstractAction {

        private int index;
        private List searchNodeList;
        private String searchKeyWord;

        public SearchAction() {
            index = 0;
            searchKeyWord = "";
            searchNodeList = new ArrayList();
        }

        private boolean isSearchConcept(Concept c) {
            boolean checkAllLabel = false;
            boolean checkAllDescription = false;
            boolean checkNullLabel = false;
            boolean checkNullDescription = false;

            if (searchURICheckBox.isSelected()) {
                if (perfectMatchSearchOptionCheckBox.isSelected() && c.getURI().equals(searchKeyWord)) {
                    return true;
                } else if (c.getURI().matches(searchKeyWord)) { return true; }
            }

            Object[] selectedLabelLangList = labelLangJList.getSelectedValues();
            for (int i = 0; i < selectedLabelLangList.length; i++) {
                if (selectedLabelLangList[i].equals("ALL")) {
                    checkAllLabel = true;
                }
                if (selectedLabelLangList[i].equals("NULL")) {
                    checkNullLabel = true;
                }
            }
            Object[] selectedDescriptionLangList = descriptionLangJList.getSelectedValues();
            for (int i = 0; i < selectedDescriptionLangList.length; i++) {
                if (selectedDescriptionLangList[i].equals("ALL")) {
                    checkAllDescription = true;
                }
                if (selectedDescriptionLangList[i].equals("NULL")) {
                    checkNullDescription = true;
                }
            }
            if (!checkNullLabel) {
                Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
                if (checkAllLabel) {
                    for (List<DODDLELiteral> labelList : langLabelListMap.values()) {
                        for (DODDLELiteral label : labelList) {
                            if (perfectMatchSearchOptionCheckBox.isSelected()) { return label.getString().matches(
                                    searchKeyWord); }
                            if (label.getString().indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                } else {
                    for (int i = 0; i < selectedLabelLangList.length; i++) {
                        if (langLabelListMap.get(selectedLabelLangList[i]) == null) {
                            continue;
                        }
                        for (DODDLELiteral label : langLabelListMap.get(selectedLabelLangList[i])) {
                            if (perfectMatchSearchOptionCheckBox.isSelected()) { return label.getString().matches(
                                    searchKeyWord); }
                            if (label.getString().indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                }
            }

            if (!checkNullDescription) {
                Map<String, List<DODDLELiteral>> langDescriptionListMap = c.getLangDescriptionListMap();
                if (checkAllDescription) {
                    for (List<DODDLELiteral> descriptionList : langDescriptionListMap.values()) {
                        for (DODDLELiteral description : descriptionList) {
                            if (perfectMatchSearchOptionCheckBox.isSelected()) { return description.getString()
                                    .matches(searchKeyWord); }
                            if (description.getString().indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                } else {
                    for (int i = 0; i < selectedDescriptionLangList.length; i++) {
                        if (langDescriptionListMap.get(selectedDescriptionLangList[i]) == null) {
                            continue;
                        }
                        for (DODDLELiteral description : langDescriptionListMap.get(selectedDescriptionLangList[i])) {
                            if (perfectMatchSearchOptionCheckBox.isSelected()) { return description.getString()
                                    .matches(searchKeyWord); }
                            if (description.getString().indexOf(searchKeyWord) != -1) { return true; }
                        }
                    }
                }
            }
            return false;
        }

        private void searchConcept(ConceptTreeNode node) {
            for (int i = 0; i < node.getChildCount(); i++) {
                ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
                try {
                    if (isSearchConcept(childNode.getConcept())) {
                        searchNodeList.add(childNode);
                    }
                } catch (PatternSyntaxException e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "PatternSyntaxException",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                searchConcept(childNode);
            }
        }

        private void setSearchFieldTitle() {
            if (searchNodeList.size() == 0) {
                searchConceptFieldBorder.setTitle(Translator.getString("ConceptTreePanel.ConceptSearch") + " (0/0)");
            } else {
                searchConceptFieldBorder.setTitle(Translator.getString("ConceptTreePanel.ConceptSearch") + " ("
                        + (index + 1) + "/" + searchNodeList.size() + ")");
            }
            searchConceptField.repaint();
        }

        private void selectSearchNode() {
            setSearchFieldTitle();
            ConceptTreeNode node = (ConceptTreeNode) searchNodeList.get(index);
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            TreeNode[] nodes = model.getPathToRoot(node);
            TreePath path = new TreePath(nodes);
            conceptTree.scrollPathToVisible(path);
            conceptTree.setSelectionPath(path);
        }

        private void searchPrevious() {
            if (searchNodeList.size() == 0) { return; }
            if (0 <= index - 1 && index - 1 < searchNodeList.size()) {
                index--;
            } else {
                index = searchNodeList.size() - 1;
            }
            selectSearchNode();
        }

        private void searchNext() {
            if (searchNodeList.size() == 0) { return; }
            if (0 <= index + 1 && index + 1 < searchNodeList.size()) {
                index++;
            } else {
                index = 0;
            }
            selectSearchNode();
        }

        private void search() {
            index = 0;
            searchNodeList.clear();
            searchKeyWord = searchConceptField.getText();
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            if (!(model.getRoot() instanceof ConceptTreeNode)) { return; }
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            searchConcept(rootNode);
            setSearchFieldTitle();
            if (searchNodeList.size() == 0) { return; }
            selectSearchNode();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == searchConceptField && searchKeyWord.equals(searchConceptField.getText())
                    && 0 < searchNodeList.size()) {
                // キーワードが変化しておらず，検索結果が１以上で，検索フィールドでエンターボタンを押した場合
                searchNext();
            } else if (e.getSource() == searchButton || e.getSource() == searchConceptField) {
                search();
            } else if (e.getSource() == searchPreviousButton) {
                searchPrevious();
            } else if (e.getSource() == searchNextButton) {
                searchNext();
            }
        }
    }

    class ConceptTreeDropTargetAdapter extends DropTargetAdapter {

        private TreePath[] dragPaths;

        public void dragEnter(DropTargetDragEvent dtde) {
            dragPaths = conceptTree.getSelectionPaths();
        }

        private ConceptTreeNode getSelectedNode(Point p) {
            TreePath path = conceptTree.getPathForLocation(p.x, p.y);
            if (path == null || path.getLastPathComponent() == null) { return null; }
            return (ConceptTreeNode) path.getLastPathComponent();
        }

        public void dragOver(DropTargetDragEvent dtde) {
            Point point = dtde.getLocation();
            ConceptTreeNode node = getSelectedNode(point);
            if (node != null) {
                int row = conceptTree.getRowForLocation(point.x, point.y);
                conceptTree.setSelectionRow(row);
            }
        }

        public void drop(DropTargetDropEvent dtde) {
            Point dropPoint = dtde.getLocation();
            ConceptTreeNode dropNode = getSelectedNode(dropPoint);

            if (dropNode == null || dragPaths == null) {
                dragPaths = null;
                return;
            } else if (UndefinedWordListPanel.isDragUndefinedList) {
                Object[] selectedValues = undefinedWordListPanel.getSelectedValues();
                DefaultListModel listModel = undefinedWordListPanel.getModel();
                DefaultListModel viewListModel = undefinedWordListPanel.getViewModel();
                for (int i = 0; i < selectedValues.length; i++) {
                    ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                    insertNewConceptTreeNode(selectedValues[i].toString(), parent.getConcept());
                    listModel.removeElement(selectedValues[i]);
                    if (viewListModel != null) {
                        viewListModel.removeElement(selectedValues[i]);
                    }
                }
                undefinedWordListPanel.setTitleWithSize();
                UndefinedWordListPanel.isDragUndefinedList = false;
            } else {
                DefaultTreeModel treeModel = (DefaultTreeModel) conceptTree.getModel();
                for (int i = 0; i < dragPaths.length; i++) {
                    DefaultMutableTreeNode movedNode = (DefaultMutableTreeNode) dragPaths[i].getLastPathComponent();
                    if (movedNode == dropNode) {
                        continue;
                    }
                    if (movedNode.getParent() != null) {
                        treeModel.removeNodeFromParent(movedNode);
                    }
                    treeModel.insertNodeInto(movedNode, dropNode, 0);
                    checkMultipleInheritanceNode(((ConceptTreeNode) movedNode).getConcept());
                }
            }
            dragPaths = null;
        }
    }

    private boolean isSameConcept(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        return concept.getURI().equals(node.getConcept().getURI());
    }

    public void searchSameConceptTreeNode(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (isSameConcept(concept, childNode, sameConceptSet)) {
                sameConceptSet.add(childNode);
            }
            searchSameConceptTreeNode(concept, childNode, sameConceptSet);
        }
    }

    private boolean hasMultipleParent(Set sameConceptTreeNodeSet) {
        ConceptTreeNode parentTreeNode = null;
        for (Iterator i = sameConceptTreeNodeSet.iterator(); i.hasNext();) {
            ConceptTreeNode node = (ConceptTreeNode) i.next();
            if (parentTreeNode == null) {
                parentTreeNode = (ConceptTreeNode) node.getParent();
            } else {
                if (parentTreeNode.getURI().equals(((ConceptTreeNode) node.getParent()).getURI())) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }

    private void makeIDParentIDSetMap(Map idParentIDSetMap, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (idParentIDSetMap.get(childNode.getURI()) != null) {
                Set parentIDSet = (Set) idParentIDSetMap.get(childNode.getURI());
                parentIDSet.add(node.getURI());
                idParentIDSetMap.put(childNode.getURI(), parentIDSet);
            } else {
                Set parentIDSet = new HashSet();
                parentIDSet.add(node.getURI());
                idParentIDSetMap.put(childNode.getURI(), parentIDSet);
            }
            makeIDParentIDSetMap(idParentIDSetMap, childNode);
        }
    }

    private void checkAllMultipleInheritanceNode(Map idParentSetMap, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            Set parentIDSet = (Set) idParentSetMap.get(childNode.getURI());
            if (1 < parentIDSet.size()) {
                childNode.setIsMultipleInheritance(true);
            }
            checkAllMultipleInheritanceNode(idParentSetMap, childNode);
        }
    }

    public void checkAllMultipleInheritanceNode(TreeModel treeModel) {
        Map idParentIDSetMap = new HashMap();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        makeIDParentIDSetMap(idParentIDSetMap, rootNode);
        checkAllMultipleInheritanceNode(idParentIDSetMap, rootNode);
    }

    public void checkMultipleInheritanceNode(Concept c) {
        ConceptTreeNode rootNode = (ConceptTreeNode) conceptTree.getModel().getRoot();
        Set sameConceptTreeNodeSet = new HashSet();
        searchSameConceptTreeNode(c, rootNode, sameConceptTreeNodeSet);
        if (hasMultipleParent(sameConceptTreeNodeSet)) {
            for (Iterator j = sameConceptTreeNodeSet.iterator(); j.hasNext();) {
                ConceptTreeNode node = (ConceptTreeNode) j.next();
                node.setIsMultipleInheritance(true);
            }
        } else {
            for (Iterator j = sameConceptTreeNodeSet.iterator(); j.hasNext();) {
                ConceptTreeNode node = (ConceptTreeNode) j.next();
                node.setIsMultipleInheritance(false);
            }
        }
    }

    public Set getAllConcept() {
        Set conceptSet = new HashSet();
        TreeModel treeModel = conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        conceptSet.add(rootNode.getConcept());
        getAllConcept(rootNode, conceptSet);
        return conceptSet;
    }

    private void getAllConcept(TreeNode node, Set conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept());
            getAllConcept(childNode, conceptSet);
        }
    }

    public Set<String> getAllConceptURI() {
        Set<String> uriSet = new HashSet<String>();
        TreeModel treeModel = conceptTree.getModel();
        if (treeModel.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
            uriSet.add(rootNode.getConcept().getURI());
            getAllConceptURI(rootNode, uriSet);
        }
        return uriSet;
    }

    private void getAllConceptURI(TreeNode node, Set conceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            conceptSet.add(childNode.getConcept().getURI());
            getAllConceptURI(childNode, conceptSet);
        }
    }

    
    private void deleteConcept(DefaultTreeModel model, ConceptTreeNode deleteNode) {        
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        Set<ConceptTreeNode> sameConceptSet = new HashSet<ConceptTreeNode>();
        searchSameConceptTreeNode(deleteNode.getConcept(), rootNode, sameConceptSet);
        for (ConceptTreeNode delNode : sameConceptSet) {
            if (delNode.getParent() != null) {
                model.removeNodeFromParent(delNode);
            }
        }
    }
    
    /**
     * 概念を削除（上位リンクの削除ではなく，概念そのものを削除）
     */
    private void deleteConcept() {
        if (conceptTree.getSelectionCount() == 1) {
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            ConceptTreeNode deleteNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            deleteConcept(model, deleteNode);
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            if (hasaTree != null) {
                model = (DefaultTreeModel) hasaTree.getModel();
                deleteConcept(model, deleteNode);
            }
        }
    }

    private Concept searchedConcept; // 領域オントロジーのノードに対応づけられたConcept
    private Set<Concept> supConceptSet; // 上位概念のセットを保存

    public Set getSupConceptSet(String id) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        supConceptSet = new HashSet<Concept>();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            searchSupConcept(id, rootNode);
        }
        return supConceptSet;
    }

    /**
     * 引数で与えたidの上位概念を検索する. （EDR全体に定義されているConceptではない)
     */
    private void searchSupConcept(String id, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept() != null && childNode.getConcept().getLocalName().equals(id)) {
                supConceptSet.add(node.getConcept());
            }
            searchSupConcept(id, childNode);
        }
    }

    /**
     * ノードに対応づけられた概念を検索する．（EDR全体に定義されているConceptではない)
     */
    private void searchConcept(String identity, ConceptTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getConcept().getURI().equals(identity)) {
                searchedConcept = childNode.getConcept();
            }
            searchConcept(identity, childNode);
        }
    }

    public void addJPWord(String identity, String word) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        searchedConcept = null;
        searchConcept(identity, rootNode);
        if (searchedConcept != null) {
            DODDLELiteral label = new DODDLELiteral("ja", word);
            searchedConcept.addLabel(label);
            searchedConcept.setInputLabel(label);
        }
    }

    public void addSubConcept(String identity, String word) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        searchedConcept = null;
        searchConcept(identity, rootNode);
        if (searchedConcept != null) {
            insertNewConceptTreeNode(word, searchedConcept);
        }
    }

    private Concept insertComplexWordConceptTreeNode(String identity, String newWord,
            ConceptTreeNode conceptTreeRootNode) {
        searchedConcept = null;
        if (idConceptMap.get(identity) == null) {
            searchConcept(identity, conceptTreeRootNode);
            idConceptMap.put(identity, searchedConcept);
        } else {
            searchedConcept = idConceptMap.get(identity);
        }
        if (searchedConcept == null) {
            searchedConcept = conceptTreeRootNode.getConcept();
        }
        if (searchedConcept != null) {
            if (complexWordConceptMap.get(newWord) != null) {
                Concept c = complexWordConceptMap.get(newWord);
                insertConceptTreeNode(c, searchedConcept, false, true);
                return c;
            }
            Concept c = insertNewConceptTreeNode(newWord, searchedConcept);
            complexWordConceptMap.put(newWord, c);
            return c;
        }
        return null;
    }

    private String getChildWordWithoutTopWord(DefaultMutableTreeNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (childNode.getUserObject() instanceof String) {
                String complexWord = (String) childNode.getUserObject();
                InputWordModel iwModel = project.getDisambiguationPanel().makeInputWordModel(complexWord);
                return iwModel.getBasicWordWithoutTopWord();
            }
        }
        return null;
    }

    public void addComplexWordConcept(String identity, TreeNode node, ConceptTreeNode conceptTreeRootNode,
            Map<DefaultMutableTreeNode, String> abstractNodeLabelMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            String newWord = "";
            if (childNode.getUserObject() instanceof Concept) {
                Concept c = (Concept) childNode.getUserObject();
                searchedConcept = null;
                searchConcept(identity, conceptTreeRootNode);
                newWord = abstractNodeLabelMap.get(childNode);
                if (newWord == null) {
                    newWord = c.getWord() + getChildWordWithoutTopWord(childNode);
                    newWord = newWord.replaceAll("\\s*", "");
                }
                newWord = "[A] " + newWord;
                abstractNodeCnt++;
                abstractLabelSet.add(newWord);
                totalAbstractNodeGroupSiblingNodeCnt += childNode.getChildCount();
                DODDLE.getLogger().log(Level.DEBUG,
                        "[" + abstractNodeCnt + "] 抽象概念(兄弟数)： " + newWord + " (" + childNode.getChildCount() + ")");
            } else {
                newWord = childNode.toString();
            }
            Concept childConcept = insertComplexWordConceptTreeNode(identity, newWord, conceptTreeRootNode);
            if (childConcept != null) {
                addComplexWordConcept(childConcept.getURI(), childNode, conceptTreeRootNode, abstractNodeLabelMap);
            }
        }
    }

    public Map<String, Concept> getComplexWordConceptMap() {
        return complexWordConceptMap;
    }

    private int abstractNodeCnt;
    private Set<String> abstractLabelSet;
    private int totalAbstractNodeGroupSiblingNodeCnt;

    public int getAbstractNodeCnt() {
        return abstractNodeCnt;
    }

    public int getAbstractConceptCnt() {
        return abstractLabelSet.size();
    }

    public int getTotalAbstractNodeGroupSiblingNodeCnt() {
        return totalAbstractNodeGroupSiblingNodeCnt;
    }

    public void addComplexWordConcept(Map matchedWordIDMap, TreeNode node, ConceptTreeNode conceptTreeRootNode,
            Map abstractNodeLabelMap) {
        idConceptMap.clear();
        abstractLabelSet.clear();
        conceptSameConceptTreeNodeMap.clear();
        complexWordConceptMap.clear();
        abstractNodeCnt = 0;
        totalAbstractNodeGroupSiblingNodeCnt = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            String identity = childNode.toString();
            addComplexWordConcept(identity, childNode, conceptTreeRootNode, abstractNodeLabelMap);
        }
    }

    private void copyConceptTreeNode(ConceptTreeNode targetNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode insertNode = new ConceptTreeNode(targetNode, project);
        model.insertNodeInto(insertNode, targetNode, 0);
    }

    private Concept insertNewConceptTreeNode(String word, Concept parentConcept) {
        Concept newConcept = new VerbConcept(DODDLE.BASE_URI + project.getUserIDStr(), word);
        newConcept.setInputLabel(new DODDLELiteral("", word));
        insertConceptTreeNode(newConcept, parentConcept, false, true);
        return newConcept;
    }

    private void insertConceptTreeNode(Concept insertConcept, Concept parentConcept, boolean isInputConcept,
            boolean isUserConcept) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
        Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
        if (parentConcept.getURI().equals(rootNode.getConcept().getURI())) {
            sameConceptTreeNodeSet.add(rootNode);
        } else {
            if (conceptSameConceptTreeNodeMap.get(parentConcept) != null) {
                sameConceptTreeNodeSet = conceptSameConceptTreeNodeMap.get(parentConcept);
            } else {
                searchSameConceptTreeNode(parentConcept, rootNode, sameConceptTreeNodeSet);
                conceptSameConceptTreeNodeMap.put(parentConcept, sameConceptTreeNodeSet);
            }
        }
        for (ConceptTreeNode parentNode : sameConceptTreeNodeSet) {
            ConceptTreeNode insertNode = new ConceptTreeNode(insertConcept, project);
            insertNode.setIsInputConcept(isInputConcept);
            insertNode.setIsUserConcept(isUserConcept);
            model.insertNodeInto(insertNode, parentNode, parentNode.getChildCount());
            if (parentNode == rootNode) {
                conceptTree.expandPath(conceptTree.getSelectionPath());
            }
        }
    }

    class CopyConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        CopyConceptAction(String title) {
            super(title, copyIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                targetConceptTreeNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                DODDLE.STATUS_BAR.setValue("コピー: " + targetConceptTreeNode.getConcept());
            }
        }
    }

    class CloneConceptAction extends AbstractAction {
        private String title;

        public String getTitle() {
            return title;
        }

        CloneConceptAction(String title) {
            super(title, cloneConceptIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                targetConceptTreeNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                pasteConcept((ConceptTreeNode) targetConceptTreeNode.getParent());
                DODDLE.STATUS_BAR.setValue("複製: " + targetConceptTreeNode);
            }
        }

    }

    class CutConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        CutConceptAction(String title) {
            super(title, cutIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                targetConceptTreeNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                deleteLinkToUpperConcept();
                DODDLE.STATUS_BAR.setValue("切り取り: " + targetConceptTreeNode.getConcept());
            }
        }

    }

    private Concept pasteConcept(ConceptTreeNode parentNode) {
        Concept targetConcept = targetConceptTreeNode.getConcept();
        if (parentNode != null) {
            insertConceptTreeNode(targetConcept, parentNode.getConcept(), targetConceptTreeNode.isInputConcept(),
                    targetConceptTreeNode.isUserConcept());
            checkMultipleInheritanceNode(targetConcept);
        }
        return targetConcept;
    }

    class PasteConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        PasteConceptAction(String title) {
            super(title, pasteIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            if (conceptTree.getSelectionCount() == 1) {
                ConceptTreeNode parentNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                Concept pasteConcept = pasteConcept(parentNode);
                DODDLE.STATUS_BAR.setValue("貼り付け: " + pasteConcept);
            }
        }

    }

    class AddConceptAction extends AbstractAction {

        private String title;

        AddConceptAction(String title) {
            super(title, addConceptIcon);
            this.title = title;
            setToolTipText(title);
        }

        public String getTitle() {
            return title;
        }

        private void insertIsaTreeConcept() {
            TreeModel isaTreeModel = null;
            if (treeType == CLASS_HASA_TREE) {
                isaTreeModel = project.getConstructClassPanel().getIsaTree().getModel();            
            } else if (treeType == PROPERTY_HASA_TREE) {
                isaTreeModel = project.getConstructPropertyPanel().getIsaTree().getModel();
            }
            conceptSelectionDialog.setTreeModel(isaTreeModel);
            conceptSelectionDialog.setVisible(true);
            Set<Concept> addConceptSet = conceptSelectionDialog.getConceptSet();
            ConceptTreeNode parentNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            if (parentNode != null) {
                ConceptTreeNode rootNode = (ConceptTreeNode) isaTreeModel.getRoot();
                for (Concept isaConcept : addConceptSet) {
                    Set<ConceptTreeNode> sameConceptTreeNodeSet = new HashSet<ConceptTreeNode>();
                    searchSameConceptTreeNode(isaConcept, rootNode, sameConceptTreeNodeSet);
                    ConceptTreeNode treeNode = (ConceptTreeNode) sameConceptTreeNodeSet.toArray()[0];
                    insertConceptTreeNode(isaConcept, parentNode.getConcept(), treeNode.isInputConcept(), treeNode
                            .isUserConcept());
                    checkMultipleInheritanceNode(isaConcept);
                }
            }
        }

        private void insertNewConcept() {
            ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
            if (parent != null) {
                insertNewConceptTreeNode(
                        Translator.getString("ConceptTreePanel.NewConcept") + project.getUserIDCount(), parent
                                .getConcept());
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (treeType == ISA_TREE) {
                insertNewConcept();
            } else if (treeType == CLASS_HASA_TREE || treeType == PROPERTY_HASA_TREE) {
                insertIsaTreeConcept();
            }
        }
    }

    private void deleteLinkToUpperConcept() {
        ConceptTreeNode targetDeleteNode = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
        deleteLinkToUpperConcept(targetDeleteNode);
    }

    /**
     * @param model
     * @param targetDeleteNode
     */
    public void deleteLinkToUpperConcept(ConceptTreeNode targetDeleteNode) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (targetDeleteNode.getParent() != null) {
            ConceptTreeNode targetDeleteNodeParent = (ConceptTreeNode) targetDeleteNode.getParent();
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            Set<ConceptTreeNode> deleteTreeNodeSet = new HashSet<ConceptTreeNode>();
            searchSameConceptTreeNode(targetDeleteNode.getConcept(), rootNode, deleteTreeNodeSet);
            for (ConceptTreeNode deleteTreeNode : deleteTreeNodeSet) {
                ConceptTreeNode deleteTreeNodeParent = (ConceptTreeNode) deleteTreeNode.getParent();
                if (deleteTreeNodeParent != null
                        && targetDeleteNodeParent.getURI().equals(deleteTreeNodeParent.getURI())) {
                    model.removeNodeFromParent(deleteTreeNode);
                }
            }
            if (treeType == ISA_TREE) {
                checkMultipleInheritanceNode(targetDeleteNode.getConcept());
                conceptDriftManagementPanel.resetConceptDriftManagementResult(rootNode);
            }
        }
    }

    class DeleteLinkToUpperConceptAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        DeleteLinkToUpperConceptAction(String title) {
            super(title, deleteIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            deleteLinkToUpperConcept();
        }
    }

    class DeleteConceptAction extends AbstractAction {

        private String title;

        DeleteConceptAction(String title) {
            super(title, deleteIcon);
            this.title = title;
            setToolTipText(title);
        }

        public String getTitle() {
            return title;
        }

        public void actionPerformed(ActionEvent e) {
            deleteConcept();
        }
    }

    class AddUndefinedWordListAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        AddUndefinedWordListAction(String title) {
            super(title, addUndefWordIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            Object[] selectedValues = undefinedWordListPanel.getSelectedValues();
            DefaultListModel listModel = undefinedWordListPanel.getModel();
            DefaultListModel viewListModel = undefinedWordListPanel.getViewModel();
            for (int i = 0; i < selectedValues.length; i++) {
                ConceptTreeNode parent = (ConceptTreeNode) conceptTree.getLastSelectedPathComponent();
                if (parent != null) {
                    insertNewConceptTreeNode(selectedValues[i].toString(), parent.getConcept());
                    listModel.removeElement(selectedValues[i]);
                    if (viewListModel != null) {
                        viewListModel.removeElement(selectedValues[i]);
                    }
                }
            }
            undefinedWordListPanel.setTitleWithSize();
        }
    }

    class MoveUndefinedWordListAction extends AbstractAction {

        private String title;

        public String getTitle() {
            return title;
        }

        MoveUndefinedWordListAction(String title) {
            super(title, undefIcon);
            this.title = title;
            setToolTipText(title);
        }

        public void actionPerformed(ActionEvent e) {
            TreePath path = conceptTree.getSelectionPath();
            if (path.getLastPathComponent() != null) {
                ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
                // もう少し，実装をつめる必要あり
                DefaultListModel listModel = undefinedWordListPanel.getModel();
                DefaultListModel viewListModel = undefinedWordListPanel.getViewModel();
                listModel.addElement(conceptTreeNode.toString());
                if (viewListModel != null
                        && conceptTreeNode.toString().matches(undefinedWordListPanel.getSearchRegex())) {
                    viewListModel.addElement(conceptTreeNode.toString());
                }
                deleteConcept();
                undefinedWordListPanel.setTitleWithSize();
            }
        }
    }

    public Map getConceptTypicalWordMap() {
        Map conceptTypicalWordMap = new HashMap();
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            saveConceptTypicalWord(rootNode, conceptTypicalWordMap);
        }
        return conceptTypicalWordMap;
    }

    private void saveConceptTypicalWord(ConceptTreeNode node, Map conceptTypicalWordMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.getInputWord() != null) {
                conceptTypicalWordMap.put(childNode.getURI(), childNode.getInputWord());
            }
            saveConceptTypicalWord(childNode, conceptTypicalWordMap);
        }
    }

    public void loadConceptTypicalWord(Map idTypicalWordMap) {
        DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
        if (model.getRoot() instanceof ConceptTreeNode) {
            ConceptTreeNode rootNode = (ConceptTreeNode) model.getRoot();
            loadConceptTypicalWord(rootNode, idTypicalWordMap);
        }
    }

    private void loadConceptTypicalWord(ConceptTreeNode node, Map conceptTypicalWordMap) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (conceptTypicalWordMap.get(childNode.getURI()) != null) {
                String inputWord = (String) conceptTypicalWordMap.get(childNode.getURI());
                Concept concept = childNode.getConcept();
                concept.setInputLabel(new DODDLELiteral("", inputWord));
                childNode.setConcept(concept);
                ((DefaultTreeModel) conceptTree.getModel()).reload(childNode);
            }
            loadConceptTypicalWord(childNode, conceptTypicalWordMap);
        }
    }

    class ExpandSelectedPathAction extends AbstractAction {

        public ExpandSelectedPathAction(String title) {
            super(title, expandTreeIcon);
        }

        public void actionPerformed(ActionEvent e) {
            conceptTree.expandPath(conceptTree.getSelectionPath());
        }
    }

    class ExpandAllPathAction extends AbstractAction {

        public ExpandAllPathAction(String title) {
            super(title, expandTreeIcon);
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < conceptTree.getRowCount(); i++) {
                conceptTree.expandPath(conceptTree.getPathForRow(i));
            }
        }
    }
}
