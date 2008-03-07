/*
 * Project Name: DODDLE (a Domain Ontology rapiD DeveLopment Environment)
 * Project Website: http://doddle-owl.sourceforge.net/
 * 
 * Copyright (C) 2004-2008 Yamaguchi Laboratory, Keio University. All rights reserved. 
 * 
 * This file is part of DODDLE-OWL.
 * 
 * DODDLE-OWL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DODDLE-OWL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DODDLE-OWL.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;
import net.java.sen.dictionary.*;

/**
 * @author takeshi morita
 */
public class InputTermModel implements Comparable {

    private int matchedPoint;
    private int ambiguousCnt;
    private String inputWord;
    private String matchedInputWord;
    private List<Token> tokenList;
    private String wordListStr;
    private boolean isSystemAdded;

    private DODDLEProject project;

    public InputTermModel(String w, List<Token> tList, String miw, int ac, int mp, DODDLEProject p) {
        project = p;
        inputWord = w;
        tokenList = tList;
        matchedInputWord = miw;
        StringBuffer buf = new StringBuffer("(");
        for (Iterator i = tokenList.iterator(); i.hasNext();) {
            Token token = (Token) i.next();
            //String word = token.getBasicString();
            String word = token.getMorpheme().getBasicForm();
            if (i.hasNext()) {
                buf.append(word + "+");
            } else {
                buf.append(word + ")");
            }
        }
        wordListStr = buf.toString();
        ambiguousCnt = ac;
        matchedPoint = mp;
    }

    public void setIsSystemAdded(boolean t) {
        isSystemAdded = t;
    }

    public boolean isSystemAdded() {
        return isSystemAdded;
    }

    // 部分照合かどうか
    public boolean isPartiallyMatchTerm() {
        // 1 < wordList.size()の条件を2006/10/5に追加
        // 「打合せ」が「打合す」と照合してしまうため
        return !inputWord.equals(matchedInputWord) && 1 < tokenList.size();
    }

    // 完全照合かどうか
    public boolean isPerfectlyMatchWord() {
        return !isPartiallyMatchTerm();
    }

    public int compareTo(Object o) {
        InputTermModel oiwModel = (InputTermModel) o;
        int onum = oiwModel.getAmbiguousCnt();
        String oword = oiwModel.getTerm();
        if (this.ambiguousCnt < onum) {
            return 1;
        } else if (this.ambiguousCnt > onum) {
            return -1;
        } else {
            return oword.compareTo(inputWord);
        }
    }

    public String getTerm() {
        return inputWord;
    }

    public String getMatchedTerm() {
        return matchedInputWord;
    }

    public int getMatchedPoint() {
        return matchedPoint;
    }

    public int getAmbiguousCnt() {
        return ambiguousCnt;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public String getTopBasicWord() {
        //return tokenList.get(0).getBasicString();
        return tokenList.get(0).getMorpheme().getBasicForm();
    }

    public String getBasicWordWithoutTopWord() {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < tokenList.size(); i++) {
            //builder.append(tokenList.get(i).getBasicString());
            builder.append(tokenList.get(i).getMorpheme().getBasicForm());
        }
        return builder.toString();
    }

    public int getCompoundWordLength() {
        return tokenList.size();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(inputWord);
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedCompoundWordCheckBox()) {
            buf.append(" " + wordListStr);
        }
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedMatchedWordBox()) {
            buf.append(" (" + matchedInputWord + ") ");
        }
        if (isPartiallyMatchTerm() && project.isPartiallyMatchedAmbiguityCntCheckBox()) {
            buf.append(" (" + ambiguousCnt + ")");
        }
        if (isPerfectlyMatchWord() && project.isPerfectlyMatchedAmbiguityCntCheckBox()) {
            buf.append(" (" + ambiguousCnt + ")");
        }
        if (isSystemAdded() && project.isPerfectlyMatchedSystemAddedWordCheckBox()) {
            buf.append(" (" + Translator.getTerm("SystemAddedLabel") + ")");
        }
        return buf.toString();
    }
}
