package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 * @(#)  2005/07/17
 */

/**
 * @author takeshi morita
 */
public class ConceptInformationPanel extends JPanel implements ActionListener {

    private Concept selectedConcept;

    private JLabel uriLabel;
    private JLabel uriValueLabel;
    private JLabel typicalWordLabel;
    private JLabel typicalWordValueLabel;

    private LabelPanel labelPanel;
    private DescriptionPanel descriptionPanel;

    private JLabel trimmedNodeCntLabel;
    private JLabel trimmedNodeCntValueLabel;

    private JTree conceptTree;
    private ConceptDriftManagementPanel conceptDriftManagementPanel;

    private EDRConceptDefinitionPanel edrConceptDefinitionPanel;

    private void init(JTree tree, DefaultTreeCellRenderer renderer) {
        conceptTree = tree;
        uriLabel = new JLabel("URI: ");
        uriValueLabel = new JLabel("");
        JPanel uriPanel = new JPanel();
        uriPanel.setLayout(new GridLayout(1, 2));
        uriPanel.add(uriLabel);
        uriPanel.add(uriValueLabel);
        typicalWordLabel = new JLabel(Translator.getString("ConceptTreePanel.DisplayWord") + ": ");
        typicalWordValueLabel = new JLabel("");

        JPanel typicalWordPanel = new JPanel();
        typicalWordPanel.setLayout(new GridLayout(1, 2));
        typicalWordPanel.add(typicalWordLabel);
        typicalWordPanel.add(typicalWordValueLabel);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(2, 1, 5, 5));
        northPanel.add(uriPanel);
        northPanel.add(typicalWordPanel);

        labelPanel = new LabelPanel(LiteralPanel.LABEL, this);
        descriptionPanel = new DescriptionPanel(LiteralPanel.DESCRIPTION);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2));
        centerPanel.add(labelPanel);
        centerPanel.add(descriptionPanel);

        trimmedNodeCntLabel = new JLabel(Translator.getString("ConceptTreePanel.TrimmedConceptCount") + "： ");
        trimmedNodeCntValueLabel = new JLabel("");
        JPanel trimmedNodeCntPanel = new JPanel();
        trimmedNodeCntPanel.add(trimmedNodeCntLabel);
        trimmedNodeCntPanel.add(trimmedNodeCntValueLabel);

        setLayout(new BorderLayout());
        add(getWestPanel(northPanel), BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(getWestPanel(trimmedNodeCntPanel), BorderLayout.SOUTH);
        setTreeConfig(renderer);
    }

    public void clearPanel() {
        labelPanel.clearListData();
        descriptionPanel.clearListData();

        uriValueLabel.setText("");
        typicalWordValueLabel.setText("");
        trimmedNodeCntValueLabel.setText("");
    }

    public ConceptInformationPanel(JTree tree, DefaultTreeCellRenderer renderer, ConceptDriftManagementPanel cdmp) {
        init(tree, renderer);
        conceptDriftManagementPanel = cdmp;
    }

    public ConceptInformationPanel(JTree tree, DefaultTreeCellRenderer renderer, EDRConceptDefinitionPanel ecdp,
            ConceptDriftManagementPanel cdmp) {
        edrConceptDefinitionPanel = ecdp;
        init(tree, renderer);
        conceptDriftManagementPanel = cdmp;
    }

    private void setTreeConfig(DefaultTreeCellRenderer renderer) {
        conceptTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path == null) { return; }
                ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
                if (conceptTreeNode != null) {
                    selectedConcept = conceptTreeNode.getConcept();
                    labelPanel.setSelectedConcept(selectedConcept);
                    descriptionPanel.setSelectedConcept(selectedConcept);
                    labelPanel.setLabelLangList();
                    labelPanel.setLabelList();
                    descriptionPanel.setDescriptionLangList();
                    descriptionPanel.setDescriptionList();
                    uriValueLabel.setText(conceptTreeNode.getURI());
                    typicalWordValueLabel.setText(conceptTreeNode.getInputWord());
                    StringBuilder trimmedCntStr = new StringBuilder();
                    for (int trimmedCnt : conceptTreeNode.getTrimmedCountList()) {
                        trimmedCntStr.append(trimmedCnt + ", ");
                    }
                    trimmedNodeCntValueLabel.setText(trimmedCntStr.toString());
                    if (edrConceptDefinitionPanel != null && conceptTreeNode.getConcept() instanceof VerbConcept) {
                        edrConceptDefinitionPanel.init();
                        VerbConcept vc = (VerbConcept) conceptTreeNode.getConcept();
                        edrConceptDefinitionPanel.setDomainList(vc.getDomainSet());
                        edrConceptDefinitionPanel.setRangeList(vc.getRangeSet());
                    }
                    conceptDriftManagementPanel.traAction(conceptTreeNode);
                }
            }
        });

        renderer.setFont(new Font("Dialog", Font.PLAIN, 14));
        conceptTree.setCellRenderer(renderer);
        conceptTree.setEditable(true);
        conceptTree.putClientProperty("JTree.lineStyle", "Angled");
        conceptTree.setVisible(false);
    }

    private JComponent getWestPanel(JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(component, BorderLayout.WEST);
        return panel;
    }

    private void deleteConceptTreeNode() {
        if (conceptTree.getSelectionCount() == 1) {
            DefaultTreeModel model = (DefaultTreeModel) conceptTree.getModel();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) conceptTree.getLastSelectedPathComponent();
            if (node.getParent() != null) {
                model.removeNodeFromParent((DefaultMutableTreeNode) conceptTree.getLastSelectedPathComponent());
            }
        }
    }

    private boolean isSameConcept(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        return concept.getURI().equals(node.getConcept().getURI());
    }

    private void searchSameConceptTreeNode(Concept concept, ConceptTreeNode node, Set sameConceptSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (isSameConcept(concept, childNode, sameConceptSet)) {
                sameConceptSet.add(childNode);
            }
            searchSameConceptTreeNode(concept, childNode, sameConceptSet);
        }
    }

    public void reloadConceptTreeNode(Concept concept) {
        DefaultTreeModel treeModel = (DefaultTreeModel) conceptTree.getModel();
        ConceptTreeNode rootNode = (ConceptTreeNode) treeModel.getRoot();
        Set sameConceptSet = new HashSet();
        searchSameConceptTreeNode(concept, rootNode, sameConceptSet);
        for (Iterator i = sameConceptSet.iterator(); i.hasNext();) {
            ConceptTreeNode node = (ConceptTreeNode) i.next();
            treeModel.reload(node);
        }
    }

    public Concept getSelectedConcept() {
        return selectedConcept;
    }

    public void actionPerformed(ActionEvent e) {
        if (!(conceptTree.getSelectionCount() == 1)) { return; }
        TreePath path = conceptTree.getSelectionPath();
        ConceptTreeNode conceptTreeNode = (ConceptTreeNode) path.getLastPathComponent();
        Concept concept = conceptTreeNode.getConcept();
        selectedConcept = concept;
        labelPanel.setSelectedConcept(selectedConcept);
        descriptionPanel.setSelectedConcept(selectedConcept);

        conceptTreeNode.setConcept(concept);
    }

    /**
     * @param concept
     * @param label
     */
    public void setTypicalWord(Concept concept, DODDLELiteral label) {
        concept.setInputLabel(label);
        typicalWordValueLabel.setText(label.getString());
        reloadConceptTreeNode(concept);
    }
}
