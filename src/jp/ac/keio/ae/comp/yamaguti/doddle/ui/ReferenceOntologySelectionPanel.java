/*
 * @(#)  2006/03/01
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

/**
 * @author takeshi morita
 */
public class ReferenceOntologySelectionPanel extends JPanel implements ActionListener {

    private JButton nextTabButton;
    private NameSpaceTable nsTable;

    private SwoogleWebServiceWrapperPanel swoogleWebServiceWrapperPanel;
    private GeneralOntologySelectionPanel generalOntologySelectionPanel;
    private OWLOntologySelectionPanel owlOntologySelectionPanel;

    private View[] mainViews;
    private RootWindow rootWindow;
    
    public ReferenceOntologySelectionPanel() {        
        generalOntologySelectionPanel = new GeneralOntologySelectionPanel();
        nsTable = new NameSpaceTable();        
        owlOntologySelectionPanel = new OWLOntologySelectionPanel(nsTable);
        swoogleWebServiceWrapperPanel = new SwoogleWebServiceWrapperPanel(nsTable, owlOntologySelectionPanel); 

        nextTabButton = new JButton(Translator.getTerm("InputConceptSelectionPanel"));
        nextTabButton.addActionListener(this);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(nextTabButton, BorderLayout.EAST);
        
        mainViews = new View[4];
        ViewMap viewMap = new ViewMap();
        mainViews[0] = new View(Translator.getTerm("GenericOntologySelectionPanel"), null,
                generalOntologySelectionPanel);
        mainViews[1] = new View(Translator.getTerm("OWLOntologySelectionPanel"), null,
                owlOntologySelectionPanel);
        mainViews[2] = new View(Translator.getTerm("NameSpaceTable"), null, nsTable);
        if (DODDLEConstants.IS_INTEGRATING_SWOOGLE) {
            mainViews[3] = new View(Translator.getTerm("SwoogleWebServiceWrapperPanel"), null, swoogleWebServiceWrapperPanel);
        } else {
            mainViews[3] = new View(Translator.getTerm("SwoogleWebServiceWrapperPanel"), null, new JPanel());
        }

        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);        
        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);   
    }
    
    public void setInputWordArea(String inputWordText) {
        swoogleWebServiceWrapperPanel.setInputWordArea(inputWordText);
    }    
    
    public void setXGALayout() {
        SplitWindow sw1 = new SplitWindow(false, 0.3f, mainViews[0], mainViews[2]);
        TabWindow tabWindow = new TabWindow(new DockingWindow[] { sw1, mainViews[3], mainViews[1]});
        rootWindow.setWindow(tabWindow);
        mainViews[0].restoreFocus();
    }    
    
    public void setUXGALayout() {
        SplitWindow sw1 = new SplitWindow(false, 0.25f, mainViews[0], mainViews[1]);
        SplitWindow sw2 = new SplitWindow(false, 0.75f, sw1, mainViews[2]);
        TabWindow tabWindow = new TabWindow(new DockingWindow[] {mainViews[3], sw2});
        rootWindow.setWindow(tabWindow);
    }
    
    public NameSpaceTable getNSTable() {
        return nsTable;
    }
    
    public String getPrefix(String ns) {
        return nsTable.getPrefix(ns);
    }

    public void actionPerformed(ActionEvent e) {
        DODDLE.setSelectedIndex(DODDLEConstants.DISAMBIGUATION_PANEL);
    }

    public void saveOWLMetaDataSet(File saveDir) {
        owlOntologySelectionPanel.saveOWLMetaDataSet(saveDir);
    }

    public void loadOWLMetaDataSet(File loadDir) {
        owlOntologySelectionPanel.loadOWLMetaDataSet(loadDir);
    }

    public void saveGeneralOntologyInfo(File saveFile) {
        generalOntologySelectionPanel.saveGeneralOntologyInfo(saveFile);
    }
    
    public void saveGeneralOntologyInfoToDB(int projectID, Statement stmt) {
        generalOntologySelectionPanel.saveGeneralOntologyInfoToDB(projectID, stmt);
    }

    public void loadGeneralOntologyInfo(File loadFile) {
        generalOntologySelectionPanel.loadGeneralOntologyInfo(loadFile);
    }
    
    public void loadGeneralOntologyInfo(int projectID, Statement stmt) {
        generalOntologySelectionPanel.loadGeneralOntologyInfo(projectID, stmt);
    }

    public String getEnableDicList() {
        return generalOntologySelectionPanel.getEnableDicList();
    }

    public boolean isEDREnable() {
        return generalOntologySelectionPanel.isEDREnable();
    }

    public boolean isEDRTEnable() {
        return generalOntologySelectionPanel.isEDRTEnable();
    }

    public boolean isWordNetEnable() {
        return generalOntologySelectionPanel.isWordNetEnable();
    }
    
    public void resetGeneralOntologiesCheckBoxes() {
        generalOntologySelectionPanel.resetCheckBoxes();
    }
}
