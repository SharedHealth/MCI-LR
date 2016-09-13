package org.sharedhealth.mci.task;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.MappingManager;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sharedhealth.mci.BaseIntegrationTest;
import org.sharedhealth.mci.client.LRClient;
import org.sharedhealth.mci.config.MCICassandraConfig;
import org.sharedhealth.mci.config.MCIProperties;
import org.sharedhealth.mci.repository.LocationRepository;
import org.sharedhealth.mci.service.LocationService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.sharedhealth.mci.util.Constants.*;

public class LRSyncTaskIT extends BaseIntegrationTest {
    private MappingManager mappingManager;
    private MCIProperties mciProperties;
    private LRSyncTask lrSyncTask;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9997);

    @Before
    public void setUp() throws Exception {
        mappingManager = MCICassandraConfig.getInstance().getMappingManager();
        LocationRepository locationRepository = new LocationRepository(mappingManager);
        LRClient lrClient = new LRClient();
        mciProperties = MCIProperties.getInstance();
        lrSyncTask = new LRSyncTask(new LocationService(locationRepository), lrClient, mciProperties);
    }

    @After
    public void tearDown() throws Exception {
        Session session = mappingManager.getSession();
        session.execute(QueryBuilder.truncate(CF_LOCATIONS));
        session.execute(QueryBuilder.truncate(CF_LR_MARKERS));
    }

    @Test
    public void shouldFetchLocationsFromGivenUrlAndSave() throws Exception {
        String locationUri = "/list/upazila";
        stubFor(get(urlPathMatching("/api/1.0/locations/list/upazila"))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .withHeader(X_AUTH_TOKEN_KEY, equalTo(mciProperties.getIdpXAuthToken()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(asString("locations/upazilas.json")))
        );

        lrSyncTask.syncLocation(locationUri, "upazila");

        ResultSet resultSet = mappingManager.getSession().execute(QueryBuilder.select().all().from(CF_LOCATIONS));
        assertFalse(resultSet.isExhausted());
        List<Row> rows = resultSet.all();
        assertEquals(2, rows.size());

        assertTrue(assertLocationRow("09", "Amtali", "1004", "1", rows));
        assertTrue(assertLocationRow("19", "Bamna", "1004", "1", rows));
    }

    @Test
    public void shouldFetchDivisionLocationsAndSaveWithDefaultParent() throws Exception {
        String locationUri = "/list/division";

        stubFor(get(urlPathMatching("/api/1.0/locations/list/division"))
                .withHeader(CLIENT_ID_KEY, equalTo(mciProperties.getIdpClientId()))
                .withHeader(X_AUTH_TOKEN_KEY, equalTo(mciProperties.getIdpXAuthToken()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.SC_OK)
                        .withBody(asString("locations/divisions.json")))
        );

        lrSyncTask.syncLocation(locationUri, "DIVISION");

        ResultSet resultSet = mappingManager.getSession().execute(QueryBuilder.select().all().from(CF_LOCATIONS));
        assertFalse(resultSet.isExhausted());
        List<Row> rows = resultSet.all();
        assertEquals(2, rows.size());

        assertTrue(assertLocationRow("20", "Chittagong", "00", "1", rows));
        assertTrue(assertLocationRow("30", "Dhaka", "00", "1", rows));
    }

    private boolean assertLocationRow(String code, String name, String parent, String isActive, List<Row> rows) {
        return rows.stream().anyMatch(row -> code.equals(row.getString(CODE)) &&
                name.equals(row.getString(NAME)) &&
                parent.equals(row.getString(PARENT)) &&
                isActive.equals(row.getString(ACTIVE)));
    }

    private String asString(String resourceName) throws IOException {
        return Resources.toString(Resources.getResource(resourceName), Charset.forName("UTF-8"));
    }
}