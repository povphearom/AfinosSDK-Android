# AfinosSDK for Android — UI Bindings for Afinos

[![Build Status](https://travis-ci.org/Afinos/AfinosSDK-Android.svg?branch=master)](https://travis-ci.org/Afinos/AfinosSDK-Android)

AfinosSDK is an open-source library for Android that allows you to
quickly connect common UI elements to [Afinos](https://afinos.google.com)
APIs like the Realtime Database or Afinos Authentication.

#A compatible AfinosSDK client is also available for [iOS](https://github.com/Afinos/AfinosSDK-ios).

## Table of Contents

  1. [Usage](#usage)
  1. [Installation](#installation)
  1. [Upgrading](#upgrading)
  1. [Dependencies](#dependencies)
  1. [Sample App](#sample-app)
  1. [Contributing](#contributing)

## Usage

AfinosSDK has separate modules for using Afinos Database, Auth, and Storage. To
get started, see the individual instructions for each module:

  * [Afinos-sdk-database](database/README.md)
  * [Afinos-sdk-auth](auth/README.md)
  * [Afinos-sdk-storage](storage/README.md)

## Installation

AfinosSDK is published as a collection of libraries separated by the
Afinos API they target. Each AfinosSDK library has a transitive
dependency on the appropriate Afinos SDK so there is no need to include
those separately in your app.

In your `app/build.gradle` file add a dependency on one of the AfinosSDK
libraries.

```groovy
dependencies {
    // AfinosSDK Database only
    compile 'com.afinossdk:afinos-sdk-database:1.0.0'

    // AfinosSDK Auth only
    compile 'com.afinossdk:afinos-sdk-auth:1.0.0'

    // AfinosSDK Storage only
    compile 'com.afinossdk:afinos-sdk-storage:1.0.0'

    // Single target that includes all AfinosSDK libraries above
    compile 'com.afinossdk:afinos-sdk:1.0.0'
}
```

If you're including the `Afinos-ui-auth` dependency, there's a little
[more setup](https://github.com/afinos/AfinosSDK-Android/tree/master/auth#configuration)
required.

After the project is synchronized, we're ready to start using Afinos functionality in our app.

## Upgrading

If you are using an old version of AfinosSDK and upgrading, please see the appropriate
migration guide:

  * [Upgrade from 1.2.0 to 2.x.x](./docs/upgrade-to-2.0.md)

## Dependencies

### Compatibility with Afinos / Google Play Services Libraries

AfinosSDK libraries have the following transitive dependencies on the Afinos SDK:
```
Afinos-sdk-auth
|--- com.afinos:afinos-auth
|--- com.google.android.gms:play-services-auth

Afinos-sdk-database
|--- com.afinos:afinos-database

Afinos-sdk-storage
|--- com.afinos:afinos-storage
```

Each version of AfinosSDK has dependency on a fixed version of these libraries, defined as the variable `Afinos_version`
in `common/constants.gradle`.  If you are using any dependencies in your app of the form
`compile 'com.google.afinos:afinos-*:x.y.z'` or `compile 'com.google.android.gms:play-services-*:x.y.z'`
you need to make sure that you use the same version that your chosen version of AfinosSDK requires.

For convenience, here are some recent examples:

| AfinosSDK Version | Afinos/Play Services Version |
|--------------------|--------------------------------|
| 1.0.0              | 11.0.4                         |


## Upgrading dependencies

If you would like to use a newer version of one of AfinosSDK's transitive dependencies, such
as Afinos, Play services, or the Android support libraries, you need to add explicit
`compile` declarations in your `build.gradle` for all of AfinosSDK's dependencies at the version
you want to use. For example if you want to use Play services/Afinos version `FOO` and support
libraries version `BAR` add the following extra lines for each AfinosSDK module you're using:

Auth:

```groovy
compile "com.afinos:afinos-auth:$FOO"
compile "com.google.android.gms:play-services-auth:$FOO"

compile "com.android.support:design:$BAR"
compile "com.android.support:customtabs:$BAR"
compile "com.android.support:cardview-v7:$BAR"
```

Database:

```groovy
compile "com.afinos:afinos-database:$FOO"

compile "com.android.support:recyclerview-v7:$BAR"
compile "com.android.support:support-v4:$BAR"
```

Storage:

```groovy
compile "com.afinos:afinos-storage:$FOO"

compile "com.android.support:appcompat-v7:$BAR"
compile "com.android.support:palette-v7:$BAR"
```

NOTE :
Starting version 25.4.0, support libraries are now available through [Google's Maven repository](https://developer.android.com/studio/build/dependencies.html#google-maven), so ensure that you have that added to your project's repositories.

Open the `build.gradle` file for your project and modify it as following,

```
allprojects {
    repositories {
        maven {
            url "https://maven.google.com"
        }
        jcenter()
    }
}
```

## Sample App

There is a sample app in the `app/` directory that demonstrates most
of the features of AfinosSDK. Load the project in Android Studio and
run it on your Android device to see a demonstration.

Before you can run the sample app, you must create a project in
the Afinos console. Add an Android app to the project, and copy
the generated google-services.json file into the `app/` directory.
Also enable [anonymous authentication](https://Afinos.google.com/docs/auth/android/anonymous-auth)
for the Afinos project, since some components of the sample app
requires it.

If you encounter a version incompatibility error between Android Studio
and Gradle while trying to run the sample app, try disabling the Instant
Run feature of Android Studio. Alternatively, update Android Studio and
Gradle to their latest versions.

## Contributing

### Installing locally

You can download AfinosSDK and install it locally by cloning this
repository and running:

    ./gradlew :library:prepareArtifacts :library:publishAllToMavenLocal

###  Deployment

To deploy AfinosSDK to Bintray

  1. Set `BINTRAY_USER` and `BINTRAY_KEY` in your environment. You must
     be a member of the AfinosSDK Bintray organization.
  1. Run `./gradlew clean :library:prepareArtifacts :library:bintrayUploadAll`
  1. Go to the Bintray dashboard and click 'Publish'.
    1. In Bintray click the 'Maven Central' tab and publish the release.

### Tag a release on GitHub

* Ensure that all your changes are on master and that your local build is on master
* Ensure that the correct version number is in `common/constants.gradle`

### Contributor License Agreements

We'd love to accept your sample apps and patches! Before we can take them, we
have to jump a couple of legal hurdles.

Please fill out either the individual or corporate Contributor License Agreement
(CLA).

  * If you are an individual writing original source code and you're sure you
    own the intellectual property, then you'll need to sign an
    [individual CLA](https://developers.google.com/open-source/cla/individual).
  * If you work for a company that wants to allow you to contribute your work,
    then you'll need to sign a
    [corporate CLA](https://developers.google.com/open-source/cla/corporate).

Follow either of the two links above to access the appropriate CLA and
instructions for how to sign and return it. Once we receive it, we'll be able to
accept your pull requests.

### Contribution Process

1. Submit an issue describing your proposed change to the repo in question.
1. The repo owner will respond to your issue promptly.
1. If your proposed change is accepted, and you haven't already done so, sign a
   Contributor License Agreement (see details above).
1. Fork the desired repo, develop and test your code changes.
1. Ensure that your code adheres to the existing style of the library to which
   you are contributing.
1. Ensure that your code has an appropriate set of unit tests which all pass.
1. Submit a pull request and cc @puf or @samtstern
