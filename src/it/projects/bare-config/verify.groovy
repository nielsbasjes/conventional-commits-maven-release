/*
 * Conventional Commits Version Policy
 * Copyright (C) 2022-2024 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

File buildLog = new File( basedir, 'build.log' )
assert buildLog.exists()
assert 1 == buildLog.getText().count("[INFO] Version and SCM analysis result:")
assert 1 == buildLog.getText().count("[INFO] - Starting from SCM tag with version 4.5.6")
assert 1 == buildLog.getText().count("[INFO] - Doing a MINOR version increase.")
assert 1 == buildLog.getText().count("[INFO] - Next release version : 4.6.0")

assert 1 == buildLog.getText().count("[INFO] Full run would be commit 1 files with message: '[maven-release-plugin] prepare release ccversion-policy-4.6.0")

// The pom based version is NOT related to what the actual version will be.
File pomXml = new File( basedir, 'pom.xml' )
assert pomXml.exists()
assert new groovy.xml.XmlSlurper().parse( pomXml ).version.text() == "1.0-SNAPSHOT"

// The actual version is based upon the tags and commit messages.
File pomXmlTag = new File( basedir, 'pom.xml.tag' )
assert pomXmlTag.exists()
assert new groovy.xml.XmlSlurper().parse( pomXmlTag ).version.text() == "4.6.0"

// The next development version should be standard
File pomXmlNext = new File( basedir, 'pom.xml.next' )
assert pomXmlNext.exists()
assert new groovy.xml.XmlSlurper().parse( pomXmlNext ).version.text() == "4.6.1-SNAPSHOT"
