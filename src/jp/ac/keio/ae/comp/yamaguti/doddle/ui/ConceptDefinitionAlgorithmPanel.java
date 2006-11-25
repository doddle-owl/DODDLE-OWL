package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author Yoshihiro Shigeta
 * 
 * last modified: 2004-12-06 modified by takeshi morita
 * 
 */
public class ConceptDefinitionAlgorithmPanel extends JPanel implements ChangeListener, ActionListener {

    private Set<WordSpace> wordSpaceSet;
    private Set<Apriori> aprioriSet;
    private Map<Document, Map> docWSResultMap;
    private Map<Document, Map> docAprioriResultMap;

    private JLabel minSupport;
    private JTextField minSupportField;
    private JSlider minConfidenceSlider;
    private JLabel confidenceValue;

    private JLabel wordSpaceValue;
    private JSlider wordSpaceValueSlider;
    private JLabel gramNumber;
    private JLabel gramCount;
    private JLabel frontscope;
    private JLabel behindscope;
    private JTextField gramNumberField;
    private JTextField gramCountField;
    private JTextField frontScopeField;
    private JTextField behindScopeField;

    private JButton exeWordSpaceButton;
    private JButton exeAprioriButton;

    private JList inputConceptJList;

    private DODDLEProject doddleProject;

    public ConceptDefinitionAlgorithmPanel(JList list, DODDLEProject project) {
        inputConceptJList = list;
        doddleProject = project;

        wordSpaceSet = new HashSet<WordSpace>();
        aprioriSet = new HashSet<Apriori>();
        docWSResultMap = new HashMap<Document, Map>();
        docAprioriResultMap = new HashMap<Document, Map>();

        wordSpaceValueSlider = new JSlider();
        wordSpaceValueSlider.addChangeListener(this);
        minConfidenceSlider = new JSlider();
        minConfidenceSlider.addChangeListener(this);

        gramNumber = new JLabel("N-Gram    ");
        gramCount = new JLabel("Gram Count    ");
        frontscope = new JLabel("Front Scope    ");
        behindscope = new JLabel("Behind Scope    ");

        minSupport = new JLabel("Minimum Support     ");

        gramNumberField = new JTextField();
        gramNumberField.setHorizontalAlignment(JTextField.RIGHT);
        gramNumberField.setText("4");
        gramCountField = new JTextField();
        gramCountField.setHorizontalAlignment(JTextField.RIGHT);
        gramCountField.setText("7");
        frontScopeField = new JTextField();
        frontScopeField.setHorizontalAlignment(JTextField.RIGHT);
        frontScopeField.setText("60");
        behindScopeField = new JTextField();
        behindScopeField.setHorizontalAlignment(JTextField.RIGHT);
        behindScopeField.setText("10");

        minSupportField = new JTextField();
        minSupportField.setHorizontalAlignment(JTextField.RIGHT);
        minSupportField.setText("0");

        exeWordSpaceButton = new JButton(Translator.getString("ConceptDefinitionPanel.ExecuteWordSpace"));
        exeWordSpaceButton.addActionListener(this);
        exeAprioriButton = new JButton(Translator.getString("ConceptDefinitionPanel.ExecuteApriori"));
        exeAprioriButton.addActionListener(this);

        JPanel wordSpaceParamPanel = getWordSpacePanel();
        wordSpaceParamPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ConceptDefinitionPanel.WordSpaceParameters")));
        JPanel aprioriParamPanel = getAprioriPanel();
        aprioriParamPanel.setBorder(BorderFactory.createTitledBorder(Translator
                .getString("ConceptDefinitionPanel.AprioriParameters")));

        JPanel panel = new JPanel();
        panel.add(wordSpaceParamPanel);
        panel.add(aprioriParamPanel);

        setLayout(new BorderLayout());
        add(getWestComponent(panel), BorderLayout.WEST);
    }

    public Map<Document, Map> getDocWordSpaceResult() {
        return docWSResultMap;
    }

    public Map<Document, Map> getDocAprioriResult() {
        return docAprioriResultMap;
    }

