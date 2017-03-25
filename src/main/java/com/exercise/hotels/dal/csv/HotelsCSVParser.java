package com.exercise.hotels.dal.csv;

import com.exercise.hotels.entity.Hotel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class HotelsCSVParser {
    private static final Comparator<Hotel> BY_PRICE_COMPARATOR = new Comparator<Hotel>() {
        @Override
        public int compare(Hotel o1, Hotel o2) {
            int result = Double.compare(o1.getPrice(), o2.getPrice());
            return result == 0 ? Long.compare(o1.getHotelId(), o2.getHotelId()) : result;
        }
    };

    public static final String[] CSV_HEADER = {"CITY", "HOTELID", "ROOM", "PRICE"};


    public static Map<String, NavigableSet<Hotel>> parse(String csvFilePath) throws IOException {
        Map<String, NavigableSet<Hotel>> hotelsByCity = new HashMap<>();
        Reader in = new FileReader(csvFilePath);
        Iterable<CSVRecord> parser = CSVFormat.DEFAULT
                .withHeader(CSV_HEADER)
                .withSkipHeaderRecord()
                .parse(in);

        for (CSVRecord record : parser) {
            if (record.size() != CSV_HEADER.length) {
                throw new CSVParseException("Unexpected fields count", record);
            }

            if (StringUtils.isEmpty(record.get(0))) {
                throw new CSVParseException(CSV_HEADER[0] + " could not be empty", record);
            }

            Long hotelId;
            Double price;

            try {
                hotelId = Long.valueOf(record.get(1));
            } catch (NumberFormatException e) {
                throw new CSVParseException(CSV_HEADER[1] + " should be long", record);
            }

            if (StringUtils.isEmpty(record.get(2))) {
                throw new CSVParseException(CSV_HEADER[2] + " could not be empty", record);
            }

            try {
                price = Double.valueOf(record.get(3));
            } catch (NumberFormatException e) {
                throw new CSVParseException(CSV_HEADER[3] + " should be double", record);
            }

            Hotel hotel = Hotel.builder()
                    .city(record.get(0))
                    .hotelId(hotelId)
                    .room(record.get(2))
                    .price(price)
                    .build();
            NavigableSet<Hotel> cityHotels = hotelsByCity.getOrDefault(hotel.getCity(), new TreeSet<>(BY_PRICE_COMPARATOR));
            cityHotels.add(hotel);
            hotelsByCity.put(hotel.getCity(), cityHotels);
        }

        return hotelsByCity;
    }

}
