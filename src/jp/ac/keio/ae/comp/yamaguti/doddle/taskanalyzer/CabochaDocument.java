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

package jp.ac.keio.ae.comp.yamaguti.doddle.taskanalyzer;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/**
 * @author takeshi morita
 */
public class CabochaDocument {

    private String docName;
    private Document document;
    private List<Sentence> sentenceList;
    private Set<Segment> segmentSet;
    private Map<String, Integer> compoundWordCountMap;
    private Map<String, Integer> compoundWordWithNokakuCountMap;
    private Map<Segment, Set<Segment>> segmentMap;
    private Process cabochaProcess;

    public CabochaDocument(Process cp) {
        cabochaProcess = cp;
        sentenceList = new ArrayList<Sentence>();
        segmentSet = new HashSet<Segment>();
        compoundWordCountMap = new HashMap<String, Integer>();
        compoundWordWithNokakuCountMap = new HashMap<String, Integer>();
        segmentMap = new HashMap<Segment, Set<Segment>>();
    }

    public CabochaDocument(String fname, Process cp) {
        this(cp);
        docName = fname;
        cabochaFileReader();
    }

    public CabochaDocument(Document doc, Process cp) {
        this(cp);
        document = doc;
        cabochaDocReader();
    }

    private void cabochaReader(BufferedReader reader) throws IOException {
        String line = "";
        Segment segment = null;
        Sentence sentence = new Sentence();
        while ((line = reader.readLine()) != null) {
            String[] elems = line.split("\\s");
            if (elems.length == 1) {
                sentence.mergeSegments();
                setSegmentMap(sentence);
                setCompoundWordCountMap(sentence);
                setCompoundWordWithNokakuCountMap(sentence);
                segmentSet.addAll(sentence.getSegmentList());
                sentenceList.add(sentence);
                sentence = new Sentence();
            } else if (elems.length == 5) {
                int num = Integer.parseInt(elems[2].replaceAll("D|O", ""));
                segment = new Segment(num);
                sentence.addSegment(segment);
            } else if (elems.length == 7) {
                Morpheme morpheme = new Morpheme(elems);
                segment.addMorpheme(morpheme);
            }
        }
    }

    private void cabochaFileReader() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(cabochaProcess.getInputStream()));
            cabochaReader(reader);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    private void cabochaDocReader() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(cabochaProcess.getInputStream()));
            cabochaReader(reader);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }
        }
    }

    public String getDocName() {
        return docName;
    }

    public Set<Segment> getSegmentSet() {
        return segmentSet;
    }

    private void setSegmentMap(Sentence sentence) {
        Map<Segment, Set<Segment>> sentenceMap = sentence.getSegmentMap();
        for (Entry<Segment, Set<Segment>> entry : sentenceMap.entrySet()) {
            if (segmentMap.get(entry.getKey()) != null) {
                Set<Segment> segSet = segmentMap.get(entry.getKey());
                segSet.addAll(entry.getValue());
            } else {
                segmentMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            if (compoundWordCountMap.get(entry.getKey()) != null) {
                compoundWordCountMap.put(entry.getKey(), entry.getValue() + compoundWordCountMap.get(entry.getKey()));
            } else {
                compoundWordCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void setCompoundWordWithNokakuCountMap(Sentence sentence) {
        Map<String, Integer> sentenceMap = sentence.getCompoundWordWithNokakuCountMap();
        for (Entry<String, Integer> entry : sentenceMap.entrySet()) {
            if (compoundWordWithNokakuCountMap.get(entry.getKey()) != null) {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue()
                        + compoundWordWithNokakuCountMap.get(entry.getKey()));
            } else {
                compoundWordWithNokakuCountMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public List<Sentence> getSentenceList() {
        return sentenceList;
    }

    public Map<Segment, Set<Segment>> getSegmentMap() {
        return segmentMap;
    }

    public Set<String> getCompoundWordSet() {
        return compoundWordCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordCountMap() {
        return compoundWordCountMap;
    }

    public Set<String> getCompoundWordWithNokakuSet() {
        return compoundWordWithNokakuCountMap.keySet();
    }

    public Map<String, Integer> getCompoundWordWithNokakuCountMap() {
        return compoundWordWithNokakuCountMap;
    }

    public List<PrimitiveTask> getPrimitiveTaskList() {
        List<PrimitiveTask> primitiveTaskList = new ArrayList<PrimitiveTask>();
        for (Sentence sentence : sentenceList) {
            primitiveTaskList.addAll(sentence.getTaskDescriptionSet());
        }
        return primitiveTaskList;
    }

    public void printTaskDescriptions() {
        for (Sentence sentence : sentenceList) {
            System.out.println("(文): " + sentence);
            for (PrimitiveTask taskDescription : sentence.getTaskDescriptionSet()) {
                System.out.println(taskDescription);
            }
            System.out.println("");
        }
    }

    public String toString() {
        return document.getFile().getName() + " sentence size: " + sentenceList.size() + " segment size: "
                + segmentSet.size();
    }
}
