package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.util.*;

import javax.swing.tree.*;

import org.apache.log4j.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.data.*;
import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/*
 * @(#)  2004/08/20
 */

/**
 * @author takeshi morita
 */
public class ConceptTreeMaker {

    private Set<Concept> inputConceptSet;
    private Map idNodeMap;
    private List MRresultList;
    private List TRresultList;

    private static ConceptTreeMaker maker = new ConceptTreeMaker();
    public static String EDR_CLASS_ROOT_ID = "3aa966";
    public static String EDR_PROPERTY_ROOT_ID = "PROP_ROOT";

    private ConceptTreeMaker() {
        inputConceptSet = new HashSet<Concept>();
        idNodeMap = new HashMap();
        MRresultList = new ArrayList();
        TRresultList = new ArrayList();
    }

    public void setInputConceptSet(Set<Concept> cSet) {
        inputConceptSet = cSet;
    }

    public void init() {
        inputConceptSet = new HashSet<Concept>();
        idNodeMap.clear();
        MRresultList.clear();
        TRresultList.clear();
    }

    public Set<List<Concept>> getPathList(Set<Concept> conceptSet) {
        inputConceptSet = conceptSet;
        Set<List<Concept>> pathSet = new HashSet<List<Concept>>();
        for (Concept c : conceptSet) {
            if (c.getPrefix().equals("edr")) {
                pathSet.addAll(EDRTree.getInstance().getPathToRootSet(c.getId()));
            } else if (c.getPrefix().equals("wn")) {
                pathSet.addAll(WordNetDic.getPathToRootSet(new Long(c.getId())));
            }
        }
        return pathSet;
    }

    public static ConceptTreeMaker getInstance() {
        return maker;
    }

    /**
     * 
     * �T�O��(String)�̃��X�g��n����,TreeModel��Ԃ�
     * 
     * @param conceptList
     *            �T�O(String)�̃��X�g
     * @return TreeModel
     */
    public TreeModel getDefaultConceptTreeModel(Set pathSet, DODDLEProject project) {
        Concept rootEDRConcept = EDRDic.getEDRConcept(EDR_CLASS_ROOT_ID); // �u�T�O�v��EDR�̃��[�g�m�[�h
        ConceptTreeNode rootNode = new ConceptTreeNode(rootEDRConcept, project);
        rootNode.setdepth(0);
        for (Iterator i = pathSet.iterator(); i.hasNext();) {
            List nodeList = (List) i.next();
            // nodeList -> [x, x, x, ..., ���͊T�O]
            addTreeNode(rootNode, nodeList, project);
        }
        return new DefaultTreeModel(rootNode);
    }

    /**
     * 
     * �T�O��(String)�̃��X�g��n����,TreeModel��Ԃ� �T�O�ϓ����s���Ă���B
     * 
     * @param conceptList
     *            �T�O(String)�̃��X�g
     * @return TreeModel
     */
    public TreeModel getTrimmedTreeModel(Set pathSet, DODDLEProject project) {
        TreeModel treeModel = getDefaultConceptTreeModel(pathSet, project);
        ConceptTreeNode root = (ConceptTreeNode) treeModel.getRoot();
        trimmedConceptSet.clear();
        root.setdepth(0);
        setDefaultDepth(root); // �g���~���O�O�̃m�[�h�̐[����ۑ�
        trimTree((DefaultMutableTreeNode) treeModel.getRoot()); // �g���~���O
        if (root.getConcept().getId().equals(EDR_CLASS_ROOT_ID)) {
            DODDLE.getLogger().log(Level.INFO, "����N���X��: " + trimmedConceptSet.size());
        } else if (root.getConcept().getId().equals(EDR_PROPERTY_ROOT_ID)) {
            DODDLE.getLogger().log(Level.INFO, "����v���p�e�B��: " + trimmedConceptSet.size());
        }
        root.setTrimmedCount(0);
        idNodeMap.put(root.getId(), root);
        for (int i = 0; i < root.getChildCount(); i++) {
            setDepth((ConceptTreeNode) root.getChildAt(i));
        }
        removeSameNode((ConceptTreeNode) treeModel.getRoot());
        conceptDriftAnalysis(treeModel);

        return treeModel;
    }

    /**
     * �g���~���O����O�̊e�m�[�h�̊K�w�̐[�����Z�b�g����
     */
    private void setDefaultDepth(ConceptTreeNode node) {
        node.setdepth(node.getLevel());
        for (int i = 0; i < node.getChildCount(); i++) {
            setDefaultDepth((ConceptTreeNode) node.getChildAt(i));
        }
    }

