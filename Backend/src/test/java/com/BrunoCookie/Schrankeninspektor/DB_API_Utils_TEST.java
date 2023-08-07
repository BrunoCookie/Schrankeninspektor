package com.BrunoCookie.Schrankeninspektor;

import com.BrunoCookie.Schrankeninspektor.Utils.DB_API_Utils;
import com.BrunoCookie.Schrankeninspektor.Utils.Resource_Helper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DB_API_Utils_TEST {
    @Autowired
    private DB_API_Utils db_api_utils;

    @Test
    public void extractPlannedDataFromXMLTest() throws ParserConfigurationException, IOException, SAXException {
        // GIVEN
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(Resource_Helper.getResource("exp_Ab-Anfahrten.xml"));

        // WHEN
        HashMap<String, DB_API_Utils.Stop> actual = db_api_utils.extractPlannedDataFromXML(doc);

        // THEN
        assertThat(actual.size()).isEqualTo(8);
    }

    @Test
    public void extractRecentChangesFromXMLTest() throws ParserConfigurationException, IOException, SAXException {
        // GIVEN
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(Resource_Helper.getResource("exp_Recent-Changes.xml"));

        // WHEN
        ArrayList<DB_API_Utils.Recent_Change> actual = db_api_utils.extractRecentChangesFromXML(doc);

        // THEN
        assertThat(actual.size()).isEqualTo(4);
    }

    @Test
    public void applyRecentChangesTest() throws ParserConfigurationException, IOException, SAXException {
        // GIVEN
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document plannedDataDoc = db.parse(Resource_Helper.getResource("exp_Ab-Anfahrten.xml"));
        Document recentChangesDoc = db.parse(Resource_Helper.getResource("exp_Recent-Changes.xml"));
        HashMap<String, DB_API_Utils.Stop> stops = db_api_utils.extractPlannedDataFromXML(plannedDataDoc);
        ArrayList<DB_API_Utils.Recent_Change> recentChanges = db_api_utils.extractRecentChangesFromXML(recentChangesDoc);
        LocalDateTime actual = LocalDateTime.of(2023,8,4,14,12);
        LocalDateTime notActual = LocalDateTime.of(2023,8,4,14,11);

        // WHEN
        db_api_utils.applyRecentChanges(stops, recentChanges);

        // THAT
        assertThat(stops.get("1936086192786741054-2308041334-16").getTime()).isEqualTo(actual);
        assertThat(stops.get("1936086192786741054-2308041334-16").getTime()).isNotEqualTo(notActual);
    }
}
