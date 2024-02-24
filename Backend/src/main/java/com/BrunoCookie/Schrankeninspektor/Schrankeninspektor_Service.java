package com.BrunoCookie.Schrankeninspektor;

import com.BrunoCookie.Schrankeninspektor.Utils.DB_API_Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

@Service
public class Schrankeninspektor_Service {
    private final DB_API_Utils db_api_utils;
    private final int MIN_UNTIL_CLOSE = 3;
    HashSet<LocalDateTime> times = new HashSet<>();

    @Autowired
    public Schrankeninspektor_Service(DB_API_Utils db_api_utils){
        this.db_api_utils = db_api_utils;
    }

    public boolean isCurrentlyOpen() throws IOException {
        updateStopTimes();
        LocalDateTime currentTime = LocalDateTime.now();
        if(currentTime.getMinute() > 55){
            times.addAll(db_api_utils.getTrainInformationFromAPI(LocalDateTime.now().plusHours(1)));
        }
        for (LocalDateTime time : times) {
            if(time.isBefore(currentTime)) continue;
            long diff = ChronoUnit.MINUTES.between(currentTime, time);
            if(diff <= MIN_UNTIL_CLOSE){
                return false;
            }
        }
        return true;
    }

    public LocalDateTime whenStatusChange() throws IOException {
        return isCurrentlyOpen() ? findClosedSlot() : findOpenSlot();
    }

    // Find the next 5min Gap between 2 stops
    private LocalDateTime findOpenSlot() throws IOException {
        LocalDateTime openSlot = null;
        ArrayList<LocalDateTime> timesList = new ArrayList(times);
        while(openSlot == null){
            openSlot = find5MinGap(LocalDateTime.now(), timesList);
            if(openSlot == null){
                LocalDateTime nextHour = timesList.get(0).plusHours(1);
                timesList = new ArrayList(db_api_utils.getTrainInformationFromAPI(nextHour));
            }
        }
        return openSlot;
    }

    // Find the next stop
    private LocalDateTime findClosedSlot() throws IOException {
        LocalDateTime closedSlot = null;
        ArrayList<LocalDateTime> timesList = new ArrayList(times);
        while(closedSlot == null){
            closedSlot = findNextStopTime(LocalDateTime.now(), timesList);
            if(closedSlot == null){
                LocalDateTime nextHour = timesList.get(0).plusHours(1);
                timesList = new ArrayList(db_api_utils.getTrainInformationFromAPI(nextHour));
            }
        }
        return closedSlot;
    }

    private void updateStopTimes() throws IOException {
        times = db_api_utils.getTrainInformationFromAPI(LocalDateTime.now());
    }

    public LocalDateTime find5MinGap(LocalDateTime currentTime, ArrayList<LocalDateTime> times){
        Collections.sort(times);
        for(int i = 0; i< times.size()-1; i++){
            if(currentTime.isAfter(times.get(i))) continue;
            long diff = ChronoUnit.MINUTES.between(times.get(i), times.get(i+1));
            if(diff > MIN_UNTIL_CLOSE){
                return times.get(i).plusMinutes(1);
            }
        }
        return null;
    }

    public LocalDateTime findNextStopTime(LocalDateTime currentTime, ArrayList<LocalDateTime> times){
        Collections.sort(times);
        for(int i = 0; i< times.size(); i++){
            if(currentTime.isBefore(times.get(i))){
                return times.get(i).minusMinutes(MIN_UNTIL_CLOSE);
            }
        }
        return null;
    }
}
