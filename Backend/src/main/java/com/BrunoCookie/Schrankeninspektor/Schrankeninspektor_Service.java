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
    private boolean isCurrentlyOpen = false;
    private LocalDateTime whenStatusChange = LocalDateTime.MIN;
    HashSet<LocalDateTime> times = new HashSet<>();
    // TODO: 1 Minute restriction

    @Autowired
    public Schrankeninspektor_Service(DB_API_Utils db_api_utils){
        this.db_api_utils = db_api_utils;
    }

    public boolean isCurrentlyOpen() throws IOException {
        updateStopTimes();
        LocalDateTime currentTime = LocalDateTime.now();
        isCurrentlyOpen = true;
        if(currentTime.getMinute() > 55){
            times.addAll(db_api_utils.getTrainInformationFromAPI(LocalDateTime.now().plusHours(1)));
        }
        for (LocalDateTime time : times) {
            if(time.isBefore(currentTime)) continue;
            long diff = ChronoUnit.MINUTES.between(currentTime, time);
            if(diff <= 5){
                isCurrentlyOpen = false;
                break;
            }
        }
        return isCurrentlyOpen;
    }

    public LocalDateTime whenStatusChange() throws IOException {
        boolean isOpen = isCurrentlyOpen();
        if(isOpen){
            return findClosedSlot();
        }
        else{
            return findOpenSlot();
        }
    }

    // Find the next 5min Gap between 2 stops
    private LocalDateTime findOpenSlot() throws IOException {
        LocalDateTime openSlot = null;
        ArrayList<LocalDateTime> sortedTimes = new ArrayList(times);
        while(openSlot == null){
            Collections.sort(sortedTimes);
            for(int i = 0; i< sortedTimes.size()-1; i++){
                if(LocalDateTime.now().isAfter(sortedTimes.get(i))) continue;
                long diff = ChronoUnit.MINUTES.between(sortedTimes.get(i), sortedTimes.get(i+1));
                if(diff > 5){
                    openSlot = sortedTimes.get(i).plusMinutes(1);
                }
            }

            if(openSlot == null){
                LocalDateTime nextHour = sortedTimes.get(0).plusHours(1);
                sortedTimes = new ArrayList(db_api_utils.getTrainInformationFromAPI(nextHour));
            }
        }
        return openSlot;
    }

    // Find the next stop
    private LocalDateTime findClosedSlot() throws IOException {
        LocalDateTime closedSlot = null;
        ArrayList<LocalDateTime> sortedTimes = new ArrayList(times);
        while(closedSlot == null){
            Collections.sort(sortedTimes);
            for(int i = 0; i< sortedTimes.size(); i++){
                if(LocalDateTime.now().isBefore(sortedTimes.get(i))){
                    closedSlot = sortedTimes.get(i).minusMinutes(5);
                }
            }

            if(closedSlot == null){
                LocalDateTime nextHour = sortedTimes.get(0).plusHours(1);
                sortedTimes = new ArrayList(db_api_utils.getTrainInformationFromAPI(nextHour));
            }
        }
        return closedSlot;
    }

    private void updateStopTimes() throws IOException {
        times = db_api_utils.getTrainInformationFromAPI(LocalDateTime.now());
    }
}
