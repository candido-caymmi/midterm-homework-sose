package com.tourism.controllers;

import com.tourism.implementation.TourismServiceImpl;
import com.tourism.models.TourismPlace;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;

@Path("/tourism")
public class TourismController {

    private final TourismServiceImpl service =
            new TourismServiceImpl();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public TourismPlace getPlaceById(@PathParam("id") String id) {
        return service.getPlaceById(id);
    }

    @GET
    @Path("/accessible")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TourismPlace> getAccessiblePlaces() {
        return service.getAccessiblePlaces();
    }

    @GET
    @Path("/sustainable")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TourismPlace> getSustainablePlaces() {
        return service.getSustainablePlaces();
    }

    @GET
    @Path("/location/{location}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TourismPlace> getPlacesByLocation(
            @PathParam("location") String location) {
        return service.getPlacesByLocation(location);
    }

    @GET
    @Path("/category/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TourismPlace> getPlacesByCategory(
            @PathParam("category") String category) {
        return service.getPlacesByCategory(category);
    }

    @GET
    @Path("/recommended")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TourismPlace> getRecommendedPlaces() {
        return service.getRecommendedPlaces();
    }
}