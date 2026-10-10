package com.example.idcard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Entity
@Table(name = "user_cards")
@Getter
@Setter
@NoArgsConstructor
public class UserCard {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int GYM_ID_SUFFIX_BOUND = 10_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 8)
    private String bloodGroup;

    @Column(nullable = false, length = 24)
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 12)
    private String gymId;

    @Column(nullable = false, length = 255)
    private String profilePicPath;

    @Column(nullable = false, length = 40)
    private String validUntil = defaultValidityMonth();

    @Column(nullable = false, length = 160)
    private String venueLocation = "Cuttack, Nuabazar, MPIS";

    @Column(nullable = false, length = 40)
    private String venueTimeSlot = "7:00 PM - 8:00 PM";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;

    public static String generateGymId() {
        return "GY000052" + String.format("%04d", RANDOM.nextInt(GYM_ID_SUFFIX_BOUND));
    }

    public static String defaultValidityMonth() {
        return YearMonth.now().plusMonths(1)
                .format(DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH));
    }

    @PrePersist
    void applyDefaults() {
        if (gymId == null || gymId.isBlank()) {
            gymId = generateGymId();
        }
        if (validUntil == null || validUntil.isBlank()) {
            validUntil = defaultValidityMonth();
        }
        if (venueLocation == null || venueLocation.isBlank()) {
            venueLocation = "Cuttack, Nuabazar, MPIS";
        }
        if (venueTimeSlot == null || venueTimeSlot.isBlank()) {
            venueTimeSlot = "7:00 PM - 8:00 PM";
        }
        if (createdTime == null) {
            createdTime = LocalDateTime.now();
        }
    }
}
