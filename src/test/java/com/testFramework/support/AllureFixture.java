package com.testFramework.support;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.AllureResultsWriter;
import io.qameta.allure.model.FixtureResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.model.TestResultContainer;

import java.io.InputStream;
import java.util.UUID;

/** Runs code the way Cucumber runs a hook — inside an Allure fixture — and reports the fixture's final name. */
public final class AllureFixture {

    public interface Body {
        void run() throws Exception;
    }

    private AllureFixture() {
    }

    public static String nameAfter(Body body) throws Exception {
        AllureLifecycle original = Allure.getLifecycle();
        AllureLifecycle capture = new AllureLifecycle(new AllureResultsWriter() {
            @Override public void write(TestResult result) { }
            @Override public void write(TestResultContainer container) { }
            @Override public void write(String source, InputStream attachment) { }
        });
        String container = UUID.randomUUID().toString();
        String uuid = UUID.randomUUID().toString();
        FixtureResult fixture = new FixtureResult().setName("unnamed hook");
        Allure.setLifecycle(capture);
        try {
            capture.startTestContainer(new TestResultContainer().setUuid(container));
            capture.startPrepareFixture(container, uuid, fixture);
            body.run();
            capture.stopFixture(uuid);
        } finally {
            Allure.setLifecycle(original);
        }
        return fixture.getName();
    }
}
