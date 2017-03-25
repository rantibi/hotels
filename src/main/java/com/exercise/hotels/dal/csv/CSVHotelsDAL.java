package com.exercise.hotels.dal.csv;

import com.exercise.hotels.entity.*;
import com.exercise.hotels.config.CSVHotelsDALConfig;
import com.exercise.hotels.dal.HotelsDAL;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

@Singleton
public class CSVHotelsDAL implements HotelsDAL {

    private final Map<String, NavigableSet<Hotel>> hotelsByCity;

    public CSVHotelsDAL(CSVHotelsDALConfig config) throws IOException {
        hotelsByCity = HotelsCSVParser.parse(config.getCsvFilePath());
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
