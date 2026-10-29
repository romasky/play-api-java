package com.testFramework.play_qa_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** Pins the JSON the user fixtures put on the wire: exact keys (NON_NULL) and Generator value shapes. */
class UserFixturesTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void randomProfileHasOnlyGeneratedFirstAndLastName() {
        JsonNode profile = JSON.valueToTree(UserFixtures.randomProfile().build());

        assertEquals(Set.of("first_name", "last_name"), keys(profile));
        assertTrue(profile.get("first_name").asText().matches("Test[a-z]{6}"), profile.toString());
        assertTrue(profile.get("last_name").asText().matches("User[a-z]{6}"), profile.toString());
    }

    @Test
    void randomUserHasExactlyCredentialsAndProfile() {
        JsonNode user = JSON.valueToTree(UserFixtures.randomUser().build());

        assertEquals(Set.of("email", "username", "password", "profile"), keys(user));
        assertTrue(user.get("email").asText().matches("[a-z0-9]{10}@play-qa\\.com"), user.toString());
        assertTrue(user.get("username").asText().matches("user_[a-z0-9]{8}"), user.toString());
        assertTrue(user.get("password").asText().matches("Pass_[a-z0-9]{10}!1"), user.toString());
        assertEquals(Set.of("first_name", "last_name"), keys(user.get("profile")));
    }

    @Test
    void randomUpdateNeverSendsAPasswordKey() {
        JsonNode update = JSON.valueToTree(UserFixtures.randomUpdate().build());

        assertEquals(Set.of("email", "username", "profile"), keys(update));
        assertEquals(Set.of("first_name", "last_name"), keys(update.get("profile")));
    }

    @Test
    void eachCallIsFresh() {
        assertNotEquals(UserFixtures.randomUser().build().getEmail(), UserFixtures.randomUser().build().getEmail());
    }

    private static Set<String> keys(JsonNode node) {
        Set<String> keys = new TreeSet<>();
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) keys.add(it.next());
        return keys;
    }
}
