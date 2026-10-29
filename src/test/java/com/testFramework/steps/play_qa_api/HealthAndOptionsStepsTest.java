package com.testFramework.steps.play_qa_api;

import com.testFramework.play_qa_api.models.health.HealthResp;
import com.testFramework.play_qa_api.models.options.UserOptionsResp;
import com.testFramework.support.StepContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.testFramework.support.Fake.*;
import static com.testFramework.support.StepContext.failsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline: the HealthSteps and OptionsSteps assertions against DTOs parsed from canned JSON.
 * The requests these steps send are pinned by {@link RequestPinTest}.
 */
@ExtendWith(StepContext.class)
class HealthAndOptionsStepsTest {

    private static final String HEALTH = "{'status':'ok','time':'2030-01-01T00:00:00Z'}";
    private static final String OPTIONS = "{'allowed_methods':['GET','POST'],'endpoints':{'list':'/api/v1/users'},"
            + "'authentication':{'type':'Bearer'}}";

    private final HealthSteps health = new HealthSteps();
    private final OptionsSteps options = new OptionsSteps();
    private final BaseSteps ctx = new BaseSteps();

    @Test
    void healthStatusOkNeedsStatusOkAndANonBlankTime() {
        ctx.save("ok", dto(HealthResp.class, HEALTH));
        ctx.save("down", dto(HealthResp.class, with(HEALTH, "status", "'down'")));
        ctx.save("noTime", dto(HealthResp.class, without(HEALTH, "time")));
        ctx.save("blankTime", dto(HealthResp.class, with(HEALTH, "time", "' '")));

        assertDoesNotThrow(() -> health.assertHealthStatusOk("ok"));
        failsWith("expected: <ok> but was: <down>", () -> health.assertHealthStatusOk("down"));
        failsWith("time field should not be null or blank: null", () -> health.assertHealthStatusOk("noTime"));
        failsWith("time field should not be null or blank:  ", () -> health.assertHealthStatusOk("blankTime"));
    }

    @Test
    void allowedMethodsChecks() {
        ctx.save("opts", dto(UserOptionsResp.class, OPTIONS));

        assertAll(
                () -> options.assertAllowedMethodsContains("POST", "opts"),
                () -> options.assertAllowedMethodsCount(2, "opts"));
        failsWith("allowed_methods should contain 'DELETE'. Actual: [GET, POST]",
                () -> options.assertAllowedMethodsContains("DELETE", "opts"));
        failsWith("expected: <3> but was: <2>", () -> options.assertAllowedMethodsCount(3, "opts"));
    }

    @Test
    void endpointsMapMustBePresentAndNonEmpty() {
        ctx.save("opts", dto(UserOptionsResp.class, OPTIONS));
        ctx.save("none", dto(UserOptionsResp.class, without(OPTIONS, "endpoints")));
        ctx.save("empty", dto(UserOptionsResp.class, with(OPTIONS, "endpoints", "{}")));

        assertDoesNotThrow(() -> options.assertEndpointsNotEmpty("opts"));
        failsWith("expected: not <null>", () -> options.assertEndpointsNotEmpty("none"));
        failsWith("expected: <false> but was: <true>", () -> options.assertEndpointsNotEmpty("empty"));
    }

    @Test
    void authenticationTypeMustBePresentAndEqual() {
        ctx.save("opts", dto(UserOptionsResp.class, OPTIONS));
        ctx.save("none", dto(UserOptionsResp.class, without(OPTIONS, "authentication")));

        assertDoesNotThrow(() -> options.assertAuthenticationType("Bearer", "opts"));
        failsWith("expected: not <null>", () -> options.assertAuthenticationType("Bearer", "none"));
        failsWith("expected: <Basic> but was: <Bearer>", () -> options.assertAuthenticationType("Basic", "opts"));
    }
}
