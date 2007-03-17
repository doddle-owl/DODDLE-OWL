/*
 * @(#)  2007/03/17
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class SwoogleWebServiceWrapperPanel extends JPanel {
    private JList acquiredOntologyJList; 
    private JList removedOntologyJList;
    private JButton removeOntologyButton;
    private JButton returnOntologyButton;
    
    private OWLMetaDataTablePanel owlMetaDataTablePanel;
    private LiteralPanel labelPanel;
    private LiteralPanel descriptionPanel;
    private JList classJList;
    private JList propertyJList;
    private JList domainJList;
    private JList rangeJList;
    
    
    public SwoogleWebServiceWrapperPanel() {
        acquiredOntologyJList = new JList();
        JScrollPane acquiredOntologyJListScroll = new JScrollPane(acquiredOntologyJList);
        acquiredOntologyJListScroll.setBorder(BorderFactory.createTitledBorder("獲得したオントロジーリスト"));
        removeOntologyButton = new JButton(Translator.getString("Remove"));        
        JPanel acquiredOntologyPanel = new JPanel();
        acquiredOntologyPanel.setLayout(new BorderLayout());
        acquiredOntologyPanel.add(acquiredOntologyJListScroll, BorderLayout.CENTER);
        acquiredOntologyPanel.add(Utils.createEastPanel(removeOntologyButton), BorderLayout.SOUTH);
        
        removedOntologyJList = new JList();
        JScrollPane removedOntologyJListScroll = new JScrollPane(removedOntologyJList);
        removedOntologyJListScroll.setBorder(BorderFactory.createTitledBorder("不要なオントロジーリスト"));
        returnOntologyButton = new JButton("戻す");
        JPanel removedOntologyPanel = new JPanel();
        removedOntologyPanel.setLayout(new BorderLayout());
        removedOntologyPanel.add(removedOntologyJListScroll, BorderLayout.CENTER);
        removedOntologyPanel.add(Utils.createEastPanel(returnOntologyButton), BorderLayout.SOUTH);

        JPanel ontologyListPanel = new JPanel();
        ontologyListPanel.setLayout(new GridLayout(2, 1));
        ontologyListPanel.add(acquiredOntologyPanel);
        ontologyListPanel.add(removedOntologyPanel); 
        
        owlMetaDataTablePanel = new OWLMetaDataTablePanel();
        owlMetaDataTablePanel.setPreferredSize(new Dimension(200, 100));
        owlMetaDataTablePanel.setMinimumSize(new Dimension(200, 100));
        
        labelPanel = new LiteralPanel(Translator.getString("Lang"), Translator.getString("Label"), LiteralPanel.LABEL);
        descriptionPanel = new LiteralPanel(Translator.getString("Lang"), Translator.getString("Description"), LiteralPanel.DESCRIPTION);
        JPanel labelAndDescriptionPanel = new JPanel();
        labelAndDescriptionPanel.setLayout(new GridLayout(1, 2));
        labelAndDescriptionPanel.add(labelPanel);
        labelAndDescriptionPanel.add(descriptionPanel);
        
        classJList = new JList();
        JScrollPane classJListScroll = new JScrollPane(classJList);
        classJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("ClassTreePanel.ConceptTree")));
        propertyJList = new JList();
        JScrollPane propertyJListScroll = new JScrollPane(propertyJList);
        propertyJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("PropertyTreePanel.ConceptTree")));
        
        JPanel conceptPanel = new JPanel();
        conceptPanel.setLayout(new GridLayout(1, 2));
        conceptPanel.add(classJListScroll);
        conceptPanel.add(propertyJListScroll);
        
        domainJList = new JList();
        JScrollPane domainJListScroll = new JScrollPane(domainJList);
        domainJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("PropertyTreePanel.Domain")));
        rangeJList = new JList();
        JScrollPane rangeJListScroll = new JScrollPane(rangeJList);
        rangeJListScroll.setBorder(BorderFactory.createTitledBorder(Translator.getString("PropertyTreePanel.Range")));
        
        JPanel regionPanel = new JPanel();
        regionPanel.setLayout(new GridLayout(1, 2));
        regionPanel.add(domainJListScroll);
        regionPanel.add(rangeJListScroll);
        
        JPanel conceptInfoPanel = new JPanel();
        conceptInfoPanel.setLayout(new GridLayout(2, 1));
        conceptInfoPanel.add(conceptPanel);
        conceptInfoPanel.add(labelAndDescriptionPanel);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(owlMetaDataTablePanel, BorderLayout.NORTH);
        centerPanel.add(conceptInfoPanel, BorderLayout.CENTER);
        centerPanel.add(regionPanel, BorderLayout.SOUTH);
        
        setLayout(new BorderLayout());
        add(ontologyListPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.add(new SwoogleWebServiceWrapperPanel());
        frame.pack();
        frame.setVisible(true);
    }
    
}
