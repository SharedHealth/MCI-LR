package org.sharedhealth.mci.client;

import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.sharedhealth.mci.config.MCIProperties;
import org.sharedhealth.mci.model.LocationData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.sharedhealth.mci.util.Constants.CLIENT_ID_KEY;
import static org.sharedhealth.mci.util.Constants.X_AUTH_TOKEN_KEY;

public class LRClient {
    private static final Logger logger = LogManager.getLogger(LRClient.class);

    public List<LocationData> fetchLocations(String lrUrl, MCIProperties mciProperties) throws IOException {
        logger.debug("HTTP GET request for {}", lrUrl);
        HttpGet request = buildRequest(lrUrl, mciProperties);
        String response = execute(request);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        LocationData[] locations = objectMapper.readValue(response, LocationData[].class);
        return Lists.newArrayList(locations);
    }

    private HttpGet buildRequest(String lrUrl, MCIProperties mciProperties) {
        HttpGet request = new HttpGet(lrUrl);
        request.addHeader(CLIENT_ID_KEY, mciProperties.getIdpClientId());
        request.addHeader(X_AUTH_TOKEN_KEY, mciProperties.getIdpXAuthToken());
        return request;
    }

    private String execute(HttpRequestBase request) throws IOException {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            ResponseHandler<String> responseHandler = response -> {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? parseContentInputString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected Response status.");
                }
            };
            return client.execute(request, responseHandler);
        }
    }

    private String parseContentInputString(HttpEntity entity) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent()));
        String inputLine;
        StringBuilder responseString = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) {
            responseString.append(inputLine);
        }
        bufferedReader.close();
        return responseString.toString().replace("\uFEFF", "");
    }


}
