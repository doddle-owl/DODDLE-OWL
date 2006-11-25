package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class InputWordSelectionPanel extends JPanel implements ActionListener {

    private JTable wordInfoTable;
    private TableRowSorter<TableModel> rowSorter;
    private WordInfoTableModel wordInfoTableModel;
    private JTextField searchWordField;
    private JTextField searchPOSField;

    private JTextArea inputWordArea;

    private int docNum;
    private Map wordInfoMap;

    private TitledBorder wordInfoTableBorder;

    private JButton addInputWordButton;
    private JButton deleteTableItemButton;
    private JButton setInputWordSetButton;
    private JButton addInputWordSetButton;

    private DisambiguationPanel disambiguationPanel;

    public static String UPPER_CONCEPT_LIST = "./upperConceptList.txt";

    public InputWordSelectionPanel(DisambiguationPanel ui) {
        searchWordField = new JTextField(20);
        searchWordField.addActionListener(this);
        searchWordField.setBorder(BorderFactory.createTitledBorder("Word Filter"));
        searchPOSField = new JTextField(20);
        searchPOSField.addActionListener(this);
        searchPOSField.setBorder(BorderFactory.createTitledBorder("POS Filter"));
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchWordField);
        searchPanel.add(searchPOSField);
        
        System.setProperty("sen.home", DODDLE.DODDLE_DIC + "sen-1.2.1");
        disambiguationPanel = ui;
        wordInfoTable = new JTable();
        JScrollPane wordInfoTableScroll = new JScrollPane(wordInfoTable);
        setWordInfoTableModel(null, 0);

        wordInfoTableBorder = BorderFactory.createTitledBorder(Translator
                .getString("InputWordSelectionPanel.ExtractedWordTable"));
        wordInfoTableScroll.setBorder(wordInfoTableBorder);

        inputWordArea = new JTextArea(10, 20);
        JScrollPane inputWordsAreaScroll = new JScrollPane(inputWordArea);
        inputWordsAreaScroll.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("InputWordSelectionPanel.InputWordList")));

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(wordInfoTableScroll, BorderLayout.CENTER);
        centerPanel.add(inputWordsAreaScroll, BorderLayout.EAST);

        addInputWordButton = new JButton(Translator.getString("InputWordSelectionPanel.AddInputWords"));
        addInputWordButton.addActionListener(this);
        deleteTableItemButton = new JButton(Translator.getString("InputWordSelectionPanel.Remove"));
        deleteTableItemButton.addActionListener(this);
        JPanel tableButtonPanel = new JPanel();
        tableButtonPanel.add(addInputWordButton);
        tableButtonPanel.add(deleteTableItemButton);

        setInputWordSetButton = new JButton(Translator.getString("InputWordSelectionPanel.SetInputWordSet"));
        setInputWordSetButton.addActionListener(this);
        addInputWordSetButton = new JButton(Translator.getString("InputWordSelectionPanel.AddInputWordSet"));
        addInputWordSetButton.addActionListener(this);
        JPanel inputWordsButtonPanel = new JPanel();
        inputWordsButtonPanel.add(setInputWordSetButton);
        inputWordsButtonPanel.add(addInputWordSetButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(tableButtonPanel, BorderLayout.WEST);
        buttonPanel.add(inputWordsButtonPanel, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void loadWordInfoTable() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadWordInfoTable(file);
        }
    }

    public void loadWordInfoTable(File loadFile) {
        if (!loadFile.exists()) { return; }
        wordInfoMap = new HashMap();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(loadFile));
            String line = reader.readLine();
            docNum = new Integer(line.split("=")[1]).intValue();
            while ((line = reader.readLine()) != null) {
                String[] items = line.split("\t");
                String word = items[0];
                WordInfo info = new WordInfo(word, docNum);
                String[] posSet = items[1].split(":");
                for (int i = 0; i < posSet.length; i++) {
                    info.addPos(posSet[i]);
                }
                String[] docSet = items[5].split(":");
                for (int i = 0; i < docSet.length; i++) {
                    if (docSet[i].split("=").length != 2) {
                        continue;
                    }
                    String doc = docSet[i].split("=")[0];
                    Integer num = new Integer(docSet[i].split("=")[1]);
                    info.putDoc(new File(doc), num);
                }
                String[] inputDocSet = items[6].split(":");
                for (int i = 0; i < inputDocSet.length; i++) {
                    String inputDoc = inputDocSet[i].split("=")[0];
                    Integer num = new Integer(inputDocSet[i].split("=")[1]);
                    info.putInputDoc(new File(inputDoc), num);
                }
                if (items.length == 8) {
                    String[] upperConceptSet = items[7].split(":");
                    for (int i = 0; i < upperConceptSet.length; i++) {
                        info.addUpperConcept(upperConceptSet[i]);
                    }
                }
                wordInfoMap.put(word, info);
            }
            reader.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        setWordInfoTableModel(wordInfoMap, docNum);
    }

    public void saveWordInfoTable() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordInfoTable(file);
        }
    }

    public void saveWordInfoTable(File saveFile) {
        if (wordInfoMap == null) { return; }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
            writer.write("docNum=" + docNum + "\n");
            for (Iterator i = wordInfoMap.values().iterator(); i.hasNext();) {
                WordInfo info = (WordInfo) i.next();
                writer.write(info.toString() + "\n");
            }
            writer.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    class WordInfoTableModel extends DefaultTableModel{
        
        WordInfoTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            String columnName = getColumnName(columnIndex);
            if (columnName.equals("TF")) {
                return Integer.class;
            } else if (columnName.equals("IDF") || columnName.equals("TF-IDF")) {
                return Double.class;
            } else {
                return String.class;
            }
        }
    }
    
    public void setWordInfoTableModel(Map wiMap, int dn) {
        docNum = dn;
        String WORD = Translator.getString("InputWordSelectionPanel.Word");
        String POS = Translator.getString("InputWordSelectionPanel.POS");
        String DOCUMENT = Translator.getString("InputWordSelectionPanel.Document");
        String INPUT_DOCUMENT = Translator.getString("InputWordSelectionPanel.InputDocument");
        String UPPER_CONCEPT = Translator.getString("InputWordSelectionPanel.UpperConcept");
        Object[] titles = new Object[] { WORD, POS, "TF", "IDF", "TF-IDF", DOCUMENT, INPUT_DOCUMENT, UPPER_CONCEPT};

        wordInfoTableModel = new WordInfoTableModel(null, titles);
        wordInfoTableModel.getColumnClass(0);
        rowSorter = new TableRowSorter<TableModel>(wordInfoTableModel);
        rowSorter.setMaxSortKeys(5);

        wordInfoTable.setRowSorter(rowSorter);
        wordInfoTable.setModel(wordInfoTableModel);
        wordInfoTable.getTableHeader().setToolTipText("列でソートします．");
        wordInfoMap = wiMap;
        if (wordInfoMap == null) { return; }
        Collection<WordInfo> wordInfoSet = wordInfoMap.values();
        for (WordInfo info : wordInfoSet) {
            wordInfoTableModel.addRow(info.getRowData());
        }
        wordInfoTableBorder.setTitle(Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + wiMap.size() + "）");
    }

    private void setInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.loadInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLE.INPUT_MODULE);
    }

    private void addInputWordSet() {
        DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.SetInputWordListDone"));
        String[] inputWords = inputWordArea.getText().split("\n");
        Set<String> inputWordSet = new HashSet<String>(Arrays.asList(inputWords));
        disambiguationPanel.addInputWordSet(inputWordSet);
        DODDLE.setSelectedIndex(DODDLE.INPUT_MODULE);
    }

    private void addInputWords() {
        int[] rows = wordInfoTable.getSelectedRows();
        StringBuilder inputWords = new StringBuilder("");
        for (int i = 0; i < rows.length; i++) {
            String word = (String) wordInfoTable.getValueAt(rows[i], 0);
            inputWords.append(word + "\n");
        }
        inputWordArea.setText(inputWordArea.getText() + inputWords.toString());
    }

    private void deleteTableItems() {
        int[] rows = wordInfoTable.getSelectedRows();
        Set<String> deleteWordSet = new HashSet<String>();
        for (int i = 0; i < rows.length; i++) {
            String deleteWord = (String) wordInfoTable.getValueAt(rows[i], 0);
            deleteWordSet.add(deleteWord);
            wordInfoMap.remove(deleteWord);
        }
        for (int i = 0; i < wordInfoTableModel.getRowCount(); i++) {
            String word = (String) wordInfoTableModel.getValueAt(i, 0);
            if (deleteWordSet.contains(word)) {
                wordInfoTableModel.removeRow(i);
                i = 0;
                continue;
            }
        }
        wordInfoTableBorder.setTitle(Translator.getString("InputWordSelectionPanel.ExtractedWordTable") + "（"
                + wordInfoMap.size() + "）");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setInputWordSetButton) {
            setInputWordSet();
        } else if (e.getSource() == addInputWordSetButton) {
            addInputWordSet();
        } else if (e.getSource() == addInputWordButton) {
            addInputWords();
        } else if (e.getSource() == deleteTableItemButton) {
            deleteTableItems();
        } else if (e.getSource() == searchWordField) {
            if (searchWordField.getText().length() == 0) {
                rowSorter.setRowFilter(RowFilter.regexFilter(".*", new int[] {0}));    
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter(searchWordField.getText(), new int[] {0}));
            }
        } else if (e.getSource() == searchPOSField) {
            if (searchPOSField.getText().length() == 0) {
                rowSorter.setRowFilter(RowFilter.regexFilter(".*", new int[] {1}));    
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter(searchPOSField.getText(), new int[] {1}));
            }
        }
    }
}
