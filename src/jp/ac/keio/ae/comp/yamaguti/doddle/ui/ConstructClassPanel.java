package jp.ac.keio.ae.comp.yamaguti.doddle.ui;

/*
 * 2005/03/01
 *  
 */

import java.awt.*;
import java.util.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.infonode.docking.*;
import net.infonode.docking.util.*;

import org.apache.log4j.*;

/**
 * @author takeshi morita
 * 
 */
public class ConstructClassPanel extends ConstructConceptTreePanel {

    public ConstructClassPanel(DODDLEProject p) {
        project = p;
        undefinedWordListPanel = new UndefinedWordListPanel();
        isaTreePanel = new ConceptTreePanel(Translator.getTerm("IsaTreeBorder"), ConceptTreePanel.CLASS_ISA_TREE,
                undefinedWordListPanel, p);
        hasaTreePanel = new ConceptTreePanel(Translator.getTerm("HasaTreeBorder"),
                ConceptTreePanel.CLASS_HASA_TREE, undefinedWordListPanel, project);
        isaTreePanel.setHasaTree(hasaTreePanel.getConceptTree());
        conceptDriftManagementPanel = new ConceptDriftManagementPanel(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE,
                isaTreePanel.getConceptTree(), project);
        isaTreePanel.setConceptDriftManagementPanel(conceptDriftManagementPanel);

        conceptInfoPanel = new ConceptInformationPanel(isaTreePanel.getConceptTree(), hasaTreePanel.getConceptTree(),
                new ConceptTreeCellRenderer(ConceptTreeCellRenderer.NOUN_CONCEPT_TREE), conceptDriftManagementPanel);

        mainViews = new View[5];
        ViewMap viewMap = new ViewMap();
        mainViews[0] = new View(Translator.getTerm("UndefinedWordListPanel"), null, undefinedWordListPanel);
        mainViews[1] = new View(Translator.getTerm("IsaConceptTreePanel"), null, isaTreePanel);
        mainViews[2] = new View(Translator.getTerm("HasaConceptTreePanel"), null, hasaTreePanel);
        mainViews[3] = new View(Translator.getTerm("ConceptInformationPanel"), null, conceptInfoPanel);
        mainViews[4] = new View(Translator.getTerm("ConceptDriftManagementPanel"), null, conceptDriftManagementPanel);

        for (int i = 0; i < mainViews.length; i++) {
            viewMap.addView(i, mainViews[i]);
        }
        rootWindow = Utils.createDODDLERootWindow(viewMap);
        setLayout(new BorderLayout());
        add(rootWindow, BorderLayout.CENTER);
    }

    public void setXGALayout() {
        conceptDriftManagementPanel.setXGALayout();
        TabWindow t1 = new TabWindow(new DockingWindow[] { mainViews[1], mainViews[2]});
        SplitWindow sw1 = new SplitWindow(false, 0.3f, mainViews[0], t1);
        SplitWindow sw2 = new SplitWindow(false, 0.5f, mainViews[3], mainViews[4]);
        SplitWindow sw3 = new SplitWindow(true, 0.3f, sw1, sw2);
        rootWindow.setWindow(sw3);
        mainViews[1].restoreFocus();
    }

    public void setUXGALayout() {
        conceptDriftManagementPanel.setUXGALayout();
        TabWindow t1 = new TabWindow(new DockingWindow[] { mainViews[1], mainViews[2]});
        SplitWindow sw1 = new SplitWindow(false, 0.3f, mainViews[0], t1);
        SplitWindow sw2 = new SplitWindow(false, 0.5f, mainViews[3], mainViews[4]);
        SplitWindow sw3 = new SplitWindow(true, 0.3f, sw1, sw2);
        rootWindow.setWindow(sw3);
        mainViews[1].restoreFocus();
    }

    public TreeModel getTreeModel(Set<Concept> conceptSet) {
        Set pathSet = treeMaker.getPathList(conceptSet);
        trimmedConceptNum = 0;
        TreeModel model = treeMaker.getTrimmedTreeModel(pathSet, project, ConceptTreeMaker.DODDLE_CLASS_ROOT_URI);
        trimmedConceptNum = treeMaker.getTrimmedConceptNum();
        beforeTrimmingConceptNum = treeMaker.getBeforeTrimmingConceptNum();
        addedSINNum = beforeTrimmingConceptNum - conceptSet.size();
        
        DODDLE.getLogger().log(Level.INFO, Translator.getTerm("ClassSINCountMessage")+": " + addedSINNum);
        DODDLE.getLogger().log(Level.INFO, Translator.getTerm("BeforeTrimmingClassCountMessage")+": " + beforeTrimmingConceptNum);
        DODDLE.getLogger().log(Level.INFO, Translator.getTerm("TrimmedClassCountMessage")+": " + trimmedConceptNum);
        DODDLE.getLogger().log(Level.INFO, Translator.getTerm("AfterTrimmingClassCountMessage")+": " + getAfterTrimmingConceptNum());
        isaTreePanel.checkAllMultipleInheritanceNode(model);
        treeMaker.conceptDriftManagement(model);
        setConceptDriftManagementResult();
        return model;
    }

    public void addComplexWordConcept(Map matchedWordIDMap, Map abstractNodeLabelMap, TreeNode rootNode) {
        DefaultTreeModel model = (DefaultTreeModel) isaTreePanel.getConceptTree().getModel();
        ConceptTreeNode conceptTreeRootNode = (ConceptTreeNode) model.getRoot();
        isaTreePanel.addComplexWordConcept(matchedWordIDMap, rootNode, conceptTreeRootNode, abstractNodeLabelMap);
        //DODDLE.getLogger().log(Level.INFO, "追加した抽象中間ノード数: " + isaTreePanel.getAbstractNodeCnt());
        addedAbstractComplexConceptCnt = isaTreePanel.getAbstractConceptCnt();
        DODDLE.getLogger().log(Level.INFO, Translator.getTerm("AbstractInternalClassCountMessage")+": " + addedAbstractComplexConceptCnt);
                
        if (addedAbstractComplexConceptCnt == 0) {
            averageAbstracComplexConceptGroupSiblingConceptCnt = 0;
        } else {
            averageAbstracComplexConceptGroupSiblingConceptCnt = isaTreePanel.getTotalAbstractNodeGroupSiblingNodeCnt()
                    / addedAbstractComplexConceptCnt;
        }
        DODDLE.getLogger().log(Level.INFO,
                Translator.getTerm("AverageAbstractSiblingConceptCountInClassesMessage")+": " + averageAbstracComplexConceptGroupSiblingConceptCnt);
    }

}