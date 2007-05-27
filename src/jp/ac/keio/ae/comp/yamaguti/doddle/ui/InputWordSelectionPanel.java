package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * @author takeshi morita
 */
public class InputWordSelectionPanel extends JPanel implements ActionListener {

    private JTextArea inputWordArea;
    private WordInfoTablePanel wordInfoTablePanel;
    private WordInfoTablePanel removedWordInfoTablePanel;
    private InputWordDocumentViewer documentViewer;

    private JButton addInputWordButton;
    private JButton deleteTableItemButton;
    private JButton returnTableItemButton;
    private JButton reloadDocumentAreaButton;
    private JButton setInputWordSetButton;
    private JButton addInputWordSetButton;

    private View[] mainViews;
    private RootWindow rootWindow;
    private DisambiguationPanel disambiguationPanel;

    public InputWordSelectionPanel(DisambiguationPanel ui) {
        wordInfoTablePanel = new WordInfoTablePanel();
        removedWordInfoTablePanel = new WordInfoTablePanel();
        documentViewer = new InputWordDocumentViewer();

        System.setProperty("sen.home", DODDLEConstants.SEN_HOME);
        disambiguationPanel = ui;

        inputWordArea = new JTextArea(10, 15);
        JScrollPane inputWordsAreaScroll = new JScrollPane(inputWordArea);

        addInputWordButton = new JButton(Translator.getTerm("AddInputWordListButton"));        
        addInputWordButton.addActionListener(this);
        deleteTableItemButton = new JButton(Translator.getTerm("RemoveButton"));
        deleteTableItemButton.addActionListener(this);
        returnTableItemButton = new JButton(Translator.getTerm("ReturnButton"));
        returnTableItemButton.addActionListener(this);
        reloadDocumentAreaButton = new JButton(Translator.getTerm("ReloadButton"));
        reloadDocumentAreaButton.addActionListener(this);
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addInputWordButton);
        tableButtonPanel.add(deleteTableItemButton);
        tableButtonPanel.add(returnTableItemButton);
        tableButtonPanel.add(reloadDocumentAreaButton);

