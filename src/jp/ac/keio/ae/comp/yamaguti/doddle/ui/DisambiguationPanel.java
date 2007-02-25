package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.actions.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

/*
 * Created on 2004/08/22
 *
 */

/**
 * @author takeshi morita
 * 
 */
public class DisambiguationPanel extends JPanel implements ListSelectionListener, ActionListener, TreeSelectionListener {

    private Set<Concept> inputConceptSet; // 入力概念のセット
    private Set<Concept> inputNounConceptSet; // 入力名詞的概念のセット
    private Set<Concept> inputVerbConceptSet; // 入力動詞的概念のセット

    private Set<InputWordModel> inputWordModelSet; // 入力単語モデルのセット
    private Map<String, Set<Concept>> wordConceptSetMap; // 入力単語と入力単語を見出しとして含む概念のマッピング
    private Map<String, Set<Concept>> wordCorrespondConceptSetMap; // 入力単語と適切に対応する概念のマッピング
    private Map<String, Set<EvalConcept>> wordEvalConceptSetMap;
    private Map<InputWordModel, ConstructTreeOption> complexConstructTreeOptionMap;

    private TitledBorder perfectMatchedWordJListTitle;
    private TitledBorder partialMatchedWordJListTitle;
    private JPanel perfectMatchedWordListPanel;
    private JPanel partialMatchedWordListPanel;

    private View[] wordListViews;
    private RootWindow wordListRootWindow;

    private JTextField searchWordField;
    private JButton searchWordButton;

    private JList perfectMatchedWordJList; // 完全照合した単語リスト
    private Set<InputWordModel> perfectMatchedWordModelSet;
    private JList partialMatchedWordJList; // 部分照合した単語リスト
    private Set<InputWordModel> partialMatchedWordModelSet;
    private JList conceptSetJList;
    private UndefinedWordListPanel undefinedWordListPanel;

    private JCheckBox perfectMatchedAmbiguityCntCheckBox;
    private JCheckBox perfectMatchedIsSyncCheckBox;
    private JCheckBox perfectMatchedIsSystemAddedWordCheckBox;

    private JCheckBox partialMatchedComplexWordCheckBox;
    private JCheckBox partialMatchedMatchedWordBox;
    private JCheckBox partialMatchedAmbiguityCntCheckBox;
    private JCheckBox partialMatchedShowOnlyRelatedComplexWordsCheckBox;

    private Concept selectedConcept;
    private JList labelLangJList;
    private JList labelJList;
    private JList descriptionLangJList;
    private JList descriptionJList;
    
    private JPanel constructTreeOptionPanel;
    private JPanel partialMatchedConstructTreeOptionPanel;
    private JPanel perfectMatchedConstructTreeOptionPanel;
    private JPanel systemAddedPerfectMatchedConstructTreeOptionPanel;

    private JCheckBox replaceSubClassesCheckBox;

    private JRadioButton addAsSubConceptRadioButton;
    private JRadioButton addAsSameConceptRadioButton;

    private JList hilightPartJList;
    private JEditorPane documentArea;
    // private JTextArea documentArea;
    private JCheckBox hilightInputWordCheckBox;
    private JCheckBox showAroundConceptTreeCheckBox;
    private JTree aroundConceptTree;
    private TreeModel aroundConceptTreeModel;

    private InputModule inputModule;
    private ConstructClassPanel constructClassPanel;
    private ConstructPropertyPanel constructPropertyPanel;

    private JButton constructNounTreeButton;
    private JButton constructNounAndVerbTreeButton;
    private ConstructionTypePanel constructionTypePanel;
    private PerfectMatchedOptionPanel perfectMatchedOptionPanel;
    private PartialMatchedOptionPanel partialMatchedOptionPanel;
    // private JButton showConceptDescriptionButton;

    private AutomaticDisAmbiguationAction automaticDisAmbiguationAction;
    private ConstructTreeAction constructNounTreeAction;
    private ConstructTreeAction constructNounAndVerbTreeAction;

    private Action saveCompleteMatchWordAction;
    private Action saveCompleteMatchWordWithComplexWordAcion;

    private ConceptDescriptionFrame conceptDescriptionFrame;

    private DocumentSelectionPanel docSelectionPanel;

    private DODDLEProject project;

    private boolean isConstructNounAndVerbTree;

    private View[] mainViews;
    private RootWindow rootWindow;

    private static final int LANG_SIZE = 60;
    
