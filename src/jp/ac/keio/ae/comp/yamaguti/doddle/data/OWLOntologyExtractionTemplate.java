/*
 * @(#)  2007/02/09
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;

import jp.ac.keio.ae.comp.yamaguti.doddle.*;

/**
 * @author takeshi morita
 */
public class OWLOntologyExtractionTemplate {

    private File searchClassSetTemplate;
    private File searchPropertySetTemplate;
    private File searchConceptTemplate;
    private File searchRegionSetTemplate;
    private File searchSubConceptTemplate;

    public static final String RESOURCE_DIR = "jp/ac/keio/ae/comp/yamaguti/doddle/resources/";

    public OWLOntologyExtractionTemplate() {
        setSearchClassSetTemplate(new File("ontology_templates/SearchClassSet.rq"));
        setSearchPropertySetTemplate(new File("ontology_templates/SearchPropertySet.rq"));
        setSearchConceptTemplate(new File("ontology_templates/SearchConcept.rq"));
        setSearchRegionSetTemplate(new File("ontology_templates/SearchRegionSet.rq"));
        setSearchSubConceptTemplate(new File("ontology_templates/SearchSubConcept.rq"));
    }

    public String getSearchSubConceptTemplateLabel() {
        if (searchSubConceptTemplate.exists()) { return searchSubConceptTemplate.getAbsolutePath(); }
        return "Default Search Sub Concept Template";
    }

    public File getSearchSubConceptTemplate() {
        return searchSubConceptTemplate;
    }

    public InputStream getDefaultSearchSubConceptTemplate() {
        return DODDLE.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "ontology_templates/SearchSubConcept.rq");
    }

    public void setSearchSubConceptTemplate(File searchSubConceptTemplate) {
        this.searchSubConceptTemplate = searchSubConceptTemplate;
    }

    public String getSearchClassSetTemplateLabel() {
        if (searchClassSetTemplate.exists()) { return searchClassSetTemplate.getAbsolutePath(); }
        return "Default Search Class Set Template";
    }

    public File getSearchClassSetTemplate() {
        return searchClassSetTemplate;
    }

    public InputStream getDefaultSearchClassSetTemplate() {
        return DODDLE.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "ontology_templates/SearchClassSet.rq");
    }

    public void setSearchClassSetTemplate(File searchClassSetTemplate) {
        this.searchClassSetTemplate = searchClassSetTemplate;
    }

    public String getSearchConceptTemplateLabel() {
        if (searchConceptTemplate.exists()) { return searchConceptTemplate.getAbsolutePath(); }
        return "Default Search Concept Template";
    }

    public File getSearchConceptTemplate() {
        return searchConceptTemplate;
    }

    public InputStream getDefaultSearchConceptTemplate() {
        return DODDLE.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + "ontology_templates/SearchConcept.rq");
    }

    public void setSearchConceptTemplate(File searchConceptTemplate) {
        this.searchConceptTemplate = searchConceptTemplate;
    }

    public String getSearchPropertySetTemplateLabel() {
        if (searchPropertySetTemplate.exists()) { return searchPropertySetTemplate.getAbsolutePath(); }
        return "Default Search Property Set Template";
    }

    public File getSearchPropertySetTemplate() {
        return searchPropertySetTemplate;
    }

    public InputStream getDefaultSearchPropertySetTemplate() {
        return DODDLE.class.getClassLoader().getResourceAsStream(
                RESOURCE_DIR + "ontology_templates/SearchPropertySet.rq");
    }

    public void setSearchPropertySetTemplate(File searchPropertySetTemplate) {
        this.searchPropertySetTemplate = searchPropertySetTemplate;
    }

    public String getSearchRegionSetTemplateLabel() {
        if (searchRegionSetTemplate.exists()) { return searchRegionSetTemplate.getAbsolutePath(); }
        return "Default Search Region Set Template";
    }

    public File getSearchRegionSetTemplate() {
        return searchRegionSetTemplate;
    }

    public InputStream getDefaultSearchRegionSetTemplate() {
        return DODDLE.class.getClassLoader()
                .getResourceAsStream(RESOURCE_DIR + "ontology_templates/SearchRegionSet.rq");
    }

    public void setSearchRegionSetTemplate(File searchRegionSetTemplate) {
        this.searchRegionSetTemplate = searchRegionSetTemplate;
    }

}
