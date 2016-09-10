package org.sharedhealth.mci.repository;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sharedhealth.mci.model.Location;
import org.sharedhealth.mci.model.LocationData;

import java.util.List;

public class LocationRepository {
    private Logger logger = LogManager.getLogger(LocationRepository.class);
    private Mapper<Location> locationMapper;

    public LocationRepository(MappingManager mappingManager) {
        this.locationMapper = mappingManager.mapper(Location.class);
    }

    public boolean saveOrUpdateLocationData(List<LocationData> locationDataList) {
        for (LocationData locationData : locationDataList) {
            logger.debug("Inserting location data for {} with code {}", locationData.getName(), locationData.getLocationCode());
            locationMapper.save(mapLocationData(locationData));
        }
        return true;
    }

    private Location mapLocationData(LocationData data) {
        Location location = new Location();
        location.setCode(data.getLocationCode());
        location.setName(data.getName());
        location.setParent(data.getParent());
        location.setActive(data.getActive());
        return location;
    }

}
