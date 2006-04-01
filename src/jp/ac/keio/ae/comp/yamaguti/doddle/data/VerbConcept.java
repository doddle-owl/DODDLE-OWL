package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

/*
 * @(#)  2005/07/18
 *
 */

/**
 * @author takeshi morita
 */
public class VerbConcept extends Concept {

    private Set<String> domainSet;
    private Set<String> rangeSet;

    public VerbConcept(Concept c) {
        super(c);
        domainSet = new TreeSet<String>();
        rangeSet = new TreeSet<String>();
    }

    public VerbConcept(String id, String concept) {
        super(id, concept);
        domainSet = new TreeSet<String>();
        rangeSet = new TreeSet<String>();
    }

    public void addAllDomain(Set<String> set) {
        domainSet.addAll(set);
    }

    public void addAllRange(Set<String> set) {
        rangeSet.addAll(set);
    }

    public void addDomain(String id) {
        domainSet.add(id);
    }

    public Set<String> getDomainSet() {
        return domainSet;
    }

    public Set<String> getRangeSet() {
        return rangeSet;
    }

    public void addRange(String id) {
        rangeSet.add(id);
    }

    public void deleteDomain(String id) {
        // System.out.println("delete domain");
        domainSet.remove(id);
    }

    public void deleteRange(String id) {
        // System.out.println("delete range");
        rangeSet.remove(id);
    }
}
