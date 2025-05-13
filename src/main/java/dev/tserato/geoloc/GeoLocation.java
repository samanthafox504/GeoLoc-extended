package dev.tserato.geoloc;

import dev.tserato.geoloc.config.ConfigManager;
import dev.tserato.geoloc.config.DefaultLocationValueHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GeoLocation {
    private String continent;
    private String continentCode;
    private String country;
    private String countryCode;
    private String regionName;
    private String regionCode;
    private String city;
    private String timezone;

    public String getContinent() {
        return continent;
    }
    public String getContinentCode() {
        return continentCode;
    }
    public String getCountry() {
        return country;
    }
    public String getCountryCode() {
        return countryCode;
    }

    public String getRegion() {
        return regionName;
    }
    public String getRegionCode() {
        return regionCode;
    }

    public String getCity() {
        return city;
    }

    public String getLocalTime() {
        if (timezone == null || timezone.isEmpty()) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timezone));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return now.format(formatter);
        } catch (Exception e) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }
    }
}