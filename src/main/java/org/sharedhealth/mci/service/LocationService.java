package org.sharedhealth.mci.service;

import org.sharedhealth.mci.model.LRMarker;
import org.sharedhealth.mci.model.LocationData;
import org.sharedhealth.mci.repository.LocationRepository;

import java.util.List;

public class LocationService {
    private LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void saveOrUpdateLocations(List<LocationData> locations) {
        locationRepository.saveOrUpdateLocationData(locations);
    }

    public LRMarker getLRMarker(String type) {
        return locationRepository.getLRMarker(type);
    }

    public void saveOrUpdateLRMarker(String type, String feedUrl) {
        locationRepository.saveOrUpdateLRMarker(type, feedUrl);
    }
}
