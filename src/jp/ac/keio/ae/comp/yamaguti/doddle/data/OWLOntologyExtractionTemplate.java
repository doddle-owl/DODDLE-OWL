/*
 * @(#)  2007/02/09
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/**
 * @author takeshi morita
 */
public class OWLOntologyExtractionTemplate {
    
    private String searchLabelSetTemplate;
    private String searchClassSetTemplate;
    private String searchPropertySetTemplate;
    private String searchConceptTemplate;
    private String searchDomainSetTemplate;
    private String searchRangeSetTemplate;
    private String searchSubConceptTemplate;

    public String getSearchSubConceptTemplate() {
        return searchSubConceptTemplate;
    }

    public void setSearchSubConceptTemplate(String searchSubConceptTemplate) {
        this.searchSubConceptTemplate = searchSubConceptTemplate;
    }

    public String getSearchClassSetTemplate() {
        return searchClassSetTemplate;
    }
    
    public void setSearchClassSetTemplate(String searchClassSetTemplate) {
        this.searchClassSetTemplate = searchClassSetTemplate;
    }
    
    public String getSearchConceptTemplate() {
        return searchConceptTemplate;
    }
    
    public void setSearchConceptTemplate(String searchConceptTemplate) {
        this.searchConceptTemplate = searchConceptTemplate;
    }
    
    public String getSearchDomainSetTemplate() {
        return searchDomainSetTemplate;
    }
    
    public void setSearchDomainSetTemplate(String searchDomainSetTemplate) {
        this.searchDomainSetTemplate = searchDomainSetTemplate;
    }
    
    public String getSearchLabelSetTemplate() {
        return searchLabelSetTemplate;
    }
    
    public void setSearchLabelSetTemplate(String searchLabelSetTemplate) {
        this.searchLabelSetTemplate = searchLabelSetTemplate;
    }
    
    public String getSearchPropertySetTemplate() {
        return searchPropertySetTemplate;
    }
    
    public void setSearchPropertySetTemplate(String searchPropertySetTemplate) {
        this.searchPropertySetTemplate = searchPropertySetTemplate;
    }
    
    public String getSearchRangeSetTemplate() {
        return searchRangeSetTemplate;
    }
    
    public void setSearchRangeSetTemplate(String searchRangeSetTemplate) {
        this.searchRangeSetTemplate = searchRangeSetTemplate;
    }

}
