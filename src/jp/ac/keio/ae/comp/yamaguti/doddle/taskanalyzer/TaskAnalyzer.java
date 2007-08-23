package jp.ac.keio.ae.comp.yamaguti.doddle.taskanalyzer;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/*
 * @(#)  2007/07/12
 */

/**
 * @author takeshi morita
 */
public class TaskAnalyzer {

    private List<CabochaDocument> cabochaDocList;
    private List<UseCaseTask> useCaseTaskList;
    private Map<String, Integer> compoundWordCountMap;
    private Map<String, Integer> compoundWordWithNokakuCountMap;

    private Set<Segment> segmentSet; // 全文書の全文節の集合を保存
    private Map<Segment, Set<Segment>> segmentMap; // 文節とその文節に係っている文節の集合を保存

    public TaskAnalyzer() {
        cabochaDocList = new ArrayList<CabochaDocument>();
        useCaseTaskList = new ArrayList<UseCaseTask>();
        segmentMap = new HashMap<Segment, Set<Segment>>();
        segmentSet = new HashSet<Segment>();
        compoundWordCountMap = new HashMap<String, Integer>();
        compoundWordWithNokakuCountMap = new HashMap<String, Integer>();
    }

    public void loadUseCaseTask(String useCaseDir) {
        File docDir = new File(useCaseDir);
        File[] files = docDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i].getName());
            loadUseCaseTask(files[i]);
        }
    }
    public void loadUseCaseTask(File file) {
        UseCaseTask useCaseTask = new UseCaseTask(file.getName());
        useCaseTaskList.add(useCaseTask);
        Document doc = new Document(file);
        CabochaDocument cabochaDoc = new CabochaDocument(doc);
        cabochaDocList.add(cabochaDoc);
        segmentSet.addAll(cabochaDoc.getSegmentSet());
        setSegmentMap(cabochaDoc);
        setCompoundWordCountMap(cabochaDoc);
        setCompoundWordWithNokakuCountMap(cabochaDoc);
        useCaseTask.addAllTask(cabochaDoc.getPrimitiveTaskList());
    }

    public List<UseCaseTask> getUseCaseTaskList() {
        return useCaseTaskList;
    }

    private void setSegmentMap(CabochaDocument doc) {
        Map<Segment, Set<Segment>> docMap = doc.getSegmentMap();
        for (Entry<Segment, Set<Segment>> entry : docMap.entrySet()) {
            if (segmentMap.get(entry.getKey()) != null) {
                Set<Segment> segSet = segmentMap.get(entry.getKey());
                segSet.addAll(entry.getValue());
            } else {
                segmentMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordCountMap(CabochaDocument doc) {
        Map<String, Integer> docMap = doc.getCompoundWordCountMap();
        for (Entry<String, Integer> entry : docMap.entrySet()) {
            if (compoundWordCountMap.get(entry.getKey()) != null) {
                compoundWordCountMap.put(entry.getKey(), entry.getValue() + compoundWordCountMap.get(entry.getKey()));
            } else {
                compoundWordCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordWithNokakuCountMap(CabochaDocument doc) {
        Map<String, Integer> docMap = doc.getCompoundWordWithNokakuCountMap();
        for (Entry<String, Integer> entry : docMap.entrySet()) {
            if (compoundWordWithNokakuCountMap.get(entry.getKey()) != null) {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue()
                        + compoundWordWithNokakuCountMap.get(entry.getKey()));
            } else {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public Map<Segment, Set<Segment>> getSegmentMap() {
        return segmentMap;
    }

    public Map<String, Integer> getCompounWordCountMap() {
        return compoundWordCountMap;
    }

    public Map<String, Integer> getCompoundWordWithNokakuCountMap() {
        return compoundWordWithNokakuCountMap;
    }

    public Set<Segment> getSegmentSet() {
        return segmentSet;
    }

    public void printSegmentSetWithNokaku() {
        System.out.println();
        System.out.println("<の格を含む文節の係り受け>");
        for (Entry<Segment, Set<Segment>> entry : segmentMap.entrySet()) {
            StringBuilder builder = new StringBuilder();
            builder.append(entry.getKey().getNounPhrase() + " => ");
            boolean isIncludingNokaku = false;
            for (Segment seg : entry.getValue()) {
                if (seg.isIncludingNoKaku()) {
                    isIncludingNokaku = true;
                    builder.append(seg + ", ");
                }
            }
            if (isIncludingNokaku) {
                System.out.println(builder.toString());
            }
        }
    }

    public void printNounAndVerbSet() {
        System.out.println();
        System.out.println("<文?ｩら抽出した形態素>");
        Set<String> nounAndVerbSet = new HashSet<String>();
        for (Segment segment : segmentSet) {
            for (Morpheme m : segment.getMorphemeList()) {
                if (m.getPos().equals(Morpheme.NOUN_NUM)) {
                    continue;
                }
                if (m.getPos().indexOf(Morpheme.NOUN) != -1 || m.getPos().indexOf(Morpheme.VERB) != -1) {
                    nounAndVerbSet.add(m.getBasic());
                }
            }
        }
        for (String s : nounAndVerbSet) {
            System.out.println(s);
        }
    }

    public void printCompoundWordSetAndCount() {
        System.out.println();
        System.out.println("<文節から抽出した複合語>");
        for (Entry<String, Integer> entry : compoundWordCountMap.entrySet()) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            System.out.println(entry.getKey());
        }
    }

    public void printCompoundWordWithNokakuSetAndCount() {
        System.out.println();
        System.out.println("<の格を含む複合語>");
        for (Entry<String, Integer> entry : compoundWordWithNokakuCountMap.entrySet()) {
            // System.out.println(entry.getKey() + ": " + entry.getValue());
            System.out.println(entry.getKey());
        }
    }

    public void printTaskDescriptions() {
        System.out.println();
        System.out.println("<タスク記述支援>");
        int i = 1;
        for (CabochaDocument cabochaDoc : cabochaDocList) {
            System.out.println("UC-06-" + i);
            cabochaDoc.printTaskDescriptions();
            System.out.println();
            i++;
        }
    }
}
