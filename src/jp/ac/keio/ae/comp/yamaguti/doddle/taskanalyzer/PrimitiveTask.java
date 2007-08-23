package jp.ac.keio.ae.comp.yamaguti.doddle.taskanalyzer;

import java.util.*;

/*
 * @(#)  2007/07/25
 */

/**
 * @author takeshi morita
 */
public class PrimitiveTask {

    private List<Segment> subjectList;
    private Segment predicate;
    private List<Segment> objectList;

    public PrimitiveTask(Segment predicate) {
        this.predicate = predicate;
        subjectList = new ArrayList<Segment>();
        objectList = new ArrayList<Segment>();
    }

    public List<Segment> getObjectList() {
        return objectList;
    }

    public String getObjectString() {
        StringBuilder builder = new StringBuilder();
        for (Segment seg : objectList) {
            builder.append(seg);
        }
        return builder.toString();
    }

    public void addObject(Segment object) {
        objectList.add(object);
    }

    public Segment getPredicate() {
        return predicate;
    }

    public String getPredicateString() {
        return predicate.toString();
    }

    public void setPredicate(Segment predicate) {
        this.predicate = predicate;
    }

    public List<Segment> getSubjectList() {
        return subjectList;
    }

    public String getSubjectString() {
        StringBuilder builder = new StringBuilder();
        for (Segment seg : subjectList) {
            builder.append(seg);
        }
        return builder.toString();
    }

    public void addSubject(Segment subject) {
        subjectList.add(subject);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t");
        for (Segment seg : subjectList) {
            builder.append(seg);
        }
        builder.append("\t");
        for (Segment seg : objectList) {
            builder.append(seg);
        }
        builder.append("\t");
        builder.append(predicate);
        builder.append("\t");
        return builder.toString();
    }
}
