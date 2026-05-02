package com.testFramework;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tests")
@ConfigurationParameter(
        key = "cucumber.glue",
        value = "com.testFramework.steps")
@ConfigurationParameter(
        key = "cucumber.plugin",
        value = "pretty,io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm,json:target/cucumber-reports/CucumberTests.json,junit:target/cucumber-reports/CucumberTests.xml,html:target/cucumber-reports/index.html")
@ConfigurationParameter(
        key = "cucumber.filter.tags",
        value = "@Run and not @Ignore and not @Bug and not @NotImplemented")
@ConfigurationParameter(
        key = "cucumber.monochrome",
        value = "true")
public class RunnerForTest {
}
