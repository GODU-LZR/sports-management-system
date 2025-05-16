package com.example.venue.service;

import com.example.venue.pojo.NearVenue;
import com.example.venue.pojo.NearVenueParam;

import java.util.List;

public interface UserVenueServer {

    public List<NearVenue> searchNearVenue(NearVenueParam nearVenueParam);
}
