package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import javax.swing.tree.*;

/*
 * @(#)  2005/07/17
 *
 */

/**
 * @author takeshi morita
 */
public interface ComplexConceptTreeInterface {
    public void addJPWord(String id, String word);
    public void addSubConcept(String id, String word);
    public void addComplexWordConcept(Map matchedWordIDMap, Map<DefaultMutableTreeNode, String> abstractNodeLabelMap, TreeNode rootNode);
}
