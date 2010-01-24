/*
 * Project Name: DODDLE (a Domain Ontology rapiD DeveLopment Environment)
 * Project Website: http://doddle-owl.sourceforge.net/
 * 
 * Copyright (C) 2004-2009 Yamaguchi Laboratory, Keio University. All rights reserved. 
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
import java.util.regex.*;

/**
 * @author takeshi morita
 */
public class UseCaseTask {

    private String id;
    private String description;
    private List<PrimitiveTask> primitiveTaskList;

    public UseCaseTask(String fileName) {
        Pattern pattern = Pattern.compile(".*[［\\[](.*)[］\\]].*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            String title = matcher.group(1);
            String[] elems = title.split("：");
            String id = elems[0];
            String description = elems[1];

            this.description = description;
            this.id = id;
        }
        primitiveTaskList = new ArrayList<PrimitiveTask>();
    }

    public UseCaseTask(String description, String id) {
        this.description = description;
        this.id = id;
        primitiveTaskList = new ArrayList<PrimitiveTask>();
    }

    public String getID() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void addAllTask(List<PrimitiveTask> taskList) {
        primitiveTaskList.addAll(taskList);
    }

    public void addTask(PrimitiveTask task) {
        primitiveTaskList.add(task);
    }

    public List<PrimitiveTask> getPrimitiveTaskList() {
        return primitiveTaskList;
    }

    public String toString() {
        return id + ": " + description;
    }

    public static void main(String[] args) {
        try {
            File taskSetDir = new File("task_set");
            File[] taskFiles = taskSetDir.listFiles();
            List<UseCaseTask> useCaseTaskList = new ArrayList<UseCaseTask>();
            for (int i = 0; i < taskFiles.length; i++) {
                File file = taskFiles[i];
                // System.out.println(file);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                reader.readLine(); // 1行?ﾇ?ﾌてる
                UseCaseTask useCaseTask = null;
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] elems = line.split("\t");
                    if (0 < elems[1].length()) {
                        useCaseTask = new UseCaseTask(elems[1], elems[2]);
                        useCaseTaskList.add(useCaseTask);
                    }
                    Morpheme preMor = new Morpheme(elems[4], "", "", "");
                    Segment preSeg = new Segment(-1);
                    preSeg.addMorpheme(preMor);

                    Morpheme objMor = new Morpheme(elems[6], "", "", "");
                    Segment objSeg = new Segment(-1);
                    objSeg.addMorpheme(objMor);

                    String[] sbjMors = elems[5].split("、");
                    for (String sm : sbjMors) {
                        Morpheme sbjMor = new Morpheme(sm, "", "", "");
                        Segment sbjSeg = new Segment(-1);
                        sbjSeg.addMorpheme(sbjMor);
                        PrimitiveTask task = new PrimitiveTask(preSeg);
                        task.addObject(objSeg);
                        task.addSubject(sbjSeg);
                        useCaseTask.addTask(task);
                    }
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("input_word_set.txt"),
                    "UTF8"));
            // BufferedReader reader = new BufferedReader(new
            // InputStreamReader(new FileInputStream(
            // "auto_extracted_term_set.txt"), "UTF8"));

            Set<String> termSet = new HashSet<String>();
            while (reader.ready()) {
                String line = reader.readLine();
                termSet.add(line);
            }
            System.out.println("用語数: " + termSet.size());
            System.out.println("ユースケース記述数: " + useCaseTaskList.size());
            int taskNum = 0;
            int validTaskNum = 0;
            Set<String> undefinedTermSet = new HashSet<String>();
            for (UseCaseTask useCaseTask : useCaseTaskList) {
                System.out.println(useCaseTask);
                for (PrimitiveTask task : useCaseTask.getPrimitiveTaskList()) {
                    taskNum++;
                    boolean isDefinedSubjectConcept = termSet.contains(task.getSubjectString());
                    boolean isDefinedObjectConcept = termSet.contains(task.getObjectString());

                    if (!(isDefinedSubjectConcept && isDefinedObjectConcept)) {
                        System.out.print(task.getPredicateString());
                        System.out.print("\t");
                        if (isDefinedSubjectConcept) {
                            System.out.print(task.getSubjectString());
                        } else {
                            undefinedTermSet.add(task.getSubjectString());
                            System.out.print("<" + task.getSubjectString() + ">");
                        }
                        System.out.print("\t");
                        if (isDefinedObjectConcept) {
                            System.out.println(task.getObjectString());
                        } else {
                            undefinedTermSet.add(task.getObjectString());
                            System.out.println("<" + task.getObjectString() + ">");
                        }
                    } else {
                        validTaskNum++;
                        System.out.println("*" + task);
                    }
                }
            }
            System.out.println(validTaskNum + "/" + taskNum);

            System.out.println("未定義語数: "+undefinedTermSet.size());
            Set<String> undefinedTermWithCaseSet = new HashSet<String>();
            for (String term : undefinedTermSet) {
                if (term.indexOf("(") != -1 || term.indexOf(")") != -1) {
                    undefinedTermWithCaseSet.add(term);
                } else {
                    System.out.println(term);
                }
            }            
            for (String term : undefinedTermWithCaseSet) {
                System.out.println(term);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
