package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/*
 * @(#)  2005/05/20
 *
 */

/**
 * @author takeshi morita
 */
public class EvalConcept implements Comparable {

    private Concept concept;
    private int evalValue;

    public EvalConcept(Concept c, int v) {
        concept = c;
        evalValue = v;
    }

    public int getEvalValue() {
        return evalValue;
    }

    public Concept getConcept() {
        return concept;
    }

    public int compareTo(Object o) {
        int ev = ((EvalConcept) o).getEvalValue();
        EvalConcept c = (EvalConcept) o;
        if (evalValue < ev) {
            return 1;
        } else if (evalValue > ev) {
            return -1;
        } else {
            if (concept == null) {
                return 1;
            } else if (c == null) { return -1; }
            return concept.getIdentity().compareTo(c.getConcept().getIdentity());
        }
    }

    public String toString() {
        if (concept == null) { return Translator.getString("DisambiguationPanel.NotAvailable"); }
        return "[" + evalValue + "]" + "[" + concept.getIdentity() + "]" + "[" + concept.getWord() + "]";
    }
}
