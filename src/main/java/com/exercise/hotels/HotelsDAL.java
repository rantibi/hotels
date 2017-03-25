package com.exercise.hotels;

public interface HotelsDAL {
    Iterable<Hotel> getHotelsByCity(String city, Order priceOrder);
}
