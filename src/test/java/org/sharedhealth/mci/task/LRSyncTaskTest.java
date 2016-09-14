package org.sharedhealth.mci.task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.sharedhealth.mci.client.LRClient;
import org.sharedhealth.mci.config.MCIProperties;
import org.sharedhealth.mci.model.LRMarker;
import org.sharedhealth.mci.model.LocationData;
import org.sharedhealth.mci.service.LocationService;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LRSyncTaskTest {
    private final String lrUrl = "http://lr.com";
    private LRSyncTask lrSyncTask;

    @Mock
    private MCIProperties mciProperties;
    @Mock
    private LocationService locationService;
    @Mock
    private LRClient lrClient;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        lrSyncTask = new LRSyncTask(locationService, lrClient, mciProperties);
        when(mciProperties.getLrUrl()).thenReturn(lrUrl);
    }

    @Test
    public void shouldBuildUrlsWhenThereIsNoLastFeedUriFound() throws Exception {
        String type = "type";
        String lrUriPath = "/locations/division";
        when(locationService.getLRMarker(type)).thenReturn(null);
        LocationData locationData = new LocationData();
        locationData.setUpdatedAt("2016-09-12 12:30:12");
        when(lrClient.fetchLocations(anyString(), eq(mciProperties))).thenReturn(asList(locationData));

        lrSyncTask.syncLocation(lrUriPath, type);
        verify(lrClient).fetchLocations(lrUrl + lrUriPath + "?offset=0&limit=100&updatedSince=0000-00-00", mciProperties);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        verify(locationService).saveOrUpdateLRMarker(argument1.capture(), argument2.capture());

        assertEquals(type, argument1.getValue());
        assertTrue(argument2.getValue().endsWith("offset=0&limit=100&updatedSince=2016-09-12 12:30:12"));
    }

    @Test
    public void shouldBuildUrlsUsingLastFeedUri() throws Exception {
        String type = "type";
        String lrUriPath = "/locations/division";
        when(locationService.getLRMarker(type)).thenReturn(new LRMarker(type, "http://lr.com?offset=0&limit=100&updatedSince=2015-09-12 12:30:12"));
        LocationData locationData = new LocationData();
        locationData.setUpdatedAt("2016-09-12 12:30:12");
        when(lrClient.fetchLocations(anyString(), eq(mciProperties))).thenReturn(asList(locationData));

        lrSyncTask.syncLocation(lrUriPath, type);
        verify(lrClient).fetchLocations(lrUrl + lrUriPath + "?offset=0&limit=100&updatedSince=2015-09-12+12%3A30%3A12", mciProperties);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        verify(locationService).saveOrUpdateLRMarker(argument1.capture(), argument2.capture());

        assertEquals(type, argument1.getValue());
        assertTrue(argument2.getValue().endsWith("offset=0&limit=100&updatedSince=2016-09-12 12:30:12"));
    }

    @Test
    public void shouldChangeTheOffsetIfFirstAndLastEntryHasSameUpdatedSince() throws Exception {
        String type = "type";
        String lrUriPath = "/locations/division";
        when(locationService.getLRMarker(type)).thenReturn(new LRMarker(type, "http://lr.com?offset=0&limit=100&updatedSince=2016-09-12 12:30:12"));
        LocationData locationData = new LocationData();
        locationData.setUpdatedAt("2016-09-12 12:30:12");
        when(lrClient.fetchLocations(anyString(), eq(mciProperties))).thenReturn(asList(locationData));

        lrSyncTask.syncLocation(lrUriPath, type);
        verify(lrClient).fetchLocations(lrUrl + lrUriPath + "?offset=0&limit=100&updatedSince=2016-09-12+12%3A30%3A12", mciProperties);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        verify(locationService).saveOrUpdateLRMarker(argument1.capture(), argument2.capture());

        assertEquals(type, argument1.getValue());
        assertTrue(argument2.getValue().endsWith("offset=1&limit=100&updatedSince=2016-09-12 12:30:12"));

    }
}