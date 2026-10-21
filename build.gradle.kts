plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 は Kotlin を内蔵しているため、kotlin.android プラグインは使わない
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
