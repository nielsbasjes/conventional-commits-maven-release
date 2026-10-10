/*
 * Conventional Commits Version Policy
 * Copyright (C) 2022-2026 Niels Basjes
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
package nl.basjes.maven.release.version.conventionalcommits

import nl.basjes.maven.release.version.conventionalcommits.ConventionalCommitsVersionConfig.Companion.fromXml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class TestConventionalCommitsVersionConfig {
    var versionRulesConfig: String = """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>
            """.trimIndent()

    @Test
    fun createAndSerdeLoopTest() {
        val config = fromXml(versionRulesConfig)
        assertNotNull(config)

        val config1 = ConventionalCommitsVersionConfig()

        config1
            .setVersionTag("My Version Tag")
            .addMinorRule("Minor One")
            .addMinorRule("Minor Two")
            .addMinorRule("Minor Three")
            .addMajorRule("Major One")
            .addMajorRule("Major Two")
            .addMajorRule("Major Three")

        assertEquals(config.toString(), config1.toString())

        val configXml = config1.toXml()

        val config2 = fromXml(configXml)
        assertNotNull(config2)

        assertEquals(config1.toString(), config2.toString())
    }

    @Test
    fun readXMLTestVersionMajorMinor() {
        val config = fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>

            """.trimIndent()
        )
        assertNotNull(config)
        assertEquals("My Version Tag", config.versionTag)
        assertEquals(mutableListOf("Minor One", "Minor Two", "Minor Three"), config.minorRules)
        assertEquals(mutableListOf("Major One", "Major Two", "Major Three"), config.majorRules)
    }

    @Test
    fun readXMLTestVersionMajorEmptyMinorEmpty() {
        val config = fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
              </minorRules>
              <majorRules>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>

            """.trimIndent()
        )
        assertNotNull(config)
        assertEquals("My Version Tag", config.versionTag)
        assertEquals(mutableListOf(), config.minorRules)
        assertEquals(mutableListOf(), config.majorRules)
    }

    @Test
    fun readXMLTestVersion() {
        val config = fromXml(
            """
            <projectVersionPolicyConfig>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>

            """.trimIndent()
        )
        assertNotNull(config)
        assertEquals("My Version Tag", config.versionTag)
        assertEquals(mutableListOf(), config.minorRules)
        assertEquals(mutableListOf(), config.majorRules)
    }

    @Test
    fun readXMLTestMinor() {
        val config = fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
            </projectVersionPolicyConfig>

            """.trimIndent()
        )
        assertNotNull(config)
        assertNull(config.versionTag)
        assertEquals(mutableListOf("Minor One", "Minor Two", "Minor Three"), config.minorRules)
        assertEquals(mutableListOf(), config.majorRules)
    }

    @Test
    fun readXMLTestMajor() {
        val config = fromXml(
            """
            <projectVersionPolicyConfig>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
            </projectVersionPolicyConfig>

            """.trimIndent()
        )
        assertNotNull(config)
        assertNull(config.versionTag)
        assertEquals(mutableListOf(), config.minorRules)
        assertEquals(mutableListOf("Major One", "Major Two", "Major Three"), config.majorRules)
    }
}
