package org.sharedhealth.mci.config;

import org.sharedhealth.mci.util.Constants;

import java.util.Map;

import static org.sharedhealth.mci.util.StringUtils.ensurePrefix;

public class MCIProperties {
    private static MCIProperties mciProperties;
    private String cassandraKeySpace;
    private String cassandraHost;
    private String cassandraPort;

    private String cassandraUser;
    private String cassandraPassword;
    private String cassandraTimeout;
    private String cassandraVersion;
    private String lrUrl;
    private String lrSyncFixedDelay;
    private String lrSyncInitialDelay;

    private MCIProperties() {
        Map<String, String> env = System.getenv();
        this.cassandraKeySpace = env.get("CASSANDRA_KEYSPACE");
        this.cassandraHost = env.get("CASSANDRA_HOST");
        this.cassandraPort = env.get("CASSANDRA_PORT");
        this.cassandraPassword = env.get("CASSANDRA_PASSWORD");
        this.cassandraUser = env.get("CASSANDRA_USER");
        this.cassandraTimeout = env.get("CASSANDRA_TIMEOUT");
        this.cassandraVersion = env.get("CASSANDRA_VERSION");
        this.lrUrl = env.get("LR_URL");
        this.lrSyncFixedDelay = env.get("LR_SYNC_FIXED_DELAY");
        this.lrSyncInitialDelay = env.get("LR_SYNC_INITIAL_DELAY");
    }

    public static MCIProperties getInstance() {
        if (mciProperties != null) return mciProperties;
        mciProperties = new MCIProperties();
        return mciProperties;
    }

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }

    public String getCassandraHost() {
        return cassandraHost;
    }

    public int getCassandraPort() {
        return Integer.parseInt(cassandraPort);
    }

    public String getCassandraUser() {
        return cassandraUser;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public int getCassandraTimeout() {
        return Integer.parseInt(cassandraTimeout);
    }

    public int getCassandraVersion() {
        return Integer.parseInt(cassandraVersion);
    }

    public String getLrUrl() {
        return ensurePrefix(lrUrl, Constants.URL_SEPARATOR);
    }

    public String getLrSyncFixedDelay() {
        return lrSyncFixedDelay;
    }

    public String getLrSyncInitialDelay() {
        return lrSyncInitialDelay;
    }
}
