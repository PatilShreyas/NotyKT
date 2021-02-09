repositories {
    jcenter()
}

dependencies {
    // Exposed
    implementation "org.jetbrains.exposed:exposed-core:$exposedVersion"
    implementation "org.jetbrains.exposed:exposed-dao:$exposedVersion"
    implementation "org.jetbrains.exposed:exposed-jdbc:$exposedVersion"
    implementation "org.jetbrains.exposed:exposed-jodatime:$exposedVersion"

    // PostgreSQL
    implementation "org.postgresql:postgresql:$postgresVersion"

    // Dagger
    implementation "com.google.dagger:dagger:$daggerVersion"
    kapt "com.google.dagger:dagger-compiler:$daggerVersion"
}