    public int getGramNumber() {
        int gramNum = 0;
        if (gramNumberField.getText() != null) {
            gramNum = new Integer(gramNumberField.getText()).intValue();
        }
        return gramNum;
    }

    public int getGramCount() {
        int gramCount = 0;
        if (gramCountField.getText() != null) {
            gramCount = new Integer(gramCountField.getText()).intValue();
        }
        return gramCount;
    }

    public int getFrontScope() {
        int frontScope = 0;
        if (frontScopeField.getText() != null) {
            frontScope = new Integer(frontScopeField.getText()).intValue();
        }
        return frontScope;
    }

    public int getBehindScope() {
        int behindScope = 0;
        if (behindScopeField.getText() != null) {
            behindScope = new Integer(behindScopeField.getText()).intValue();
        }
        return behindScope;
    }

    public double getMinSupport() {
        double minSupport = 0;
        if (minSupportField.getText() != null) {
            minSupport = new Double(minSupportField.getText()).doubleValue();
        }
        return minSupport;
    }

    private JComponent getEastComponent(JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(comp, BorderLayout.EAST);
        return p;
    }

    private JComponent getWestComponent(JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(comp, BorderLayout.WEST);
        return p;
    }

    private JPanel getAprioriPanel() {
        confidenceValue = new JLabel("0.50");
        confidenceValue.setFont(new Font("Dialog", Font.PLAIN, 14));
        JPanel barPanel = new JPanel();
        barPanel.setPreferredSize(new Dimension(150, 20));
        barPanel.setLayout(new BorderLayout());
        barPanel.add(confidenceValue, BorderLayout.WEST);
        barPanel.add(minConfidenceSlider, BorderLayout.CENTER);

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridLayout(2, 2, 0, 0));
        paramPanel.add(minSupport);
        paramPanel.add(minSupportField);
        paramPanel.add(new JLabel("Minimum Confidence"));
        paramPanel.add(barPanel);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(paramPanel, BorderLayout.NORTH);
        panel.add(getEastComponent(exeAprioriButton), BorderLayout.SOUTH);

