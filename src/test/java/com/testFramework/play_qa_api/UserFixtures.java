package com.testFramework.play_qa_api;

import com.testFramework.core.Generator;
import com.testFramework.play_qa_api.models.createUser.CreateUserReq;
import com.testFramework.play_qa_api.models.createUser.ProfileReq;

/**
 * Random, valid user request bodies. Each call returns a fresh builder, so a step overrides
 * only the field its scenario is about and every other field stays a valid random value.
 */
public final class UserFixtures {

    private UserFixtures() { }

    /** Profile with a random first and last name — the only profile fields the API requires. */
    public static ProfileReq.ProfileReqBuilder randomProfile() {
        return ProfileReq.builder()
                .firstName(Generator.firstName())
                .lastName(Generator.lastName());
    }

    /** Create-user body: random email, username, password and {@link #randomProfile()}. */
    public static CreateUserReq.CreateUserReqBuilder randomUser() {
        return CreateUserReq.builder()
                .email(Generator.email())
                .username(Generator.username())
                .password(Generator.password())
                .profile(randomProfile().build());
    }

    /** Update body: {@link #randomUser()} without a password — update never sends a password key. */
    public static CreateUserReq.CreateUserReqBuilder randomUpdate() {
        return randomUser().password(null);
    }
}
