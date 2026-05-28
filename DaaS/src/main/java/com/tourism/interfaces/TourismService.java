package com.tourism.interfaces;

import java.util.List;
import com.tourism.models.TourismPlace;

public interface TourismService {

    TourismPlace getPlaceById(String id);

    List<TourismPlace> getAccessiblePlaces();

    List<TourismPlace> getSustainablePlaces();

    List<TourismPlace> getPlacesByLocation(String location);

    List<TourismPlace> getPlacesByCategory(String category);

    List<TourismPlace> getRecommendedPlaces();
}