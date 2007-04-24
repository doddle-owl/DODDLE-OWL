/*
 * @(#)  2007/03/17
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class OWLMetaDataTablePanel extends JPanel {

    private JTable owlMetaDataTable;

    public OWLMetaDataTablePanel() {
        owlMetaDataTable = new JTable();
        owlMetaDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane owlMetaDataTableScroll = new JScrollPane(owlMetaDataTable);
        owlMetaDataTableScroll.setBorder(BorderFactory.createTitledBorder(Translator.getTerm("OWLMetaDataTable")));

        setLayout(new BorderLayout());
        add(owlMetaDataTableScroll, BorderLayout.CENTER);
    }

    public void setModel(TableModel model) {
        owlMetaDataTable.setModel(model);
    }
}
