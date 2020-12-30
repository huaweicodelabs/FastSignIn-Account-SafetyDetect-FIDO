# Accountkit-SafetyDetect-FIDO


## Table of Contents

 * [Introduction](#introduction)
 * [Getting Started](#getting-started)
 * [Supported Environments](#supported-environments)
 * [Result](#result)
 * [License](#license)


## Introduction
    Android sample code encapsulates APIs of the HUAWEI Account Kit, SafetyDetect, FIDO.
    It provides sample program for your reference or usage.

    The following describes of Android sample code.

    java:   Java code packages. This package contains java code that implements Account Sign, SafetyDetect check, FIDO - Bioauthn login.

    kotlin: kotlin code packages. This package contains kotlin code that implements Account Sign, SafetyDetect check, FIDO - Bioauthn login.


## Getting Started

   1. Check whether the Android studio(3.x+) development environment is ready, and your device or virtual device which have installed latest Huawei Mobile Service(HMS Core) Apk.
   2. Register a [HUAWEI account](https://developer.huawei.com/consumer/en/).
   3. Create an app and configure the app information in AppGallery Connect. See details: [HUAWEI Account Kit Development Preparations](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)
   4. Open the Account Account Kit, SafetyDetect, FIDO services from [AppGallery Connect](https://developer.huawei.com/consumer/en/service/josp/agc/index.html).
   5. Download and build this demo.
   6. Configure the sample code:
        (1) Download the file "agconnect-services.json" of the app on AppGallery Connect, and add the file to the app root directory(\app) of the demo.
        (2) Change the value of applicationId and signingConfigs in the app-level build.gradle file of the sample project to the package name of your app.
   7. Run the sample on your Android device or virtual device.


## Supported Environments
    Android SDK Version >= 23 and JDK version >= 1.8 is recommended.

##  Result
    This demo provides demonstration for following scenarios:
    1. How to integrate the Account Kit to login to the HUAWEI ID.
    2. How to inherit Safety Detect to detect fake users.
    3. How to integrate FIDO BioAuthn to implement fingerprint login.


##  License

    Copyright 2020. Huawei Technologies Co., Ltd.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.