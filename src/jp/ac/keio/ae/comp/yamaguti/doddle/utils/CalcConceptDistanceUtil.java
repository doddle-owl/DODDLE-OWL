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

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

/**
 * @author takeshi morita
 */
public class CalcConceptDistanceUtil {

    public static Set<List<String>[]> makeCombination(Set<List<String>> pathSet1, Set<List<String>> pathSet2) {
        Set<List<String>[]> combinationSet = new HashSet<List<String>[]>();
        for (List<String> path1: pathSet1) {
            for (List<String> path2: pathSet2) {
                combinationSet.add(new List[] { path1, path2});
            }
        }
        return combinationSet;
    }

    private static Set<List<String>[]> getCombinationSet(Concept c1, Concept c2) {
        Set<List<String>> pathSet1 = new HashSet<List<String>>();
        Set<List<String>> pathSet2 = new HashSet<List<String>>();
        if (c1.getNameSpace().equals(DODDLEConstants.EDR_URI) && c2.getNameSpace().equals(DODDLEConstants.EDR_URI)) {
            pathSet1 = EDRTree.getEDRTree().getURIPathToRootSet(c1.getLocalName());
            pathSet2 = EDRTree.getEDRTree().getURIPathToRootSet(c2.getLocalName());
        } else if (c1.getNameSpace().equals(DODDLEConstants.EDRT_URI) && c2.getNameSpace().equals(DODDLEConstants.EDRT_URI)) {
            pathSet1 = EDRTree.getEDRTTree().getURIPathToRootSet(c1.getLocalName());
            pathSet2 = EDRTree.getEDRTTree().getURIPathToRootSet(c2.getLocalName());
        } else if (c1.getNameSpace().equals(DODDLEConstants.WN_URI) && c2.getNameSpace().equals(DODDLEConstants.WN_URI)) {
            pathSet1 = WordNetDic.getURIPathToRootSet(new Long(c1.getLocalName()));
            pathSet2 = WordNetDic.getURIPathToRootSet(new Long(c2.getLocalName()));
        }
        return makeCombination(pathSet1, pathSet2);
    }

    private static List<Integer> getConceptDiff(Concept concept1, Concept concept2) {
        Set<List<String>[]> combinationSet = getCombinationSet(concept1, concept2);
        List<Integer> conceptDiffList = new ArrayList<Integer>();
        for (List<String>[] combination : combinationSet) {
            List<String> path1 = combination[0];
            List<String> path2 = combination[1];
            conceptDiffList.add(calcConceptDiff(path1, path2));
        }
        Collections.sort(conceptDiffList);
        return conceptDiffList;
    }

    private static List<Integer> getExtendDiff(Concept concept1, Concept concept2) {
        Set<List<String>[]> combinationSet = getCombinationSet(concept1, concept2);

        List<Integer> conceptDiffList = new ArrayList<Integer>();
        for (List<String>[] combination: combinationSet) {
            List<String> path1 = combination[0];
            List<String> path2 = combination[1];
            Integer distance = calcConceptDiff(path2.get(path2.size() - 1), path1);
            if (distance != null) {
                conceptDiffList.add(distance);
            }
        }
        Collections.sort(conceptDiffList);
        return conceptDiffList;
    }

    private static Integer calcConceptDiff(String c, List<String> path) {
        if (path.contains(c)) {
            for (int i = path.size() - 1, diff = 0; 0 <= i; i--, diff++) {
                if (path.get(i).equals(c)) { return new Integer(diff); }
            }
        }
        return null;
    }

    private static Integer calcConceptDiff2(List<String> path1, List<String> path2) {
        for (int i = path1.size() - 1; 0 <= i; i--) {
            String c1 = path1.get(i);
            for (int j = path2.size() - 1; 0 <= j; j--) {
                String c2 = path2.get(j);
                if (c1.equals(c2)) {
                    int len1 = path1.size() - i - 1;
                    int len2 = path2.size() - j - 1;
                    return new Integer(len1 + len2);
                }
            }
        }
        return null;
    }

