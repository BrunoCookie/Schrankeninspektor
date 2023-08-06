package com.BrunoCookie.Schrankeninspektor;

import com.BrunoCookie.Schrankeninspektor.Utils.DB_API_Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;

@Service
public class Schrankeninspektor_Service {
    private final DB_API_Utils db_api_utils;

    @Autowired
    public Schrankeninspektor_Service(DB_API_Utils db_api_utils){
        this.db_api_utils = db_api_utils;
    }

    public boolean isCurrentlyOpen() throws IOException {
        HashSet<LocalDateTime> times = db_api_utils.getTrainInformationFromAPI(LocalDateTime.now());
        LocalDateTime currentTime = LocalDateTime.now();
        if(currentTime.getMinute() > 55){
            times.addAll(db_api_utils.getTrainInformationFromAPI(LocalDateTime.now().plusHours(1)));
        }
        for (LocalDateTime time : times) {
            if(time.isBefore(currentTime)) continue;
            long diff = ChronoUnit.MINUTES.between(currentTime, time);
            if(diff <= 5) return false;
        }
        return true;
    }

    public LocalDateTime whenStatusChange(){
        return LocalDateTime.now();
    }
}
