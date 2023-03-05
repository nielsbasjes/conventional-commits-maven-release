
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

File buildLog = new File( basedir, 'build.log' )
assert buildLog.exists()
assert 1 == buildLog.getText().count("[INFO] From SCM tag with version 1.2.3 doing a MINOR version increase based on commit messages to version 1.3.0")
assert 1 == buildLog.getText().count("[INFO] Full run would be commit 1 files with message: 'ci(Release): Version v1.3.0'")

// The pom based version is NOT related to what the actual version will be.
File pomXml = new File( basedir, 'pom.xml' )
assert pomXml.exists()
assert new XmlSlurper().parse( pomXml ).version.text() == "1.0-SNAPSHOT"

// The actual version is based upon the tags and commit messages.
File pomXmlTag = new File( basedir, 'pom.xml.tag' )
assert pomXmlTag.exists()
assert new XmlSlurper().parse( pomXmlTag ).version.text() == "1.3.0"

// The next development version should be standard
File pomXmlNext = new File( basedir, 'pom.xml.next' )
assert pomXmlNext.exists()
assert new XmlSlurper().parse( pomXmlNext ).version.text() == "1.3.1-SNAPSHOT"
