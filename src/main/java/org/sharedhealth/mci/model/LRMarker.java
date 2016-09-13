package org.sharedhealth.mci.model;


import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import static org.sharedhealth.mci.util.Constants.*;

@Table(name = CF_LR_MARKERS)
public class LRMarker {
    @PartitionKey
    @Column(name = TYPE)
    private String type;

    @Column(name = LAST_FEED_URI)
    private String lastFeedUrl;

    public LRMarker(String type, String lastFeedUrl) {
        this.type = type;
        this.lastFeedUrl = lastFeedUrl;
    }

    public LRMarker() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastFeedUrl() {
        return lastFeedUrl;
    }

    public void setLastFeedUrl(String lastFeedUrl) {
        this.lastFeedUrl = lastFeedUrl;
    }


}