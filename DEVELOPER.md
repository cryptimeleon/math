This Gradle project consists of two sub-projects.
The root project is the Math library itself.
The `android-test` sub-project is only used to run the library's tests on Android.
For that purpose it contains an Android-specific build file.

Since there are multiple sub-projects, you will need to prefix Gradle commands with a colon (`:`) if you want them to run only for the Math library and not the Android sub-project.
For example, instead of `./gradlew build` use `./gradlew :build` to build the Math library.
The former will fail since the android sub-project is not configured to be actually buildable.
See [here](https://docs.gradle.org/current/userguide/intro_multi_project_builds.html) for more information on how multi-project builds work.
