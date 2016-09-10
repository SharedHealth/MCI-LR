package org.sharedhealth.mci.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sharedhealth.mci.client.LRClient;
import org.sharedhealth.mci.model.LocationData;
import org.sharedhealth.mci.service.LocationService;

import java.io.IOException;
import java.util.List;

import static org.sharedhealth.mci.util.Constants.URL_SEPARATOR;
import static org.sharedhealth.mci.util.StringUtils.ensureSuffix;
import static org.sharedhealth.mci.util.StringUtils.removePrefix;

public class LRSyncTask {
    private static final Logger logger = LogManager.getLogger(LRSyncTask.class);

    private static final String LR_BASE_URL = "http://lr.com";

    private static final String LR_DIVISION_URI_PATH = "/list/division";

    private static final int DEFAULT_LIMIT = 100;
    private static final int INITIAL_OFFSET = 0;
    private static final String OFFSET = "offset";
    private static final String UPDATED_SINCE = "updatedSince";
    private static final String INITIAL_UPDATED_SINCE = "0000-00-00";
    private static final String EXTRA_FILTER_PATTERN = "?offset=%s&limit=%s&updatedSince=%s";

    private LocationService locationService;
    private LRClient lrClient;

    public LRSyncTask(LocationService locationService, LRClient lrClient) {
        this.locationService = locationService;
        this.lrClient = lrClient;
    }

    public void syncDivision() {
        logger.debug("Starting Division sync");
        String lrDivisionUrl = String.format("%s%s", ensureSuffix(LR_BASE_URL, URL_SEPARATOR),
                removePrefix(LR_DIVISION_URI_PATH, URL_SEPARATOR));
        lrClient.fetchLocations(lrDivisionUrl);
    }

    public void syncLRData(String uri, String type) throws IOException {

        List<LocationData> lastRetrieveList;
        int offset;
        String updatedSince;

//        String lastFeedUrl = getLastFeedUri(type);

//        if (lastFeedUrl != null) {
//            Map<String, String> parameters = parseURL(new URL(lastFeedUrl));
//            offset = Integer.parseInt(parameters.get(OFFSET));
//            updatedSince = parameters.get(UPDATED_SINCE);
//        } else {
        offset = INITIAL_OFFSET;
        updatedSince = INITIAL_UPDATED_SINCE;
//        }

//        String url = getCompleteUrl(uri, offset, DEFAULT_LIMIT, updatedSince);
//
//        try {
//            lastRetrieveList = getNextChunkOfDataFromLR(url);
//            if (lastRetrieveList != null && lastRetrieveList.size() > 0) {
//                locationService.saveOrUpdateLocationData(lastRetrieveList);
//                updatedSince = lastRetrieveList.get(lastRetrieveList.size() - 1).getUpdatedAt();
//
//                if (lastRetrieveList.size() == DEFAULT_LIMIT) {
//                    url = getCompleteUrl(uri, offset + DEFAULT_LIMIT, DEFAULT_LIMIT, updatedSince);
//                } else {
//                    url = getCompleteUrl(uri, INITIAL_OFFSET, DEFAULT_LIMIT, updatedSince);
//                }
//                locationService.saveOrUpdateLRMarkerData(type, url);
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return false;
//        }
//
//        return true;
    }

    private String getCompleteUrl(String uri, int offset, int defaultLimit, String updatedSince) {
        return null;
    }

    private String getLastFeedUri(String type) {
        return null;
    }
}