    /**
     * �e�m�[�h�Ǝq�m�[�h�̊Ԃ̐[���̍����Z�b�g����
     */
    private void setDepth(ConceptTreeNode node) {
        int level = node.getdepth() - ((ConceptTreeNode) (node.getParent())).getdepth();
        node.setTrimmedCount(level - 1);
        idNodeMap.put(node.getId(), node);
        // System.out.println(node.getId());
        for (int i = 0; i < node.getChildCount(); i++) {
            setDepth((ConceptTreeNode) node.getChildAt(i));
        }
    }

    /**
     * �s�K�v�ȃm�[�h���ǂ���
     */
    private boolean isUnnecessaryNode(ConceptTreeNode node) {
        return (node.getChildCount() == 1 && !isInputConcept(node.getConcept()));
    }

    private static Set trimmedConceptSet = new HashSet();

    /**
     * �s�v�Ȓ��ԊT�O�𙒒�
     */
    private void trimTree(DefaultMutableTreeNode treeNode) {
        for (Enumeration i = treeNode.children(); i.hasMoreElements();) {
            ConceptTreeNode childNode = (ConceptTreeNode) i.nextElement();
            if (isUnnecessaryNode(childNode)) {
                // �q����������Ȃ��ꍇ�ɂ����C�g���~���O�͍s���Ȃ����߁CgetChildAt(0)�ł悢
                ConceptTreeNode grandChildNode = (ConceptTreeNode) childNode.getChildAt(0);
                trimmedConceptSet.add(childNode.getConcept()); // �폜���ꂽ�T�O��ۑ�
                VerbConcept gvc = (VerbConcept) grandChildNode.getConcept();
                gvc.addTrimmedConcept(childNode.getConcept()); // �폜���ꂽ�T�O��ۑ�
                // ���肳���T�O���ێ����Ă��陒��T�O��ۑ�
                gvc.addAllTrimmedConcept(((VerbConcept) childNode.getConcept()).getTrimmedConceptSet());
                grandChildNode.setConcept(gvc);
                treeNode.add(grandChildNode);
                treeNode.remove(childNode);
                trimTree(treeNode);
            } else {
                trimTree(childNode);
            }
        }
    }

    /**
     * Trimming�������Ƃɂ��C�������x���œ���̊T�O���܂܂��ꍇ�����邽�߁C ��������������D
     * 
     * @param treeNode
     */
    private void removeSameNode(ConceptTreeNode treeNode) {
        Set sameLevelNodeSet = new HashSet();
        Set sameNodeSet = new HashSet();
        for (Enumeration i = treeNode.children(); i.hasMoreElements();) {
            ConceptTreeNode childNode = (ConceptTreeNode) i.nextElement();
            if (sameLevelNodeSet.contains(childNode.getId())) {
                sameNodeSet.add(childNode);
            } else {
                sameLevelNodeSet.add(childNode.getId());
            }
            if (0 < childNode.getChildCount()) {
                removeSameNode(childNode);
            }
        }
        for (Iterator i = sameNodeSet.iterator(); i.hasNext();) {
            treeNode.remove((ConceptTreeNode) i.next());
        }
    }

    /**
     * �T�O�؂̃m�[�h�ƊT�OX���܂ޖ؂�����킷���X�g��n����, ���X�g���T�O�؂̒��̓K�؂ȏꏊ�ɒǉ�����
     * 
     * nodeList -> a > b > c > X
     */
    private boolean addTreeNode(ConceptTreeNode treeNode, List nodeList, DODDLEProject project) {
        if (nodeList.isEmpty()) { return false; }
        if (treeNode.isLeaf()) { return insertTreeNodeList(treeNode, nodeList, project); }
        for (Enumeration i = treeNode.children(); i.hasMoreElements();) {
            ConceptTreeNode node = (ConceptTreeNode) i.nextElement();
            Concept firstNode = (Concept) nodeList.get(0);
            if (node.getConcept().getIdentity().equals(firstNode.getIdentity())) { return addTreeNode(node, nodeList
                    .subList(1, nodeList.size()), project); }
        }
        return insertTreeNodeList(treeNode, nodeList, project);
    }

    /**
     * �T�OX���܂ޖ؂�����킷���X�g���T�O�؂ɑ}������
     */
    private boolean insertTreeNodeList(DefaultMutableTreeNode treeNode, List nodeList, DODDLEProject project) {
        if (nodeList.isEmpty()) { return false; }

        Concept firstNode = (Concept) nodeList.get(0);
        if (firstNode != null) {
            ConceptTreeNode childNode = new ConceptTreeNode(new VerbConcept(firstNode), project);
            childNode.setIsInputConcept(isInputConcept(firstNode));
            treeNode.add(childNode);
            insertTreeNodeList(childNode, nodeList.subList(1, nodeList.size()), project);
        }

        return true;
    }

