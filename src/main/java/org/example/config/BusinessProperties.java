package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "business")
public class BusinessProperties {

    private LoyaltyConfig loyalty = new LoyaltyConfig();
    private BookingConfig booking = new BookingConfig();
    private RoomConfig room = new RoomConfig();
    private UserConfig user = new UserConfig();
    private HotelConfig hotel = new HotelConfig();

    @Data
    public static class LoyaltyConfig {
        private Map<String, Integer> tiers;
        private Integer pointsPer100Dollars;
        private Map<String, Integer> minPointsForDiscount;
        private Map<String, Double> baseDiscountRates;
        private Map<String, Double> maxDiscountRates;
        private Map<String, Integer> bonusMultipliers;
        private Map<String, Double> maxBonus;
    }

    @Data
    public static class BookingConfig {
        private String defaultStatus;
        private Integer minBookingDays;
        private Integer maxBookingDays;
        private Integer maxBookingsPerUser;
        private Integer freeCancellationDays;
    }

    @Data
    public static class RoomConfig {
        private Double minPricePerNight;
        private Double maxPricePerNight;
        private Map<String, Integer> defaultCapacity;
        private Map<String, Double> minArea;
    }

    @Data
    public static class UserConfig {
        private String defaultAdminPassword;
        private String defaultAdminUsername;
        private String defaultAdminEmail;
        private Integer minPasswordLength;
        private Integer maxPasswordLength;
        private String defaultRole;
    }

    @Data
    public static class HotelConfig {
        private Integer maxHotelsPerManager;
        private Double defaultRating;
        private Double maxRating;
    }
}