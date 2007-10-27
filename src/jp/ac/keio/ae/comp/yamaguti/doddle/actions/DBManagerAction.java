/*
 * @(#)  2007/09/13
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.actions;

import java.awt.event.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class DBManagerAction extends AbstractAction {

    private DBManagerDialog dbManagerPanel;
    
    public DBManagerAction(String title) {
        super(title, Utils.getImageIcon("database.png"));
        dbManagerPanel = new DBManagerDialog();
    }
    
    public void closeDB() {
        dbManagerPanel.closeDB();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dbManagerPanel.setVisible(true);
    }
}
