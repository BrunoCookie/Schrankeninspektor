package com.BrunoCookie.Schrankeninspektor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/api/inspektor")
public class Schrankeninspektor_Resource {
    private final Schrankeninspektor_Service service;

    @Autowired
    public Schrankeninspektor_Resource(Schrankeninspektor_Service service){
        this.service = service;
    }

    @GetMapping("/isCurrentlyOpen")
    public boolean isCurrentlyOpen() throws IOException {
        return service.isCurrentlyOpen();
    }

    @GetMapping("/opensClosesNext")
    public LocalDateTime opensClosesNext(){
        return service.whenStatusChange();
    }
}