    private static Integer calcConceptDiff(List<String> path1, List<String> path2) {
        Integer diff = null;
        diff = calcConceptDiff(path2.get(path2.size() - 1), path1);
        if (diff != null) { return diff; }
        diff = calcConceptDiff(path1.get(path1.size() - 1), path2);
        if (diff != null) { return diff; }
        diff = calcConceptDiff2(path1, path2);
        if (diff != null) { return diff; }
        return new Integer(path1.size() + path2.size());
    }

    public static int getShortestConceptDiff(Concept c1, Concept c2) {
        List<Integer> conceptDiffList = getConceptDiff(c1, c2);
        if (conceptDiffList.size() == 0) { return 0; }
        Integer longestConceptDiff = conceptDiffList.get(0);
        return longestConceptDiff.intValue();
    }

    public static int getLongestConceptDiff(Concept c1, Concept c2) {
        List<Integer> conceptDiffList = getConceptDiff(c1, c2);
        if (conceptDiffList.size() == 0) { return 0; }
        Integer longestConceptDiff = conceptDiffList.get(conceptDiffList.size() - 1);
        return longestConceptDiff.intValue();
    }

    public static int getAverageConceptDiff(Concept c1, Concept c2) {
        List<Integer> conceptDiffList = getConceptDiff(c1, c2);
        int total = 0;
        for (Integer length: conceptDiffList) {
            total += length.intValue();
        }
        return total / conceptDiffList.size();
    }

    public static boolean isShortestExtendMatching(Concept outputConcept, Concept inputConcept, int threshold) {
        List<Integer> conceptDiffList = getExtendDiff(outputConcept, inputConcept);
        if (conceptDiffList.size() == 0) { return false; }
        Integer longestConceptDiff = conceptDiffList.get(0);
        int distance = longestConceptDiff.intValue();
        return distance <= threshold;
    }

    public static boolean isLongestExtendMatching(Concept outputConcept, Concept inputConcept, int threshold) {
        List<Integer> conceptDiffList = getExtendDiff(outputConcept, inputConcept);
        if (conceptDiffList.size() == 0) { return false; }
        Integer longestConceptDiff = conceptDiffList.get(conceptDiffList.size() - 1);
        int distance = longestConceptDiff.intValue();
        return distance <= threshold;
    }

    public static boolean isAverageExtendMatching(Concept outputConcept, Concept inputConcept, int threshold) {
        List<Integer> conceptDiffList = getExtendDiff(outputConcept, inputConcept);
        if (conceptDiffList.size() == 0) { return false; }
        int total = 0;
        for (Integer length: conceptDiffList) {
            total += length.intValue();
        }
        int distance = total / conceptDiffList.size();
        return distance <= threshold;
    }

    public static void testConceptDiff() {
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3cf5e5"), EDRDic.getEDRConcept("30f6af")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("30f6af"), EDRDic.getEDRConcept("3cf5e5")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("1f585c"), EDRDic.getEDRConcept("3c90af")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3cfd0d"), EDRDic.getEDRConcept("3cf5fb")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3cf5fb"), EDRDic.getEDRConcept("3cfd0d")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3cf5e5"), EDRDic.getEDRConcept("3cfd0d")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3bdc67"), EDRDic.getEDRConcept("101b25")));
        System.out.println(getShortestConceptDiff(EDRDic.getEDRConcept("3bdc67"), EDRDic.getEDRConcept("3bc83c")));
        System.out.println(getLongestConceptDiff(EDRDic.getEDRConcept("3bdc67"), EDRDic.getEDRConcept("3bc83c")));
        System.out.println(getAverageConceptDiff(EDRDic.getEDRConcept("3bdc67"), EDRDic.getEDRConcept("3bc83c")));
        System.out.println(getAverageConceptDiff(WordNetDic.getInstance().getWNConcept("2001223"), WordNetDic
                .getInstance().getWNConcept("2037721")));
        System.out.println(getAverageConceptDiff(EDRDic.getEDRTConcept("2f16f0"), EDRDic.getEDRTConcept("2f14dd")));
    }

    public static void main(String[] args) {
        EDRDic.initEDRDic();
        testConceptDiff();
    }
}
