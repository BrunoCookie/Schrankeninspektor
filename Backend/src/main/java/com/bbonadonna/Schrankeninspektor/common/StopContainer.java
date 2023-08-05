package com.bbonadonna.Schrankeninspektor.common;

import lombok.Data;

import java.util.ArrayList;

@Data
public class StopContainer {
    private final StopContainer stopContainer = new StopContainer();
    private ArrayList<Stop> stops;

    public StopContainer(){
        stops = new ArrayList<>();
    }

    public void clearStops(){
        stops.clear();
    }
}
