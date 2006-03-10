package jp.ac.keio.ae.comp.yamaguti.doddle.utils;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

import javax.swing.*;

import org.semanticweb.mmm.mr3.*;
import org.semanticweb.mmm.mr3.actions.*;
import org.semanticweb.mmm.mr3.data.*;
import org.semanticweb.mmm.mr3.editor.*;
import org.semanticweb.mmm.mr3.jgraph.*;
import org.semanticweb.mmm.mr3.ui.*;

import com.hp.hpl.jena.shared.*;

/*
 * 
 * @author takeshi morita
 * 
 */
public class Translator {

    protected static ResourceBundle resourceBundle;
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
            File resDir = new File("./resources");
            InputStream ins = null;
            if (resDir != null) {
                File resFile = new File(resDir.getAbsolutePath() + "/DODDLE_" + lang + ".properties");
                if (resFile.exists()) {
                    ins = new FileInputStream(resFile);
                }
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

    public static void main(String[] args) {
        Translator.loadResourceBundle("en");
        System.out.println(Translator.getString("Lang"));
    }
}
