package org.example.fixfw.cucumber;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/main/resources/features",
        glue = "org.example.fixfw.steps",
        plugin = {
                "pretty",
                "html:target/cucumber-report/cucumber.html",
                "json:target/cucumber-report/cucumber.json",
                "summary"
        },
        monochrome=true
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {
}
