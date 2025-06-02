package com.example.venue.service;

import com.example.venue.dto.VenueAddRequest;
import com.example.venue.dto.VenueSearchRequest;
import com.example.venue.pojo.es.ESVenue;

import java.util.List;

public interface ESService {

    public boolean createTable();

    public boolean addVenue(VenueAddRequest venueAddRequest);

    public List<ESVenue> searchVenue(VenueSearchRequest venueSearchRequest);

}
