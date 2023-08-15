package com.BrunoCookie.Schrankeninspektor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping(path = "/api/inspektor")
@CrossOrigin(origins = "0.0.0.0:4200")
public class Schrankeninspektor_Resource {
    private final Schrankeninspektor_Service service;

    private boolean isCurrentlyOpen = false;
    private LocalDateTime isCurrentlyOpenCallTime = LocalDateTime.MIN;

    private LocalDateTime statusChange = LocalDateTime.MIN;
    private LocalDateTime statusChangeCallTime = LocalDateTime.MIN;

    @Autowired
    public Schrankeninspektor_Resource(Schrankeninspektor_Service service){
        this.service = service;
    }

    @GetMapping(value = "/isCurrentlyOpen", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isCurrentlyOpen() throws IOException {
        if(compareCooldownTimer(isCurrentlyOpenCallTime)){
            isCurrentlyOpenCallTime = LocalDateTime.now();
            isCurrentlyOpen = service.isCurrentlyOpen();
        }
        return isCurrentlyOpen;
    }

    @GetMapping("/StatusChange")
    public String statusChange() throws IOException {
        if(compareCooldownTimer(statusChangeCallTime)){
            statusChangeCallTime = LocalDateTime.now();
            statusChange = service.whenStatusChange();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String iso8601 = statusChange.format(formatter);
        return iso8601;
    }

    private boolean compareCooldownTimer(LocalDateTime timer){
        LocalDateTime currentTime = LocalDateTime.now().withSecond(0).withNano(0);
        timer = timer.withSecond(0).withNano(0);
        return !currentTime.isEqual(timer);
    }
}
