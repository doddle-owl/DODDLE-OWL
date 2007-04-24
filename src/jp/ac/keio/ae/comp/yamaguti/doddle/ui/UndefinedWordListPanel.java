package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 * @(#)  2005/07/17
 *
 */

/**
 * @author takeshi morita
 */
public class UndefinedWordListPanel extends JPanel implements ActionListener {

    public static boolean isDragUndefinedList = false;

    private JButton searchButton;
    private JTextField searchField;

    private JTextField addWordField;
    private JButton addButton;
    private JButton removeButton;

    private ListModel undefinedWordListModel;
    private JList undefinedWordJList;
    private TitledBorder undefinedWordJListTitle;

    public UndefinedWordListPanel() {
        searchButton = new JButton(Translator.getTerm("SearchButton"));
        searchButton.addActionListener(this);
        searchField = new JTextField(30);
        searchField.addActionListener(this);
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        undefinedWordListModel = new DefaultListModel();
        undefinedWordJList = new JList(undefinedWordListModel);
        JScrollPane unefinedWordJListScroll = new JScrollPane(undefinedWordJList);
        undefinedWordJListTitle = BorderFactory.createTitledBorder(Translator.getTerm("UndefinedWordList"));
        unefinedWordJListScroll.setBorder(undefinedWordJListTitle);
        undefinedWordJList.setDragEnabled(true);
        new DropTarget(undefinedWordJList, new UndefinedWordJListDropTargetAdapter());

        addWordField = new JTextField(30);
        addWordField.addActionListener(this);
        addButton = new JButton(Translator.getTerm("AddButton"));
        addButton.addActionListener(this);
        removeButton = new JButton(Translator.getTerm("RemoveButton"));
        removeButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        JPanel addRemovePanel = new JPanel();
        addRemovePanel.setLayout(new BorderLayout());
        addRemovePanel.add(addWordField, BorderLayout.CENTER);
        addRemovePanel.add(buttonPanel, BorderLayout.EAST);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(addRemovePanel, BorderLayout.NORTH);
        northPanel.add(searchPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(unefinedWordJListScroll, BorderLayout.CENTER);
    }

    class UndefinedWordJListDropTargetAdapter extends DropTargetAdapter {

        public void dragEnter(DropTargetDragEvent dtde) {
            isDragUndefinedList = true;
        }

        public void drop(DropTargetDropEvent dtde) {
        }
    }

    public Object[] getSelectedValues() {
        return undefinedWordJList.getSelectedValues();
    }

    public DefaultListModel getModel() {
        return (DefaultListModel) undefinedWordListModel;
    }

    public DefaultListModel getViewModel() {
        if (undefinedWordJList.getModel() == undefinedWordListModel) { return null; }
        return (DefaultListModel) undefinedWordJList.getModel();
    }

    public String getSearchRegex() {
        return searchField.getText();
    }

    public void clearSelection() {
        undefinedWordJList.clearSelection();
    }

    public void setTitle() {
        undefinedWordJListTitle.setTitle(Translator.getTerm("UndefinedWordList"));
    }

    public void setTitleWithSize() {
        undefinedWordJListTitle.setTitle(Translator.getTerm("UndefinedWordList") + " （"
                + undefinedWordJList.getModel().getSize() + "/" + getModel().getSize() + "）");
        repaint();
    }

    public void setUndefinedWordListModel(ListModel model) {
        undefinedWordListModel = model;
        undefinedWordJList.setModel(model);
        setTitle();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchField || e.getSource() == searchButton) {
            String searchRegex = searchField.getText();
            if (searchRegex.length() == 0) {
                undefinedWordJList.setModel(undefinedWordListModel);
            } else {
                DefaultListModel searchListModel = new DefaultListModel();
                for (int i = 0; i < undefinedWordListModel.getSize(); i++) {
                    String word = (String) undefinedWordListModel.getElementAt(i);
                    if (word.matches(searchRegex)) {
                        searchListModel.addElement(word);
                    }
                }
                undefinedWordJList.setModel(searchListModel);
            }
            setTitleWithSize();
        } else if (e.getSource() == addButton || e.getSource() == addWordField) {
            String addWord = addWordField.getText();
            if (0 < addWord.length()) {
                for (int i = 0; i < undefinedWordListModel.getSize(); i++) {
                    String word = (String) undefinedWordListModel.getElementAt(i);
                    if (word.equals(addWord)) { return; }
                }
                ((DefaultListModel) undefinedWordListModel).addElement(addWord);
                if (getViewModel() != null && addWord.matches(getSearchRegex())) {
                    getViewModel().addElement(addWord);
                }
                setTitleWithSize();
                addWordField.setText("");
            }
        } else if (e.getSource() == removeButton) {
            DefaultListModel model = (DefaultListModel) undefinedWordJList.getModel();
            Object[] removeWordList = undefinedWordJList.getSelectedValues();
            for (int i = 0; i < removeWordList.length; i++) {
                model.removeElement(removeWordList[i]);
                ((DefaultListModel) undefinedWordListModel).removeElement(removeWordList[i]);
            }
            setTitleWithSize();
        }
    }
}
