/*
 * @(#)  2007/02/18
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;

import javax.xml.parsers.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author takeshi morita
 */
public class FreeMindModelMaker {

    public static final String FREEMIND_URI = "http://freemind.sourceforge.net/wiki/index.php/Main_Page#";

    public static Element getFreeMindElement(ConceptTreeNode node, Document doc) {
        Element freeMindNode = doc.createElement("node");
        freeMindNode.setAttribute("ID", node.getConcept().getURI());
        freeMindNode.setAttribute("TEXT", node.getConcept().getWord());
        Element uriAttr = doc.createElement("attribute");
        uriAttr.setAttribute("NAME", "URI");
        uriAttr.setAttribute("VALUE", node.getConcept().getURI());
        freeMindNode.appendChild(uriAttr);
        Element jaWordAttr = doc.createElement("attribute");
        jaWordAttr.setAttribute("NAME", "JA_WORD");
        jaWordAttr.setAttribute("VALUE", node.getConcept().getJaWord());
        freeMindNode.appendChild(jaWordAttr);
        Element enWordAttr = doc.createElement("attribute");
        enWordAttr.setAttribute("NAME", "EN_WORD");
        enWordAttr.setAttribute("VALUE", node.getConcept().getEnWord());
        freeMindNode.appendChild(enWordAttr);
        Element jaExplanationAttr = doc.createElement("attribute");
        jaExplanationAttr.setAttribute("NAME", "JA_EXPLANATION");
        jaExplanationAttr.setAttribute("VALUE", node.getConcept().getJaExplanation());
        freeMindNode.appendChild(jaExplanationAttr);
        Element enExplanationAttr = doc.createElement("attribute");
        enExplanationAttr.setAttribute("NAME", "EN_EXPLANATION");
        enExplanationAttr.setAttribute("VALUE", node.getConcept().getEnExplanation());
        freeMindNode.appendChild(enExplanationAttr);
        return freeMindNode;
    }

