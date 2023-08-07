package com.BrunoCookie.Schrankeninspektor.Utils;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Component
public class DB_API_Utils {
    @Value("${clientId}")
    private String clientId;
    @Value("${clientKey}")
    private String clientKey;
    private final String URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/";
    private final String TIMETABLE_PLAN = "plan/";
    private final String TIMETABLE_RECENT_CHANGES = "rchg/";
    private final String GRIESHEIM_EVA_NO = "8002046";
    private final String FRANKFURT_HBF = "Frankfurt Hbf (tief)";
    private final Logger logger = LoggerFactory.getLogger(DB_API_Utils.class);

    public HashSet<LocalDateTime> getTrainInformationFromAPI(LocalDateTime time) throws IOException {
        // Get Planned Data
        HashMap<String, Stop> stops = getPlannedData(time);

        // Apply recent changes to planned data
        applyRecentChanges(stops, getRecentChanges());

        HashSet<LocalDateTime> stopTimes = new HashSet<>();
        stops.forEach((id, stop) -> stopTimes.add(stop.getTime()));

        return stopTimes;
    }

    public void applyRecentChanges(HashMap<String, Stop> stops, ArrayList<Recent_Change> recentChanges) {
        logger.info("Applying recent changes to planned data...");

        // Compare both Hashmaps
        // Replace stop time if Change available
        for (int i = 0; i < recentChanges.size(); i++) {
            Recent_Change recentChange = recentChanges.get(i);
            if (!stops.containsKey(recentChange.getId())) {
                continue;
            }

            Stop stop = stops.get(recentChange.getId());
            LocalDateTime newStopTime;
            if (stop.isTimeFromArrival()) {
                newStopTime = recentChange.getArTime();
            } else {
                newStopTime = recentChange.getDpTime();
            }
            if(newStopTime == null) stops.remove(stop.id);
            else stop.setTime(newStopTime);
        }

        // Add new stops if not available in planned --> Ignore for now
    }

    private HashMap<String, Stop> getPlannedData(LocalDateTime time) throws IOException {
        // Format Day and Time
        String formattedTime = Date_Utils.formatDate(time);

        // HTTP GET REQUEST to DB API
        HttpURLConnection con = getHttpRequest(new URL(this.URL + TIMETABLE_PLAN + GRIESHEIM_EVA_NO + "/" + formattedTime));

        // Extract % edit XML Response
        Document doc = getXMLFromHttpRequest(con);
        return extractPlannedDataFromXML(doc);
    }

    public HashMap<String, Stop> extractPlannedDataFromXML(Document doc){
        logger.info("Extracting planned data from XML-File: " + doc.getDocumentURI());

        HashMap<String, Stop> stops = new HashMap<>();
        NodeList nodeList = doc.getElementsByTagName("s");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element stop = (Element) nodeList.item(i);
            String stopId = stop.getAttribute("id");

            Element arrival = (Element) stop.getElementsByTagName("ar").item(0);
            Element departure = (Element) stop.getElementsByTagName("dp").item(0);
            String nextStation = findNextDestination(stop);

            String timeXML;
            if(nextStation.isEmpty()){
                continue;
            }
            else if (nextStation.equals(FRANKFURT_HBF) && arrival != null) {
                timeXML = arrival.getAttribute("pt");
            } else {
                timeXML = departure.getAttribute("pt");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm"); // Bsp. Time: 2308041414
            LocalDateTime formattedXMLTime = LocalDateTime.parse(timeXML, formatter);

            Stop stopObj = Stop.builder().
                               id(stopId).
                               time(formattedXMLTime).
                               isTimeFromArrival(nextStation.equals(FRANKFURT_HBF)).
                               build();

            stops.put(stopId, stopObj);
        }
        return stops;
    }

    private ArrayList<Recent_Change> getRecentChanges() throws IOException {
        // HTTP GET REQUEST to DB API
        HttpURLConnection con = getHttpRequest(new URL(this.URL + TIMETABLE_RECENT_CHANGES + GRIESHEIM_EVA_NO));

        // Extract % edit XML Response
        Document doc = getXMLFromHttpRequest(con);
        return extractRecentChangesFromXML(doc);
    }

    public ArrayList<Recent_Change> extractRecentChangesFromXML(Document doc){
        logger.info("Extracting recent changes from XML-File: " + doc.getDocumentURI());

        ArrayList<Recent_Change> recentChanges = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("s");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element stop = (Element) nodeList.item(i);
            Element arrival = (Element) stop.getElementsByTagName("ar").item(0);
            Element departure = (Element) stop.getElementsByTagName("dp").item(0);
            String stopId = stop.getAttribute("id");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm"); // Bsp. Time: 2308041414
            LocalDateTime arTime = null;
            LocalDateTime dpTime = null;

            if(arrival != null) arTime = LocalDateTime.parse(arrival.getAttribute("ct"), formatter);
            if(departure != null) dpTime = LocalDateTime.parse(departure.getAttribute("ct"), formatter);

            recentChanges.add(Recent_Change.builder().
                                           id(stopId).
                                           arTime(arTime).
                                           dpTime(dpTime).
                                           build());
        }
        return recentChanges;
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

    public String findNextDestination(Element stop){
        Element departure = (Element) stop.getElementsByTagName("dp").item(0);
        if(departure != null){
            return departure.getAttribute("ppth").split("\\|")[0];
        }
        return "";
    }

    @Builder
    @Data
    public static class Stop {
        private String id;
        private LocalDateTime time;
        private boolean isTimeFromArrival;
    }

    @Builder
    @Data
    public static class Recent_Change {
        private String id;
        private LocalDateTime arTime;
        private LocalDateTime dpTime;
    }
}
