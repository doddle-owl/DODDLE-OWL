package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public class Translator {

    protected static ResourceBundle resourceBundle;
    private static Map<String, String> uriTermMap;
    private static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";

    public static String getString(String sKey) {
        try {
            return resourceBundle.getString(sKey);
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
    }

    private static Set<Locale> systemLocaleSet;
    static {
        systemLocaleSet = new HashSet<Locale>();
        systemLocaleSet.add(Locale.JAPAN);
        systemLocaleSet.add(Locale.ENGLISH);
        systemLocaleSet.add(Locale.CHINA);
    }

    /*
     * デフォルトのロカールの言語ファイルがシステムに内蔵されている場合は， その言語を返し，内蔵されていない場合には，英語の言語を返す.
     */
    public static String getSystemLanguage() {
        if (systemLocaleSet.contains(Locale.getDefault())) { return Locale.getDefault().getLanguage(); }
        return Locale.ENGLISH.getLanguage();
    }

    private static boolean isSystemLanguage(String lang) {
        for (Locale locale : systemLocaleSet) {
            if (locale.getLanguage().equals(lang)) { return true; }
        }
        return false;
    }

    public static void loadResourceBundle(String lang) {
        try {
            InputStream ins = null;
            File resFile = new File("./resources/DODDLE_" + lang + ".properties");
            if (resFile.exists()) {
                ins = new FileInputStream(resFile);
            }
            if (ins == null) {
                if (isSystemLanguage(lang)) {
                    ins = Utils.class.getClassLoader().getResourceAsStream(
                            RESOURCE_DIR + "DODDLE_" + lang + ".properties");
                } else {
                    ins = Utils.class.getClassLoader().getResourceAsStream(
                            RESOURCE_DIR + "DODDLE_" + getSystemLanguage() + ".properties");
                }
            }
            resourceBundle = new PropertyResourceBundle(ins);
            ins.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void loadDODDLEComponentOntology(String lang) {
        uriTermMap = new HashMap<String, String>();
        try {
            File componentOntFile = new File("./resources/DODDLEComponent.owl");
            Model ontModel = ModelFactory.createDefaultModel();
            if (componentOntFile.exists()) {
                ontModel.read(new FileInputStream(componentOntFile), DODDLE.BASE_URI);
            } else {
                InputStream ins = Utils.class.getClassLoader()
                        .getResourceAsStream(RESOURCE_DIR + "DODDLEComponent.owl");
                ontModel.read(ins, DODDLE.BASE_URI);
            }

            for (ResIterator resItor = ontModel.listSubjectsWithProperty(RDF.type, OWL.Class); resItor.hasNext();) {
                Resource res = resItor.nextResource();
                for (StmtIterator stmtItor = res.listProperties(RDFS.label); stmtItor.hasNext();) {
                    Statement stmt = stmtItor.nextStatement();
                    Literal label = (Literal) stmt.getObject();
                    if (label.getLanguage().equals(lang)) {
                        uriTermMap.put(res.getURI(), label.getString());
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static String getTerm(String key) {
        return uriTermMap.get(DODDLE.BASE_URI + key);
    }

    public static void main(String[] args) {
        // Translator.loadResourceBundle("en");
        // System.out.println(Translator.getString("Lang"));
        Translator.loadDODDLEComponentOntology("ja");
        System.out.println(Translator.getTerm("PropertyTreeConstructionPanel"));
        System.out.println(Translator.getTerm("RangeLabel"));
        System.out.println(Translator.getTerm("DisambiguationAction"));
    }
}