    public static void makeFreeMindModel(Document document, ConceptTreeNode node, Element freeMindNode) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            Element freeMindChildNode = getFreeMindElement(childNode, document);
            freeMindNode.appendChild(freeMindChildNode);
            makeFreeMindModel(document, childNode, freeMindChildNode);
        }
    }

    public static Element getDocumentElement(File file) {
        Element docElement = null;
        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
            Document document = docbuilder.parse(file);
            docElement = document.getDocumentElement();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return docElement;
    }

    public static Model getOWLModel(File file) {
        Model ontModel = ModelFactory.createDefaultModel();
        Element docElement = getDocumentElement(file);
        Element rootNode = null;
        Element nounRootNode = null;
        Element verbRootNode = null;
        NodeList rootNodeList = docElement.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
            }
        }
        rootNodeList = rootNode.getChildNodes();
        for (int i = 0; i < rootNodeList.getLength(); i++) {
            if (rootNodeList.item(i).getNodeName().equals("node")) {
                rootNode = (Element) rootNodeList.item(i);
                if (rootNode.getAttribute("ID").equals(DODDLE.BASE_URI + "CLASS_ROOT")) {
                    nounRootNode = rootNode;
                } else if (rootNode.getAttribute("ID").equals(DODDLE.BASE_URI + "PROP_ROOT")) {
                    verbRootNode = rootNode;
                }
            }
        }
        setOWLModel(DODDLE.BASE_URI + "CLASS_ROOT", ontModel, nounRootNode, OWL.Class);
        setOWLModel(DODDLE.BASE_URI + "PROP_ROOT", ontModel, verbRootNode, OWL.ObjectProperty);
        return ontModel;
    }

    public static void setOWLModel(String upperURI, Model ontModel, Element element, Resource type) {
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            Element childElement = null;
            if (childNode instanceof Element) {
                childElement = (Element) childNode;
            } else {
                continue;
            }
            if (childElement.getNodeName().equals("node")) {
                String inputWord = childElement.getAttribute("TEXT");
                String uri = childElement.getAttribute("ID");
                if (uri.matches("Freemind_Link.*")) {
                    uri = FREEMIND_URI + uri;
                }
                if (uri.equals("_")) {
                    uri = FREEMIND_URI + DODDLE.getCurrentProject().getUserIDStr();                    
                }
                Literal literal = ontModel.createLiteral(inputWord, "ja");
                ontModel.add(ResourceFactory.createResource(uri), RDFS.label, literal);
                ontModel.add(ResourceFactory.createResource(uri), RDF.type, type);
                if (type == OWL.Class) {
                    ontModel.add(ResourceFactory.createResource(uri), RDFS.subClassOf, ResourceFactory.createResource(upperURI));
                } else if (type == OWL.ObjectProperty) {
                    ontModel.add(ResourceFactory.createResource(uri), RDFS.subPropertyOf, ResourceFactory.createResource(upperURI));
                }                
                NodeList attrList = childElement.getChildNodes();
                for (int j = 0; j < attrList.getLength(); j++) {
                    Node attrNode = attrList.item(j);
                    if (attrNode instanceof Element && attrNode.getNodeName().equals("attribute")) {
                        Element attrElement = (Element) attrNode;
                        String attrName = attrElement.getAttribute("NAME");
                        if (attrName.equals("URI")) {
                            uri = attrElement.getAttribute("VALUE");
                            ontModel.add(ResourceFactory.createResource(uri), RDF.type, type);
                        } else if (attrName.equals("JA_WORD")) {
                            String jaWord = attrElement.getAttribute("VALUE");
                            String[] jaWords = jaWord.split("\t");
                            for (int k = 0; k < jaWords.length; k++) {
                                literal = ontModel.createLiteral(jaWords[k], "ja");
                                ontModel.add(ResourceFactory.createResource(uri), RDFS.label, literal);  
                            }                            
                        } else if (attrName.equals("EN_WORD")) {
                            String enWord = attrElement.getAttribute("VALUE");
                            String[] enWords = enWord.split("\t");
                            for (int k = 0; k < enWords.length; k++) {
                                literal = ontModel.createLiteral(enWords[k], "en");
                                ontModel.add(ResourceFactory.createResource(uri), RDFS.label, literal);                              
                            }                            
                        } else if (attrName.equals("JA_EXPLANATION")) {
                            String jaExplanation = attrElement.getAttribute("VALUE");
                            literal = ontModel.createLiteral(jaExplanation.replaceAll("\t", ""), "ja");
                            ontModel.add(ResourceFactory.createResource(uri), RDFS.comment, literal);
                        } else if (attrName.equals("EN_EXPLANATION")) {
                            String enExplanation = attrElement.getAttribute("VALUE");
                            literal = ontModel.createLiteral(enExplanation.replaceAll("\t", ""), "en");
                            ontModel.add(ResourceFactory.createResource(uri), RDFS.comment, literal);
                        }
                    }
                }
                setOWLModel(uri, ontModel, childElement, type);
            }
        }
    }

    public static void setConceptTreeModel(ConceptTreeNode treeNode, Element element) {
        NodeList childNodeList = element.getChildNodes();
        for (int i = 0; i < childNodeList.getLength(); i++) {
            Node childNode = childNodeList.item(i);
            Element childElement = null;
            if (childNode instanceof Element) {
                childElement = (Element) childNode;
            } else {
                continue;
            }
            if (childElement.getNodeName().equals("node")) {
                String inputWord = childElement.getAttribute("TEXT");
                VerbConcept concept = new VerbConcept("", inputWord);
                String uri = childElement.getAttribute("ID");
                if (uri.matches("Freemind_Link.*")) {
                    uri = FREEMIND_URI + uri;
                    concept.addJaWord(inputWord);
                }
                if (uri.equals("_")) {
                    uri = FREEMIND_URI + DODDLE.getCurrentProject().getUserIDStr();
                    concept.addJaWord(inputWord);
                }
                concept.setInputWord(inputWord);
                concept.setURI(uri);
                NodeList attrList = childElement.getChildNodes();
                for (int j = 0; j < attrList.getLength(); j++) {
                    Node attrNode = attrList.item(j);
                    if (attrNode instanceof Element && attrNode.getNodeName().equals("attribute")) {
                        Element attrElement = (Element) attrNode;
                        String attrName = attrElement.getAttribute("NAME");
                        if (attrName.equals("URI")) {
                            uri = attrElement.getAttribute("VALUE");
                            concept.setURI(uri);
                        } else if (attrName.equals("JA_WORD")) {
                            String jaWord = attrElement.getAttribute("VALUE");
                            concept.setJaWord(jaWord);
                            if (jaWord.indexOf(inputWord) == -1) {
                                concept.addJaWord(inputWord);
                            }
                        } else if (attrName.equals("EN_WORD")) {
                            String enWord = attrElement.getAttribute("VALUE");
                            concept.setEnWord(enWord);
                        } else if (attrName.equals("JA_EXPLANATION")) {
                            String jaExplanation = attrElement.getAttribute("VALUE");
                            concept.setJaExplanation(jaExplanation);
                        } else if (attrName.equals("EN_EXPLANATION")) {
                            String enExplanation = attrElement.getAttribute("VALUE");
                            concept.setEnExplanation(enExplanation);
                        }
                    }
                }
                ConceptTreeNode childTreeNode = new ConceptTreeNode(concept, DODDLE.getCurrentProject());
                treeNode.add(childTreeNode);
                setConceptTreeModel(childTreeNode, childElement);
            }
        }
    }
}
