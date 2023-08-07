package com.BrunoCookie.Schrankeninspektor;

import com.BrunoCookie.Schrankeninspektor.Utils.DB_API_Utils;
import com.BrunoCookie.Schrankeninspektor.Utils.Resource_Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class Schrankeninspektor_Service_TEST {
    @Autowired
    private Schrankeninspektor_Service service;

    @Autowired
    private DB_API_Utils db_api_utils;

    private ArrayList<LocalDateTime> times = new ArrayList<>();

    @BeforeEach
    public void setup() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document plannedDataDoc = db.parse(Resource_Helper.getResource("exp_Ab-Anfahrten.xml"));
        Document recentChangesDoc = db.parse(Resource_Helper.getResource("exp_Recent-Changes.xml"));
        HashMap<String, DB_API_Utils.Stop> stops = db_api_utils.extractPlannedDataFromXML(plannedDataDoc);
        ArrayList<DB_API_Utils.Recent_Change> recentChanges = db_api_utils.extractRecentChangesFromXML(recentChangesDoc);
        db_api_utils.applyRecentChanges(stops, recentChanges);
        stops.forEach((id, stop) -> times.add(stop.getTime()));
    }

    @Test
    public void find5MinGapTest(){
        // GIVEN
        LocalDateTime currentTime = LocalDateTime.of(2023,8,4,14,8);
        LocalDateTime expected = LocalDateTime.of(2023,8,4,14,20);

        // WHEN
        LocalDateTime actual = service.find5MinGap(currentTime, times);

        // THEN
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findNextStopTimeTest(){
        // GIVEN
        LocalDateTime currentTime = LocalDateTime.of(2023,8,4,14,35);
        LocalDateTime expected = LocalDateTime.of(2023,8,4,14,39);

        // WHEN
        LocalDateTime actual = service.findNextStopTime(currentTime, times);

        // THEN
        assertThat(actual).isEqualTo(expected);
    }
}
