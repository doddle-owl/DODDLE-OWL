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
public class OWLOntologyManager {

    private static Map<String, ReferenceOWLOntology> refOntMap = new HashMap<String, ReferenceOWLOntology>();

    public static void addRefOntology(String uri, ReferenceOWLOntology ontInfo) {
        refOntMap.put(uri, ontInfo);
    }

    public static ReferenceOWLOntology getRefOntology(String uri) {
        return refOntMap.get(uri);
    }

    public static void removeRefOntology(String uri) {
        refOntMap.remove(uri);
    }

    public static Collection<ReferenceOWLOntology> getRefOntologySet() {
        return refOntMap.values();
    }

    public static Set<List<Concept>> getPathToRootSet(String uri) {
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<List<Concept>> set = refOnt.getPathToRootSet(uri);
                if (set != null) {
                    pathToRootSet.addAll(set);
                }
            }
        }
        return pathToRootSet;
    }
    
    public static Set<List<String>> getURIPathToRootSet(String uri) {
        Set<List<String>> pathToRootSet = new HashSet<List<String>>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<List<String>> set = refOnt.getURIPathToRootSet(uri);
                if (set != null) {
                    pathToRootSet.addAll(set);
                }
            }
        }
        return pathToRootSet;
    }

    public static Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<Concept>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                for (String uri : refOnt.getPropertySet()) {
                    Concept c = refOnt.getConcept(uri);
                    if (inputConceptSet.contains(c)) {
                        verbConceptSet.add(c);
                    }
                }
            }
        }
        return verbConceptSet;
    }

    public static Concept getConcept(String uri) {
        Concept concept = null;
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                concept = refOnt.getConcept(uri);
            }
        }
        return concept;
    }

    public static Set<String> getDomainSet(Concept c, List<List<Concept>> trimmedConceptList) {
        Set<String> domainSet = new HashSet<String>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                domainSet.addAll(refOnt.getDomainSet(c.getURI()));
                for (List<Concept> list : trimmedConceptList) {
                    for (Concept trimmedConcept : list) {
                        domainSet.addAll(refOnt.getDomainSet(trimmedConcept.getURI()));
                    }
                }
            }
        }
        return domainSet;
    }

    public static Set<String> getRangeSet(Concept c, List<List<Concept>> trimmedConceptList) {
        Set<String> rangeSet = new HashSet<String>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                rangeSet.addAll(refOnt.getRangeSet(c.getURI()));
                for (List<Concept> list : trimmedConceptList) {
                    for (Concept trimmedConcept : list) {
                        rangeSet.addAll(refOnt.getRangeSet(trimmedConcept.getURI()));
                    }
                }
            }
        }
        return rangeSet;
    }

    public static Set<String> getSubURISet(String uri, Set<String> nounURISet) {
        Set<String> subURISet = new HashSet<String>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> uriSet = new HashSet<String>();
                refOnt.getSubURISet(uri, nounURISet, uriSet);
                subURISet.addAll(uriSet);
            }
        }
        return subURISet;
    }

    public static Set<String> getURISet(String word) {
        Set<String> uriSet = new HashSet<String>();
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> set = refOnt.getURISet(word);
                if (set == null) {
                    continue;
                }
                uriSet.addAll(set);
            }
        }
        return uriSet;
    }

    public static void setOWLConceptSet(String word, Set<Concept> conceptSet) {
        for (ReferenceOWLOntology refOnt : getRefOntologySet()) {
            if (refOnt.isAvailable()) {
                Set<String> uriSet = refOnt.getURISet(word);
                if (uriSet == null) {
                    continue;
                }
                for (String uri : uriSet) {
                    conceptSet.add(refOnt.getConcept(uri));
                }
            }
        }
    }
}
