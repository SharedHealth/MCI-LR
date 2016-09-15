package org.sharedhealth.mci.task;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.time.DateUtils;
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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sharedhealth.mci.util.Constants.URL_SEPARATOR;
import static org.sharedhealth.mci.util.StringUtils.*;

public class LRSyncTask {
    private static final Logger logger = LogManager.getLogger(LRSyncTask.class);

    private static final int DEFAULT_LIMIT = 100;
    private static final int INITIAL_OFFSET = 0;
    private static final String OFFSET = "offset";
    private static final String UPDATED_SINCE = "updatedSince";
    private static final String INITIAL_UPDATED_SINCE = "0000-00-00";
    private static final String EXTRA_FILTER_PATTERN = "?offset=%s&limit=%s&updatedSince=%s";
    private final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";

    private LocationService locationService;
    private LRClient lrClient;
    private MCIProperties mciProperties;

    public LRSyncTask(LocationService locationService, LRClient lrClient, MCIProperties mciProperties) {
        this.locationService = locationService;
        this.lrClient = lrClient;
        this.mciProperties = mciProperties;
    }

    public void syncLocation(String lrUriPath, String type) {
        logger.debug("Starting {} sync", type);
        try {
            Map<String, String> initialQueryParams = getPreviousOrDefaultOffsetAndUpdatedSince(type);
            int offset = Integer.parseInt(initialQueryParams.get(OFFSET));
            String nextUrlForLocation = buildFullUrlForLocation(lrUriPath, offset, initialQueryParams.get(UPDATED_SINCE));
            List<LocationData> locations = lrClient.fetchLocations(nextUrlForLocation, mciProperties);

            if (locations == null || locations.size() == 0) return;
            String lastFeedUrl = getLastFeedUrl(lrUriPath, initialQueryParams, locations);

            logger.info("Inserting {} locations of type {}", locations.size(), type);
            locationService.saveOrUpdateLocations(locations);
            locationService.saveOrUpdateLRMarker(type, lastFeedUrl);
        } catch (Exception e) {
            logger.error(String.format("Error while syncing from %s", lrUriPath), e);
        }
    }

    private String getLastFeedUrl(String lrUriPath, Map<String, String> initialQueryParams, List<LocationData> locations) throws ParseException, UnsupportedEncodingException {
        String lastUpdatedAt = locations.get(locations.size() - 1).getUpdatedAt();
        if (isSameDate(initialQueryParams, lastUpdatedAt)) {
            int offset = Integer.parseInt(initialQueryParams.get(OFFSET)) + locations.size();
            return buildFullUrlForLocation(lrUriPath, offset, lastUpdatedAt);
        }
        return buildFullUrlForLocation(lrUriPath, INITIAL_OFFSET, lastUpdatedAt);
    }

    private boolean isSameDate(Map<String, String> initialQueryParams, String lastUpdatedAt) throws ParseException, UnsupportedEncodingException {
        if (initialQueryParams.get(UPDATED_SINCE).equals(INITIAL_UPDATED_SINCE)) return false;
        String updatedSince = URLDecoder.decode(initialQueryParams.get(UPDATED_SINCE), Charsets.UTF_8.name());
        return DateUtils.parseDate(updatedSince, DATE_FORMAT).equals(DateUtils.parseDate(lastUpdatedAt, DATE_FORMAT));
    }

    private String buildFullUrlForLocation(String lrUriPath, int offset, String updatedSince) {
        String extraFilter = String.format(EXTRA_FILTER_PATTERN, offset, DEFAULT_LIMIT, updatedSince);
        String pathToNextUrl = String.format("%s%s", removeSuffix(lrUriPath, Constants.URL_SEPARATOR), extraFilter);
        return String.format("%s%s", ensureSuffix(mciProperties.getLrUrl(), URL_SEPARATOR), removePrefix(pathToNextUrl, URL_SEPARATOR));
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
