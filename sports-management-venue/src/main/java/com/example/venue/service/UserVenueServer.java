package com.example.venue.service;

import com.example.venue.pojo.NearVenueParam;
import com.example.venue.pojo.VenueDocument;

import java.util.List;

public interface UserVenueServer {

    public List<VenueDocument> searchNearVenue(NearVenueParam nearVenueParam);
}
