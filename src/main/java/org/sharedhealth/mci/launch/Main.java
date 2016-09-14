package org.sharedhealth.mci.launch;

import com.datastax.driver.mapping.MappingManager;
import org.sharedhealth.mci.client.LRClient;
import org.sharedhealth.mci.config.MCICassandraConfig;
import org.sharedhealth.mci.config.MCIProperties;
import org.sharedhealth.mci.repository.LocationRepository;
import org.sharedhealth.mci.service.LocationService;
import org.sharedhealth.mci.task.LRSyncTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static LRSyncTask lrSyncTask;

    public static final String LR_DIVISION_URI_PATH = "/list/division";
    public static final String LR_DISTRICT_URI_PATH = "/list/district";
    public static final String LR_UPAZILA_URI_PATH = "/list/upazila";
    public static final String LR_PAURASAVA_PATH = "/list/paurasava";
    public static final String LR_UNION_URI_PATH = "/list/union";
    public static final String LR_WARD_URI_PATH = "/list/ward";

    public static final String DIVISION_TYPE = "DIVISION";
    public static final String DISTRICT_TYPE = "DISTRICT";
    public static final String UPAZILA_TYPE = "UPAZILA";
    public static final String PAURASAVA_TYPE = "PAURASAVA";
    public static final String UNION_TYPE = "UNION";
    public static final String WARD_TYPE = "WARD";

    private static void createLRSyncScheduler() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            lrSyncTask.syncLocation(LR_DIVISION_URI_PATH, DIVISION_TYPE);
            lrSyncTask.syncLocation(LR_DISTRICT_URI_PATH, DISTRICT_TYPE);
            lrSyncTask.syncLocation(LR_UPAZILA_URI_PATH, UPAZILA_TYPE);
            lrSyncTask.syncLocation(LR_PAURASAVA_PATH, PAURASAVA_TYPE);
            lrSyncTask.syncLocation(LR_UNION_URI_PATH, UNION_TYPE);
            lrSyncTask.syncLocation(LR_WARD_URI_PATH, WARD_TYPE);
        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) {
        MCIProperties instance = MCIProperties.getInstance();
        MappingManager mappingManager = MCICassandraConfig.getInstance().getMappingManager();
        LocationRepository locationRepository = new LocationRepository(mappingManager);
        LocationService locationService = new LocationService(locationRepository);
        lrSyncTask = new LRSyncTask(locationService, new LRClient(), instance);

        createLRSyncScheduler();

    }
}