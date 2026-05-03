// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val room_version = "2.8.4"

    alias(libs.plugins.android.application) apply false
    id("androidx.room") version room_version apply false
    alias(libs.plugins.kotlin.compose) apply false
}