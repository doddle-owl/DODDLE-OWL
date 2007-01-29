/*
 * Created on 2003/10/29
 *  
 */
package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;
import net.java.sen.*;

/**
 * @author 2006-01-12: takeshi morita
 * 
 */
public class WordSpace {

    private List<String> corpusTokenList;

    private Map gramNumMap;
    private Map<String, List<ConceptPair>> wordSpaceResult;

    private WordSpaceData wsData;
    private List allConceptPairs;

    private List<String> inputWordList;
    private Document document;
    private ConceptDefinitionPanel conceptDefinitionPanel;

    public WordSpace(ConceptDefinitionPanel cdp, Document doc) {
        document = doc;
        conceptDefinitionPanel = cdp;
        inputWordList = conceptDefinitionPanel.getInputWordList();

        gramNumMap = new HashMap();
        wordSpaceResult = new HashMap<String, List<ConceptPair>>();
        allConceptPairs = new ArrayList();
        makeTokenList(doc, inputWordList);
    }

    public Document getDocument() {
        return document;
    }

    public void setWSData(WordSpaceData d) {
        wsData = d;
    }

    private void makeTokenList(Document doc, List<String> inputWordList) {
        if (doc.getLang().equals("ja")) {
            makeJaTokenList(DocumentSelectionPanel.getTextString(doc), inputWordList);
        } else if (doc.getLang().equals("en")) {
            makeEnTokenList(DocumentSelectionPanel.getTextString(doc));
        }
    }

    private void makeEnTokenList(String text) {
        if (text == null) { return; }
        corpusTokenList = new ArrayList<String>();
        text = text.replaceAll("\\.|．", "");
        for (String token : text.split("\\s+")) {
            corpusTokenList.add(token.toLowerCase());
        }
        Utils.addEnComplexWord(corpusTokenList, inputWordList); // 以下，複合語の追加
    }

