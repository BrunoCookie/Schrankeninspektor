package com.BrunoCookie.Schrankeninspektor.Utils;

import lombok.Builder;
import lombok.Data;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Service
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

    public HashSet<LocalDateTime> getTrainInformationFromAPI(LocalDateTime time) throws IOException {
        // Get Planned Data
        HashMap<String, Stop> stops = getPlannedData(time);

        // Apply recent changes to planned data
        applyRecentChanges(stops);

        HashSet<LocalDateTime> stopTimes = new HashSet<>();
        stops.forEach((id, stop) -> stopTimes.add(stop.getTime()));

        return stopTimes;
    }

    private void applyRecentChanges(HashMap<String, Stop> stops) throws IOException {
        // Get Recent Changes
        ArrayList<Recent_Change> recentChanges = getRecentChanges();

        // Compare both Hashmaps
        // Replace stop time if Change available
        for (int i = 0; i < recentChanges.size(); i++) {
            Recent_Change recentChange = recentChanges.get(i);
            if (!stops.containsKey(recentChange.getId())) {
                continue;
            }

            Stop stop = stops.get(recentChange.getId());
            if (stop.isTimeFromArrival()) {
                stop.setTime(recentChange.getArTime());
            } else {
                stop.setTime(recentChange.getDpTime());
            }
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

        // Fill Hashmap
        HashMap<String, Stop> stops = new HashMap<>();
        NodeList nodeList = doc.getElementsByTagName("s");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element stop = (Element) nodeList.item(i);
            String stopId = stop.getAttribute("id");

            Element arrival = (Element) stop.getElementsByTagName("ar").item(0);
            Element departure = (Element) stop.getElementsByTagName("dp").item(0);
            String nextStation = departure.getAttribute("ppth").split("\\|")[0];

            String timeXML;
            boolean isTimeFromArrival = nextStation.equals(FRANKFURT_HBF);
            if (isTimeFromArrival) {
                timeXML = arrival.getAttribute("pt");
            } else {
                timeXML = departure.getAttribute("pt");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm"); // Bsp. Time: 2308041414
            LocalDateTime formattedXMLTime = LocalDateTime.parse(timeXML, formatter);

            Stop stopObj = Stop.builder().
                               id(stopId).
                               time(formattedXMLTime).
                               isTimeFromArrival(isTimeFromArrival).
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

        // Fill Hashmap
        ArrayList<Recent_Change> recentChanges = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("s");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element stop = (Element) nodeList.item(i);
            String stopId = stop.getAttribute("id");

            Element arrival = (Element) stop.getElementsByTagName("ar").item(0);
            Element departure = (Element) stop.getElementsByTagName("dp").item(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm"); // Bsp. Time: 2308041414
            LocalDateTime arTime = LocalDateTime.parse(arrival.getAttribute("ct"), formatter);
            LocalDateTime dpTime = LocalDateTime.parse(departure.getAttribute("ct"), formatter);

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
