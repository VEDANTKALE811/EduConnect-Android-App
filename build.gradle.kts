plugins {
    id("com.android.application") version "8.12.3" apply false
    id("com.android.library") version "8.12.3" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