    private void makeJaTokenList(String text, List<String> inputWordList) {
        corpusTokenList = new ArrayList<String>();
        if (text == null) { return; }
        try {
            StringTagger tagger = StringTagger.getInstance();
            Token[] tokenList = tagger.analyze(text);
            if (tokenList == null) { return; }
            for (int i = 0; i < tokenList.length; i++) {
                String basicStr = tokenList[i].getBasicString();
                corpusTokenList.add(basicStr);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Utils.addJaComplexWord(corpusTokenList, inputWordList); // 以下，複合語の追加
    }

    public Map<String, List<ConceptPair>> calcWordSpaceResult(List<String> targetInputWordList) {
        allConceptPairs.clear();
        if (corpusTokenList.size() == 0) { return null; }
        if (0 < wsData.getGramNumber()) {
            for (int i = 1; i <= wsData.getGramNumber(); i++) {
                setGram(i, corpusTokenList);
            }
        }
        DODDLE.STATUS_BAR.addProjectValue();
        // System.out.println("all gram num" + gramNumMap.size());
        List gramText = makeGramText(corpusTokenList);
        DODDLE.STATUS_BAR.addProjectValue();
        // System.out.println("gram txt: " + gramText);
        int allGramNum = gramNumMap.size();
        gramNumMap.clear();
        int[][] matrix = getGramMatrix(allGramNum, gramText);
        DODDLE.STATUS_BAR.addProjectValue();
        wordSpaceResult = getWordSpaceResult(matrix, allGramNum, targetInputWordList);
        DODDLE.STATUS_BAR.addProjectValue();
        // System.out.println("result: " + wordSpaceResult);
        return wordSpaceResult;
    }

    public Map getWordSpaceResult() {
        return wordSpaceResult;
    }

    private int[][] getGramMatrix(int allGramNum, List gramText) {
        // System.out.println("All Gram Num: " + allGramNum);// +++
        // System.out.println("Gram Text Size: " + gramText.size());

        int new_gram_number = 1;
        int matrix[][] = new int[allGramNum][allGramNum];
        gramNumMap.put(gramText.get(0), new Integer(0));

        for (int i = 0; i < gramText.size(); i++) {
            int row = ((Integer) gramNumMap.get(gramText.get(i))).intValue();
            for (int j = 1; j <= wsData.getFrontScope(); j++) {
                int place = i - j;
                if (place < 0) {
                    break;
                }
                int col = ((Integer) gramNumMap.get(gramText.get(place))).intValue();
                matrix[row][col]++;
            }

            for (int j = 1; j <= wsData.getBehindScope(); j++) {
                int place = i + j;
                if (place >= gramText.size()) {
                    break;
                }
                int col;
                String gram = (String) gramText.get(place);
                if (gramNumMap.containsKey(gram)) {
                    col = ((Integer) gramNumMap.get(gram)).intValue();
                } else {
                    gramNumMap.put(gram, new Integer(new_gram_number));
                    col = new_gram_number;
                    new_gram_number++;
                }
                matrix[row][col]++;
            }
        }
        return matrix;
    }

    private Map<String, List<ConceptPair>> getWordSpaceResult(int matrix[][], int allGramNum,
            List<String> targetInputWordList) {
        Map<String, List<ConceptPair>> wordPairMap = new HashMap<String, List<ConceptPair>>();

        for (int i = 0; i < targetInputWordList.size(); i++) {
            String w1 = targetInputWordList.get(i);
            List<ConceptPair> pairList = new ArrayList<ConceptPair>();
            for (int j = 0; j < targetInputWordList.size(); j++) {
                String w2 = targetInputWordList.get(j);
                if (i != j) {
                    Concept c1 = conceptDefinitionPanel.getConcept(w1);
                    Concept c2 = conceptDefinitionPanel.getConcept(w2);
                    Double similarity = getSimilarityValue(c1, c2, matrix, allGramNum);
                    // System.out.println(c1 + "=>" + c2 + " = " + similarity);
                    if (wsData.getUnderValue() < similarity.doubleValue()) {
                        ConceptPair pair = new ConceptPair(w1, w2, similarity);
                        allConceptPairs.add(pair);
                        pairList.add(pair);
                    }
                }
            }
            wordPairMap.put(w1, pairList);
        }

        return wordPairMap;
    }

    private void setVec(int[] vec, List cLabelList, int[][] matrix, int allGramNum) {
        for (int i = 0; i < cLabelList.size(); i++) {
            String w1 = (String) cLabelList.get(i);
            // System.out.println("----" + w1);
            if (gramNumMap.containsKey(w1)) {
                for (int j = 0; j < allGramNum; j++) {
                    vec[j] += matrix[((Integer) gramNumMap.get(w1)).intValue()][j];
                }
            }
        }
    }

    private Double getSimilarityValue(Concept c1, Concept c2, int matrix[][], int allGramNum) {
        // 英語ラベルと日本語ラベルを両方とも考慮
        List<String> c1LabelList = new ArrayList<String>();
        List<String> c2LabelList = new ArrayList<String>();
        c1LabelList.addAll(Arrays.asList(c1.getEnWords()));
        c1LabelList.addAll(Arrays.asList(c1.getJaWords()));
        c2LabelList.addAll(Arrays.asList(c2.getEnWords()));
        c2LabelList.addAll(Arrays.asList(c2.getJaWords()));

        int[] vec1 = new int[allGramNum];
        int[] vec2 = new int[allGramNum];

        // System.out.println("C1::" + c1LabelList);
        // System.out.println("C2::" + c2LabelList);
        for (int i = 0; i < allGramNum; i++) {
            vec1[i] = 0;
            vec2[i] = 0;
        }

        setVec(vec1, c1LabelList, matrix, allGramNum);
        setVec(vec2, c2LabelList, matrix, allGramNum);

        double absVec1 = 0;
        double absVec2 = 0;
        double innerProduct = 0;
        for (int i = 0; i < allGramNum; i++) {
            // System.out.println(concept1[i] + "--" + concept2[i]);
            innerProduct += vec1[i] * vec2[i];
            absVec1 += Math.pow(vec1[i], 2);
            absVec2 += Math.pow(vec2[i], 2);
        }
        absVec1 = StrictMath.sqrt(absVec1);
        absVec2 = StrictMath.sqrt(absVec2);
        return new Double(innerProduct / (absVec1 * absVec2));
    }

    /*
     * tokenListは，入力文書の形態素の配列
     */
    private void setGram(int gramNum, List tokenList) {
        for (Iterator i = tokenList.iterator(); i.hasNext();) {
            StringBuffer gramBuf = new StringBuffer("");
            for (int j = 0; j < gramNum; j++) {
                if (i.hasNext()) {
                    gramBuf.append((String) i.next());
                    gramBuf.append("_");
                }
            }
            String gram = gramBuf.substring(0, gramBuf.length() - 1);
            if (gramNumMap.containsKey(gram)) {
                Integer num = (Integer) gramNumMap.get(gram);
                gramNumMap.put(gram, new Integer(num.intValue() + 1));
            } else {
                gramNumMap.put(gram, new Integer(1));
            }
        }
    }

    public List makeGramText(List tokenList) {
        List gramText = new ArrayList();
        for (Iterator i = tokenList.iterator(); i.hasNext();) {
            List gramList = new ArrayList();
            for (int j = 0; j < wsData.getGramNumber(); j++) {
                if (i.hasNext()) {
                    gramList.add(i.next());
                }
            }
            addUsableGramToList(gramList, gramText);
        }
        return gramText;
    }

    private boolean isInputWord(String key) {
        return inputWordList.contains(key);
    }

    private boolean isUsableGram(String key) {
        if (gramNumMap.containsKey(key)) {
            int num = ((Integer) (gramNumMap.get(key))).intValue();
            if (wsData.getGramCount() <= num || isInputWord(key)) { return true; }
            gramNumMap.remove(key);
        }
        return false;
    }

    private List addUsableGramToList(List gramList, List gramText) {
        for (int i = 1; i <= wsData.getGramNumber(); i++) {
            for (int j = 0; j <= wsData.getGramNumber() - i; j++) {
                StringBuffer gramBuf = new StringBuffer("");
                for (int k = 0; k < i; k++) {
                    if ((k + j) < gramList.size()) {
                        gramBuf.append(gramList.get(k + j));
                        gramBuf.append("_");
                    }
                }
                if (0 < gramBuf.length()) {
                    String gram = gramBuf.substring(0, gramBuf.length() - 1);
                    if (isUsableGram(gram)) {
                        gramText.add(gram);
                    }
                }
            }
        }
        return gramText;
    }

    public List getAllConceptPairs() {
        return allConceptPairs;
    }
}