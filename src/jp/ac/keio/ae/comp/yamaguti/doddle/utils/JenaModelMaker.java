package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

/*
 * Created on 2004/02/06
 *  
 */

import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 * 
 */
public class JenaModelMaker {

    public static final String SKOS_URI = "http://www.w3.org/2004/02/skos/core#";
    public static final Property SKOS_PREFLABEL = ResourceFactory.createProperty(SKOS_URI+"prefLabel");
    
    private static void addDefaultConceptInfo(Model ontology, Resource child, ConceptTreeNode node) {
        ontology.add(child, SKOS_PREFLABEL, ontology.createLiteral(node.getConcept().getWord()));
        String[] enWords = node.getEnWords();
        for (int i = 0; i < enWords.length; i++) {
            if (!enWords[i].equals("")) {
                ontology.add(child, RDFS.label, ontology.createLiteral(enWords[i], "en"));
            }
        }
        String[] jpWords = node.getJpWords();
        for (int i = 0; i < jpWords.length; i++) {
            if (!jpWords[i].equals("")) {
                ontology.add(child, RDFS.label, ontology.createLiteral(jpWords[i], "ja"));
            }
        }
        if (!node.getEnExplanation().equals("")) {
            ontology.add(child, RDFS.comment, ontology.createLiteral(node.getEnExplanation().replaceAll("\t", ""), "en"));
        }
        if (!node.getJpExplanation().equals("")) {
            ontology.add(child, RDFS.comment, ontology.createLiteral(node.getJpExplanation().replaceAll("\t", ""), "ja"));
        }
    }

    private static Resource createResource(ConceptTreeNode node) {        
        return ResourceFactory.createResource(node.getURIStr());
    }

    private static Resource getResource(ConceptTreeNode node, Model ontology) {
        ConceptTreeNode parentNode = (ConceptTreeNode) node.getParent();
        return ontology.getResource(parentNode.getURIStr());
    }

    public static Model makeClassModel(ConceptTreeNode node, Model ontology) {
        if (node == null) { return ontology; }
        if (node.isLeaf()) {
            Resource child = createResource(node);
            ontology.add(child, RDF.type, OWL.Class);
            addDefaultConceptInfo(ontology, child, node);
            if (node.getParent() != null) {
                Resource parent = getResource(node, ontology);
                ontology.add(child, RDFS.subClassOf, parent);
            }
        } else {
            if (node.isRoot()) {
                ontology.add(createResource(node), RDF.type, OWL.Class);
            } else {
                Resource child = createResource(node);
                ontology.add(child, RDF.type, OWL.Class);
                addDefaultConceptInfo(ontology, child, node);
                Resource parent = getResource(node, ontology);
                ontology.add(child, RDFS.subClassOf, parent);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            ontology = makeClassModel((ConceptTreeNode) node.getChildAt(i), ontology);
        }
        return ontology;
    }

    private static void addRegion(Resource resource, Property region, Model ontology, Set<String> regionSet) {
        for (String uri : regionSet) {
            ontology.add(resource, region, ontology.getResource(uri));
        }
    }

    public static Model makePropertyModel(ConceptTreeNode node, Model ontology) {
        if (node == null) { return ontology; }
        if (node.isLeaf() && !node.isRoot()) {
            Resource child = createResource(node);
            ontology.add(child, RDF.type, OWL.ObjectProperty);
            addDefaultConceptInfo(ontology, child, node);
            VerbConcept vc = (VerbConcept) node.getConcept();
            addRegion(child, RDFS.domain, ontology, vc.getDomainSet());
            addRegion(child, RDFS.range, ontology, vc.getRangeSet());
            Resource parent = getResource(node, ontology);
            ontology.add(child, RDFS.subPropertyOf, parent);
        } else {
            if (node.isRoot()) {
                ontology.add(createResource(node), RDF.type, OWL.ObjectProperty);
            } else {
                Resource child = createResource(node);
                ontology.add(child, RDF.type, OWL.ObjectProperty);
                addDefaultConceptInfo(ontology, child, node);
                VerbConcept vc = (VerbConcept) node.getConcept();
                addRegion(child, RDFS.domain, ontology, vc.getDomainSet());
                addRegion(child, RDFS.range, ontology, vc.getRangeSet());
                Resource parent = getResource(node, ontology);
                ontology.add(child, RDFS.subPropertyOf, parent);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            ontology = makePropertyModel((ConceptTreeNode) node.getChildAt(i), ontology);
        }
        return ontology;
    }
}