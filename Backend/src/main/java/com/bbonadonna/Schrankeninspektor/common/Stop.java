package com.bbonadonna.Schrankeninspektor.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Stop {
    private String id;
    private LocalDateTime fahrtzeitpunkt;
}