        setInputWordSetButton = new JButton(Translator.getTerm("SetInputWordSetButton"));
        setInputWordSetButton.addActionListener(this);
        addInputWordSetButton = new JButton(Translator.getTerm("AddInputWordSetButton"));
        addInputWordSetButton.addActionListener(this);
        JPanel inputWordsButtonPanel = new JPanel();
        inputWordsButtonPanel.add(setInputWordSetButton);
        inputWordsButtonPanel.add(addInputWordSetButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(tableButtonPanel, BorderLayout.WEST);
        buttonPanel.add(inputWordsButtonPanel, BorderLayout.EAST);

        DockingWindowAction action = new DockingWindowAction();
        mainViews = new View[4];
        ViewMap viewMap = new ViewMap();
        mainViews[0] = new View(Translator.getTerm("CorrectWordInfoTablePanel"), null, wordInfoTablePanel);
        mainViews[1] = new View(Translator.getTerm("RemovedWordInfoTablePanel"), null, removedWordInfoTablePanel);
        mainViews[2] = new View(Translator.getTerm("InputDocumentViewerPanel"), null, documentViewer);
        mainViews[3] = new View(Translator.getTerm("InputWordListArea"), null, inputWordsAreaScroll);
        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        rootWindow.addListener(action);
        action.viewFocusChanged(mainViews[2], mainViews[0]);

        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setXGALayout() {
        TabWindow tabWindow = new TabWindow(new DockingWindow[] { mainViews[0], mainViews[1], mainViews[2]});
        SplitWindow sw1 = new SplitWindow(true, 0.8f, tabWindow, mainViews[3]);
        rootWindow.setWindow(sw1);
        mainViews[0].restoreFocus();
    }

    public void setUXGALayout() {
        SplitWindow sw1 = new SplitWindow(true, mainViews[1], mainViews[2]);
        SplitWindow sw2 = new SplitWindow(false, mainViews[0], sw1);
        SplitWindow sw3 = new SplitWindow(true, 0.8f, sw2, mainViews[3]);
        rootWindow.setWindow(sw3);
        mainViews[0].restoreFocus();
    }

    public void setWindowTitle() {
        mainViews[0].getViewProperties().setTitle(
                Translator.getTerm("CorrectWordInfoTablePanel") + "（" + wordInfoTablePanel.getTableSize() + "）");
        mainViews[1].getViewProperties().setTitle(
                Translator.getTerm("RemovedWordInfoTablePanel") + "（" + removedWordInfoTablePanel.getTableSize() + "）");
        rootWindow.repaint();
    }

    public void setWordInfoTableModel(Map<String, WordInfo> wordInfoMap, int docNum) {
        wordInfoTablePanel.setWordInfoTableModel(wordInfoMap, docNum);
        removedWordInfoTablePanel.setWordInfoTableModel(new HashMap<String, WordInfo>(), docNum);
        documentViewer.setTableModel(wordInfoTablePanel.getTableModel(), removedWordInfoTablePanel.getTableModel());
        setWindowTitle();
    }

    public void setInputDocumentListModel(ListModel listModel) {
        documentViewer.setDocumentList(listModel);
    }

    public void loadWordInfoTable() {
        wordInfoTablePanel.loadWordInfoTable();
        removedWordInfoTablePanel.loadWordInfoTable();
        setWindowTitle();
    }

    public void loadWordInfoTable(File file, File removedFile) {
        wordInfoTablePanel.loadWordInfoTable(file);
        removedWordInfoTablePanel.loadWordInfoTable(removedFile);
        setWindowTitle();
    }

    public void saveWordInfoTable() {
        wordInfoTablePanel.saveWordInfoTable();
        removedWordInfoTablePanel.saveWordInfoTable();
    }

    public void saveWordInfoTable(File file, File removedFile) {
        wordInfoTablePanel.saveWordInfoTable(file);
        removedWordInfoTablePanel.saveWordInfoTable(removedFile);
    }

    private void setInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.loadInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLEConstants.DISAMBIGUATION_PANEL);
    }

    private void addInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getTerm("SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.addInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLEConstants.DISAMBIGUATION_PANEL);
    }

    private void addInputWords() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        int[] rows = wordInfoTable.getSelectedRows();
        StringBuilder inputWords = new StringBuilder("");
        for (int i = 0; i < rows.length; i++) {
            String word = (String) wordInfoTable.getValueAt(rows[i], 0);
            inputWords.append(word + "\n");
        }
        inputWordArea.setText(inputWordArea.getText() + inputWords.toString());
    }

    private void deleteTableItems() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        JTable removedWordInfoTable = removedWordInfoTablePanel.getWordInfoTable();
        DefaultTableModel wordInfoTableModel = (DefaultTableModel) wordInfoTable.getModel();
        DefaultTableModel removedWordInfoTableModel = (DefaultTableModel) removedWordInfoTable.getModel();

        int[] rows = wordInfoTable.getSelectedRows();
        Set<String> deleteWordSet = new HashSet<String>();
        for (int i = 0; i < rows.length; i++) {
            String deleteWord = (String) wordInfoTable.getValueAt(rows[i], 0);
            deleteWordSet.add(deleteWord);
            WordInfo info = wordInfoTablePanel.getWordInfo(deleteWord);
            removedWordInfoTablePanel.addWordInfoMapKey(deleteWord, info);
            wordInfoTablePanel.removeWordInfoMapKey(deleteWord);
        }
        for (int i = 0; i < wordInfoTableModel.getRowCount(); i++) {
            String word = (String) wordInfoTableModel.getValueAt(i, 0);
            if (deleteWordSet.contains(word)) {
                Vector rowData = new Vector();
                for (int j = 0; j < wordInfoTableModel.getColumnCount(); j++) {
                    rowData.add(wordInfoTableModel.getValueAt(i, j));
                }
                removedWordInfoTableModel.insertRow(0, rowData);
                wordInfoTableModel.removeRow(i);
                i = 0;
                continue;
            }
        }
        setWindowTitle();
    }

    private void returnTableItems() {
        JTable wordInfoTable = wordInfoTablePanel.getWordInfoTable();
        JTable removedWordInfoTable = removedWordInfoTablePanel.getWordInfoTable();
        DefaultTableModel wordInfoTableModel = (DefaultTableModel) wordInfoTable.getModel();
        DefaultTableModel removedWordInfoTableModel = (DefaultTableModel) removedWordInfoTable.getModel();

        int[] rows = removedWordInfoTable.getSelectedRows();
        Set<String> returnWordSet = new HashSet<String>();
        for (int i = 0; i < rows.length; i++) {
            String returnWord = (String) removedWordInfoTable.getValueAt(rows[i], 0);
            returnWordSet.add(returnWord);
            WordInfo info = removedWordInfoTablePanel.getWordInfo(returnWord);
            wordInfoTablePanel.addWordInfoMapKey(returnWord, info);
            removedWordInfoTablePanel.removeWordInfoMapKey(returnWord);
        }
        for (int i = 0; i < removedWordInfoTableModel.getRowCount(); i++) {
            String word = (String) removedWordInfoTableModel.getValueAt(i, 0);
            if (returnWordSet.contains(word)) {
                Vector rowData = new Vector();
                for (int j = 0; j < wordInfoTableModel.getColumnCount(); j++) {
                    rowData.add(removedWordInfoTableModel.getValueAt(i, j));
                }
                wordInfoTableModel.insertRow(0, rowData);
                removedWordInfoTableModel.removeRow(i);
                i = 0;
                continue;
            }
        }
        setWindowTitle();
    }

    public void actionPerformed(ActionEvent e) {
        DODDLEProject project = DODDLE.getCurrentProject();

        if (e.getSource() == setInputWordSetButton) {
            setInputWordSet();
            project.addLog("SetInputWordSetButton");
        } else if (e.getSource() == addInputWordSetButton) {
            addInputWordSet();
            project.addLog("AddInputWordSetButton");
        } else if (e.getSource() == addInputWordButton) {
            addInputWords();
            project.addLog("AddInputWordListButton");
        } else if (e.getSource() == deleteTableItemButton) {
            deleteTableItems();
            project.addLog("RemoveButton", "InputWordSelectionPanel");
        } else if (e.getSource() == returnTableItemButton) {
            returnTableItems();
            project.addLog("ReturnButton", "InputWordSelectionPanel");
        } else if (e.getSource() == reloadDocumentAreaButton) {
            documentViewer.setDocumentArea();
        }
    }

    class DockingWindowAction extends DockingWindowAdapter {
        public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
            if (focusedView == mainViews[0]) {
                addInputWordButton.setVisible(true);
                deleteTableItemButton.setVisible(true);
                returnTableItemButton.setVisible(false);
                reloadDocumentAreaButton.setVisible(false);
            } else if (focusedView == mainViews[1]) {
                addInputWordButton.setVisible(false);
                deleteTableItemButton.setVisible(false);
                returnTableItemButton.setVisible(true);
                reloadDocumentAreaButton.setVisible(false);
            } else if (focusedView == mainViews[2]) {
                addInputWordButton.setVisible(false);
                deleteTableItemButton.setVisible(false);
                returnTableItemButton.setVisible(false);
                reloadDocumentAreaButton.setVisible(true);
            }
        }
    }
}
