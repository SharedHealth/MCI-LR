package org.sharedhealth.mci.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationData {
    public static final String DEFAULT_PARENT_CODE_FOR_DIVISION = "00";

    @JsonProperty("code")
    private String code;

    @JsonProperty("name")
    private String name;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("active")
    private String active;

    @JsonProperty("updatedAt")
    private String updatedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = StringUtils.trim(code);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getParent() {
        String parent = StringUtils.substring(code, 0, code.length() - 2);
        return StringUtils.isNotEmpty(parent) ? parent : DEFAULT_PARENT_CODE_FOR_DIVISION;
    }

    public String getLocationCode() {
        int length = code.length();
        return StringUtils.substring(code, length - 2, length);
    }
}
