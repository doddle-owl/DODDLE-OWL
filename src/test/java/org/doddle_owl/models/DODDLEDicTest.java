package org.doddle_owl.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DODDLEDicTest {

    @BeforeEach
    public void setUp() {
        DODDLEConstants.EDR_HOME = "/Users/t_morita/DODDLE-OWL/EDR-DIC/";
        DODDLEConstants.EDRT_HOME = "/Users/t_morita/DODDLE-OWL/EDRT-DIC/";
        DODDLEConstants.ENWN_3_1_HOME = "/Users/t_morita/DODDLE-OWL/enwn_dict_3.1";
        DODDLEConstants.ENWN_HOME = DODDLEConstants.ENWN_3_1_HOME;
        DODDLEConstants.JPWN_HOME = "/Users/t_morita/DODDLE-OWL/jpwn_dict_1.1/";
        EDRDic.initEDRDic();
        EDRDic.initEDRTDic();
        JpnWordNetDic.initJPNWNDic();
        WordNetDic.initWordNetDictionary();
        EDRDic.isEDRAvailable = true;
        EDRDic.isEDRTAvailable = true;
        JpnWordNetDic.isAvailable = true;
        WordNetDic.isAvailable = true;
    }

    @Test
    @DisplayName("DODDLEDicからEDRのConceptを取得")
    public void getEDRConcept() {
        String expected = "dog";
        String actual = DODDLEDic.getConcept(DODDLEConstants.EDR_URI + "ID3bdc67").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicからEDRのConceptを取得")
    public void getEDRTConcept() {
        String expected = "ツリー検索";
        String actual = DODDLEDic.getConcept(DODDLEConstants.EDRT_URI + "ID2deac6").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicからWordNetのConceptを取得")
    public void getWordNetConcept() {
        String expected = "computer";
        String actual = DODDLEDic.getConcept(DODDLEConstants.WN_URI + "03086983").getWord();
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("DODDLEDicから日本語WordNetのConceptを取得")
    public void getJpWordNetConcept() {
        String expected = "urban_area";
        String actual = DODDLEDic.getConcept(DODDLEConstants.JPN_WN_URI + "08675967-n").getWord();
        assertEquals(expected, actual);
    }
}