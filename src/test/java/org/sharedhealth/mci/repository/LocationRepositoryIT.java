package org.sharedhealth.mci.repository;

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import org.junit.Before;
import org.junit.Test;
import org.sharedhealth.mci.BaseIntegrationTest;
import org.sharedhealth.mci.config.MCICassandraConfig;
import org.sharedhealth.mci.model.Location;
import org.sharedhealth.mci.model.LocationData;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class LocationRepositoryIT extends BaseIntegrationTest {
    private Mapper<Location> locationMapper;
    private LocationRepository locationRepository;

    @Before
    public void setUp() throws Exception {
        MappingManager mappingManager = MCICassandraConfig.getInstance().getMappingManager();
        locationMapper = mappingManager.mapper(Location.class);
        locationRepository = new LocationRepository(mappingManager);
    }

    @Test
    public void shouldInsertLocations() throws Exception {
        LocationData dhk = buildLocationData("30", "DHK", "1");
        LocationData gzh = buildLocationData("3033", "GZH", "1");
        LocationData klj = buildLocationData("303334", "KLJ", "1");
        List<LocationData> locations = asList(dhk, gzh, klj);

        locationRepository.saveOrUpdateLocationData(locations);

        assertLocation(dhk, locationMapper.get("30", "00"));
        assertLocation(gzh, locationMapper.get("33", "30"));
        assertLocation(klj, locationMapper.get("34", "3033"));
    }

    @Test
    public void shouldUpdateLocationIfAlreadyExist() throws Exception {
        locationMapper.save(new Location("30", "first", "00", "0"));
        locationMapper.save(new Location("33", "second", "30", "1"));

        LocationData dhk = buildLocationData("30", "DHK", "1");
        LocationData gzh = buildLocationData("3033", "GZH", "1");
        LocationData klj = buildLocationData("303334", "KLJ", "1");
        List<LocationData> locations = asList(dhk, gzh, klj);

        locationRepository.saveOrUpdateLocationData(locations);

        assertLocation(dhk, locationMapper.get("30", "00"));
        assertLocation(gzh, locationMapper.get("33", "30"));
        assertLocation(klj, locationMapper.get("34", "3033"));
    }

    private void assertLocation(LocationData dhk, Location location) {
        assertEquals(dhk.getLocationCode(), location.getCode());
        assertEquals(dhk.getLocationCode(), location.getCode());
        assertEquals(dhk.getName(), location.getName());
        assertEquals(dhk.getActive(), location.getActive());
    }

    private LocationData buildLocationData(String code, String name, String active) {
        LocationData locationData = new LocationData();
        locationData.setCode(code);
        locationData.setId(code);
        locationData.setActive(active);
        locationData.setName(name);
        return locationData;
    }
}