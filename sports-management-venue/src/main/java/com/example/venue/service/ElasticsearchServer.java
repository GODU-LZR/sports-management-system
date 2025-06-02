package com.example.venue.service;

import com.example.venue.pojo.VenueDocument;

import java.util.List;

public interface ElasticsearchServer {

    boolean createVenueIndex();

    boolean indexVenue(VenueDocument venueDocument);

    List<VenueDocument> findAllVenues();

    boolean deleteVenueIndex();
}
