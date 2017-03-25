package com.exercise.hotels;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class CSVHotelsDAL implements HotelsDAL {

    private final Map<String, NavigableSet<Hotel>> hotelsByCity;

    public CSVHotelsDAL(String csvFilePath) throws IOException {
        hotelsByCity = HotelsCSVParser.parse(csvFilePath);
    }


    @Override
    public Iterable<Hotel> getHotelsByCity(String city, Order priceOrder) {
        NavigableSet<Hotel> hotels = hotelsByCity.get(city);
        final Iterator<Hotel> iter;

        if (priceOrder == null) {
            iter = hotels.iterator();
        } else {
            switch (priceOrder) {
                case DESC:
                    iter = hotels.descendingIterator();
                    break;
                default:
                    iter = hotels.iterator();
            }
        }

        return new Iterable<Hotel>() {
            @Override
            public Iterator<Hotel> iterator() {
                return iter;
            }
        };
    }
}
