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

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.util.*;

import javax.swing.tree.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.utils.*;

/**
 * @author takeshi morita
 */
public class EDRTree {

    private boolean isSpecial;
    private static EDRTree edrTree;
    private static EDRTree edrtTree;
    private DefaultTreeModel edrTreeModel;
    private Map<String, Set<TreeNode>> uriNodeSetMap;

    public static EDRTree getEDRTree() {
        if (edrTree == null) {
            edrTree = new EDRTree(false);
        }
        return edrTree;
    }

    public static EDRTree getEDRTTree() {
        if (edrtTree == null) {
            edrtTree = new EDRTree(true);
        }
        return edrtTree;
    }

    private EDRTree(boolean t) {
        isSpecial = t;
        uriNodeSetMap = new HashMap<String, Set<TreeNode>>();
    }

    public void clear() {
        edrTreeModel = null;
        uriNodeSetMap.clear();
    }

    private Set<List<Concept>> getConceptPathToRootSetUsingTree(String uri) {
        String id = Utils.getLocalName(uri);
        Concept c = getConcept(id);
        Set<TreeNode> nodeSet = uriNodeSetMap.get(uri);
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        if (nodeSet == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new Concept[] { c}));
            return pathToRootSet;
        }
        for (TreeNode node : nodeSet) {
            TreeNode[] pathToRoot = edrTreeModel.getPathToRoot(node);
            List<Concept> path = new ArrayList<Concept>();
            for (int i = 0; i < pathToRoot.length; i++) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) pathToRoot[i];
                String nuri = (String) n.getUserObject();
                String nid = Utils.getLocalName(nuri);
                path.add(getConcept(nid));
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    private Set<List<String>> getURIPathToRootSetUsingTree(String uri) {
        Set<TreeNode> nodeSet = uriNodeSetMap.get(uri);
        Set<List<String>> pathToRootSet = new HashSet<List<String>>();
        if (nodeSet == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new String[] { uri}));
            return pathToRootSet;
        }
        for (TreeNode node : nodeSet) {
            TreeNode[] pathToRoot = edrTreeModel.getPathToRoot(node);
            List<String> path = new ArrayList<String>();
            for (int i = 0; i < pathToRoot.length; i++) {
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) pathToRoot[i];
                String nuri = (String) n.getUserObject();
                path.add(nuri);
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    public Set<List<String>> getURIPathToRootSet(String id) {
        if (edrTreeModel != null && 0 < uriNodeSetMap.size()) { return getURIPathToRootSetUsingTree(getURI(id)); }
        Set<List<String>> pathToRootSet = new HashSet<List<String>>();
        String treeData = EDRDic.getTreeData(isSpecial, id);

        if (treeData == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new String[] { getURI(id)}));
            return pathToRootSet;
        }

        String[] pathArray = treeData.split("\\|");
        for (int i = 1; i < pathArray.length; i++) {
            String[] idArray = pathArray[i].split("\t");
            List<String> path = new ArrayList<String>();
            for (int j = 0; j < idArray.length; j++) {
                String nid = idArray[j];
                path.add(getURI(nid));
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    public Set<List<Concept>> getConceptPathToRootSet(String id) {
        if (edrTreeModel != null && 0 < uriNodeSetMap.size()) { return getConceptPathToRootSetUsingTree(getURI(id)); }
        Concept c = getConcept(id);
        Set<List<Concept>> pathToRootSet = new HashSet<List<Concept>>();
        String treeData = EDRDic.getTreeData(isSpecial, id);

        if (treeData == null) { // 上位・下位関係が定義されていない（できない）概念
            pathToRootSet.add(Arrays.asList(new Concept[] { c}));
            return pathToRootSet;
        }

        String[] pathArray = treeData.split("\\|");
        for (int i = 1; i < pathArray.length; i++) {
            String[] idArray = pathArray[i].split("\t");
            List<Concept> path = new ArrayList<Concept>();
            for (int j = 0; j < idArray.length; j++) {
                String nid = idArray[j];
                path.add(getConcept(nid));
            }
            pathToRootSet.add(path);
        }
        return pathToRootSet;
    }

    private void getSubURI(TreeNode node, Set<String> subIDSet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = node.getChildAt(i);
            // subConceptSet.add(childNode.toString());
            String id = childNode.toString();
            subIDSet.add(getURI(id));
            getSubURI(childNode, subIDSet);
        }
    }

    private void getSubURISet(TreeNode node, Set<String> subURISet) {
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
            subURISet.add(childNode.toString());
        }
    }

    public void getSubURISet(String uri, Set<String> nounURISet, Set<String> refineSubURISet) {
        Set<String> subURISet = new HashSet<String>();
        Set<TreeNode> nodeSet = uriNodeSetMap.get(uri);
        if (nodeSet == null) { return; }
        for (TreeNode node : nodeSet) {
            getSubURISet(node, subURISet);
        }
        if (subURISet.size() == 0) { return; }
        for (String subURI : subURISet) {
            if (nounURISet.contains(subURI)) {
                refineSubURISet.add(subURI);
            }
        }
        if (0 < refineSubURISet.size()) { return; }
        for (String subURI : subURISet) {
            getSubURISet(subURI, nounURISet, refineSubURISet);
        }
    }

    public Set<Set<String>> getSubURISet(String uri) {
        Set<TreeNode> nodeSet = uriNodeSetMap.get(uri);
        Set<Set<String>> subURIsSet = new HashSet<Set<String>>();
        if (nodeSet == null) { return subURIsSet; }
        for (TreeNode node : nodeSet) {
            Set<String> subURISet = new HashSet<String>();
            getSubURI(node, subURISet);
            subURIsSet.add(subURISet);
        }
        return subURIsSet;
    }

    private String getURI(String id) {
        if (isSpecial) { return DODDLEConstants.EDRT_URI + id; }
        return DODDLEConstants.EDR_URI + id;
    }

    private Concept getConcept(String id) {
        if (isSpecial) { return EDRDic.getEDRTConcept(id); }
        return EDRDic.getEDRConcept(id);
    }

    public Set<Set<String>> getSiblingURISet(String uri) {
        Set<TreeNode> nodeSet = uriNodeSetMap.get(uri);
        Set<Set<String>> siblingIDsSet = new HashSet<Set<String>>();
        if (nodeSet == null) { return siblingIDsSet; }
        for (TreeNode node : nodeSet) {
            Set<String> siblingIDSet = new HashSet<String>();
            // System.out.println("NODE: " + node);
            TreeNode parentNode = node.getParent();
            // System.out.println("PARENT_NODE: " + parentNode);
            if (parentNode != null) {
                for (int i = 0; i < parentNode.getChildCount(); i++) {
                    TreeNode siblingNode = parentNode.getChildAt(i);
                    if (siblingNode != node) {
                        String sid = siblingNode.toString();
                        siblingIDSet.add(getURI(sid));
                    }
                }
            }
            siblingIDsSet.add(siblingIDSet);
        }
        return siblingIDsSet;
    }

    public void makeEDRTree(Set<String> idSet) {
        if (0 < uriNodeSetMap.size()) { return; }
        Set<List<String>> pathSet = new HashSet<List<String>>();
        int i = 0;
        for (String id : idSet) {
            i++;
            if (i % 10000 == 0) {
                System.out.println(i);
            }
            pathSet.addAll(getURIPathToRootSet(id));
        }
        String rootURI = null;
        if (isSpecial) {
            rootURI = DODDLEConstants.EDRT_URI + ConceptTreeMaker.EDRT_CLASS_ROOT_ID;
        } else {
            rootURI = DODDLEConstants.EDR_URI + ConceptTreeMaker.EDR_CLASS_ROOT_ID;
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootURI);
        Set<TreeNode> nodeSet = new HashSet<TreeNode>();
        nodeSet.add(rootNode);
        uriNodeSetMap.put(rootURI, nodeSet);

        for (List<String> nodeList : pathSet) {
            // nodeList -> [x, x, x, ..., 入力概念]
            addTreeNode(rootNode, nodeList);
        }
        edrTreeModel = new DefaultTreeModel(rootNode);
    }

    /**
     * 概念木のノードと概念Xを含む木をあらわすリストを渡して, リストを概念木の中の適切な場所に追加する
     * 
     * nodeList -> a > b > c > X
     */
    private boolean addTreeNode(DefaultMutableTreeNode treeNode, List<String> nodeList) {
        if (nodeList.isEmpty()) { return false; }
        if (treeNode.isLeaf()) { return insertTreeNodeList(treeNode, nodeList); }
        for (Enumeration i = treeNode.children(); i.hasMoreElements();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.nextElement();
            String firstNode = nodeList.get(0);
            if (firstNode != null && node.toString().equals(firstNode)) { return addTreeNode(node, nodeList.subList(1,
                    nodeList.size())); }
        }
        return insertTreeNodeList(treeNode, nodeList);
    }

    /**
     * IDXを含む木をあらわすリストを概念木に挿入する
     */
    private boolean insertTreeNodeList(DefaultMutableTreeNode treeNode, List nodeList) {
        if (nodeList.isEmpty()) { return false; }
        String firstNode = (String) nodeList.get(0);
        if (firstNode != null) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(firstNode);
            treeNode.add(childNode);
            if (uriNodeSetMap.get(firstNode) == null) {
                Set<TreeNode> nodeSet = new HashSet<TreeNode>();
                nodeSet.add(childNode);
                uriNodeSetMap.put(firstNode, nodeSet);
            } else {
                Set<TreeNode> nodeSet = uriNodeSetMap.get(firstNode);
                nodeSet.add(childNode);
            }
            insertTreeNodeList(childNode, nodeList.subList(1, nodeList.size()));
        }
        return true;
    }

}
