package com.bbonadonna.Schrankeninspektor.DB_Api_Adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Service
public class DB_Api_Service {
    @Value("${clientId}")
    private String clientId;
    @Value("${clientKey}")
    private String clientKey;
    private final String URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/";
    private final String GRIESHEIM_EVA_NO = "8002046";
    private final String FRANKFURT_HBF = "Frankfurt Hbf (tief)";
    private HashMap<String, LocalDateTime> stopMap = new HashMap<>(); // String = StopID, LocalDateTime = Abfahrts-/Ankunftszeit

    public void getTrainInformationFromAPI() throws IOException {
        // Get Planned Data
        HashMap<String, LocalDateTime> stops = getPlannedData();

        // Apply recent changes to planned data

        // Clear hashmap and save
    }

    private void applyRecentChanges(HashMap<String, LocalDateTime> stops) {
        // Get Recent Changes

        // Compare both Hashmaps
        // Replace stop time if Change available
    }

    private HashMap<String, LocalDateTime> getPlannedData() throws IOException {
        // Get Day and Time
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd/HH");
        String dayAndTime = currentTime.format(formatter);

        // HTTP GET REQUEST to DB API
        HttpURLConnection con = getHttpRequest(new URL(this.URL + "plan/" + GRIESHEIM_EVA_NO+ "/" + dayAndTime));

        // Extract % edit XML Response
        Document doc = getXMLFromHttpRequest(con);

        // Fill Hashmap
        HashMap<String, LocalDateTime> stops = new HashMap<>();
        NodeList nodeList = doc.getElementsByTagName("s");
        for(int i = 0; i<nodeList.getLength(); i++){
            Element stop = (Element) nodeList.item(i);
            String stopId = stop.getAttribute("id");

            Element arrival = (Element) stop.getElementsByTagName("ar").item(0);
            Element departure = (Element) stop.getElementsByTagName("dp").item(0);
            String nextStation = departure.getAttribute("ppth").split("\\|")[0];

            String timeXML;
            if(nextStation.equals(FRANKFURT_HBF)){
                timeXML = arrival.getAttribute("pt");
            }
            else{
                timeXML = departure.getAttribute("pt");
            }
            formatter = DateTimeFormatter.ofPattern("yyMMddHHmm"); // Bsp. Time: 2308041414
            LocalDateTime time = LocalDateTime.parse(timeXML, formatter);

            stops.put(stopId, time);
        }

        return stops;
    }

    private HashMap<String, LocalDateTime> getRecentChanges() {
        // HTTP GET REQUEST to DB API

        // Extract % edit XML Response

        // Fill Hashmap

        return null;
    }

    private HttpURLConnection getHttpRequest(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("DB-Client-Id", clientId);
        con.setRequestProperty("DB-Api-Key", clientKey);
        con.setRequestProperty("Accept", "application/xml");
        return con;
    }

    private Document getXMLFromHttpRequest(HttpURLConnection con) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(con.getInputStream());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        return doc;
    }
}
