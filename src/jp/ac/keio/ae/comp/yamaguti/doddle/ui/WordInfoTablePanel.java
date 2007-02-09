/*
 * @(#)  2006/11/29
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class WordInfoTablePanel extends JPanel implements ActionListener {

    private int docNum;
    private Map<String, WordInfo> wordInfoMap;

    private JTextField searchWordField;
    private JTextField searchPOSField;
    private JTable wordInfoTable;
    private TableRowSorter<TableModel> rowSorter;
    private WordInfoTableModel wordInfoTableModel;

    public WordInfoTablePanel() {
        searchWordField = new JTextField(20);
        searchWordField.addActionListener(this);
        searchWordField.setBorder(BorderFactory.createTitledBorder(Translator.getString("InputWordSelectionPanel.WordFilter")));
        searchPOSField = new JTextField(20);
        searchPOSField.addActionListener(this);
        searchPOSField.setBorder(BorderFactory.createTitledBorder(Translator.getString("InputWordSelectionPanel.POSFilter")));
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchWordField);
        searchPanel.add(searchPOSField);

        wordInfoTable = new JTable();
        JScrollPane wordInfoTableScroll = new JScrollPane(wordInfoTable);
        setWordInfoTableModel(null, 0);

        setLayout(new BorderLayout());
        add(searchPanel, BorderLayout.NORTH);
        add(wordInfoTableScroll, BorderLayout.CENTER);
    }
    
    public TableModel getTableModel() {
        return wordInfoTable.getModel();
    }

    public int getTableSize() {
        return wordInfoMap.size();
    }
    
    public WordInfo getWordInfo(String word) {
        return wordInfoMap.get(word);
    }
    
    public void addWordInfoMapKey(String addWord, WordInfo info) {
            wordInfoMap.put(addWord, info);
    }
    
    public void removeWordInfoMapKey(String deleteWord) {
        wordInfoMap.remove(deleteWord);
    }
    
    public JTable getWordInfoTable() {
        return wordInfoTable;
    }

    public void setWordInfoTableModel(Map<String, WordInfo> wiMap, int dn) {
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
            Vector rowData = info.getRowData();
            wordInfoTableModel.addRow(rowData);                       
        }
    }

    public void loadWordInfoTable(File loadFile) {
        if (!loadFile.exists()) { return; }
        wordInfoMap = new HashMap<String, WordInfo>();
        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(loadFile);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
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
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
            }
        }
        setWordInfoTableModel(wordInfoMap, docNum);
    }

    public void saveWordInfoTable(File saveFile) {
        if (wordInfoMap == null) { return; }
        BufferedWriter writer = null;
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            writer.write("docNum=" + docNum + "\n");
            for (Iterator i = wordInfoMap.values().iterator(); i.hasNext();) {
                WordInfo info = (WordInfo) i.next();
                writer.write(info.toString() + "\n");
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
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

    class WordInfoTableModel extends DefaultTableModel {

        WordInfoTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        public Class< ? > getColumnClass(int columnIndex) {
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

    public void loadWordInfoTable() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showOpenDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            loadWordInfoTable(file);
        }
    }

    public void saveWordInfoTable() {
        JFileChooser chooser = new JFileChooser(".");
        int retval = chooser.showSaveDialog(DODDLE.rootPane);
        if (retval == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveWordInfoTable(file);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchWordField || e.getSource() == searchPOSField) {
            if (searchWordField.getText().length() == 0 && searchPOSField.getText().length() == 0) {
                rowSorter.setRowFilter(RowFilter.regexFilter(".*", new int[] {0}));      
            }
            if (searchWordField.getText().length() != 0) {
                rowSorter.setRowFilter(RowFilter.regexFilter(searchWordField.getText(), new int[] {0}));
            }
            if (searchPOSField.getText().length() != 0) {
                rowSorter.setRowFilter(RowFilter.regexFilter(searchPOSField.getText(), new int[] {1}));
            }
        }
    }

}
