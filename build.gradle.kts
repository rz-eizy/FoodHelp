// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // SonarQube
    id("org.sonarqube") version "6.3.1.5724"
}

sonar {
    properties {

        property("sonar.projectKey", "FoodHelp")
        property("sonar.projectName", "FoodHelp")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", "sqp_b164150551bfc6e1f925b209e10f325ea0c612c3")
    }
}