    public DisambiguationPanel(ConstructClassPanel tp, ConstructPropertyPanel pp, DODDLEProject p) {
        project = p;
        constructClassPanel = tp;
        constructPropertyPanel = pp;
        inputModule = new InputModule(project);
        wordCorrespondConceptSetMap = new HashMap<String, Set<Concept>>();
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();

        conceptDescriptionFrame = new ConceptDescriptionFrame();

        conceptSetJList = new JList();
        conceptSetJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        conceptSetJList.addListSelectionListener(this);
        JScrollPane conceptJListScroll = new JScrollPane(conceptSetJList);

        labelLangJList = new JList();
        labelLangJList.addListSelectionListener(this);
        JScrollPane langLabelJListScroll = new JScrollPane(labelLangJList);
        langLabelJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 10));
        langLabelJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 10));
        langLabelJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Lang")));
        labelJList = new JList();
        JScrollPane labelJListScroll = new JScrollPane(labelJList);
        labelJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Label")));
        
        descriptionLangJList = new JList();
        descriptionLangJList.addListSelectionListener(this);
        JScrollPane langDescriptionJListScroll = new JScrollPane(descriptionLangJList);
        langDescriptionJListScroll.setPreferredSize(new Dimension(LANG_SIZE, 10));
        langDescriptionJListScroll.setMinimumSize(new Dimension(LANG_SIZE, 10));
        langDescriptionJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Lang")));
        descriptionJList = new JList();
        JScrollPane descriptionJListScroll = new JScrollPane(descriptionJList);
        descriptionJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("Description")));

        addAsSameConceptRadioButton = new JRadioButton(Translator.getString("DisambiguationPanel.SameConcept"), true);
        addAsSameConceptRadioButton.addActionListener(this);
        addAsSubConceptRadioButton = new JRadioButton(Translator.getString("DisambiguationPanel.SubConcept"));
        addAsSubConceptRadioButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(addAsSameConceptRadioButton);
        group.add(addAsSubConceptRadioButton);
        constructTreeOptionPanel = new JPanel();
        constructTreeOptionPanel.setLayout(new BorderLayout());
        constructTreeOptionPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.ConstructTreeOption")));
        partialMatchedConstructTreeOptionPanel = new JPanel();
        partialMatchedConstructTreeOptionPanel.setLayout(new GridLayout(1, 2));
        partialMatchedConstructTreeOptionPanel.add(addAsSameConceptRadioButton);
        partialMatchedConstructTreeOptionPanel.add(addAsSubConceptRadioButton);

        perfectMatchedConstructTreeOptionPanel = new JPanel();
        systemAddedPerfectMatchedConstructTreeOptionPanel = new JPanel();
        systemAddedPerfectMatchedConstructTreeOptionPanel.setLayout(new BorderLayout());
        replaceSubClassesCheckBox = new JCheckBox("下位概念に置換");
        replaceSubClassesCheckBox.addActionListener(this);
        systemAddedPerfectMatchedConstructTreeOptionPanel.add(replaceSubClassesCheckBox, BorderLayout.CENTER);

        constructTreeOptionPanel.add(partialMatchedConstructTreeOptionPanel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BorderLayout());
        labelPanel.add(langLabelJListScroll, BorderLayout.WEST);
        labelPanel.add(labelJListScroll, BorderLayout.CENTER);
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BorderLayout());
        descriptionPanel.add(langDescriptionJListScroll, BorderLayout.WEST);
        descriptionPanel.add(descriptionJListScroll, BorderLayout.CENTER);
        JPanel labelAndDescriptionPanel = new JPanel();
        labelAndDescriptionPanel.setLayout(new GridLayout(2, 1));
        labelAndDescriptionPanel.add(labelPanel);
        labelAndDescriptionPanel.add(descriptionPanel);
        
        JPanel conceptInfoPanel = new JPanel();
        conceptInfoPanel.setLayout(new BorderLayout());
        conceptInfoPanel.add(labelAndDescriptionPanel, BorderLayout.CENTER);
        conceptInfoPanel.add(constructTreeOptionPanel, BorderLayout.SOUTH);

        undefinedWordListPanel = new UndefinedWordListPanel();

        hilightPartJList = new JList();
        hilightPartJList.addListSelectionListener(this);
        JScrollPane hilightPartJListScroll = new JScrollPane(hilightPartJList);
        hilightPartJListScroll.setBorder(BorderFactory.createTitledBorder("行番号"));
        hilightPartJListScroll.setPreferredSize(new Dimension(100, 100));
        documentArea = new JEditorPane("text/html", "");
        // documentArea = new JTextArea();
        documentArea.setEditable(false);
        // documentArea.setLineWrap(true);
        JScrollPane documentAreaScroll = new JScrollPane(documentArea);
        hilightInputWordCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.HighlightInputWords"), false);
        hilightInputWordCheckBox.addActionListener(this);

        showAroundConceptTreeCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.ShowConceptTree"),
                false);
        showAroundConceptTreeCheckBox.addActionListener(this);

        aroundConceptTreeModel = new DefaultTreeModel(null);
        aroundConceptTree = new JTree(aroundConceptTreeModel);
        aroundConceptTree.addTreeSelectionListener(this);
        aroundConceptTree.setEditable(false);
        aroundConceptTree.setCellRenderer(new AroundTreeCellRenderer());
        JScrollPane aroundConceptTreeScroll = new JScrollPane(aroundConceptTree);

        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(aroundConceptTreeScroll, BorderLayout.CENTER);
        treePanel.add(showAroundConceptTreeCheckBox, BorderLayout.SOUTH);

        JPanel documentPanel = new JPanel();
        documentPanel.setLayout(new BorderLayout());
        documentPanel.add(documentAreaScroll, BorderLayout.CENTER);
        documentPanel.add(hilightInputWordCheckBox, BorderLayout.SOUTH);
        // documentPanel.add(hilightPartJListScroll, BorderLayout.WEST);

        automaticDisAmbiguationAction = new AutomaticDisAmbiguationAction(Translator
                .getString("DisambiguationPanel.AutomaticDisambiguation"));
        // showConceptDescriptionButton = new JButton(new
        // ShowConceptDescriptionAction("概念記述を表示"));

        // JPanel p1 = new JPanel();
        // p1.add(automaticDisAmbiguationButton);
        // p1.add(showConceptDescriptionButton);

        constructionTypePanel = new ConstructionTypePanel();
        perfectMatchedOptionPanel = new PerfectMatchedOptionPanel();
        partialMatchedOptionPanel = new PartialMatchedOptionPanel();

        constructNounTreeButton = new JButton(new ConstructNounTreeAction());
        constructNounAndVerbTreeButton = new JButton(new ConstructNounAndVerbTreeAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1));
        buttonPanel.add(constructNounTreeButton);
        buttonPanel.add(constructNounAndVerbTreeButton);

        JPanel optionPanel = new JPanel();
        optionPanel.add(constructionTypePanel);
        optionPanel.add(perfectMatchedOptionPanel);
        optionPanel.add(partialMatchedOptionPanel);
        optionPanel.add(buttonPanel);

        mainViews = new View[7];
        ViewMap viewMap = new ViewMap();

        mainViews[0] = new View(Translator.getString("DisambiguationPanel.WordListPanel"), null, getWordListPanel());
        mainViews[1] = new View(Translator.getString("DisambiguationPanel.ConceptList"), null, conceptJListScroll);
        mainViews[2] = new View(Translator.getString("DisambiguationPanel.ConceptInformationPanel"), null, conceptInfoPanel);
        mainViews[3] = new View(Translator.getString("DisambiguationPanel.UndefinedWordList"), null,
                undefinedWordListPanel);
        mainViews[4] = new View(Translator.getString("DisambiguationPanel.ConceptTree"), null, treePanel);
        mainViews[5] = new View(Translator.getString("DisambiguationPanel.InputDocument"), null, documentPanel);
        mainViews[6] = new View(Translator.getString("DisambiguationPanel.OptionPanel"), null, optionPanel);
        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        setXGALayout();
        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
    }

    public void setXGALayout() {
        wordListRootWindow.setWindow(new TabWindow(new DockingWindow[] { wordListViews[0], wordListViews[1]}));
        wordListViews[0].restoreFocus();

        SplitWindow sw1 = new SplitWindow(true, 0.5f, mainViews[2], mainViews[3]);
        SplitWindow sw2 = new SplitWindow(true, 0.3f, mainViews[1], sw1);
        SplitWindow sw3 = new SplitWindow(true, mainViews[4], mainViews[5]);
        SplitWindow sw4 = new SplitWindow(false, 0.6f, sw2, sw3);
        SplitWindow sw5 = new SplitWindow(true, 0.3f, mainViews[0], sw4);
        SplitWindow sw6 = new SplitWindow(false, 0.85f, sw5, mainViews[6]);
        rootWindow.setWindow(sw6);
    }

    public void setUXGALayout() {
        wordListRootWindow.setWindow(new SplitWindow(false, wordListViews[0], wordListViews[1]));
        wordListViews[0].restoreFocus();

        SplitWindow sw1 = new SplitWindow(true, 0.5f, mainViews[2], mainViews[3]);
        SplitWindow sw2 = new SplitWindow(true, 0.3f, mainViews[1], sw1);
        SplitWindow sw3 = new SplitWindow(true, mainViews[4], mainViews[5]);
        SplitWindow sw4 = new SplitWindow(false, 0.6f, sw2, sw3);
        SplitWindow sw5 = new SplitWindow(true, 0.3f, mainViews[0], sw4);
        SplitWindow sw6 = new SplitWindow(false, 0.85f, sw5, mainViews[6]);
        rootWindow.setWindow(sw6);
    }

    public void removeRefOntConceptLabel(Concept c, boolean isInputConcept) {
        if (!isInputConcept || perfectMatchedOptionPanel.isIncludeRefOntConceptLabel()) { return; }
        Object[] labelList = labelJList.getSelectedValues();
        for (int i = 0; i <labelList.length; i++){
            c.removeLabel((DODDLELiteral)labelList[i]);
        }
    }

    public void setConstructNounAndVerbTree(boolean t) {
        isConstructNounAndVerbTree = t;
    }

    public boolean isConstructNounAndVerbTree() {
        return isConstructNounAndVerbTree;
    }

    class EditPanel extends JPanel implements ActionListener {
        private JTextField inputWordField;
        private JButton addInputWordButton;
        private JButton removeInputWordButton;

        EditPanel() {
            inputWordField = new JTextField();
            addInputWordButton = new JButton(Translator.getString("DisambiguationPanel.Add"));
            addInputWordButton.addActionListener(this);
            removeInputWordButton = new JButton(Translator.getString("DisambiguationPanel.Remove"));
            removeInputWordButton.addActionListener(this);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(addInputWordButton);
            buttonPanel.add(removeInputWordButton);

            setLayout(new BorderLayout());
            add(inputWordField, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.EAST);
        }

        public void actionPerformed(ActionEvent e) {
            JList wordJList = null;
            Set<InputWordModel> wordModelSet = null;
            wordJList = getTargetWordJList();
            wordModelSet = getTargetWordModelSet();

            if (e.getSource() == addInputWordButton) {
                Set<String> inputWordSet = new HashSet<String>();
                if (0 < inputWordField.getText().length()) {
                    inputWordSet.add(inputWordField.getText());
                    addInputWordSet(inputWordSet);
                    inputWordField.setText("");
                }
            } else if (e.getSource() == removeInputWordButton) {
                Object[] values = wordJList.getSelectedValues();
                for (int i = 0; i < values.length; i++) {
                    InputWordModel removeWordModel = (InputWordModel) values[i];
                    wordSet.remove(removeWordModel.getWord());
                    wordModelSet.remove(removeWordModel);
                    inputWordModelSet.remove(removeWordModel);
                    wordCorrespondConceptSetMap.remove(removeWordModel.getWord());
                    complexConstructTreeOptionMap.remove(removeWordModel);
                }
                wordJList.setListData(wordModelSet.toArray());
                wordJList.setSelectedIndex(0);
                project.getConceptDefinitionPanel().setInputConceptSet();
            }
        }
    }

    public Set<String> getURISetForReplaceSubConcepts() {
        Set<String> uriSet = new HashSet<String>();
        for (ConstructTreeOption option : complexConstructTreeOptionMap.values()) {
            if (option.isReplaceSubConcepts()) {
                uriSet.add(option.getConcept().getURI());
            }
        }
        return uriSet;
    }

    public ConstructionTypePanel getConstructionTypePanel() {
        return constructionTypePanel;
    }

    public PerfectMatchedOptionPanel getPerfectMatchedOptionPanel() {
        return perfectMatchedOptionPanel;
    }

    public PartialMatchedOptionPanel getPartialMatchedOptionPanel() {
        return partialMatchedOptionPanel;
    }

    public class ConstructionTypePanel extends JPanel {
        private JRadioButton newButton;
        private JRadioButton addButton;

        ConstructionTypePanel() {
            newButton = new JRadioButton(Translator.getString("DisambiguationPanel.ConstructionOption.New"), true);
            addButton = new JRadioButton(Translator.getString("DisambiguationPanel.ConstructionOption.Add"));
            ButtonGroup group = new ButtonGroup();
            group.add(newButton);
            group.add(addButton);
            add(newButton);
            add(addButton);
            setBorder(BorderFactory.createTitledBorder(Translator.getString("DisambiguationPanel.ConstructionOption")));
        }

        public boolean isNewConstruction() {
            return newButton.isSelected();
        }
    }

    public class PerfectMatchedOptionPanel extends JPanel implements ActionListener {
        private JCheckBox constructionBox;
        private JCheckBox trimmingBox;
        private JCheckBox includeRefOntConceptLabelBox;

        PerfectMatchedOptionPanel() {
            constructionBox = new JCheckBox(
                    Translator.getString("DisambiguationPanel.PerfectMatchOption.Construction"), true);
            constructionBox.addActionListener(this);
            trimmingBox = new JCheckBox(Translator.getString("DisambiguationPanel.PerfectMatchOption.Trimming"), true);
            includeRefOntConceptLabelBox = new JCheckBox("参照オントロジーの概念見出しを追加", true);
            add(constructionBox);
            add(trimmingBox);
            add(includeRefOntConceptLabelBox);
            setBorder(BorderFactory.createTitledBorder(Translator.getString("DisambiguationPanel.PerfectMatchOption")));
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == constructionBox) {
                trimmingBox.setEnabled(constructionBox.isSelected());
                includeRefOntConceptLabelBox.setEnabled(constructionBox.isSelected());
            }
        }

        public void setConstruction(boolean t) {
            constructionBox.setSelected(t);
        }

        public void setTrimming(boolean t) {
            trimmingBox.setSelected(t);
        }

        public void setIncludeRefOntConceptLabel(boolean t) {
            includeRefOntConceptLabelBox.setSelected(t);
        }

        public boolean isConstruction() {
            return constructionBox.isSelected();
        }

        public boolean isTrimming() {
            return trimmingBox.isSelected();
        }

        public boolean isIncludeRefOntConceptLabel() {
            return includeRefOntConceptLabelBox.isSelected();
        }
    }

    public class PartialMatchedOptionPanel extends JPanel implements ActionListener {
        private JCheckBox constructionBox;
        private JCheckBox trimmingBox;
        private JCheckBox addAbstractConceptBox;
        private JTextField abstractConceptChildNodeNumField;

        PartialMatchedOptionPanel() {
            constructionBox = new JCheckBox(
                    Translator.getString("DisambiguationPanel.PartialMatchOption.Construction"), false);
            constructionBox.addActionListener(this);
            trimmingBox = new JCheckBox(Translator.getString("DisambiguationPanel.PartialMatchOption.Trimming"), false);
            trimmingBox.setEnabled(false);
            addAbstractConceptBox = new JCheckBox(Translator
                    .getString("DisambiguationPanel.PartialMatchOption.AddAbstractConcept"), false);
            addAbstractConceptBox.setEnabled(false);
            abstractConceptChildNodeNumField = new JTextField(2);
            abstractConceptChildNodeNumField.setText("2");
            add(constructionBox);
            add(trimmingBox);
            add(addAbstractConceptBox);
            add(abstractConceptChildNodeNumField);
            setBorder(BorderFactory.createTitledBorder(Translator.getString("DisambiguationPanel.PartialMatchOption")));
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == constructionBox) {
                constructionBoxAction(constructionBox.isSelected());
            }
        }

        private void constructionBoxAction(boolean t) {
            trimmingBox.setEnabled(t);
            addAbstractConceptBox.setEnabled(t);
        }

        public void setConstruction(boolean t) {
            constructionBox.setSelected(t);
            constructionBoxAction(t);
        }

        public void setTrimming(boolean t) {
            trimmingBox.setSelected(t);
        }

        public void setAddAbstractConcept(boolean t) {
            addAbstractConceptBox.setSelected(t);
        }

        public boolean isConstruction() {
            return constructionBox.isSelected();
        }

        public boolean isTrimming() {
            return trimmingBox.isSelected();
        }

        public boolean isAddAbstractConcept() {
            return addAbstractConceptBox.isSelected();
        }

        public int getAbstractConceptChildNodeNum() {
            int num = 2;
            String numStr = abstractConceptChildNodeNumField.getText();
            try {
                num = Integer.parseInt(numStr);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return num;
        }
    }

    public Map<InputWordModel, ConstructTreeOption> getComplexConstructTreeOptionMap() {
        return complexConstructTreeOptionMap;
    }

    private JPanel getWordListPanel() {
        perfectMatchedWordListPanel = getPerfectMatchedWordListPanel();
        partialMatchedWordListPanel = getPartialMatchedWordListPanel();

        wordListViews = new View[2];
        wordListViews[0] = new View(Translator.getString("DisambiguationPanel.PerfectMatch"), null,
                perfectMatchedWordListPanel);
        wordListViews[1] = new View(Translator.getString("DisambiguationPanel.PartialMatch"), null,
                partialMatchedWordListPanel);

        ViewMap viewMap = new ViewMap();
        viewMap.addView(0, wordListViews[0]);
        viewMap.addView(1, wordListViews[1]);

        wordListRootWindow = Utils.createDODDLERootWindow(viewMap);

        JPanel wordListPanel = new JPanel();
        wordListPanel.setLayout(new BorderLayout());
        wordListPanel.add(getSearchWordPanel(), BorderLayout.NORTH);
        wordListPanel.add(wordListRootWindow, BorderLayout.CENTER);
        wordListPanel.add(new EditPanel(), BorderLayout.SOUTH);
        wordListPanel.setPreferredSize(new Dimension(300, 100));
        wordListPanel.setMinimumSize(new Dimension(300, 100));

        return wordListPanel;
    }

    private JPanel getSearchWordPanel() {
        searchWordField = new JTextField();
        searchWordField.addActionListener(this);
        searchWordButton = new JButton(Translator.getString("DisambiguationPanel.Search"));
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
        perfectMatchedWordJListTitle = BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.PerfectMatchWordList"));
        // perfectMatchedWordListScroll.setBorder(perfectMatchedWordJListTitle);

        perfectMatchedAmbiguityCntCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.SenseCount"), true);
        perfectMatchedAmbiguityCntCheckBox.addActionListener(this);
        perfectMatchedIsSyncCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.ApplyDisambiguationResultToCorrenpondancePartialMatchedWords"), true);
        perfectMatchedIsSystemAddedWordCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.CheckSystemAddedWord"), true);
        perfectMatchedIsSystemAddedWordCheckBox.addActionListener(this);
        JPanel perfectMatchedFilterPanel = new JPanel();
        perfectMatchedFilterPanel.setLayout(new GridLayout(3, 1));
        perfectMatchedFilterPanel.add(perfectMatchedAmbiguityCntCheckBox);
        perfectMatchedFilterPanel.add(perfectMatchedIsSystemAddedWordCheckBox);
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
        partialMatchedWordJListTitle = BorderFactory.createTitledBorder(Translator
                .getString("DisambiguationPanel.PartialMatchWordList"));
        // partialMatchedWordListScroll.setBorder(partialMatchedWordJListTitle);

        partialMatchedComplexWordCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.ComplexWord"), true);
        partialMatchedComplexWordCheckBox.addActionListener(this);
        partialMatchedMatchedWordBox = new JCheckBox(Translator.getString("DisambiguationPanel.MatchResult"), true);
        partialMatchedMatchedWordBox.addActionListener(this);
        partialMatchedAmbiguityCntCheckBox = new JCheckBox(Translator.getString("DisambiguationPanel.SenseCount"), true);
        partialMatchedAmbiguityCntCheckBox.addActionListener(this);
        partialMatchedShowOnlyRelatedComplexWordsCheckBox = new JCheckBox(Translator
                .getString("DisambiguationPanel.ShowOnlyCorrespondComplexWords"), false);
        partialMatchedShowOnlyRelatedComplexWordsCheckBox.addActionListener(this);
        JPanel partialMatchedFilterPanel = new JPanel();
        partialMatchedFilterPanel.setLayout(new GridLayout(2, 2));
        partialMatchedFilterPanel.add(partialMatchedComplexWordCheckBox);
        partialMatchedFilterPanel.add(partialMatchedMatchedWordBox);
        partialMatchedFilterPanel.add(partialMatchedAmbiguityCntCheckBox);
        JPanel partialMatchedOptionPanel = new JPanel();
        partialMatchedOptionPanel.setLayout(new BorderLayout());
        partialMatchedOptionPanel.add(partialMatchedFilterPanel, BorderLayout.CENTER);
        partialMatchedOptionPanel.add(partialMatchedShowOnlyRelatedComplexWordsCheckBox, BorderLayout.SOUTH);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(partialMatchedWordListScroll, BorderLayout.CENTER);
        panel.add(partialMatchedOptionPanel, BorderLayout.SOUTH);

        return panel;
    }

    public int getPartialMatchedWordCnt() {
        if (partialMatchedWordModelSet == null) { return 0; }
        return partialMatchedWordModelSet.size();
    }

    public int getPerfectMatchedWordCnt(boolean isSystemAdded) {
        if (perfectMatchedWordModelSet == null) { return 0; }
        int num = 0;
        for (InputWordModel iwModel : perfectMatchedWordModelSet) {
            if (isSystemAdded) {
                if (iwModel.isSystemAdded()) {
                    num++;
                }
            } else {
                if (!iwModel.isSystemAdded()) {
                    num++;
                }
            }
        }
        return num;
    }

    public int getInputWordCnt() {
        return getPartialMatchedWordCnt() + getPerfectMatchedWordCnt() + getUndefinedWordCnt();
    }

    public int getPerfectMatchedWordCnt() {
        return getPerfectMatchedWordCnt(false);
    }

    public int getSystemAddedPerfectMatchedWordCnt() {
        return getPerfectMatchedWordCnt(true);
    }

    public int getMatchedWordCnt() {
        return getPartialMatchedWordCnt() + getPerfectMatchedWordCnt() + getSystemAddedPerfectMatchedWordCnt();
    }

    public int getUndefinedWordCnt() {
        return undefinedWordListPanel.getModel().getSize();
    }

    public UndefinedWordListPanel getUndefinedWordListPanel() {
        return undefinedWordListPanel;
    }

    public void setDocumentSelectionPanel(DocumentSelectionPanel p) {
        docSelectionPanel = p;
    }

    public void selectTopList() {
        if (perfectMatchedWordJList.getModel().getSize() != 0) {
            wordListRootWindow.getWindow().getChildWindow(0).restoreFocus();
            perfectMatchedWordJList.setSelectedIndex(0);
        } else {
            clearConceptInfoPanel();
        }
    }

    private void clearConceptInfoPanel() {
        labelLangJList.setListData(new Object[0]);
        labelJList.setListData(new Object[0]);
        descriptionLangJList.setListData(new Object[0]);
        descriptionJList.setListData(new Object[0]);
        setPartialMatchedOptionButton(false);
    }

    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = aroundConceptTree.getSelectionPath();
        if (path != null) {
            ConceptTreeNode node = (ConceptTreeNode) path.getLastPathComponent();
            selectedConcept = node.getConcept();
            setLabelList();
            setDescriptionList();
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

    private void saveReplaceSubConceptsOption() {
        JList wordJList = getTargetWordJList();
        InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
        if (iwModel != null && iwModel.isSystemAdded()) {
            ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
            ctOption.setIsReplaceSubConcepts(replaceSubClassesCheckBox.isSelected());
            complexConstructTreeOptionMap.put(iwModel, ctOption);
        }
    }

    private void showOnlyRelatedComplexWords() {
        if (partialMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
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
            partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                    + " (" + searchedPartialMatchedWordModelSet.size() + "/" + partialMatchedWordModelSet.size() + ")");
        } else {
            partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
            partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                    + " (" + partialMatchedWordModelSet.size() + ")");
        }
        perfectMatchedWordJList.repaint();
        partialMatchedWordJList.repaint();
        wordListRootWindow.getWindow().repaint();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == perfectMatchedWordJList || e.getSource() == partialMatchedWordJList) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == hilightInputWordCheckBox) {
            hilightInputWord();
        } else if (e.getSource() == showAroundConceptTreeCheckBox) {
            showAroundConceptTree();
        } else if (e.getSource() == addAsSameConceptRadioButton) {
            saveCompoundOption("SAME");
        } else if (e.getSource() == addAsSubConceptRadioButton) {
            saveCompoundOption("SUB");
        } else if (e.getSource() == replaceSubClassesCheckBox) {
            saveReplaceSubConceptsOption();
        } else if (e.getSource() == perfectMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedAmbiguityCntCheckBox
                || e.getSource() == partialMatchedComplexWordCheckBox || e.getSource() == partialMatchedMatchedWordBox
                || e.getSource() == perfectMatchedIsSystemAddedWordCheckBox) {
            perfectMatchedWordJList.repaint();
            partialMatchedWordJList.repaint();
        } else if (e.getSource() == partialMatchedShowOnlyRelatedComplexWordsCheckBox) {
            showOnlyRelatedComplexWords();
        } else if (e.getSource() == searchWordButton || e.getSource() == searchWordField) {
            String keyWord = searchWordField.getText();
            if (keyWord.length() == 0) {
                perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList")
                        + " (" + perfectMatchedWordModelSet.size() + ")");
                wordListViews[0].getViewProperties().setTitle(perfectMatchedWordJListTitle.getTitle());

                partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                        + " (" + partialMatchedWordModelSet.size() + ")");
                wordListViews[1].getViewProperties().setTitle(partialMatchedWordJListTitle.getTitle());

            } else {
                Set searchedPerfectMatchedWordModelSet = new TreeSet();
                Set searchedPartialMatchedWordModelSet = new TreeSet();
                for (InputWordModel iwModel : perfectMatchedWordModelSet) {
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        searchedPerfectMatchedWordModelSet.add(iwModel);
                    }
                }
                perfectMatchedWordJList.setListData(searchedPerfectMatchedWordModelSet.toArray());
                perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList")
                        + " (" + searchedPerfectMatchedWordModelSet.size() + "/" + perfectMatchedWordModelSet.size()
                        + ")");
                wordListViews[0].getViewProperties().setTitle(perfectMatchedWordJListTitle.getTitle());

                InputWordModel targetIWModel = (InputWordModel) perfectMatchedWordJList.getSelectedValue();
                if (targetIWModel == null && 0 < perfectMatchedWordJList.getModel().getSize()) {
                    targetIWModel = (InputWordModel) perfectMatchedWordJList.getModel().getElementAt(0);
                    perfectMatchedWordJList.setSelectedValue(targetIWModel, true);
                }
                for (InputWordModel iwModel : partialMatchedWordModelSet) {
                    if (iwModel.getWord().indexOf(keyWord) != -1) {
                        if (partialMatchedShowOnlyRelatedComplexWordsCheckBox.isSelected()) {
                            if (targetIWModel != null
                                    && iwModel.getMatchedWord().equals(targetIWModel.getMatchedWord())) {
                                searchedPartialMatchedWordModelSet.add(iwModel);
                            }
                        } else {
                            searchedPartialMatchedWordModelSet.add(iwModel);
                        }
                    }
                }

                partialMatchedWordJList.setListData(searchedPartialMatchedWordModelSet.toArray());
                partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList")
                        + " (" + searchedPartialMatchedWordModelSet.size() + "/" + partialMatchedWordModelSet.size()
                        + ")");
                wordListViews[1].getViewProperties().setTitle(partialMatchedWordJListTitle.getTitle());

            }
            wordListRootWindow.getWindow().repaint();
        }
    }

    /**
     * 入力文書中の入力単語を強調表示する
     */
    private void hilightInputWord() {
        if (hilightInputWordCheckBox.isSelected()) {
            JList wordJList = getTargetWordJList();
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            if (iwModel != null) {
                // String targetLines =
                // docSelectionPanel.getTargetTextLines(iwModel.getWord());
                String targetLines = docSelectionPanel.getTargetHtmlLines(iwModel.getWord());
                documentArea.setText(targetLines);
            }
        } else {
            documentArea.setText("");
        }
    }

    public boolean isPerfectMatchedAmbiguityCntCheckBox() {
        return perfectMatchedAmbiguityCntCheckBox.isSelected();
    }

    public boolean isPerfectMatchedSystemAddedWordCheckBox() {
        return perfectMatchedIsSystemAddedWordCheckBox.isSelected();
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

    /*
     * 特に現状では使っていない
     */
    public Action getSaveCompleteMatchWordWithComplexWordAction() {
        return saveCompleteMatchWordWithComplexWordAcion;
    }

    public Map<String, Set<Concept>> getWordCorrespondConceptSetMap() {
        return wordCorrespondConceptSetMap;
    }

    public Map<String, Set<Concept>> getWordConceptSetMap() {
        return wordConceptSetMap;
    }

    public void loadWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            loadWordCorrespondConceptSetMap(chooser.getSelectedFile());
        }
    }

    public void loadWordCorrespondConceptSetMap(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
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

            while (reader.ready()) {
                String line = reader.readLine();
                String[] wordURI = line.replaceAll("\n", "").split(",");
                if (0 < wordURI[0].length()) {
                    String word = wordURI[0];
                    InputWordModel iwModel = inputModule.makeInputWordModel(word);
                    if (iwModel != null && inputWordSet.contains(iwModel.getWord())) {
                        Set<Concept> correspondConceptSet = new HashSet<Concept>();
                        for (int i = 1; i < wordURI.length; i++) {
                            String uri = wordURI[i];
                            Concept c = DODDLEDic.getConcept(uri);
                            if (c != null) { // 参照していないオントロジーの概念と対応づけようとした場合にnullとなる
                                correspondConceptSet.add(c);
                            }
                        }
                        if (0 < correspondConceptSet.size()) {
                            wordCorrespondConceptSetMap.put(iwModel.getWord(), correspondConceptSet);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

    public Set<Concept> setInputConceptSet() {
        inputConceptSet = new HashSet<Concept>();
        if (inputWordModelSet == null) { return inputConceptSet; }
        for (InputWordModel iwModel : inputWordModelSet) {
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(iwModel.getWord());
            if (correspondConceptSet == null) {
                correspondConceptSet = new HashSet<Concept>();
            }
            if (iwModel.isPartialMatchWord()) {
                wordCorrespondConceptSetMap.put(iwModel.getWord(), correspondConceptSet);
            }
            if (correspondConceptSet.size() == 0) {
                Set<Concept> conceptSet = wordConceptSetMap.get(iwModel.getMatchedWord());
                if (conceptSet != null) {
                    Concept c = (Concept) conceptSet.toArray()[0];
                    correspondConceptSet.add(c);
                    wordCorrespondConceptSetMap.put(iwModel.getWord(), correspondConceptSet);
                }
            }
            if (correspondConceptSet.size() == 1 && correspondConceptSet.contains(nullConcept)) {
                complexConstructTreeOptionMap.remove(iwModel);
                continue;
            }
            if (iwModel.isPartialMatchWord()) {
                for (Concept c : correspondConceptSet) { // 最初の概念だけを扱っても良い．
                    ConstructTreeOption ctOption = new ConstructTreeOption(c);
                    complexConstructTreeOptionMap.put(iwModel, ctOption);
                }
            }
            for (Concept c : correspondConceptSet) {
                if (c != null) {
                    c.setInputLabel(new DODDLELiteral("", iwModel.getMatchedWord())); // メインとなる見出しを設定する
                    inputConceptSet.add(c);
                }
            }
        }
        return inputConceptSet;
    }

    public static Concept nullConcept = new Concept("null", Translator.getString("DisambiguationPanel.NotAvailable"));
    public static EvalConcept nullEvalConcept = new EvalConcept(null, -1);

    private void setPartialMatchedOptionButton(boolean t) {
        addAsSameConceptRadioButton.setEnabled(t);
        addAsSubConceptRadioButton.setEnabled(t);
    }

    private void selectAmbiguousConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty()) {
            String orgWord = ((InputWordModel) wordJList.getSelectedValue()).getWord();
            String selectedWord = ((InputWordModel) wordJList.getSelectedValue()).getMatchedWord();
            Set<Concept> conceptSet = wordConceptSetMap.get(selectedWord);

            Set<EvalConcept> evalConceptSet = null;

            if (!(wordEvalConceptSetMap == null || wordEvalConceptSetMap.get(selectedWord) == null)) {
                evalConceptSet = wordEvalConceptSetMap.get(selectedWord);
            } else {
                evalConceptSet = getEvalConceptSet(conceptSet);
                evalConceptSet.add(nullEvalConcept);
            }

            conceptSetJList.setListData(evalConceptSet.toArray());
            Set<Concept> correspondConceptSet = wordCorrespondConceptSetMap.get(orgWord);
            if (correspondConceptSet != null) {
                if (correspondConceptSet.size() == 1 && correspondConceptSet.contains(nullConcept)) {
                    conceptSetJList.setSelectedValue(nullEvalConcept, true);
                    setPartialMatchedOptionButton(false);
                    return;
                }

                int[] selectedIndices = new int[correspondConceptSet.size()];
                int index = 0;
                ListModel model = conceptSetJList.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    EvalConcept evalConcept = (EvalConcept) model.getElementAt(i);
                    if (evalConcept.getConcept() == null) {
                        continue;
                    }
                    if (correspondConceptSet.contains(evalConcept.getConcept())) {
                        selectedIndices[index++] = i;
                    }
                }
                conceptSetJList.setSelectedIndices(selectedIndices);
            } else {
                conceptSetJList.setSelectedIndex(0);
                EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                correspondConceptSet = new HashSet<Concept>();
                correspondConceptSet.add(evalConcept.getConcept());
                wordCorrespondConceptSetMap.put(orgWord, correspondConceptSet);
            }
            hilightInputWord();

            // 完全照合単語，システムが追加した完全照合単語，部分照合単語に応じて
            // 階層構築オプションパネルを切り替える
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            if (iwModel.isPartialMatchWord()) {
                switchConstructTreeOptionPanel(partialMatchedConstructTreeOptionPanel);
                setPartialMatchedOptionButton(true);
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    if (ctOption.getOption().equals("SAME")) {
                        addAsSameConceptRadioButton.setSelected(true);
                    } else {
                        addAsSubConceptRadioButton.setSelected(true);
                    }
                } else {
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    if (evalConcept != null) {
                        ctOption = new ConstructTreeOption(evalConcept.getConcept());
                        complexConstructTreeOptionMap.put(iwModel, ctOption);
                        addAsSameConceptRadioButton.setSelected(true);
                    }
                }
            } else if (iwModel.isSystemAdded()) {
                switchConstructTreeOptionPanel(systemAddedPerfectMatchedConstructTreeOptionPanel);
                if (complexConstructTreeOptionMap.get(iwModel) == null) {
                    replaceSubClassesCheckBox.setSelected(false);
                    EvalConcept evalConcept = (EvalConcept) conceptSetJList.getSelectedValue();
                    ConstructTreeOption ctOption = new ConstructTreeOption(evalConcept.getConcept());
                    complexConstructTreeOptionMap.put(iwModel, ctOption);
                } else {
                    ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                    replaceSubClassesCheckBox.setSelected(ctOption.isReplaceSubConcepts());
                }
            } else {
                switchConstructTreeOptionPanel(perfectMatchedConstructTreeOptionPanel);
            }
        }
    }

    private void switchConstructTreeOptionPanel(JPanel optionPanel) {
        constructTreeOptionPanel.remove(partialMatchedConstructTreeOptionPanel);
        constructTreeOptionPanel.remove(perfectMatchedConstructTreeOptionPanel);
        constructTreeOptionPanel.remove(systemAddedPerfectMatchedConstructTreeOptionPanel);
        constructTreeOptionPanel.add(optionPanel);
        constructTreeOptionPanel.validate();
        constructTreeOptionPanel.repaint();
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
        if (wordListRootWindow.getWindow().getLastFocusedChildWindow() == null) { return perfectMatchedWordJList; }
        DockingWindow lastFocusedWindow = wordListRootWindow.getWindow().getLastFocusedChildWindow();
        if (lastFocusedWindow.getTitle().equals(wordListViews[0].getViewProperties().getTitle())) {
            return perfectMatchedWordJList;
        } else if (lastFocusedWindow.getTitle().equals(wordListViews[1].getViewProperties().getTitle())) { return partialMatchedWordJList; }
        return perfectMatchedWordJList;
    }

    private Set<InputWordModel> getTargetWordModelSet() {
        if (getTargetWordJList() == perfectMatchedWordJList) {
            return perfectMatchedWordModelSet;
        } else if (getTargetWordJList() == partialMatchedWordJList) { return partialMatchedWordModelSet; }
        return perfectMatchedWordModelSet;
    }

    private void syncPartialMatchedAmbiguousConceptSet(String orgWord, Set<Concept> correspondConceptSet) {
        if (!perfectMatchedIsSyncCheckBox.isSelected()) { return; }
        for (InputWordModel iwModel : partialMatchedWordModelSet) {
            if (iwModel.getMatchedWord().equals(orgWord)) {
                wordCorrespondConceptSetMap.put(iwModel.getWord(), correspondConceptSet);
            }
        }
    }
    
    
    private void setLabelLangList() {
        Set<String> langSet = selectedConcept.getLangLabelListMap().keySet();
        labelLangJList.setListData(langSet.toArray());
        if (langSet.size() == 0) {
            return;
        }
        labelLangJList.setSelectedValue(DODDLE.LANG, true);
        if (labelLangJList.getSelectedValue() == null) {
            labelLangJList.setSelectedIndex(0);
        }
    }
    
    private void setLabelList() {
        DefaultListModel listModel = new DefaultListModel();
        Map<String, List<DODDLELiteral>> langLabelListMap = selectedConcept.getLangLabelListMap();        
        Object[] langList = labelLangJList.getSelectedValues();
        for (int i = 0; i < langList.length; i++) {
            if (langLabelListMap.get(langList[i]) != null) {
                for (DODDLELiteral label: langLabelListMap.get(langList[i])) {
                    listModel.addElement(label);  
                }
            }
        }
        labelJList.setModel(listModel);
    }
    
    private void setDescriptionLangList() {
        Set<String> langSet = selectedConcept.getLangDescriptionListMap().keySet();
        descriptionLangJList.setListData(langSet.toArray());
        if (langSet.size() == 0) {
            return;
        }
        descriptionLangJList.setSelectedValue(DODDLE.LANG, true);
        if (descriptionLangJList.getSelectedValue() == null) {
            descriptionLangJList.setSelectedIndex(0);
        }
    }
    
    private void setDescriptionList() {
        DefaultListModel listModel = new DefaultListModel();
        Map<String, List<DODDLELiteral>> langDescriptionListMap = selectedConcept.getLangDescriptionListMap();        
        Object[] langList = descriptionLangJList.getSelectedValues();
        for (int i = 0; i < langList.length; i++) {
            if (langDescriptionListMap.get(langList[i]) != null) {
                for (DODDLELiteral label: langDescriptionListMap.get(langList[i])) {
                listModel.addElement(label);  
                }
            }
        }
        descriptionJList.setModel(listModel);
    }

    private void selectCorrectConcept(JList wordJList) {
        if (!wordJList.isSelectionEmpty() && !conceptSetJList.isSelectionEmpty()) {
            InputWordModel iwModel = (InputWordModel) wordJList.getSelectedValue();
            Object[] evalConcepts = conceptSetJList.getSelectedValues();
            String word = iwModel.getWord();
            for (int i = 0; i < evalConcepts.length; i++) {
                if (evalConcepts[i] == nullEvalConcept) {
                    Set<Concept> correspondConceptSet = new HashSet<Concept>();
                    correspondConceptSet.add(nullConcept);
                    wordCorrespondConceptSetMap.put(word, correspondConceptSet);
                    syncPartialMatchedAmbiguousConceptSet(word, correspondConceptSet);
                    labelLangJList.setListData(new Object[0]);
                    labelJList.setListData(new Object[0]);
                    descriptionLangJList.setListData(new Object[0]);
                    descriptionJList.setListData(new Object[0]);
                    aroundConceptTree.setModel(new DefaultTreeModel(null));
                    switchConstructTreeOptionPanel(perfectMatchedConstructTreeOptionPanel);
                    return;
                }
            }

            if (getTargetWordJList() == perfectMatchedWordJList) {
                switchConstructTreeOptionPanel(perfectMatchedConstructTreeOptionPanel);
            } else {
                switchConstructTreeOptionPanel(partialMatchedConstructTreeOptionPanel);
            }

            Set<Concept> correspondConceptSet = new HashSet<Concept>();
            for (int i = 0; i < evalConcepts.length; i++) {
                correspondConceptSet.add(((EvalConcept) evalConcepts[i]).getConcept());
            }
            wordCorrespondConceptSetMap.put(word, correspondConceptSet);
            syncPartialMatchedAmbiguousConceptSet(word, correspondConceptSet);
            selectedConcept = ((EvalConcept) evalConcepts[0]).getConcept();
            if (selectedConcept != null) {
                setLabelLangList();
                setDescriptionLangList();
                setLabelList();
                setDescriptionList();
            }

            showAroundConceptTree();
            for (Concept c : correspondConceptSet) {
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                if (ctOption != null) {
                    ctOption.setConcept(c);
                    complexConstructTreeOptionMap.put(iwModel, ctOption);
                }
            }
        }
    }

    /**
     * 選択されている概念のオントロジー中のルートまでのパスを表示
     */
    private void showAroundConceptTree() {
        if (showAroundConceptTreeCheckBox.isSelected()) {
            Object[] ecs = conceptSetJList.getSelectedValues();
            Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
            for (int i = 0; i < ecs.length; i++) {
                EvalConcept ec = (EvalConcept) ecs[i];
                pathToRootSet.addAll(OWLOntologyManager.getPathToRootSet(ec.getConcept().getURI()));
                int pathSize = 0;
                for (List<Concept> pathToRoot : pathToRootSet) {
                    if (pathSize < pathToRoot.size()) {
                        pathSize = pathToRoot.size();
                    }
                }
                if (pathSize <= 1 && DODDLE.GENERAL_ONTOLOGY_NAMESPACE_SET.contains(ec.getConcept().getNameSpace())) {
                    pathToRootSet.clear();
                    if (ec.getConcept().getNameSpace().equals(DODDLE.EDR_URI)) {
                        pathToRootSet.addAll(EDRTree.getEDRTree().getPathToRootSet(ec.getConcept().getLocalName()));
                    } else if (ec.getConcept().getNameSpace().equals(DODDLE.EDRT_URI)) {
                        pathToRootSet.addAll(EDRTree.getEDRTTree().getPathToRootSet(ec.getConcept().getLocalName()));
                    } else if (ec.getConcept().getNameSpace().equals(DODDLE.WN_URI)) {
                        pathToRootSet.addAll(WordNetDic.getPathToRootSet(new Long(ec.getConcept().getLocalName())));
                    }
                }
            }
            TreeModel model = constructClassPanel.getDefaultConceptTreeModel(pathToRootSet,
                    ConceptTreeMaker.DODDLE_CLASS_ROOT_URI);
            aroundConceptTree.setModel(model);
            for (int i = 0; i < aroundConceptTree.getRowCount(); i++) {
                aroundConceptTree.expandPath(aroundConceptTree.getPathForRow(i));
            }
        } else {
            aroundConceptTree.setModel(new DefaultTreeModel(null));
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
        } else if (e.getSource() == labelLangJList) {
            setLabelList();
        } else if (e.getSource() == descriptionLangJList) {
            setDescriptionList();
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

        for (InputWordModel iwModel : inputWordModelSet) {
            if (iwModel.isPartialMatchWord()) {
                partialMatchedWordModelSet.add(iwModel);
            } else {
                perfectMatchedWordModelSet.add(iwModel);
            }
        }
        perfectMatchedWordJList.setListData(perfectMatchedWordModelSet.toArray());
        perfectMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PerfectMatchWordList") + " ("
                + perfectMatchedWordModelSet.size() + ")");
        wordListViews[0].getViewProperties().setTitle(perfectMatchedWordJListTitle.getTitle());

        partialMatchedWordJList.setListData(partialMatchedWordModelSet.toArray());
        partialMatchedWordJListTitle.setTitle(Translator.getString("DisambiguationPanel.PartialMatchWordList") + " ("
                + partialMatchedWordModelSet.size() + ")");
        wordListViews[1].getViewProperties().setTitle(partialMatchedWordJListTitle.getTitle());

        wordConceptSetMap = inputModule.getWordConceptSetMap();

        Set undefinedSet = inputModule.getUndefinedWordSet();
        DefaultListModel listModel = undefinedWordListPanel.getModel();
        listModel.clear();
        for (Iterator i = undefinedSet.iterator(); i.hasNext();) {
            listModel.addElement(i.next());
        }
        undefinedWordListPanel.setTitleWithSize();
        repaint(); // titledBorderのタイトルを再表示させるため
    }

    public void loadInputWordSet(File file) {
        if (!file.exists()) { return; }
        inputFile = file;
        Set<String> wordSet = new HashSet<String>();
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            while (reader.ready()) {
                String line = reader.readLine();
                String word = line.replaceAll("\n", "");
                wordSet.add(word);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        loadInputWordSet(wordSet);
    }

    private Set<String> wordSet;

    private void clearPanel() {
        conceptSetJList.setListData(new Object[0]);
        clearConceptInfoPanel();
        documentArea.setText("");
        aroundConceptTree.setModel(new DefaultTreeModel(null));
    }

    public void loadInputWordSet(Set<String> ws) {
        wordSet = ws;
        perfectMatchedWordJList.clearSelection();
        partialMatchedWordJList.clearSelection();
        undefinedWordListPanel.clearSelection();
        complexConstructTreeOptionMap.clear();
        clearPanel();

        if (DODDLE.IS_USING_DB) {
            inputModule.initDataWithDB(wordSet);
        } else {
            inputModule.initDataWithMem(wordSet);
        }
        initWordList();
        project.getConceptDefinitionPanel().setInputConceptSet();
    }

    public void addInputWordSet(Set<String> ws) {
        clearPanel();
        if (wordSet == null) {
            wordSet = new HashSet<String>();
        }
        wordSet.addAll(ws);
        if (DODDLE.IS_USING_DB) {
            inputModule.initDataWithDB(wordSet);
        } else {
            inputModule.initDataWithMem(wordSet);
        }
        initWordList();
        project.getConceptDefinitionPanel().setInputConceptSet();
    }

    public Set<Concept> getInputConceptSet() {
        return inputConceptSet;
    }

    public Set<Concept> getInputNounConceptSet() {
        return inputNounConceptSet;
    }

    public Set<Concept> getInputVerbConceptSet() {
        return inputVerbConceptSet;
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
                conceptDescriptionFrame.setConcept(evalConcept.getConcept().getURI());
                setInputConceptSet();
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

        private Map<Concept, EvalConcept> getConceptEvalConceptMap() {
            Map<Concept, EvalConcept> conceptEvalConceptMap = new HashMap<Concept, EvalConcept>();
            for (InputWordModel inputWordModel : perfectMatchedWordModelSet) {
                String inputWord = inputWordModel.getMatchedWord();
                // for (String inputWord : wordSet) {
                for (Concept c : wordConceptSetMap.get(inputWord)) {
                    if (conceptEvalConceptMap.get(c) == null) {
                        conceptEvalConceptMap.put(c, new EvalConcept(c, 0));
                    }
                }
            }
            return conceptEvalConceptMap;
        }

        private void calcEvalValueUsingSpreadActivatingAlgorithm(int i, Concept c1, EvalConcept ec1,
                Object[] allDisambiguationCandidate, Map<Concept, EvalConcept> conceptEvalConceptMap) {
            for (int j = i + 1; j < allDisambiguationCandidate.length; j++) {
                c1 = (Concept) allDisambiguationCandidate[i];
                ec1 = conceptEvalConceptMap.get(c1);
                Concept c2 = (Concept) allDisambiguationCandidate[j];
                EvalConcept ec2 = conceptEvalConceptMap.get(c2);
                double ev = 0;
                if (OptionDialog.isCheckShortestSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getShortestConceptDiff(c1, c2);
                } else if (OptionDialog.isCheckLongestSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getLongestConceptDiff(c1, c2);
                } else if (OptionDialog.isCheckAverageSpreadActivation()) {
                    ev = CalcConceptDistanceUtil.getAverageConceptDiff(c1, c2);
                }
                if (0 < ev) {
                    ec1.setEvalValue(ec1.getEvalValue() + (1 / ev));
                    ec2.setEvalValue(ec2.getEvalValue() + (1 / ev));
                }
            }
        }

        private void calcEvalValueUsingSupSubSibConcepts(Concept c, EvalConcept ec) {
            int evalValue = 0;
            if (OptionDialog.isCheckSupConcepts()) {
                evalValue += cntRelevantSupConcepts(c);
            }
            if (OptionDialog.isCheckSubConcepts()) {
                evalValue += cntRelevantSubConcepts(c);
            }
            if (OptionDialog.isCheckSiblingConcepts()) {
                evalValue += cntRelevantSiblingConcepts(c);
            }
            ec.setEvalValue(ec.getEvalValue() + evalValue);
        }

        /**
         * 
         * 多義性のある概念リストと入力語彙を入力として，評価値つき概念リストを返すメソッド
         * 
         */
        public void setWordEvalConceptSetMap(Set<InputWordModel> inputWordSet) {
            if (inputWordModelSet == null) { return; }
            wordSet = new HashSet<String>();
            for (InputWordModel iwModel : perfectMatchedWordModelSet) {
                wordSet.add(iwModel.getWord());
            }
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();
            Map<Concept, EvalConcept> conceptEvalConceptMap = getConceptEvalConceptMap();

            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(conceptEvalConceptMap.keySet().size());
            Object[] allDisambiguationCandidate = conceptEvalConceptMap.keySet().toArray();
            for (int i = 0; i < allDisambiguationCandidate.length; i++) {
                Concept c = (Concept) allDisambiguationCandidate[i];
                EvalConcept ec = conceptEvalConceptMap.get(c);
                if (OptionDialog.isUsingSpreadActivatingAlgorithm()) {
                    calcEvalValueUsingSpreadActivatingAlgorithm(i, c, ec, allDisambiguationCandidate,
                            conceptEvalConceptMap);
                }
                calcEvalValueUsingSupSubSibConcepts(c, ec);
                DODDLE.STATUS_BAR.addValue();
            }

            for (InputWordModel inputWordModel : perfectMatchedWordModelSet) {
                String inputWord = inputWordModel.getMatchedWord();
                Set<Concept> conceptSet = wordConceptSetMap.get(inputWord);
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (Concept c : conceptSet) {
                    if (conceptEvalConceptMap.get(c) != null) {
                        evalConceptSet.add(conceptEvalConceptMap.get(c));
                    }
                }
                evalConceptSet.add(nullEvalConcept);
                wordEvalConceptSetMap.put(inputWord, evalConceptSet);
            }
            DODDLE.STATUS_BAR.hideProgressBar();
        }

        private Set getSiblingConceptSet(Concept c) {
            Set siblingConceptSet = null;
            if (c.getNameSpace().equals(DODDLE.EDR_URI)) {
                siblingConceptSet = EDRTree.getEDRTree().getSiblingConceptSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.EDRT_URI)) {
                siblingConceptSet = EDRTree.getEDRTTree().getSiblingConceptSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.WN_URI)) {
                siblingConceptSet = WordNetDic.getSiblingConceptSet(new Long(c.getLocalName()));
            }
            return siblingConceptSet;
        }

        private Set getSubConceptSet(Concept c) {
            Set subConceptSet = null;
            if (c.getNameSpace().equals(DODDLE.EDR_URI)) {
                subConceptSet = EDRTree.getEDRTree().getSubConceptSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.EDRT_URI)) {
                subConceptSet = EDRTree.getEDRTTree().getSubConceptSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.WN_URI)) {
                subConceptSet = WordNetDic.getSubConceptSet(new Long(c.getLocalName()));
            }
            return subConceptSet;
        }

        private Set getPathToRootSet(Concept c) {
            Set pathSet = null;
            if (c.getNameSpace().equals(DODDLE.EDR_URI)) {
                pathSet = EDRTree.getEDRTree().getPathToRootSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.EDRT_URI)) {
                pathSet = EDRTree.getEDRTTree().getPathToRootSet(c.getLocalName());
            } else if (c.getNameSpace().equals(DODDLE.WN_URI)) {
                pathSet = WordNetDic.getPathToRootSet(new Long(c.getLocalName()));
            }
            return pathSet;
        }

        private int getMaxEvalValue(Set<Collection> pathSet, String uri) {
            if (pathSet == null) { return 0; }
            int maxEvalValue = 0;
            for (Collection<Concept> path : pathSet) {
                int evalValue = 0;
                for (Concept sc : path) {
                    if (sc == null) {
                        continue;
                    }
                    if (sc.getURI().equals(uri)) {
                        continue;
                    }
                    Concept c = DODDLEDic.getConcept(sc.getURI());
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

        private int cntRelevantSiblingConcepts(Concept c) {
            Set pathSet = getSiblingConceptSet(c);
            return getMaxEvalValue(pathSet, c.getURI());
        }

        private int cntRelevantSupConcepts(Concept c) {
            Set pathSet = getPathToRootSet(c);
            return getMaxEvalValue(pathSet, c.getURI());
        }

        private int cntRelevantSubConcepts(Concept c) {
            Set pathSet = getSubConceptSet(c);
            return getMaxEvalValue(pathSet, c.getURI());
        }

        private boolean isIncludeInputWords(Set wordSet, Concept c) {
            if (c == null) { return false; }
            Map<String, List<DODDLELiteral>> langLabelListMap = c.getLangLabelListMap();
            for (List<DODDLELiteral> labelList: langLabelListMap.values()) {
                for (DODDLELiteral label: labelList) {
                    if (wordSet.contains(label.getString())) { return true; }
                }
            }
            Map<String, List<DODDLELiteral>> langDescriptionListMap = c.getLangDescriptionListMap();
            for (List<DODDLELiteral> descriptionList: langDescriptionListMap.values()) {
                for (DODDLELiteral description: descriptionList) {
                    if (wordSet.contains(description.getString())) { return true; }
                }
            }
            return false;
        }

        public void doDisAmbiguation() {
            SwingWorker<String, String> worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    setWordEvalConceptSetMap(getInputWordModelSet());
                    return "done";
                }
            };
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }

        public void doDisambiguationTest() {
            new Thread() {
                public void run() {
                    OptionDialog.setUsingSpreadActivationAlgorithmForDisambiguation(false);
                    boolean[][] pattern = { { true, false, false}, { false, true, false}, { false, false, true},
                            { true, true, false}, { true, false, true}, { false, true, true}, { true, true, true}};
                    String[] patternName = { "Sup.txt", "Sub.txt", "Sib.txt", "Sup_Sub.txt", "Sup_Sib.txt",
                            "Sib_Sub.txt", "Sup_Sub_Sib.txt"};

                    for (int i = 0; i < pattern.length; i++) {
                        System.out.println(pattern[i][0] + " " + pattern[i][1] + " " + pattern[i][2]);
                        OptionDialog.setSupDisambiguation(pattern[i][0]);
                        OptionDialog.setSubDisambiguation(pattern[i][1]);
                        OptionDialog.setSiblingDisambiguation(pattern[i][2]);
                        setWordEvalConceptSetMap(getInputWordModelSet());
                        saveWordEvalConceptSet(new File(patternName[i]));
                    }
                    OptionDialog.setSupDisambiguation(false);
                    OptionDialog.setSubDisambiguation(false);
                    OptionDialog.setSiblingDisambiguation(false);
                    OptionDialog.setUsingSpreadActivationAlgorithmForDisambiguation(true);

                    OptionDialog.setShortestSpreadActivatingAlgorithmforDisambiguation(true);
                    System.out.println("shortest");
                    setWordEvalConceptSetMap(getInputWordModelSet());
                    saveWordEvalConceptSet(new File("shortest.txt"));

                    OptionDialog.setLongestSpreadActivatingAlgorithmforDisambiguation(true);
                    System.out.println("longest");
                    setWordEvalConceptSetMap(getInputWordModelSet());
                    saveWordEvalConceptSet(new File("longest.txt"));

                    OptionDialog.setAverageSpreadActivatingAlgorithmforDisambiguation(true);
                    System.out.println("average");
                    setWordEvalConceptSetMap(getInputWordModelSet());
                    saveWordEvalConceptSet(new File("average.txt"));

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
            list.setBorder(BorderFactory.createTitledBorder("入力されたすべての単語(" + allWordSet.size() + ")"));
            list.setListData(allWordSet.toArray());
            JScrollPane listScroll = new JScrollPane(list);
            frame.getContentPane().add(listScroll);
            frame.setBounds(50, 50, 200, 600);
            frame.setVisible(true);
        }
    }

    public void saveConstructTreeOption(File file) {
        BufferedWriter writer = null;
        try {
            Properties properties = new Properties();
            properties.setProperty("ConstructTree.isTreeConstruction", String.valueOf(perfectMatchedOptionPanel
                    .isConstruction()));
            properties.setProperty("ConstructTree.isTrimmingInternalNode", String.valueOf(perfectMatchedOptionPanel
                    .isTrimming()));
            properties.setProperty("ConstructTree.isConstructionWithComplexWordTree", String
                    .valueOf(partialMatchedOptionPanel.isConstruction()));
            properties.setProperty("ConstructTree.isTrimmingInternalNodeWithComplexWordTree", String
                    .valueOf(partialMatchedOptionPanel.isTrimming()));
            properties.setProperty("ConstructTree.isAddAbstractConceptWithComplexWordTree", String
                    .valueOf(partialMatchedOptionPanel.isAddAbstractConcept()));
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            properties.store(writer, "Construct Tree Option");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void loadConstructTreeOption(File file) {
        if (!file.exists()) { return; }
        BufferedReader reader = null;
        try {
            Properties properties = new Properties();
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            properties.load(reader);
            boolean t = new Boolean(properties.getProperty("ConstructTree.isTreeConstruction"));
            perfectMatchedOptionPanel.setConstruction(t);
            t = new Boolean(properties.getProperty("ConstructTree.isTrimmingInternalNode"));
            perfectMatchedOptionPanel.setTrimming(t);
            t = new Boolean(properties.getProperty("ConstructTree.isConstructionWithComplexWordTree"));
            partialMatchedOptionPanel.setConstruction(t);
            t = new Boolean(properties.getProperty("ConstructTree.isTrimmingInternalNodeWithComplexWordTree"));
            partialMatchedOptionPanel.setTrimming(t);
            t = new Boolean(properties.getProperty("ConstructTree.isAddAbstractConceptWithComplexWordTree"));
            partialMatchedOptionPanel.setAddAbstractConcept(t);
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

    public void saveInputWordConstructTreeOptionSet(File file) {
        if (complexConstructTreeOptionMap == null) { return; }
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : complexConstructTreeOptionMap.keySet()) {
                ConstructTreeOption ctOption = complexConstructTreeOptionMap.get(iwModel);
                if (iwModel != null && ctOption != null && ctOption.getConcept() != null) {
                    buf.append(iwModel.getWord() + "\t" + ctOption.getConcept().getURI() + "\t" + ctOption.getOption()
                            + "\n");
                }
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void loadInputWordConstructTreeOptionSet(File file) {
        if (!file.exists()) { return; }
        complexConstructTreeOptionMap = new HashMap<InputWordModel, ConstructTreeOption>();
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] strs = line.split("\t");
                String iw = strs[0];
                String uri = strs[1];
                String opt = strs[2];
                if (0 < iw.length()) {
                    InputWordModel iwModel = inputModule.makeInputWordModel(iw);
                    complexConstructTreeOptionMap.put(iwModel, new ConstructTreeOption(DODDLEDic.getConcept(uri), opt));
                }
            }
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

    public InputWordModel makeInputWordModel(String iw) {
        return inputModule.makeInputWordModel(iw);
    }

    public InputWordModel makeInputWordModel(String iw, Map<String, Set<Concept>> wcSetMap) {
        return inputModule.makeInputWordModel(iw.replaceAll("_", " "), wcSetMap);
    }

    public void saveInputWordSet(File file) {
        if (inputWordModelSet == null) { return; }
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                if (!iwModel.isSystemAdded()) {
                    buf.append(iwModel.getWord() + "\n");
                }
            }
            for (Iterator i = inputModule.getUndefinedWordSet().iterator(); i.hasNext();) {
                buf.append(i.next() + "\n");
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void loadUndefinedWordSet(File file) {
        if (!file.exists()) { return; }
        DefaultListModel undefinedWordListModel = undefinedWordListPanel.getModel();
        undefinedWordListModel.clear();
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-16"));
            while (reader.ready()) {
                String line = reader.readLine();
                undefinedWordListModel.addElement(line);
            }
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
        constructClassPanel.setUndefinedWordListModel(undefinedWordListModel);
        constructPropertyPanel.setUndefinedWordListModel(undefinedWordListModel);
    }

    public void loadInputConceptSet(File file) {
        if (!file.exists()) { return; }
        inputConceptSet = new HashSet<Concept>();
        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while (reader.ready()) {
                String uri = reader.readLine();
                inputConceptSet.add(DODDLEDic.getConcept(uri));
            }
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

    public void saveUndefinedWordSet(File file) {
        DefaultListModel undefinedWordListModel = undefinedWordListPanel.getModel();
        if (undefinedWordListModel == null) { return; }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-16"));
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < undefinedWordListModel.getSize(); i++) {
                String undefinedWord = (String) undefinedWordListModel.getElementAt(i);
                buf.append(undefinedWord);
                buf.append("\n");
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void saveInputConceptSet(File file) {
        if (inputConceptSet == null) { return; }
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            StringBuffer buf = new StringBuffer();
            for (Concept c : inputConceptSet) {
                buf.append(c.getURI() + "\n");
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void saveWordCorrespondConceptSetMap(File file) {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            StringBuffer buf = new StringBuffer();
            for (Iterator i = getWordCorrespondConceptSetMap().entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                String word = (String) entry.getKey();
                Set<Concept> conceptSet = (Set<Concept>) entry.getValue();
                buf.append(word + ",");
                for (Concept c : conceptSet) {
                    buf.append(c.getURI() + ",");
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void saveWordConceptMap() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordCorrespondConceptSetMap(file);
        }
    }

    public void saveWordEvalConceptSet(File file) {
        if (wordEvalConceptSetMap == null || inputWordModelSet == null) { return; }
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            StringBuffer buf = new StringBuffer();
            for (InputWordModel iwModel : inputWordModelSet) {
                Set<EvalConcept> evalConceptSet = wordEvalConceptSetMap.get(iwModel.getMatchedWord());
                if (evalConceptSet == null) {
                    continue;
                }
                buf.append(iwModel.getWord());
                double evalValue = -1;
                for (EvalConcept ec : evalConceptSet) {
                    if (evalValue == ec.getEvalValue()) {
                        buf.append("\t" + ec.getConcept().getURI());
                    } else {
                        if (ec.getConcept() != null) {
                            buf.append("||" + ec.getEvalValue() + "\t" + ec.getConcept().getURI());
                            evalValue = ec.getEvalValue();
                        }
                    }
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public void savePerfectMatchedWord() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            savePerfectMatchedWord(file);
        }
    }

    private void savePerfectMatchedWord(File file) {
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            for (InputWordModel iwModel : perfectMatchedWordModelSet) {
                writer.write(iwModel.getWord() + "\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
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
        BufferedWriter writer = null;
        try {
            OutputStream os = new FileOutputStream(file);
            writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            Map completeWordComplexWordMap = new TreeMap();
            for (InputWordModel iwModel : inputWordModelSet) {
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
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
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
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            wordEvalConceptSetMap = new HashMap<String, Set<EvalConcept>>();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] wordAndResults = line.split("\\|\\|");
                String word = wordAndResults[0];
                Set<EvalConcept> evalConceptSet = new TreeSet<EvalConcept>();
                for (int i = 1; i < wordAndResults.length; i++) {
                    String[] valueAndURIs = wordAndResults[i].split("\t");
                    double value = Double.parseDouble(valueAndURIs[0]);
                    for (int j = 1; j < valueAndURIs.length; j++) {
                        String uri = valueAndURIs[j];
                        Concept c = DODDLEDic.getConcept(uri);
                        evalConceptSet.add(new EvalConcept(c, value));
                    }
                }
                wordEvalConceptSetMap.put(word, evalConceptSet);
            }
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
}