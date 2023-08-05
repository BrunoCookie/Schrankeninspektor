package com.BrunoCookie.Schrankeninspektor.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Date_Utils {
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd/HH");

    public static String formatDate(LocalDateTime time){
        return time.format(dateFormatter);
    }

    public static boolean isDateValid(String dateStr){
        try {
            dateFormatter.parse(dateStr);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
