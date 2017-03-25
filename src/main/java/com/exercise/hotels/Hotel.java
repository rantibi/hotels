package com.exercise.hotels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Hotel {
    private String city;
    private long hotelId;
    private String room;
    private double price;
}
