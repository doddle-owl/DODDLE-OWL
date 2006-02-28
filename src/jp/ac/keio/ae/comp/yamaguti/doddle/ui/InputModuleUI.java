package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

import org.apache.log4j.*;

/*
 * Created on 2004/08/22
 *
 */

/**
 * @author takeshi morita
 * 
 */
public class InputModuleUI extends JPanel implements ListSelectionListener, ActionListener, TreeSelectionListener {

    private Set<Concept> inputConceptSet; // ����ID�̃Z�b�g
    private Set<InputWordModel> inputWordModelSet; // ���͒P�ꃂ�f���̃Z�b�g
    private Map<String, Set<Concept>> wordConceptSetMap; // ���͒P��Ɠ��͒P������o�����܂�ID�̃}�b�s���O
    private Map<String, Concept> wordConceptMap; // ���͒P��ƓK�؂ɑΉ�����ID�̃}�b�s���O
    private Map<String, Set<EvalConcept>> wordEvalConceptSetMap;
    private Map<InputWordModel, ConstructTreeOption> complexConstructTreeOptionMap;

    private TitledBorder perfectMatchedWordJListTitle;
    private TitledBorder partialMatchedWordJListTitle;

    private JTabbedPane wordListTabbedPane;

    private JTextField searchWordField;
    private JButton searchWordButton;

    private JList perfectMatchedWordJList; // ���S�ƍ������P�ꃊ�X�g
    private Set<InputWordModel> perfectMatchedWordModelSet;
    private JList partialMatchedWordJList; // �����ƍ������P�ꃊ�X�g
    private Set<InputWordModel> partialMatchedWordModelSet;
    private JList conceptSetJList;
    private UndefinedWordListPanel undefinedWordListPanel;

    private DefaultListModel undefinedWordListModel;

    private JCheckBox perfectMatchedAmbiguityCntCheckBox;
    private JCheckBox perfectMatchedShowOnlyRelatedComplexWordsCheckBox;
    private JCheckBox perfectMatchedIsSyncCheckBox;

    private JCheckBox partialMatchedComplexWordCheckBox;
    private JCheckBox partialMatchedMatchedWordBox;
    private JCheckBox partialMatchedAmbiguityCntCheckBox;

    private JList jpWordList;
    private JList enWordList;
    private JTextArea jpExplanationArea;
    private JTextArea enExplanationArea;

    private JRadioButton addAsSubConceptRadioButton;
    private JRadioButton addAsSameConceptRadioButton;

    private JList hilightPartJList;
    // private JEditorPane documentArea;
    private JTextArea documentArea;
    private JCheckBox viewHilightCheckBox;
    private JLabel supNodeNumLabel;
    private JTextField supNodeNumField;
    private JCheckBox showAroundConceptTreeCheckBox;
    private JTree aroundConceptTree;
    private TreeModel aroundConceptTreeModel;

    private InputModule inputModule;
    private ConstructConceptTreePanel constructConceptTreePanel;
    private ConstructPropertyTreePanel constructPropertyTreePanel;

    private JButton automaticCancelAmbiguityButton;
    private JButton constructNounTreeButton;
    private JButton constructNounAndVerbTreeButton;
    private JButton showConceptDescriptionButton;

    private AutomaticDisAmbiguationAction automaticDisAmbiguationAction;
    private ConstructTreeAction constructNounTreeAction;
    private ConstructTreeAction constructNounAndVerbTreeAction;

    private Action saveCompleteMatchWordAction;
    private Action saveCompleteMatchWordWithComplexWordAcion;

    private ConceptDescriptionFrame conceptDescriptionFrame;

    private DocumentSelectionPanel docSelectionPanel;

    private DODDLEProject project;

    public InputModuleUI(ConstructConceptTreePanel tp, ConstructPropertyTreePanel pp, DODDLEProject p) {
        project = p;
        constructConceptTreePanel = tp;
        constructPropertyTreePanel = pp;
        inputModule = new InputModule(project);
        wordConceptMap = new HashMap<String, Concept>();
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();

        conceptDescriptionFrame = new ConceptDescriptionFrame();

        conceptSetJList = new JList();
        conceptSetJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conceptSetJList.addListSelectionListener(this);
        JScrollPane conceptJListScroll = new JScrollPane(conceptSetJList);
        conceptJListScroll.setBorder(BorderFactory.createTitledBorder("�T�O���X�g"));

        jpWordList = new JList();
        JScrollPane jpWordsAreaScroll = new JScrollPane(jpWordList);
        jpWordsAreaScroll.setBorder(BorderFactory.createTitledBorder("���{�ꌩ�o��"));
        enWordList = new JList();
        JScrollPane enWordsAreaScroll = new JScrollPane(enWordList);
        enWordsAreaScroll.setBorder(BorderFactory.createTitledBorder("�p�ꌩ�o��"));
        jpExplanationArea = new JTextArea();
        jpExplanationArea.setLineWrap(true);
        JScrollPane jpExplanationAreaScroll = new JScrollPane(jpExplanationArea);
        jpExplanationAreaScroll.setBorder(BorderFactory.createTitledBorder("���{�����"));
        enExplanationArea = new JTextArea();
        enExplanationArea.setLineWrap(true);
        JScrollPane enExplanationAreaScroll = new JScrollPane(enExplanationArea);
        enExplanationAreaScroll.setBorder(BorderFactory.createTitledBorder("�p�����"));

        addAsSameConceptRadioButton = new JRadioButton("����T�O", true);
        addAsSameConceptRadioButton.addActionListener(this);
        addAsSubConceptRadioButton = new JRadioButton("���ʊT�O");
        addAsSubConceptRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(addAsSameConceptRadioButton);
        group.add(addAsSubConceptRadioButton);
        JPanel constructTreeOptionPanel = new JPanel();
        constructTreeOptionPanel.setLayout(new GridLayout(1, 2));
        constructTreeOptionPanel.setBorder(BorderFactory.createTitledBorder("�K�w�\�z�I�v�V����"));
        constructTreeOptionPanel.add(addAsSameConceptRadioButton);
        constructTreeOptionPanel.add(addAsSubConceptRadioButton);

        JPanel explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridLayout(5, 1, 5, 5));
        explanationPanel.add(jpWordsAreaScroll);
        explanationPanel.add(enWordsAreaScroll);
        explanationPanel.add(jpExplanationAreaScroll);
        explanationPanel.add(enExplanationAreaScroll);
        explanationPanel.add(constructTreeOptionPanel);

