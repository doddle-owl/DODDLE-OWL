/*
 * @(#)  2006/01/15
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

import javax.swing.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.ui.*;

import org.pdfbox.pdfparser.*;
import org.pdfbox.pdmodel.*;
import org.pdfbox.util.*;

/**
 * @author takeshi morita
 */
public class Document implements Comparable<Document>{
   
    private File file;
    private String lang;
    private String text;
    private String[] texts;

    public Document(File f) {
        lang = "ja";
        file = f;
        text = getTextString();
        texts = getSplitText();
    }

    public Document(String l, File f) {
        lang = l;
        file = f;
        text = getTextString();
        texts = getSplitText();
    }
    
    private String[] getSplitText() {
        return text.split(DocumentSelectionPanel.PUNCTUATION_CHARS+"|\n");
    }

    private String getTextString() {
        if (!file.exists()) { return ""; }
        try {
            FileInputStream fis = new FileInputStream(file);
            if (!isWindowsOS() && file.getAbsolutePath().toLowerCase().matches(".*.pdf")) {
                // CMAPの設定をしないと，日本語の処理はうまくできない．
                // うまくできている場合もある．JISAutoDetectがいけないのか？
                // 登録されていないエンコーディングは，処理できない．
                PDFParser pdfParser = new PDFParser(fis);
                pdfParser.parse();
                PDDocument pddoc = pdfParser.getPDDocument();
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pddoc);
                pddoc.close();
                return text;
            } else if (file.getAbsolutePath().toLowerCase().matches(".*.txt")) {
                return getTextString(new InputStreamReader(fis, "UTF-8"));
            } else if (isWindowsOS()) {
                Runtime rt = Runtime.getRuntime();
                Process process = rt.exec(DocumentSelectionPanel.XDOC2TXT_EXE + " " + file.getAbsolutePath());
                return getTextString(new InputStreamReader(process.getInputStream()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
        return "";
    }
    
    /**
     * @param reader
     * @return
     * @throws IOException
     */
    private String getTextString(Reader reader) throws IOException {
        BufferedReader bufReader = new BufferedReader(reader);
        StringWriter writer = new StringWriter();
        String line = "";
        while ((line = bufReader.readLine()) != null) {
            writer.write(line);
        }
        writer.close();
        reader.close();
        String text = writer.toString();
        return text;
    }
    
    private boolean isWindowsOS() {
        return UIManager.getSystemLookAndFeelClassName().equals("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }
    
    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            builder.append(texts[i]);
            builder.append("\n");
        }
        return builder.toString();
    }
    
    public String[] getTexts() {
        return texts;
    }    

    public void resetText() {
        texts = getSplitText();
    }
    
    public void setText(String text) {
        this.text = text;
        texts = getSplitText();
    }
    
    public int getSize() {
        return texts.length;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String toString() {
        return "[" + lang + "]" + " " + file.getAbsolutePath();
    }

    @Override
    public int compareTo(Document d) {
        return file.getAbsoluteFile().compareTo(d.getFile());
    }
}
