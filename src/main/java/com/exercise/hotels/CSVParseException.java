package com.exercise.hotels;

import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

public class CSVParseException extends IOException {
    private CSVRecord record;

    public CSVParseException(String message, CSVRecord record) {
        super(message);
        this.record = record;
    }
}