    /**
     * �T�O�ϓ����s�����߂ɕK�v�ȏ��𕪐͂��C�Z�b�g����
     */
    public void conceptDriftAnalysis(TreeModel model) {
        MRresultList.clear();
        TRresultList.clear();
        MRAnalysis((ConceptTreeNode) model.getRoot());
        TRAnalysis((ConceptTreeNode) model.getRoot(), 3);
    }

    public void resetTRA() {
        TRresultList.clear();
    }

    /**
     * 
     * TRA(Trimmed Result Analysis)���s���D
     * 
     */
    public void TRAnalysis(ConceptTreeNode node, int trimmedNum) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (trimmedNum <= childNode.getTrimmedCount()) {
                TRresultList.add(childNode);
            }
            TRAnalysis(childNode, trimmedNum);
        }
    }

    /**
     * 
     * MRA(Matched Result Analysis)���s���D
     * 
     */
    private void MRAnalysis(ConceptTreeNode rootNode) {
        Set sinNodeSet = new HashSet();
        getSINNodeSet(rootNode, sinNodeSet);
        for (Iterator i = sinNodeSet.iterator(); i.hasNext();) {
            ConceptTreeNode node = (ConceptTreeNode) i.next();
            // System.out.println("Sin Node: " + node);
            Set stm = new HashSet();
            stm.add(node);
            getSTMSet(node, stm);
            MRresultList.add(stm);
        }
    }

    /**
     * 
     * STM(Sub Trees manually Moved)�𓾂�D MRA(Matched Result Analysis)�ŗp����D
     * 
     * @param node
     * @param stm
     */
    private void getSTMSet(ConceptTreeNode node, Set stm) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (childNode.isInputConcept()) {
                stm.add(childNode);
                getSTMSet(childNode, stm);
            }
        }
    }

    /**
     * 
     * SIN(a Salient Internal Nodes)�̏W���𓾂�D -> SIN�́CSTM�̃��[�g�ƂȂ邽�߁D
     * 
     * @param node
     * @param sinNodeSet
     */
    private void getSINNodeSet(ConceptTreeNode node, Set sinNodeSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            ConceptTreeNode childNode = (ConceptTreeNode) node.getChildAt(i);
            if (!childNode.isInputConcept()) {
                sinNodeSet.add(childNode);
            }
            getSINNodeSet(childNode, sinNodeSet);
        }
    }

    public List getMRAresult() {
        return MRresultList;
    }

    public List getTRAresult() {
        return TRresultList;
    }

    public boolean isInputConcept(Concept c) {
        for (Concept ic : inputConceptSet) {
            if (ic.getIdentity().equals(c.getIdentity())) { return true; }
        }
        return false;
        // return inputConceptSet.contains(EDRDic.getEDRConcept(c.getId()));
    }

    private static Map supSubSetMap;

    public TreeNode getConceptTreeRoot(DODDLEProject currentProject, Model model, Resource rootResource) {
        supSubSetMap = new HashMap();
        for (ResIterator i = model.listSubjectsWithProperty(RDF.type); i.hasNext();) {
            Resource resource = i.nextResource();
            for (StmtIterator j = resource.listProperties(RDFS.subClassOf); j.hasNext();) {
                Statement stmt = j.nextStatement();
                Resource supResource = (Resource) stmt.getObject();
                Set subResourceSet = (Set) supSubSetMap.get(supResource);
                if (subResourceSet == null) {
                    subResourceSet = new HashSet();
                }
                subResourceSet.add(resource);
                supSubSetMap.put(supResource, subResourceSet);
            }
        }
        Concept rootEDRConcept = EDRDic.getEDRConcept(EDR_CLASS_ROOT_ID); // �u�T�O�v��EDR�̃��[�g�m�[�h
        ConceptTreeNode rootNode = new ConceptTreeNode(rootEDRConcept, currentProject);
        if (supSubSetMap.get(rootResource) != null) {
            makeTree(currentProject, rootResource, rootNode);
        } else { // �O�̃o�[�W�����Ƃ̌݊�����ێ����邽��
            rootResource = ResourceFactory.createResource(DODDLE.OLD_EDR_URI + "ID" + EDR_CLASS_ROOT_ID);
            if (supSubSetMap.get(rootResource) != null) {
                makeTree(currentProject, rootResource, rootNode);
            }
        }
        return rootNode;
    }

    public TreeNode getPropertyTreeRoot(DODDLEProject currentProject, Model model, Resource rootResource) {
        supSubSetMap = new HashMap();
        for (ResIterator i = model.listSubjectsWithProperty(RDF.type); i.hasNext();) {
            Resource resource = i.nextResource();
            for (StmtIterator j = resource.listProperties(RDFS.subPropertyOf); j.hasNext();) {
                Statement stmt = j.nextStatement();
                Resource supResource = (Resource) stmt.getObject();
                Set subResourceSet = (Set) supSubSetMap.get(supResource);
                if (subResourceSet == null) {
                    subResourceSet = new HashSet();
                }
                subResourceSet.add(resource);
                supSubSetMap.put(supResource, subResourceSet);
            }
        }
        // System.out.println(supSubSetMap);
        VerbConcept propRoot = new VerbConcept(EDR_PROPERTY_ROOT_ID, "�����I�T�O");
        propRoot.setPrefix("keio");
        ConceptTreeNode rootNode = new ConceptTreeNode(propRoot, currentProject);
        if (supSubSetMap.get(rootResource) != null) {
            makeTree(currentProject, rootResource, rootNode);
        } else { // �O�̃o�[�W�����Ƃ̌݊�����ێ����邽��
            rootResource = ResourceFactory.createResource(DODDLE.OLD_EDR_URI + "ID" + EDR_PROPERTY_ROOT_ID);
            if (supSubSetMap.get(rootResource) != null) {
                makeTree(currentProject, rootResource, rootNode);
            }
        }
        return rootNode;
    }

    private String getPrefix(Resource res) {
        if (res.getNameSpace().equals(DODDLE.EDR_URI)) {
            return "edr";
        } else if (res.getNameSpace().equals(DODDLE.WN_URI)) {
            return "wn";
        } else if (res.getNameSpace().equals(DODDLE.BASE_URI)) {
            return "keio";
        } else { // �ߋ��Ƃ̌݊����̂���
            return "edr";
        }
    }

    private void makeTree(DODDLEProject currentProject, Resource resource, DefaultMutableTreeNode node) {
        Set subRDFSSet = (Set) supSubSetMap.get(resource);
        for (Iterator i = subRDFSSet.iterator(); i.hasNext();) {
            Resource subRDFS = (Resource) i.next();
            // String id = subRDFS.getLocalName().split("ID")[1];
            String id = subRDFS.getLocalName();
            if (id.indexOf("UID") == -1) {
                id = id.split("ID")[1];
            }
            VerbConcept concept = new VerbConcept(id, "");
            concept.setPrefix(getPrefix(subRDFS));
            String jaWord = "";
            String enWord = "";
            for (StmtIterator stmtIter = subRDFS.listProperties(RDFS.label); stmtIter.hasNext();) {
                Statement stmt = stmtIter.nextStatement();
                // Resource res = stmt.getSubject();
                Literal lit = (Literal) stmt.getObject();
                if (lit.getLanguage().equals("ja")) {
                    jaWord += lit.getString() + "\t";
                } else if (lit.getLanguage().equals("en")) {
                    enWord += lit.getString() + "\t";
                }
            }
            concept.setJaWord(jaWord);
            concept.setEnWord(enWord);
            String jaExplanation = "";
            String enExplanation = "";
            for (StmtIterator stmtIter = subRDFS.listProperties(RDFS.comment); stmtIter.hasNext();) {
                Statement stmt = stmtIter.nextStatement();
                // Resource res = stmt.getSubject();
                Literal lit = (Literal) stmt.getObject();
                if (lit.getLanguage().equals("ja")) {
                    jaExplanation += lit.getString();
                } else if (lit.getLanguage().equals("en")) {
                    enExplanation += lit.getString();
                }
            }
            concept.setJaExplanation(jaExplanation);
            concept.setEnExplanation(enExplanation);

            Set domainSet = new HashSet();
            for (StmtIterator stmtIter = subRDFS.listProperties(RDFS.domain); stmtIter.hasNext();) {
                Statement stmt = stmtIter.nextStatement();
                Resource domain = (Resource) stmt.getObject();
                if (domain.getLocalName().indexOf("UID") != -1) {
                    domainSet.add(domain.getLocalName());
                } else {
                    domainSet.add(domain.getLocalName().split("ID")[1]);
                }
            }
            concept.addAllDomain(domainSet);

            Set rangeSet = new HashSet();
            for (StmtIterator stmtIter = subRDFS.listProperties(RDFS.range); stmtIter.hasNext();) {
                Statement stmt = stmtIter.nextStatement();
                Resource range = (Resource) stmt.getObject();
                if (range.getLocalName().indexOf("UID") != -1) {
                    rangeSet.add(range.getLocalName());
                } else {
                    rangeSet.add(range.getLocalName().split("ID")[1]);
                }
            }
            concept.addAllRange(rangeSet);

            ConceptTreeNode subNode = new ConceptTreeNode(concept, currentProject);
            if (concept != null && isInputConcept(concept)) {
                subNode.setIsInputConcept(true);
            }
            if (subRDFS.getLocalName().indexOf("UID") != -1) {
                currentProject.setUserIDCount(Integer.parseInt(id.split("ID")[1]));
                subNode.setIsUserConcept(true);
            }
            if (supSubSetMap.get(subRDFS) != null) {
                makeTree(currentProject, subRDFS, subNode);
            }
            node.add(subNode);
        }
    }
}