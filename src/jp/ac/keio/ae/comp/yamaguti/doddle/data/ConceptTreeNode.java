package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/*
 * Created on 2003/09/08
 * 
 */

import java.util.*;
import java.util.Map.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * 
 * @author mioki
 * 
 * modified by takeshi morita
 */
public class ConceptTreeNode extends DefaultMutableTreeNode {

    private int depth; // �g���~���O�O�̃m�[�h�̐[��
    private int trimmedNum; // �g���~���O��̐e�m�[�h�Ƃ̍��i���m�[�h�폜���ꂽ���j

    private String conceptID;

    private boolean isInputConcept;
    private boolean isUserConcept;
    private boolean isMultipleInheritance;

    private DODDLEProject project;

    public ConceptTreeNode(Concept c, DODDLEProject p) {
        super();
        project = p;
        if (project.getConcept(c.getId()) == null) {
            setInputWord(c);
            project.putConcept(c.getId(), c);
        }
        conceptID = c.getId();
    }

    /**
     * ��\���o����DB���g�p���Ă��鎞�ɐݒ肷�邽�߂̃��\�b�h �����C�����������̂ƁC������T�O�ɂ��ēK�؂�
     * ���삵�Ă��邩�ǂ������e�X�g����K�v������D
     */
    private void setInputWord(Concept c) {
        if (DODDLE.IS_USING_DB) {
            Map wordConceptMap = project.getInputModuleUI().getWordConceptMap();
            for (Iterator i = wordConceptMap.entrySet().iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                String word = (String) entry.getKey();
                Concept concept = (Concept) entry.getValue();
                if (concept.equals(c) && EDRDic.getIDSet(word) != null) {
                    c.setInputWord(word);
                    break;
                }
            }
        }
    }

    public Concept getConcept() {
        return project.getConcept(conceptID);
    }

    public void setConcept(Concept c) {
        conceptID = c.getId();
        project.putConcept(conceptID, c);
    }

    public void removeRelation() {
        this.removeAllChildren();
        this.removeFromParent();
    }

    public String getJpWord() {
        return getConcept().getJpWord();
    }

    public String[] getJpWords() {
        return getConcept().getJpWords();
    }

    public String getEnWord() {
        return getConcept().getEnWord();
    }

    public String[] getEnWords() {
        return getConcept().getEnWords();
    }

    public String getJpExplanation() {
        return getConcept().getJpExplanation();
    }

    public String getEnExplanation() {
        return getConcept().getEnExplanation();
    }

    public void setTrimmedCount(int i) {
        trimmedNum = i;
    }

    public int getTrimmedCount() {
        return trimmedNum;
    }

    public void setdepth(int d) {
        depth = d;
    }

    public int getdepth() {
        return depth;
    }

    public String getPrefix() {
        return getConcept().getPrefix();
    }

    public String getId() {
        return conceptID;
    }

    public String getIdentity() {
        return getConcept().getIdentity();
    }

    public String getInputWord() {
        return getConcept().getInputWord();
    }

    public String getIdStr() {
        if (isUserConcept) { return getConcept().getId(); }
        return "ID" + getConcept().getId();
    }

    public void setIsInputConcept(boolean t) {
        isInputConcept = t;
    }

    public boolean isInputConcept() {
        return isInputConcept;
    }

    public void setIsMultipleInheritance(boolean t) {
        isMultipleInheritance = t;
    }

    public boolean isMultipleInheritance() {
        return isMultipleInheritance;
    }

    public boolean isUserConcept() {
        return isUserConcept;
    }

    public void setIsUserConcept(boolean t) {
        isUserConcept = t;
    }

    public String toString() {
        // return getConcept().getWord();
        // ���Ƃŕ\����؂�ւ�����悤�ɂ���ׂ�
        return getConcept().getPrefix() + ":" + getConcept().getWord();
    }
}