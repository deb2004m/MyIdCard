package com.example.idcard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCardTest {

    @Test
    void generatesGymIdsInTheExpectedFormat() {
        for (int i = 0; i < 1_000; i++) {
            assertTrue(UserCard.generateGymId().matches("GY000052\\d{4}"));
        }
    }

    @Test
    void appliesDefaultsBeforePersistence() {
        UserCard card = new UserCard();
        card.applyDefaults();

        assertTrue(card.getGymId().matches("GY000052\\d{4}"));
        assertEquals(UserCard.defaultValidityMonth(), card.getValidUntil());
        assertEquals("Cuttack, Nuabazar, MPIS", card.getVenueLocation());
        assertEquals("7:00 PM - 8:00 PM", card.getVenueTimeSlot());
        assertNotNull(card.getCreatedTime());
    }
}
