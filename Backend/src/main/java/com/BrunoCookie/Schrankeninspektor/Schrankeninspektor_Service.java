package com.BrunoCookie.Schrankeninspektor;

import com.BrunoCookie.Schrankeninspektor.Utils.DB_API_Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class Schrankeninspektor_Service {
    private final DB_API_Utils db_api_utils;

    // String = StopID, LocalDateTime = Abfahrts-/Ankunftszeit
    private HashMap<String, LocalDateTime> stopMap = new HashMap<>();

    @Autowired
    public Schrankeninspektor_Service(DB_API_Utils db_api_utils){
        this.db_api_utils = db_api_utils;
    }

    public boolean isCurrentlyOpen() throws IOException {
        db_api_utils.getTrainInformationFromAPI(LocalDateTime.now());
        return false;
    }

    public LocalDateTime whenStatusChange(){
        return LocalDateTime.now();
    }
}
