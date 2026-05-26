package com.tourism.implementation;

import com.tourism.interfaces.TourismService;
import com.tourism.models.TourismPlace;
import com.tourism.rdf.RDFLoader;
import com.tourism.rdf.SPARQLExecutor;

import org.apache.jena.rdf.model.Model;

import java.util.List;

public class TourismServiceImpl implements TourismService {

    private final RDFLoader loader = new RDFLoader();

    private final SPARQLExecutor executor = new SPARQLExecutor();

    @Override
    public TourismPlace getPlaceById(String id) {

        Model model = loader.loadModel();

        return executor.getPlaceById(model, id);
    }

    @Override
    public List<TourismPlace> getAccessiblePlaces() {

        Model model = loader.loadModel();

        return executor.getAccessiblePlaces(model);
    }

    @Override
    public List<TourismPlace> getSustainablePlaces() {

        Model model = loader.loadModel();

        return executor.getSustainablePlaces(model);
    }

    @Override
    public List<TourismPlace> getPlacesByLocation(String location) {

        Model model = loader.loadModel();

        return executor.getPlacesByLocation(model, location);
    }

    @Override
    public List<TourismPlace> getPlacesByCategory(String category) {

        Model model = loader.loadModel();

        return executor.getPlacesByCategory(model, category);
    }

    @Override
    public List<TourismPlace> getRecommendedPlaces() {

        Model model = loader.loadModel();

        return executor.getRecommendedPlaces(model);
    }
}