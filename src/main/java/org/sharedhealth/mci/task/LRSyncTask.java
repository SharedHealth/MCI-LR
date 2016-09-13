package org.sharedhealth.mci.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sharedhealth.mci.client.LRClient;
import org.sharedhealth.mci.config.MCIProperties;
import org.sharedhealth.mci.model.LRMarker;
import org.sharedhealth.mci.model.LocationData;
import org.sharedhealth.mci.service.LocationService;
import org.sharedhealth.mci.util.Constants;
import org.sharedhealth.mci.util.URLParser;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.mci.util.Constants.URL_SEPARATOR;
import static org.sharedhealth.mci.util.StringUtils.removePrefix;
import static org.sharedhealth.mci.util.StringUtils.removeSuffix;

public class LRSyncTask {
    private static final Logger logger = LogManager.getLogger(LRSyncTask.class);

    private static final int DEFAULT_LIMIT = 100;
    private static final int INITIAL_OFFSET = 0;
    private static final String OFFSET = "offset";
    private static final String UPDATED_SINCE = "updatedSince";
    private static final String INITIAL_UPDATED_SINCE = "0000-00-00";
    private static final String EXTRA_FILTER_PATTERN = "?offset=%s&limit=%s&updatedSince=%s";

    private LocationService locationService;
    private LRClient lrClient;
    private MCIProperties mciProperties;

    public LRSyncTask(LocationService locationService, LRClient lrClient, MCIProperties mciProperties) {
        this.locationService = locationService;
        this.lrClient = lrClient;
        this.mciProperties = mciProperties;
    }

    public synchronized void syncLocation(String lrUriPath, String type) {
        logger.debug("Starting {} sync", type);
        try {
            Map<String, String> map = getPreviousOrDefaultOffsetAndUpdatedSince(type);
            String nextUrlForLocation = buildFullUrlForLocation(lrUriPath, map.get(OFFSET), map.get(UPDATED_SINCE));
            List<LocationData> locations = lrClient.fetchLocations(nextUrlForLocation, mciProperties);

            if (locations == null || locations.size() == 0) return;
            locationService.saveOrUpdateLocations(locations);
            String lastUpdatedAt = locations.get(locations.size() - 1).getUpdatedAt();
            String lastFeedUrl;
            if (locations.size() == DEFAULT_LIMIT) {
                //shouldn't in this case lastUpdateAt should be replaced by map.get(UPDATED_SINCE)
                lastFeedUrl = buildFullUrlForLocation(lrUriPath, map.get(OFFSET) + DEFAULT_LIMIT, lastUpdatedAt);
            } else {
                lastFeedUrl = buildFullUrlForLocation(lrUriPath, String.valueOf(INITIAL_OFFSET), lastUpdatedAt);
            }
            locationService.saveOrUpdateLRMarker(type, lastFeedUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildFullUrlForLocation(String lrUriPath, String offset, String updatedSince) {
        String extraFilter = String.format(EXTRA_FILTER_PATTERN, offset, DEFAULT_LIMIT, updatedSince);
        String pathToNextUrl = String.format("%s%s", removeSuffix(lrUriPath, Constants.URL_SEPARATOR), extraFilter);
        return String.format("%s%s", mciProperties.getLrUrl(), removePrefix(pathToNextUrl, URL_SEPARATOR));
    }

    private Map<String, String> getPreviousOrDefaultOffsetAndUpdatedSince(String type) throws IOException {
        LRMarker lrMarker = locationService.getLRMarker(type);
        if (lrMarker != null && lrMarker.getLastFeedUrl() != null) {
            return URLParser.parseURL(new URL(lrMarker.getLastFeedUrl()));
        }
        HashMap<String, String> defaultQueryParams = new HashMap<>();
        defaultQueryParams.put(OFFSET, String.valueOf(INITIAL_OFFSET));
        defaultQueryParams.put(UPDATED_SINCE, INITIAL_UPDATED_SINCE);
        return defaultQueryParams;
    }

}