        return panel;
    }

    public double getMinConfidence() {
        return (new Double(confidenceValue.getText())).doubleValue();
    }

    public double getWordSpaceUnder() {
        return (new Double(wordSpaceValue.getText())).doubleValue();
    }

    private JPanel getWordSpacePanel() {
        // Integer inte = new Integer(wordSpaceValueSlider.getValue());
        wordSpaceValue = new JLabel("0.50");
        wordSpaceValue.setFont(new Font("Dialog", Font.PLAIN, 14));

        JPanel barPanel = new JPanel();
        barPanel.setPreferredSize(new Dimension(150, 20));
        barPanel.setLayout(new BorderLayout());
        barPanel.add(wordSpaceValue, BorderLayout.WEST);
        barPanel.add(wordSpaceValueSlider, BorderLayout.CENTER);

        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new GridLayout(3, 4, 5, 5));
        paramPanel.add(gramNumber);
        paramPanel.add(gramNumberField);
        paramPanel.add(gramCount);
        paramPanel.add(gramCountField);
        paramPanel.add(frontscope);
        paramPanel.add(frontScopeField);
        paramPanel.add(behindscope);
        paramPanel.add(behindScopeField);
        paramPanel.add(new JLabel("WordSpace Value"));
        paramPanel.add(barPanel);
        paramPanel.add(new Label()); // dammy
        paramPanel.add(exeWordSpaceButton);

        return paramPanel;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == minConfidenceSlider) {
            Integer inte = new Integer(minConfidenceSlider.getValue());
            Double value = new Double(inte.doubleValue() / 100);
            if (value.toString().length() == 4) {
                confidenceValue.setText(value.toString());
            } else {
                confidenceValue.setText(value.toString() + "0");
            }
        } else if (e.getSource() == wordSpaceValueSlider) {
            Integer inte = new Integer(wordSpaceValueSlider.getValue());
            Double value = new Double(inte.doubleValue() / 100);
            if (value.toString().length() == 4) {
                wordSpaceValue.setText(value.toString());
            } else {
                wordSpaceValue.setText(value.toString() + "0");
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exeWordSpaceButton) {
            SwingWorker worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    exeWordSpace();
                    return "done";
                }
            };
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        } else if (e.getSource() == exeAprioriButton) {
            SwingWorker worker = new SwingWorker<String, String>() {
                public String doInBackground() {
                    exeApriori();
                    return "done";
                }
            };
            DODDLE.STATUS_BAR.setSwingWorker(worker);
            worker.execute();
        }
    }

    public WordSpaceData getWordSpaceData() {
        WordSpaceData wsData = new WordSpaceData();
        wsData.setGramNumber(getGramNumber());
        wsData.setGramCount(getGramCount());
        wsData.setFrontScope(getFrontScope());
        wsData.setBehindScope(getBehindScope());
        wsData.setUnderValue(getWordSpaceUnder());
        return wsData;
    }

    public void setInputConcept() {
        wordSpaceSet.clear();
        aprioriSet.clear();
        ConceptDefinitionPanel conceptDefinitionPanel = doddleProject.getConceptDefinitionPanel();
        conceptDefinitionPanel.setInputConceptJList();
        if (0 < conceptDefinitionPanel.getInputWordList().size()) {
            Set<Document> docSet = doddleProject.getDocumentSelectionPanel().getDocSet();
            for (Document doc : docSet) {
                wordSpaceSet.add(new WordSpace(conceptDefinitionPanel, doc));
                aprioriSet.add(new Apriori(conceptDefinitionPanel, doc));
            }
        }
    }

    private List<String> getTargetInputWordList(Document doc) {
        List<String> inputWordList = doddleProject.getConceptDefinitionPanel().getInputWordList();
        if (inputWordList == null) { return null; }
        System.out.println("inputWordListSize: " + inputWordList.size());
        List<String> targetInputWordList = new ArrayList<String>();
        for (String iw : inputWordList) {
            String text = DocumentSelectionPanel.getTextString(doc);
            // '_'が入力単語に含まれている場合には，スペースに変換した場合もチェックする
            if (text.indexOf(iw.replaceAll("_", " ")) != -1 || text.indexOf(iw) != -1) {
                targetInputWordList.add(iw);
            } else {
                // System.out.println("文書中に存在しない入力単語: " + iw);
            }
        }
        System.out.println("targetInputWordListSize: " + targetInputWordList.size());
        return targetInputWordList;
    }

    public void exeWordSpace() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.OpenProjectDone"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(4 * wordSpaceSet.size());
            DODDLE.STATUS_BAR.lock();
            docWSResultMap.clear();
            WordSpaceData wsData = getWordSpaceData();
            for (WordSpace ws : wordSpaceSet) {
                if (ws != null) {
                    ws.setWSData(wsData);
                    List<String> targetInputWordList = getTargetInputWordList(ws.getDocument());
                    docWSResultMap.put(ws.getDocument(), ws.calcWordSpaceResult(targetInputWordList));
                }
            }
            if (0 < inputConceptJList.getModel().getSize()) {
                inputConceptJList.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }

    public void exeApriori() {
        try {
            DODDLE.STATUS_BAR.setLastMessage(Translator.getString("StatusBar.Message.OpenProjectDone"));
            DODDLE.STATUS_BAR.startTime();
            DODDLE.STATUS_BAR.initNormal(2 * aprioriSet.size());
            DODDLE.STATUS_BAR.lock();
            docAprioriResultMap.clear();
            double minSupport = getMinSupport();
            double minConfidence = getMinConfidence();
            for (Apriori apriori : aprioriSet) {
                if (apriori != null) {
                    apriori.setParameters(minSupport, minConfidence);
                    List<String> targetInputWordList = getTargetInputWordList(apriori.getDocument());
                    docAprioriResultMap.put(apriori.getDocument(), apriori.calcAprioriResult(targetInputWordList));
                }
            }
            if (0 < inputConceptJList.getModel().getSize()) {
                inputConceptJList.setSelectedIndex(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DODDLE.STATUS_BAR.unLock();
            DODDLE.STATUS_BAR.hideProgressBar();
        }
    }
}