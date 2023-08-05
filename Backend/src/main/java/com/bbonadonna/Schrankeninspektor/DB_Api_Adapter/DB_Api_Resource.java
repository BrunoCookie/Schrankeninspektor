package com.bbonadonna.Schrankeninspektor.DB_Api_Adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "inspektor")
@CrossOrigin(origins = "http://localhost:8080")
public class DB_Api_Resource {
    private final DB_Api_Service service;

    @Autowired
    public DB_Api_Resource(DB_Api_Service service){
        this.service = service;
    }

    @GetMapping("/isCurrentlyOpen")
    public boolean isCurrentlyOpen() throws IOException {
        service.getTrainInformationFromAPI();
        return false;
    }

    @GetMapping("/opensClosesNext")
    public LocalDateTime opensClosesNext(){
        return LocalDateTime.now();
    }
}