        undefinedWordListPanel = new UndefinedWordListPanel();

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(1, 3, 5, 5));
        listPanel.add(conceptJListScroll);
        listPanel.add(explanationPanel);
        listPanel.add(undefinedWordListPanel);

        hilightPartJList = new JList();
        hilightPartJList.addListSelectionListener(this);
        JScrollPane hilightPartJListScroll = new JScrollPane(hilightPartJList);
        hilightPartJListScroll.setBorder(BorderFactory.createTitledBorder("�s�ԍ�"));
        hilightPartJListScroll.setPreferredSize(new Dimension(100, 100));
        // documentArea = new JEditorPane("text/html", "");
        documentArea = new JTextArea();
        documentArea.setEditable(false);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        documentAreaScroll.setBorder(BorderFactory.createTitledBorder("���͕���"));
        viewHilightCheckBox = new JCheckBox("���͒P����n�C���C�g", false);

        supNodeNumLabel = new JLabel("��ʊT�O��");
        supNodeNumField = new JTextField(10);
        showAroundConceptTreeCheckBox = new JCheckBox("���[�g�m�[�h�܂ŕ\��", false);

        aroundConceptTreeModel = new DefaultTreeModel(null);
        aroundConceptTree = new JTree(aroundConceptTreeModel);
        aroundConceptTree.addTreeSelectionListener(this);
        aroundConceptTree.setEditable(false);
        aroundConceptTree.setCellRenderer(new AroundTreeCellRenderer());
        JScrollPane aroundConceptTreeScroll = new JScrollPane(aroundConceptTree);
        aroundConceptTreeScroll.setBorder(BorderFactory.createTitledBorder("�T�O�K�w"));

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(aroundConceptTreeScroll, BorderLayout.CENTER);
        treePanel.add(showAroundConceptTreeCheckBox, BorderLayout.SOUTH);

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);
        documentPanel.add(viewHilightCheckBox, BorderLayout.SOUTH);
        // documentPanel.add(hilightPartJListScroll, BorderLayout.WEST);

        JPanel referencePanel = new JPanel();
        referencePanel.setLayout(new GridLayout(1, 2));
        referencePanel.add(documentPanel);
        referencePanel.add(treePanel);

        automaticDisAmbiguationAction = new AutomaticDisAmbiguationAction("���`������");
        automaticCancelAmbiguityButton = new JButton(automaticDisAmbiguationAction);
        // showConceptDescriptionButton = new JButton(new
        // ShowConceptDescriptionAction("�T�O�L�q��\��"));

        JPanel p1 = new JPanel();
        p1.add(automaticCancelAmbiguityButton);
        // p1.add(showConceptDescriptionButton);

        constructNounTreeAction = new ConstructTreeAction("�T�O�K�w�\�z�i�����j", false);
        constructNounTreeButton = new JButton(constructNounTreeAction);
        constructNounAndVerbTreeAction = new ConstructTreeAction("�T�O�K�w�\�z�i��������ѓ����j", true);
        constructNounAndVerbTreeButton = new JButton(constructNounAndVerbTreeAction);
        JPanel p2 = new JPanel();
        p2.add(constructNounTreeButton);
        p2.add(constructNounAndVerbTreeButton);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(p1, BorderLayout.WEST);
        buttonPanel.add(p2, BorderLayout.EAST);

        setLayout(new BorderLayout());
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, referencePanel);
        verticalSplitPane.setOneTouchExpandable(true);
        verticalSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getWordListPanel(),
                verticalSplitPane);
        horizontalSplitPane.setOneTouchExpandable(true);
        horizontalSplitPane.setDividerSize(DODDLE.DIVIDER_SIZE);
        add(horizontalSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel getWordListPanel() {
        JPanel perfectMatchedWordListPanel = getPerfectMatchedWordListPanel();
        JPanel partialMatchedWordListPanel = getPartialMatchedWordListPanel();

        wordListTabbedPane = new JTabbedPane();
        wordListTabbedPane.add("���S�ƍ�", perfectMatchedWordListPanel);
        wordListTabbedPane.add("�����ƍ�", partialMatchedWordListPanel);

        JPanel wordListPanel = new JPanel();
        wordListPanel.setLayout(new BorderLayout());
        wordListPanel.add(getSearchWordPanel(), BorderLayout.NORTH);
        wordListPanel.add(wordListTabbedPane, BorderLayout.CENTER);

        return wordListPanel;
    }

    private JPanel getSearchWordPanel() {
        searchWordField = new JTextField();
        searchWordField.addActionListener(this);
        searchWordButton = new JButton("����");
        searchWordButton.addActionListener(this);
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchWordField, BorderLayout.CENTER);
        searchPanel.add(searchWordButton, BorderLayout.EAST);
        return searchPanel;
    }

    private JPanel getPerfectMatchedWordListPanel() {
        perfectMatchedWordJList = new JList();
        perfectMatchedWordJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        perfectMatchedWordJList.addListSelectionListener(this);
        JScrollPane perfectMatchedWordListScroll = new JScrollPane(perfectMatchedWordJList);
        perfectMatchedWordJListTitle = BorderFactory.createTitledBorder("���S�ƍ� �P�ꃊ�X�g");
        perfectMatchedWordListScroll.setBorder(perfectMatchedWordJListTitle);

        perfectMatchedAmbiguityCntCheckBox = new JCheckBox("���`����", true);
        perfectMatchedAmbiguityCntCheckBox.addActionListener(this);
        perfectMatchedShowOnlyRelatedComplexWordsCheckBox = new JCheckBox("�Ή����镡����̂ݕ\��", false);
        perfectMatchedShowOnlyRelatedComplexWordsCheckBox.addActionListener(this);
        perfectMatchedIsSyncCheckBox = new JCheckBox("�����ƍ��P�ꃊ�X�g�Ƒ��`���������ʂ����L", true);
        JPanel perfectMatchedFilterPanel = new JPanel();
        perfectMatchedFilterPanel.setLayout(new GridLayout(3, 1));
        perfectMatchedFilterPanel.add(perfectMatchedAmbiguityCntCheckBox);
        perfectMatchedFilterPanel.add(perfectMatchedShowOnlyRelatedComplexWordsCheckBox);
        perfectMatchedFilterPanel.add(perfectMatchedIsSyncCheckBox);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(perfectMatchedWordListScroll, BorderLayout.CENTER);
        panel.add(perfectMatchedFilterPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel getPartialMatchedWordListPanel() {
        partialMatchedWordJList = new JList();
        partialMatchedWordJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partialMatchedWordJList.addListSelectionListener(this);
        JScrollPane partialMatchedWordListScroll = new JScrollPane(partialMatchedWordJList);
        partialMatchedWordJListTitle = BorderFactory.createTitledBorder("�����ƍ� �P�ꃊ�X�g");
        partialMatchedWordListScroll.setBorder(partialMatchedWordJListTitle);

        partialMatchedComplexWordCheckBox = new JCheckBox("������", true);
        partialMatchedComplexWordCheckBox.addActionListener(this);
        partialMatchedMatchedWordBox = new JCheckBox("�ƍ�����", true);
        partialMatchedMatchedWordBox.addActionListener(this);
        partialMatchedAmbiguityCntCheckBox = new JCheckBox("���`����", true);
        partialMatchedAmbiguityCntCheckBox.addActionListener(this);
        JPanel partialMatchedFilterPanel = new JPanel();
        partialMatchedFilterPanel.setLayout(new GridLayout(2, 2));
        partialMatchedFilterPanel.add(partialMatchedComplexWordCheckBox);
        partialMatchedFilterPanel.add(partialMatchedMatchedWordBox);
        partialMatchedFilterPanel.add(partialMatchedAmbiguityCntCheckBox);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(partialMatchedWordListScroll, BorderLayout.CENTER);
        panel.add(partialMatchedFilterPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void setDocumentSelectionPanel(DocumentSelectionPanel p) {
        docSelectionPanel = p;
    }

    public void selectTopList() {
        if (perfectMatchedWordJList.getModel().getSize() != 0) {
            perfectMatchedWordJList.setSelectedIndex(0);
        } else {
            enWordList.setListData(new Object[0]);
            jpWordList.setListData(new Object[0]);
            enExplanationArea.setText("");
            jpExplanationArea.setText("");
            addAsSameConceptRadioButton.setEnabled(false);
            addAsSubConceptRadioButton.setEnabled(false);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = aroundConceptTree.getSelectionPath();
        if (path != null) {
            ConceptTreeNode node = (ConceptTreeNode) path.getLastPathComponent();
            enWordList.setListData(node.getEnWords());
            jpWordList.setListData(node.getJpWords());
            enExplanationArea.setText(node.getEnExplanation());
            jpExplanationArea.setText(node.getJpExplanation());
        }
    }

    private void saveCompoundOption(String option) {
        JList wordJList = getTargetWordJList();
        InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
        if (iwModel != null && iwModel.isPartialMatchWord()) {
            ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
            ctOption.setOption(option);
            complexConstructTreeOptionMap.put(iwModel, ctOption);
        }
    }

    private void showOnlyRelatedComplexWords() {
        if (perfectMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
            InputWordModel targetIWModel = (InputWordModel) perfectMatchedWordJList.getSelectedValue();
            if (targetIWModel == null) { return; }
            Set searchedPartialMatchedWordModelSet = new TreeSet();
            for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (iwModel.getMatchedWord().equals(targetIWModel.getMatchedWord())) {
                    searchedPartialMatchedWordModelSet.add(iwModel);
                }
            }
            partialMatchedWordJList.setListData(searchedPartialMatchedWordModelSet.toArray());
            partialMatchedWordJListTitle.setTitle("�����ƍ� �P�ꃊ�X�g (" + searchedPartialMatchedWordModelSet.size() + "/"
                    + partialMatchedWordModelSet.size() + ")");
        } else {
            partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
            partialMatchedWordJListTitle.setTitle("�����ƍ� �P�ꃊ�X�g (" + partialMatchedWordModelSet.size() + ")");
        }
        perfectMatchedWordJList.repaint();
        partialMatchedWordJList.repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == perfectMatchedWordJList || e.getSource() == partialMatchedWordJList) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == addAsSameConceptRadioButton) {
            saveCompoundOption("SAME");
        } else if (e.getSource() == addAsSubConceptRadioButton) {
            saveCompoundOption("SUB");
        } else if (e.getSource() == perfectMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedComplexWordCheckBox || e.getSource() == partialMatchedMatchedWordBox) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == perfectMatchedShowOnlyRelatedComplexWordsCheckBox) {
            showOnlyRelatedComplexWords();
        } else if (e.getSource() == searchWordButton || e.getSource() == searchWordField) {
            String keyWord = searchWordField.getText();
            if (keyWord.length() == 0) {
                perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle("���S�ƍ� �P�ꃊ�X�g (" + perfectMatchedWordModelSet.size() + ")");
                partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle("�����ƍ� �P�ꃊ�X�g (" + partialMatchedWordModelSet.size() + ")");
            } else {
                Set searchedPerfectMatchedWordModelSet = new TreeSet();
                Set searchedPartialMatchedWordModelSet = new TreeSet();
                for (Iterator i = perfectMatchedWordModelSet.iterator(); i.hasNext();) {
                    InputWordModel iwModel = (InputWordModel) i.next();
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        searchedPerfectMatchedWordModelSet.add(iwModel);
                    }
                }
                InputWordModel targetIWModel = (InputWordModel) perfectMatchedWordJList.getSelectedValue();
                for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
                    InputWordModel iwModel = (InputWordModel) i.next();
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        if (perfectMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
                            if (iwModel.getMatchedWord().equals(targetIWModel.getMatchedWord())) {
                                searchedPartialMatchedWordModelSet.add(iwModel);
                            }
                        } else {
                            searchedPartialMatchedWordModelSet.add(iwModel);
                        }
                    }
                }
                perfectMatchedWordJList.setListData(searchedPerfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle("���S�ƍ� �P�ꃊ�X�g (" + searchedPerfectMatchedWordModelSet.size() + "/"
                        + perfectMatchedWordModelSet.size() + ")");
                partialMatchedWordJList.setListData(searchedPartialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle("�����ƍ� �P�ꃊ�X�g (" + searchedPartialMatchedWordModelSet.size() + "/"
                        + partialMatchedWordModelSet.size() + ")");
            }
            wordListTabbedPane.repaint();
        }
    }

    public boolean isPerfectMatchedAmbiguityCntCheckBox() {
        return perfectMatchedAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPartialMatchedAmbiguityCntCheckBox() {
        return partialMatchedAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPartialMatchedComplexWordCheckBox() {
        return partialMatchedComplexWordCheckBox.isSelected();
    }

    public boolean isPartialMatchedMatchedWordBox() {
        return partialMatchedMatchedWordBox.isSelected();
    }

    public ConstructTreeAction getConstructNounTreeAction() {
        return constructNounTreeAction;
    }

    public ConstructTreeAction getConstructNounAndVerbTreeAction() {
        return constructNounAndVerbTreeAction;
    }

    public Action getSaveCompleteMatchWordAction() {
        return saveCompleteMatchWordAction;
    }

    public Action getSaveCompleteMatchWordWithComplexWordAction() {
        return saveCompleteMatchWordWithComplexWordAcion;
    }

    public Map<String, Concept> getWordConceptMap() {
        return wordConceptMap;
    }

    public Map<String, Set<Concept>> getWordConceptSetMap() {
        return wordConceptSetMap;
    }

    public void loadWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            loadWordConceptMap(chooser.getSelectedFile());
        }
    }

    public void loadWordConceptMap(File file) {
        if (!file.exists()) { return; }
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            Set<String> inputWordSet = new HashSet<String>();
            while (inputWordModelSet == null) {
                try {
                    Thread.sleep(1000);
                    // System.out.println("sleep");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (InputWordModel iwModel : inputWordModelSet) {
                inputWordSet.add(iwModel.getWord());
            }
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] wordID = line.replaceAll("\n", "").split(",");
                if (0 < wordID[0].length()) {
                    String word = wordID[0];
                    String id = wordID[1];
                    InputWordModel iwModel = inputModule.makeInputWordModel(word);
                    if (inputWordSet.contains(iwModel.getWord())) {
                        wordConceptMap.put(iwModel.getWord(), getConcept(id));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void setInputConceptSet() {
        inputConceptSet = new HashSet<Concept>();
        for (InputWordModel iwModel : inputWordModelSet) {
            Concept c = wordConceptMap.get(iwModel.getWord());
            if (c == null) {
                if (iwModel.isPartialMatchWord()) {
                    c = wordConceptMap.get(iwModel.getMatchedWord());
                    wordConceptMap.put(iwModel.getWord(), c);
                }
                if (c == null) {
                    Set<Concept> conceptSet = wordConceptSetMap.get(iwModel.getMatchedWord());
                    if (conceptSet != null) {
                        c = (Concept) conceptSet.toArray()[0];
                        wordConceptMap.put(iwModel.getWord(), c);
                    }
                }
            }
            if (c.equals(nullConcept)) {
                complexConstructTreeOptionMap.remove(iwModel);
                continue;
            }
            if (iwModel.isPartialMatchWord()) {
                ConstructTreeOption ctOption = new ConstructTreeOption(c);
                complexConstructTreeOptionMap.put(iwModel, ctOption);
            }
            Concept edrConcept = EDRDic.getEDRConcept(c.getId());
            if (edrConcept != null) {
                edrConcept.setInputWord(iwModel.getMatchedWord()); // ���C���ƂȂ錩�o����ݒ肷��
            }
            inputConceptSet.add(c);
        }
    }

    private void setInputConceptSetWithDB() {
        inputConceptSet = null;
        EDRDic.getEDRDBManager().setConceptSet(wordConceptMap, complexConstructTreeOptionMap, wordConceptSetMap,
                inputWordModelSet);
        while (inputConceptSet == null) {
            inputConceptSet = EDRDic.getEDRDBManager().getConceptSet();
            try {
                Thread.sleep(1000);
                // System.out.println("sleep");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Concept nullConcept = new Concept("null", "�Y���Ȃ�");
    private static EvalConcept nullEvalConcept = new EvalConcept(null, -1);

    private void selectAmbiguousConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty()) {
            String orgWord = ((InputWordModel) wordJList.getSelectedValue()).getWord();
            String selectedWord = ((InputWordModel) wordJList.getSelectedValue()).getMatchedWord();
            Set<Concept> conceptSet = wordConceptSetMap.get(selectedWord);

            Set evalConceptSet = null;
            if (DODDLE.IS_USING_DB) {
                wordEvalConceptSetMap = EDRDic.getEDRDBManager().getWordEvalConceptSetMap();
            }

            if (!(wordEvalConceptSetMap == null || wordEvalConceptSetMap.get(selectedWord) == null)) {
                evalConceptSet = wordEvalConceptSetMap.get(selectedWord);
            } else {
                evalConceptSet = getEvalConceptSet(conceptSet);
            }
            evalConceptSet.add(nullEvalConcept);

            conceptSetJList.setListData(evalConceptSet.toArray());
            Concept correctConcept = wordConceptMap.get(orgWord);
            if (correctConcept != null) {
                if (correctConcept.equals(nullConcept)) {
                    conceptSetJList.setSelectedValue(nullEvalConcept, true);
                    addAsSameConceptRadioButton.setEnabled(false);
                    addAsSubConceptRadioButton.setEnabled(false);
                    return;
                }
                for (Iterator i = evalConceptSet.iterator(); i.hasNext();) {
                    EvalConcept evalConcept = (EvalConcept) i.next();
                    if (evalConcept.getConcept() == null) {
                        continue;
                    }
                    if (correctConcept.equals(evalConcept.getConcept())) {
                        conceptSetJList.setSelectedValue(evalConcept, true);
                    }
                }
            } else {
                conceptSetJList.setSelectedIndex(0);
                EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                wordConceptMap.put(orgWord, evalConcept.getConcept());
            }
            // hilightSelectedWord(selectedWord);
            hilightSelectedWord(orgWord);

            // ���S�ƍ��̏ꍇ�́C�K�w�\�z�I�v�V�����p�l���𖳌��ɂ���
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            if (iwModel.isPartialMatchWord()) {
                addAsSameConceptRadioButton.setEnabled(true);
                addAsSubConceptRadioButton.setEnabled(true);
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    if (ctOption.getOption().equals("SAME")) {
                        addAsSameConceptRadioButton.setSelected(true);
                    } else {
                        addAsSubConceptRadioButton.setSelected(true);
                    }
                } else {
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    ctOption = new ConstructTreeOption(evalConcept.getConcept());
                    complexConstructTreeOptionMap.put(iwModel, ctOption);
                    addAsSameConceptRadioButton.setSelected(true);
                }
            } else {
                addAsSameConceptRadioButton.setEnabled(false);
                addAsSubConceptRadioButton.setEnabled(false);
            }
        }
    }

    private void hilightSelectedWord(String word) {
        if (viewHilightCheckBox.isSelected()) {
            String targetLines = docSelectionPanel.getTargetTextLines(word);
            // String targetLines = docSelectionPanel.getTargetHtmlLines(word);
            documentArea.setText(targetLines);
        }
    }

    /**
     * @param conceptSet
     * @return
     */
    private Set getEvalConceptSet(Set<Concept> conceptSet) {
        Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
        for (Concept c : conceptSet) {
            evalConceptSet.add(new EvalConcept(c, 0));
        }
        return evalConceptSet;
    }

    private JList getTargetWordJList() {
        int selectedIndex = wordListTabbedPane.getSelectedIndex();
        if (selectedIndex == 0) {
            return perfectMatchedWordJList;
        } else if (selectedIndex == 1) { return partialMatchedWordJList; }
        return null;
    }

    private void syncPartialMatchedAmbiguousConceptSet(String orgWord, Concept c) {
        if (!perfectMatchedIsSyncCheckBox.isSelected()) { return; }
        for (Iterator i = partialMatchedWordModelSet.iterator(); i.hasNext();) {
            InputWordModel iwModel = (InputWordModel) i.next();
            if (iwModel.getMatchedWord().equals(orgWord)) {
                wordConceptMap.put(iwModel.getWord(), c);
            }
        }
    }

    private void selectCorrectConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty() && !conceptSetJList.isSelectionEmpty()) {
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
            String word = iwModel.getWord();
            if (evalConcept == nullEvalConcept) {
                wordConceptMap.put(word, nullConcept);
                syncPartialMatchedAmbiguousConceptSet(word, nullConcept);
                jpWordList.setListData(new Object[0]);
                enWordList.setListData(new Object[0]);
                jpExplanationArea.setText("");
                enExplanationArea.setText("");
                aroundConceptTree.setModel(new DefaultTreeModel(null));
                return;
            }
            Concept c = evalConcept.getConcept();
            wordConceptMap.put(word, c);
            syncPartialMatchedAmbiguousConceptSet(word, c);
            Concept edrConcept = evalConcept.getConcept();
            jpWordList.setListData(edrConcept.getJaWords());
            enWordList.setListData(edrConcept.getEnWords());
            jpExplanationArea.setText(edrConcept.getJaExplanation());
            enExplanationArea.setText(edrConcept.getEnExplanation());

            if (showAroundConceptTreeCheckBox.isSelected()) {
                EvalConcept ec = (EvalConcept) conceptSetJList.getSelectedValue();
                Set pathSet = null;
                if (ec.getConcept().getPrefix().equals("edr")) {
                    pathSet = EDRTree.getEDRTree().getPathToRootSet(ec.getConcept().getId());
                } else if (ec.getConcept().getPrefix().equals("edrt")) {
                    pathSet = EDRTree.getEDRTTree().getPathToRootSet(ec.getConcept().getId());
                } else if (ec.getConcept().getPrefix().equals("wn")) {
                    pathSet = WordNetDic.getPathToRootSet(new Long(ec.getConcept().getId()));
                }
                TreeModel model = constructConceptTreePanel.getDefaultConceptTreeModel(pathSet);
                aroundConceptTree.setModel(model);
                for (int i = 0; i < aroundConceptTree.getRowCount(); i++) {
                    aroundConceptTree.expandPath(aroundConceptTree.getPathForRow(i));
                }
            } else {
                aroundConceptTree.setModel(new DefaultTreeModel(null));
            }

            ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
            if (ctOption != null) {
                ctOption.setConcept(evalConcept.getConcept());
                complexConstructTreeOptionMap.put(iwModel, ctOption);
            }
        }
    }

    private final ImageIcon bestMatchIcon = Utils.getImageIcon("class_best_match_icon.png");
    private final ImageIcon ConceptNodeIcon = Utils.getImageIcon("class_sin_icon.png");

    public class AroundTreeCellRenderer extends DefaultTreeCellRenderer {

        public AroundTreeCellRenderer() {
            setOpaque(true);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {

            Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                    hasFocus);

            setText(value.toString());

            if (selected) {
                setBackground(new Color(0, 0, 128));
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            if (value.getClass().equals(ConceptTreeNode.class)) {
                ConceptTreeNode node = (ConceptTreeNode) value;
                if (node.isLeaf()) {
                    setIcon(bestMatchIcon);
                } else {
                    setIcon(ConceptNodeIcon);
                }
            }
            return component;
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == perfectMatchedWordJList) {
            selectAmbiguousConcept(perfectMatchedWordJList);
            showOnlyRelatedComplexWords();
        } else if (e.getSource() == partialMatchedWordJList) {
            selectAmbiguousConcept(partialMatchedWordJList);
        } else if (e.getSource() == conceptSetJList) {
            selectCorrectConcept(getTargetWordJList());
        } else if (e.getSource() == hilightPartJList) {
            jumpHilightPart();
        }
    }

    private void jumpHilightPart() {
        Integer lineNum = (Integer) hilightPartJList.getSelectedValue();
        Rectangle rect = documentArea.getVisibleRect();
        rect.y = 0;
        documentArea.scrollRectToVisible(rect);
        int lineHeight = documentArea.getFontMetrics(documentArea.getFont()).getHeight();
        // System.out.println(lineHeight);
        rect.y = (lineNum.intValue() + 1) * lineHeight;
        documentArea.scrollRectToVisible(rect);
    }

    private File inputFile;

    private void initWordList() {
        inputWordModelSet = inputModule.getInputWordModelSet();

        perfectMatchedWordModelSet = new TreeSet<InputWordModel>();
        partialMatchedWordModelSet = new TreeSet<InputWordModel>();
        for (Iterator i = inputWordModelSet.iterator(); i.hasNext();) {
            InputWordModel iwModel = (InputWordModel) i.next();
            if (iwModel.isPartialMatchWord()) {
                partialMatchedWordModelSet.add(iwModel);
            } else {
                perfectMatchedWordModelSet.add(iwModel);
            }
        }
        perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
        perfectMatchedWordJListTitle.setTitle("���S�ƍ� �P�ꃊ�X�g (" + perfectMatchedWordModelSet.size() + ")");
        partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
        partialMatchedWordJListTitle.setTitle("�����ƍ� �P�ꃊ�X�g (" + partialMatchedWordModelSet.size() + ")");

        wordConceptSetMap = inputModule.getWordConceptSetMap();

        Set undefinedSet = inputModule.getUndefinedWordSet();
        DefaultListModel listModel = undefinedWordListPanel.getModel();
        listModel.clear();
        for (Iterator i = undefinedSet.iterator(); i.hasNext();) {
            listModel.addElement(i.next());
        }
        undefinedWordListPanel.setTitleWithSize();
        repaint(); // titledBorder�̃^�C�g�����ĕ\�������邽��
    }

    public void loadInputWordSet(File file) {
        if (!file.exists()) { return; }
        inputFile = file;
        Set<String> wordSet = new HashSet<String>();
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "JISAutoDetect"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String word = line.replaceAll("\n", "");
                wordSet.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadInputWordSet(wordSet);
    }

    private Set<String> wordSet;

    public void loadInputWordSet(Set<String> ws) {
        wordSet = ws;
        perfectMatchedWordJList.clearSelection();
        partialMatchedWordJList.clearSelection();
        undefinedWordListPanel.clearSelection();
        complexConstructTreeOptionMap.clear();

        if (DODDLE.IS_USING_DB) {
            inputModule.initDataWithDB(wordSet);
            initWordList();
        } else {
            inputModule.initDataWithMem(wordSet);
            initWordList();
        }
    }

    public Set<Concept> getInputConceptSet() {
        return inputConceptSet;
    }

    public Set<InputWordModel> getInputWordModelSet() {
        return inputModule.getInputWordModelSet();
    }

    public AutomaticDisAmbiguationAction getAutomaticDisAmbiguationAction() {
        return automaticDisAmbiguationAction;
    }

    public class ShowConceptDescriptionAction extends AbstractAction {

        public ShowConceptDescriptionAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent e) {
            EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
            if (evalConcept != null) {
                conceptDescriptionFrame.setConcept(evalConcept.getConcept().getId());
                if (DODDLE.IS_USING_DB) {
                    setInputConceptSetWithDB();
                } else {
                    setInputConceptSet();
                }
                conceptDescriptionFrame.setInputConceptSet();
                conceptDescriptionFrame.setVisible(true);
            }
        }
    }

    class ConceptDescriptionFrame extends JFrame {

        private ConceptDescriptionUI conceptDescrptionPanel;

        ConceptDescriptionFrame() {
            setBounds(50, 50, 800, 600);
            conceptDescrptionPanel = new ConceptDescriptionUI();
            Container contentPane = getContentPane();
            contentPane.add(conceptDescrptionPanel);
        }

        public void setConcept(String id) {
            conceptDescrptionPanel.setConcept(id);
        }

        public void setInputConceptSet() {
            conceptDescrptionPanel.setInputConceptSet(inputConceptSet);
        }
    }

    public class AutomaticDisAmbiguationAction extends AbstractAction {

        private Set<String> wordSet;

        public AutomaticDisAmbiguationAction(String title) {
            super(title);
        }

        /**
         * 
         * ���`���̂���T�O���X�g�Ɠ��͌�b����͂Ƃ��āC�]���l���T�O���X�g��Ԃ����\�b�h
         * 
         */
        public void setWordEvalConceptSetMap(Set<InputWordModel> inputWordSet) {
            wordSet = new HashSet<String>();
            for (InputWordModel iwModel : inputWordModelSet) {
                wordSet.add(iwModel.getMatchedWord());
            }
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();

            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(wordSet.size());
            for (Iterator i = wordSet.iterator(); i.hasNext();) {
                String inputWord = (String) i.next();
                Set<Concept> conceptSet = wordConceptSetMap.get(inputWord);
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (Concept c : conceptSet) {
                    int evalValue = 0;
                    if (OptionDialog.isCheckSupConcepts()) {
                        evalValue += cntRelevantSupConcepts(c.getId());
                    }
                    if (OptionDialog.isCheckSubConcepts()) {
                        evalValue += cntRelevantSubConcepts(c.getId());
                    }
                    if (OptionDialog.isCheckSiblingConcepts()) {
                        evalValue += cntRelevantSiblingConcepts(c.getId());
                    }
                    evalConceptSet.add(new EvalConcept(c, evalValue));
                }
                wordEvalConceptSetMap.put(inputWord, evalConceptSet);
                DODDLE.STATUS_BAR.addValue();
            }
            DODDLE.STATUS_BAR.hideProgressBar();
        }

        private int getMaxEvalValue(Set idsSet, String id) {
            int maxEvalValue = 0;
            for (Iterator i = idsSet.iterator(); i.hasNext();) {
                Collection idSet = (Collection) i.next();
                int evalValue = 0;
                for (Iterator j = idSet.iterator(); j.hasNext();) {
                    String sid = (String) j.next();
                    if (sid.equals(id)) {
                        continue;
                    }
                    Concept c = EDRDic.getEDRConcept(sid);
                    if (isIncludeInputWords(wordSet, c)) {
                        evalValue++;
                    }
                }
                if (maxEvalValue < evalValue) {
                    maxEvalValue = evalValue;
                }
            }
            return maxEvalValue;
        }

        private int cntRelevantSiblingConcepts(String id) {
            return getMaxEvalValue(EDRTree.getEDRTree().getSiblingIDsSet(id), id);
        }

        private int cntRelevantSupConcepts(String id) {
            return getMaxEvalValue(EDRTree.getEDRTree().getPathToRootSet(id), id);
        }

        private int cntRelevantSubConcepts(String id) {
            return getMaxEvalValue(EDRTree.getEDRTree().getSubIDsSet(id), id);
        }

        private boolean isIncludeInputWords(Set wordSet, Concept c) {
            if (c == null) { return false; }
            String[] jpWords = c.getJaWords();
            for (int j = 0; j < jpWords.length; j++) {
                if (wordSet.contains(jpWords[j])) { return true; }
            }
            String[] enWords = c.getEnWords();
            for (int j = 0; j < enWords.length; j++) {
                if (wordSet.contains(enWords[j])) { return true; }
            }
            if (wordSet.contains(c.getJaExplanation())) { return true; }
            if (wordSet.contains(c.getEnExplanation())) { return true; }
            return false;
        }

        public void doDisAmbiguation() {
            new Thread() {
                public void run() {
                    if (DODDLE.IS_USING_DB) {
                        EDRDic.getEDRDBManager().setWordEvalConceptSetMap(getInputWordModelSet());
                    } else {
                        setWordEvalConceptSetMap(getInputWordModelSet());
                    }
                }
            }.start();
        }

        public void actionPerformed(ActionEvent e) {
            doDisAmbiguation();
        }
    }

    public void showAllWords() {
        JFrame frame = new JFrame();
        Set allWordSet = new TreeSet();
        if (inputWordModelSet != null) {
            for (InputWordModel iwModel : inputWordModelSet) {
                allWordSet.add(iwModel.getWord());
            }
            allWordSet.addAll(inputModule.getUndefinedWordSet());
            JList list = new JList();
            list.setBorder(BorderFactory.createTitledBorder("���͂��ꂽ���ׂĂ̒P��(" + allWordSet.size() + ")"));
            list.setListData(allWordSet.toArray());
            JScrollPane listScroll = new JScrollPane(list);
            frame.getContentPane().add(listScroll);
            frame.setBounds(50, 50, 200, 600);
            frame.setVisible(true);
        }
    }

    public void saveConstructTreeOptionSet(File file) {
        if (complexConstructTreeOptionMap == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (Iterator i = complexConstructTreeOptionMap.keySet().iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                buf.append(iwModel.getWord() + "\t" + ctOption.getConcept().getIdentity() + "\t" + ctOption.getOption()
                        + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadConstructTreeOptionSet(File file) {
        if (!file.exists()) { return; }
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] strs = line.split("\t");
                String iw = strs[0];
                String id = strs[1];
                String opt = strs[2];
                if (0 < iw.length()) {
                    InputWordModel iwModel = inputModule.makeInputWordModel(iw);
                    complexConstructTreeOptionMap.put(iwModel, new ConstructTreeOption(getConcept(id), opt));
                }
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public InputModule getInputModule() {
        return inputModule;
    }

    public void saveInputWordSet(File file) {
        if (inputWordModelSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                buf.append(iwModel.getWord() + "\n");
            }
            for (Iterator i = inputModule.getUndefinedWordSet().iterator(); i.hasNext();) {
                buf.append(i.next() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void loadUndefinedWordSet(File file) {
        if (!file.exists()) { return; }
        undefinedWordListModel = new DefaultListModel();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                undefinedWordListModel.addElement(line);
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        constructConceptTreePanel.setUndefinedWordListModel(undefinedWordListModel);
        if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
            constructPropertyTreePanel.setUndefinedWordListModel(undefinedWordListModel);
        }
    }

    private Concept getConcept(String id) {
        String[] identity = id.split(":");
        if (identity.length == 2) {
            if (identity[0].equals("edr")) {
                return EDRDic.getEDRConcept(identity[1]);
            } else if (identity[0].equals("wn")) { return WordNetDic.getWNConcept(identity[1]); }
        } else {
            return EDRDic.getEDRConcept(id);
        }
        return null;
    }

    public void loadInputConceptSet(File file) {
        if (!file.exists()) { return; }
        inputConceptSet = new HashSet<Concept>();
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "SJIS"));
            String id = "";
            while ((id = reader.readLine()) != null) {
                inputConceptSet.add(getConcept(id));
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveUndefinedWordSet(File file) {
        if (undefinedWordListModel == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < undefinedWordListModel.getSize(); i++) {
                buf.append(undefinedWordListModel.getElementAt(i) + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveInputConceptSet(File file) {
        if (inputConceptSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));
            StringBuffer buf = new StringBuffer();
            for (Concept c : inputConceptSet) {
                buf.append(c.getIdentity() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordConceptMap(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            StringBuffer buf = new StringBuffer();
            for (Iterator i = getWordConceptMap().entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String word = (String) entry.getKey();
                Concept concept = (Concept) entry.getValue();
                buf.append(word + "," + concept.getIdentity() + "\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordConceptMap(file);
        }
    }

    public void saveWordEvalConceptSet(File file) {
        if (DODDLE.IS_USING_DB) {
            wordEvalConceptSetMap = EDRDic.getEDRDBManager().getWordEvalConceptSetMap();
        }
        if (wordEvalConceptSetMap == null || inputWordModelSet == null) { return; }
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                Set<EvalConcept> evalConceptSet = wordEvalConceptSetMap.get(iwModel.getMatchedWord());
                if (evalConceptSet == null) {
                    continue;
                }
                buf.append(iwModel.getWord());
                int evalValue = -1;
                for (EvalConcept ec : evalConceptSet) {
                    if (evalValue == ec.getEvalValue()) {
                        buf.append("\t" + ec.getConcept().getIdentity());
                    } else {
                        buf.append("||" + ec.getEvalValue() + "\t" + ec.getConcept().getIdentity());
                        evalValue = ec.getEvalValue();
                    }
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveCompleteMatchWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveCompleteMatchWord(file);
        }
    }

    private void saveCompleteMatchWord(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            Set completeWordSet = new TreeSet();
            for (Iterator i = inputWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (iwModel.isPartialMatchWord()) {
                    completeWordSet.add(iwModel.getMatchedWord());
                } else {
                    completeWordSet.add(iwModel.getWord());
                }
            }
            for (Iterator i = completeWordSet.iterator(); i.hasNext();) {
                writer.write(i.next() + "\n");
            }
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveCompleteMatchWordWithComplexWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveCompleteMatchWordWithComplexWord(file);
        }
    }

    private void saveCompleteMatchWordWithComplexWord(File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "SJIS"));

            Map completeWordComplexWordMap = new TreeMap();
            for (Iterator i = inputWordModelSet.iterator(); i.hasNext();) {
                InputWordModel iwModel = (InputWordModel) i.next();
                if (completeWordComplexWordMap.get(iwModel.getMatchedWord()) != null) {
                    Set complexWordSet = (Set) completeWordComplexWordMap.get(iwModel.getMatchedWord());
                    complexWordSet.add(iwModel.getWord());
                    completeWordComplexWordMap.put(iwModel.getMatchedWord(), complexWordSet);
                } else {
                    Set complexWordSet = new TreeSet();
                    complexWordSet.add(iwModel.getWord());
                    completeWordComplexWordMap.put(iwModel.getMatchedWord(), complexWordSet);
                }
            }

            StringBuffer buf = new StringBuffer();
            for (Iterator i = completeWordComplexWordMap.keySet().iterator(); i.hasNext();) {
                String matchedWord = (String) i.next();
                buf.append(matchedWord + "=>");
                Set complexWordSet = (Set) completeWordComplexWordMap.get(matchedWord);
                for (Iterator j = complexWordSet.iterator(); j.hasNext();) {
                    String complexWord = (String) j.next();
                    buf.append(complexWord + ",");
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void saveWordEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordEvalConceptSet(file);
        }
    }

    public void loadWordEvalConceptSet() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadWordEvalConceptSet(file);
        }
    }

    public void loadWordEvalConceptSet(File file) {
        if (!file.exists()) { return; }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "SJIS"));
            String line = "";
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();
            while ((line = reader.readLine()) != null) {
                String[] wordAndResults = line.split("\\|\\|");
                String word = wordAndResults[0];
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (int i = 1; i < wordAndResults.length; i++) {
                    String[] valueAndIDs = wordAndResults[i].split("\t");
                    int value = Integer.parseInt(valueAndIDs[0]);
                    for (int j = 1; j < valueAndIDs.length; j++) {
                        String id = valueAndIDs[j];
                        Concept c = getConcept(id);
                        evalConceptSet.add(new EvalConcept(c, value));
                    }
                }
                wordEvalConceptSetMap.put(word, evalConceptSet);
            }
            if (DODDLE.IS_USING_DB) {
                EDRDic.getEDRDBManager().setWordEvalConceptSetMap(wordEvalConceptSetMap);
            }
            reader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private Map<DefaultMutableTreeNode, String> abstractNodeLabelMap;

    public Map<DefaultMutableTreeNode, String> getAbstractNodeLabelMap() {
        return abstractNodeLabelMap;
    }

    public class ConstructTreeAction extends AbstractAction {

        private boolean isNounAndVerbTree;

        public ConstructTreeAction(String title, boolean t) {
            super(title);
            isNounAndVerbTree = t;
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
                private void addComplexWordNode(int len, InputWordModel iwModel, TreeNode node) {
                    if (len == iwModel.getComplexWordLength()) { return; }
                    List wordList = iwModel.getWordList();
                    StringBuffer buf = new StringBuffer();
                    for (int i = wordList.size() - len - 1; i < wordList.size(); i++) {
                        buf.append(wordList.get(i));
                    }
                    String word = buf.toString();
                    for (int i = 0; i < node.getChildCount(); i++) {
                        TreeNode childNode = node.getChildAt(i);
                        if (childNode.toString().equals(word)) {
                            addComplexWordNode(len + 1, iwModel, childNode);
                            return;
                        } else if (node.getParent() == null
                                && childNode.toString().equals(wordConceptMap.get(iwModel.getWord()).getIdentity())) {
                            addComplexWordNode(len + 1, iwModel, childNode);
                            return;
                        }
                    }
                    DefaultMutableTreeNode childNode = null;
                    if (len == 0) {
                        Concept c = wordConceptMap.get(iwModel.getWord());
                        childNode = new DefaultMutableTreeNode(c.getIdentity());
                    } else {
                        childNode = new DefaultMutableTreeNode(word);
                    }
                    ((DefaultMutableTreeNode) node).add(childNode);
                    addComplexWordNode(len + 1, iwModel, childNode);
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
                            // trimming���ꂽ�ꍇ�C�ŏ����珈�������Ȃ���
                            i = -1; // 0����͂��߂�ɂ́C-1�ɂ���K�v����
                            continue;
                        }
                        extractMoveComplexWordNodeSet(sameNodeSet, addNodeSet, removeNodeSet, childNode);
                        extractReconstructedNodeSet(reconstructNodeSet, childNode);
                    }
                    moveComplexWordNodeSet(node, addNodeSet, removeNodeSet, reconstructNodeSet);
                    // �Z��T�O�����ׂď���������ɁC�q�m�[�h�̏����Ɉڂ�
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
                private void moveComplexWordNodeSet(DefaultMutableTreeNode node,
                        Set<DefaultMutableTreeNode> addNodeSet, Set<DefaultMutableTreeNode> removeNodeSet,
                        Set<String> reconstructNodeSet) {
                    // �q�m�[�h������������Ȃ����ۃm�[�h�̎q�m�[�h��node�ɒǉ�
                    for (Iterator i = addNodeSet.iterator(); i.hasNext();) {
                        DefaultMutableTreeNode addNode = (DefaultMutableTreeNode) i.next();
                        node.add(addNode);
                    }
                    // �q�m�[�h������������Ȃ����ۃm�[�h���폜
                    for (Iterator i = removeNodeSet.iterator(); i.hasNext();) {
                        DefaultMutableTreeNode removeNode = (DefaultMutableTreeNode) i.next();
                        node.remove(removeNode);
                    }
                    // ���ꃌ�x���ɍč\��(���ۃm�[�h�ɒǉ�)���ꂽ�m�[�h���܂܂�Ă���ꍇ�ɂ͍폜
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                        if (reconstructNodeSet.contains(childNode.toString())) {
                            node.remove(childNode);
                            i = -1; // 0����͂��߂�ɂ́C-1�ɂ���K�v����
                            continue;
                        }
                    }
                }

                /**
                 * @param reconstructNodeSet
                 * @param childNode
                 */
                private void extractReconstructedNodeSet(Set<String> reconstructNodeSet,
                        DefaultMutableTreeNode childNode) {
                    // �Q�ȏ�q�m�[�h(������)�������ԃm�[�h�ɒǉ����ꂽ�m�[�h��reconstructNodeSet�ɕۑ�
                    // ����ł́C������ȊO�̃m�[�h���܂߂ĂQ�ȏ�ł��悭�Ȃ��Ă���̂ŁC
                    // �P����childcount�ŏ�������̂ł͂Ȃ��āC�����ꂩ�ǂ������`�F�b�N���Ă��珈������
                    // �悤�ɂ���D
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
                private void extractMoveComplexWordNodeSet(Set<String> sameNodeSet,
                        Set<DefaultMutableTreeNode> addNodeSet, Set<DefaultMutableTreeNode> removeNodeSet,
                        DefaultMutableTreeNode childNode) {
                    if (childNode.getUserObject() instanceof Concept && hasONEComplexWordChild(childNode)) {
                        for (int i = 0; i < childNode.getChildCount(); i++) {
                            DefaultMutableTreeNode grandChildNode = (DefaultMutableTreeNode) childNode.getChildAt(i);
                            if (grandChildNode.getUserObject() instanceof String) {
                                removeNodeSet.add(childNode);
                                if (!sameNodeSet.contains(grandChildNode.toString())) {
                                    DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(grandChildNode
                                            .toString());
                                    // ���̏ꍇ�ړ����ׂ����C�R�s�[���ׂ������l����
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
                 * trimming���ꂽ��true��Ԃ�
                 * 
                 * @param node
                 * @param childNode
                 */
                private boolean trimNotInputComplexWord(DefaultMutableTreeNode node, DefaultMutableTreeNode childNode) {
                    if (OptionDialog.isTrimNodeWithComplexWordConceptConstruction()
                            && childNode.getUserObject() instanceof String
                            && !complexWordSet.contains(childNode.toString())) {
                        DefaultMutableTreeNode[] grandChildNodeList = new DefaultMutableTreeNode[childNode
                                .getChildCount()];
                        for (int i = 0; i < childNode.getChildCount(); i++) {
                            grandChildNodeList[i] = (DefaultMutableTreeNode) childNode.getChildAt(i);
                        }
                        for (int i = 0; i < grandChildNodeList.length; i++) {
                            // (����)
                            // childNode����grandChildNode���폜���āCnode�ɒǉ�����
                            // ���ڒǉ����Ă��܂��ƁCchildNode�̑S�q�v�f�������Ȃ��̂ŁC
                            // �z��Ɋi�[���Ă���Cnode�ɒǉ����Ă���D
                            node.add(grandChildNodeList[i]);
                        }
                        node.remove(childNode);
                        return true;
                    }
                    return false;
                }

                private Set<String> complexWordSet; // ���͌�b�Ɋ܂܂�Ȃ���������폜���邳���ɎQ��

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
                    DODDLE.getLogger().log(Level.DEBUG, "������K�w�\�z");
                    if (OptionDialog.isAddAbstractInternalComplexWordConcept()) {
                        addAbstractTreeNode(rootNode);
                        DODDLE.getLogger().log(Level.DEBUG, "���ۊT�O��ǉ�");
                    }
                    // printDebugTree(rootNode, "before trimming");
                    for (int i = 0; i < rootNode.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                        trimComplexWordNode(childNode);
                    }
                    DODDLE.getLogger().log(Level.DEBUG, "������̙���");
                    // printDebugTree(rootNode, "add abstract node");
                    complexWordSet.clear();
                    ccTreeInterface.addComplexWordConcept(matchedWordIDMap, rootNode);
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
                        // ���d�p�����Ă���ꍇ������̂ŁC��x�N���[���𒊏ۃm�[�h�ɑ}��������ɁC
                        // �e�m�[�h����폜����D
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
                    // System.out.println("ccccc: " + tmpcnt);
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
                 * �ړ���ŕ�����K�w���č\������
                 * 
                 * d: �f�o�b�O�p�D�ċA�̐[�����͂��邽�߁D
                 */
                private void reconstructComplexTree(int d, DefaultMutableTreeNode node) {
                    if (node.getChildCount() == 0) { return; }
                    // System.out.println(node + ": " + d);
                    Map abstractConceptTreeNodeMap = new HashMap();
                    // node�̍Ō�ɒ��ۃm�[�h���ǉ�����Ă������C
                    // node.getChildCount���������邽��i�̒l��ύX���Ȃ��Ă����͂Ȃ�
                    // ���ۃm�[�h�݂̂��Ō�ɒǉ�����Ă������߁C����ȊO�̃m�[�h�Ɋւ��Ă͂��ׂď��������
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                        // ���ۃm�[�h����ʂɎ�������͏������Ȃ�
                        if (node.getUserObject() instanceof String && childNode.getUserObject() instanceof String) {
                            String complexWord = childNode.toString();
                            InputWordModel iwModel = inputModule.makeInputWordModel(complexWord);
                            // String word = (String)
                            // iwModel.getWordList().get(0);
                            String word = iwModel.getTopWord();
                            Concept c = wordConceptMap.get(word);
                            tmpcnt++;
                            if (c != null && 1 < iwModel.getWordList().size()) {
                                Set supConceptSet = getSupConceptSet(c.getId());
                                for (Iterator j = supConceptSet.iterator(); j.hasNext();) {
                                    Concept supConcept = (Concept) j.next();
                                    reconstructComplexNode(node, abstractConceptTreeNodeMap, childNode, supConcept,
                                            iwModel);
                                }
                            }
                        }
                    }
                    // �Z��m�[�h�����ׂď���������ɁC�q�m�[�h�̏����Ɉڂ�
                    for (int i = 0; i < node.getChildCount(); i++) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
                        reconstructComplexTree(++d, childNode);
                    }
                }

                private Set getSupConceptSet(String id) {
                    Set supConceptSet = null;
                    supConceptSet = constructConceptTreePanel.getSupConceptSet(id);
                    supConceptSet.addAll(constructPropertyTreePanel.getSupConceptSet(id));
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
                    // System.out.println("�ꓪ�̏�ʊT�O: " + supConcept.getWord());
                    // System.out.println("������̏�ʊT�O: " + node.toString());
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
                    deepCloneTreeNode(childNode, insertNode); // ���d�p�����Ă���ꍇ������̂ŁC�N���[����}������
                    abstractNode.add(insertNode);
                }

                /*
                 * TreeNode�̐[���R�s�[���s���D orgNode��insertNode�ɃR�s�[����
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
                private DefaultMutableTreeNode getAbstractNode(DefaultMutableTreeNode node,
                        Map abstractConceptTreeNodeMap, Concept supConcept, InputWordModel iwModel) {
                    DefaultMutableTreeNode abstractNode = null;
                    if (abstractConceptTreeNodeMap.get(supConcept) != null) {
                        abstractNode = (DefaultMutableTreeNode) abstractConceptTreeNodeMap.get(supConcept);
                    } else {
                        abstractNode = new DefaultMutableTreeNode(supConcept);
                        abstractNodeLabelMap.put(abstractNode, supConcept.getWord() + iwModel.getWordWithoutTopWord());
                        // ���ۃm�[�h��node�̍Ō�ɒǉ�����
                        // node�̐擪�ɑ}�����Ă��܂��ƁCnode�̂Q�Ԗڂ̗v�f�����ݏ������Ă���q�m�[�h�ƂȂ��Ă��܂��C
                        // �d�������������s�����ƂɂȂ��Ă��܂��D�܂��C���ۃm�[�h�̐������q�m�[�h����������Ȃ��D
                        node.add(abstractNode);
                        abstractConceptTreeNodeMap.put(supConcept, abstractNode);
                    }
                    return abstractNode;
                }

                private TreeModel makeNounConceptTreeModel(Set nounConceptSet) {
                    constructConceptTreePanel.init();
                    TreeModel nounTreeModel = constructConceptTreePanel.getTreeModel(nounConceptSet);
                    constructConceptTreePanel.setTreeModel(nounTreeModel);
                    constructConceptTreePanel.setUndefinedWordListModel(undefinedWordListModel);
                    DODDLE.STATUS_BAR.addValue();
                    setComplexConcept(constructConceptTreePanel, nounConceptSet);
                    DODDLE.STATUS_BAR.addValue();
                    return nounTreeModel;
                }

                private TreeModel makeVerbConceptTreeModel(Set verbIDSet) {
                    constructPropertyTreePanel.init();
                    TreeModel propertyTreeModel = constructPropertyTreePanel.getTreeModel(constructConceptTreePanel
                            .getAllConceptID(), verbIDSet);
                    constructPropertyTreePanel.setTreeModel(propertyTreeModel);
                    constructPropertyTreePanel.setUndefinedWordListModel(undefinedWordListModel);
                    // System.out.println("nounIDSet: " + inputIDSet);
                    // System.out.println(verbIDSet);
                    DODDLE.STATUS_BAR.addValue();
                    setComplexConcept(constructPropertyTreePanel, verbIDSet);
                    DODDLE.STATUS_BAR.addValue();
                    constructPropertyTreePanel.removeNounNode(); // �����I�T�O�K�w���疼���I�T�O���폜
                    return propertyTreeModel;
                }

                /*
                 * �u�Y���Ȃ��v�Ƃ��ꂽ�T�O�������ڂ��Ă��Ȃ��P�ꃊ�X�g�ɒǉ�
                 */
                private void setUndefinedWordSet() {
                    for (Iterator i = wordConceptMap.entrySet().iterator(); i.hasNext();) {
                        Entry entry = (Entry) i.next();
                        String word = (String) entry.getKey();
                        Concept c = (Concept) entry.getValue();
                        if (c.equals(nullConcept)) {
                            undefinedWordListModel.addElement(word);
                        }
                    }
                }

                public void run() {
                    constructConceptTreePanel.setVisibleConceptTree(false);
                    constructPropertyTreePanel.setVisibleConceptTree(false);
                    DODDLE.STATUS_BAR.setLastMessage("�K�w�\�z����");
                    DODDLE.STATUS_BAR.initNormal(9);
                    DODDLE.STATUS_BAR.startTime();
                    project.resetIDConceptMap();

                    undefinedWordListModel = new DefaultListModel();
                    setUndefinedWordSet();

                    if (DODDLE.IS_USING_DB) {
                        setInputConceptSetWithDB();
                    } else {
                        setInputConceptSet();
                    }
                    DODDLE.getLogger().log(Level.INFO, "���S�ƍ� �P�ꐔ: " + perfectMatchedWordModelSet.size());
                    DODDLE.getLogger().log(Level.INFO, "�����ƍ�  �P�ꐔ: " + partialMatchedWordModelSet.size());
                    DODDLE.getLogger().log(Level.INFO,
                            "���͌�b��: " + (perfectMatchedWordModelSet.size() + partialMatchedWordModelSet.size()));
                    DODDLE.getLogger().log(Level.INFO, "���͊T�O��: " + inputConceptSet.size());
                    DODDLE.STATUS_BAR.addValue();
                    project.initUserIDCount();

                    for (int i = 0; i < undefinedWordListPanel.getModel().getSize(); i++) {
                        undefinedWordListModel.addElement(undefinedWordListPanel.getModel().getElementAt(i));
                    }

                    DODDLE.STATUS_BAR.addValue();
                    if (OptionDialog.isNounAndVerbConceptHierarchyConstructionMode()) {
                        ConceptDefinition conceptDefinition = ConceptDefinition.getInstance();
                        Set verbConceptSet = conceptDefinition.getVerbIDSet(inputConceptSet);
                        Set nounConceptSet = new HashSet(inputConceptSet);
                        nounConceptSet.removeAll(verbConceptSet);

                        DODDLE.getLogger().log(Level.INFO, "���� �����I�T�O��: " + nounConceptSet.size());
                        DODDLE.STATUS_BAR.addValue();
                        constructConceptTreePanel.setTreeModel(makeNounConceptTreeModel(nounConceptSet));
                        DODDLE.STATUS_BAR.addValue();
                        DODDLE.getLogger().log(Level.INFO, "���� �����I�T�O��: " + verbConceptSet.size());
                        constructPropertyTreePanel.setTreeModel(makeVerbConceptTreeModel(verbConceptSet));
                        DODDLE.STATUS_BAR.addValue();
                    } else {
                        Set nounConceptSet = new HashSet(inputConceptSet);
                        DODDLE.getLogger().log(Level.INFO, "���� �����I�T�O��: " + nounConceptSet.size());
                        constructConceptTreePanel.setTreeModel(makeNounConceptTreeModel(nounConceptSet));
                        DODDLE.STATUS_BAR.addValue();
                        constructPropertyTreePanel.setTreeModel(makeVerbConceptTreeModel(new HashSet()));
                        DODDLE.STATUS_BAR.addValue();
                    }

                    constructConceptTreePanel.expandTree();
                    DODDLE.STATUS_BAR.addValue();
                    constructPropertyTreePanel.expandTree();
                    DODDLE.STATUS_BAR.addValue();
                    constructConceptTreePanel.setVisibleConceptTree(true);
                    constructPropertyTreePanel.setVisibleConceptTree(true);
                    DODDLE.setSelectedIndex(DODDLE.TAXONOMIC_PANEL);
                    DODDLE.STATUS_BAR.addValue();
                    DODDLE.STATUS_BAR.hideProgressBar();
                }
            }.start();
        }
    }
}