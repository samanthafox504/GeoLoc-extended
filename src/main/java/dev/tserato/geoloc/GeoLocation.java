package dev.tserato.geoloc;

import dev.tserato.geoloc.config.ConfigManager;
import dev.tserato.geoloc.config.DefaultLocationValueHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class GeoLocation {
    private String country;
    private String regionName;
    private String city;
    private String timezone;

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return regionName;
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