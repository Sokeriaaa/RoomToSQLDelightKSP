plugins {
    kotlin("jvm")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:2.3.3")
    compileOnly("androidx.room:room-common:2.8.4")
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
}
