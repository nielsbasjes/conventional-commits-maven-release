Conventional Commits for the Maven Release Plugin
========================================
[![Github actions Build status](https://img.shields.io/github/actions/workflow/status/nielsbasjes/conventional-commits-maven-release/build.yml?branch=main)](https://github.com/nielsbasjes/conventional-commits-maven-release/actions)
[![Coverage Status](https://img.shields.io/codecov/c/github/nielsbasjes/conventional-commits-maven-release)](https://app.codecov.io/gh/nielsbasjes/conventional-commits-maven-release)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/nl.basjes.maven.release/conventional-commits-version-policy.svg)](https://central.sonatype.com/namespace/nl.basjes.maven.release)
[![GitHub stars](https://img.shields.io/github/stars/nielsbasjes/conventional-commits-maven-release?label=GitHub%20stars)](https://github.com/nielsbasjes/conventional-commits-maven-release/stargazers)
[![If this project has business value for you then don't hesitate to support me with a small donation.](https://img.shields.io/badge/Donations-via%20Paypal-blue.svg)](https://www.paypal.me/nielsbasjes)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org)

# Introduction
When using the [maven-release-plugin](https://maven.apache.org/maven-release/maven-release-plugin/) you can add custom rules on how the next version is determined. Such a "next version calculation" is called a Version Policy.

This is a version policy that enforces the Semantic Versioning format and upgrades major/minor/patch element for next development version depending on the commit messages since the previous release which is detected by looking at the tags in the SCM.

The default configuration follows the [Conventional Commits](https://www.conventionalcommits.org/) standard for calculating the next version.

# Documentation
Check the documentation site for more information:  https://maven.basjes.nl

# License
    Conventional Commits Version Policy
    Copyright (C) 2022-2023 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
