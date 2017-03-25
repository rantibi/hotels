package com.exercise.hotels.dal;

import com.exercise.hotels.entity.Hotel;
import com.exercise.hotels.entity.Order;

public interface HotelsDAL {
    Iterable<Hotel> getHotelsByCity(String city, Order priceOrder);
}
