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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val LOG: Logger = LogManager.getLogger()

internal class TestConfigParsing {
    private val defaultVersionRules = VersionRules()

    private val customVersionTagRegex = "^The awesome ([0-9]+\\.[0-9]+\\.[0-9]+) release$"
    private val customMajorRulesRegex = "^.*Big Change.*$"
    private val customMinorRulesRegex = "^.*Nice Change.*$"

    private val customVersionTagXML = "<versionTag>$customVersionTagRegex</versionTag>"
    private val customMajorRulesXML = "<majorRules><majorRule>$customMajorRulesRegex</majorRule></majorRules>"
    private val customMinorRulesXML = "<minorRules><minorRule>$customMinorRulesRegex</minorRule></minorRules>"

    val regexFlags = setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL, RegexOption.UNIX_LINES)

    private val customVersionTagPattern = Regex(customVersionTagRegex, regexFlags)
    private val customMajorRulesPatterns = mutableListOf(Regex(customMajorRulesRegex, regexFlags))
    private val customMinorRulesPatterns = mutableListOf(Regex(customMinorRulesRegex, regexFlags))

    private fun assertTagPatternIsDefault(versionRules: VersionRules) {
        assertEquals(defaultVersionRules.tagPattern.toString(), versionRules.tagPattern.toString())
    }

    private fun assertTagPatternIsCustom(versionRules: VersionRules) {
        assertEquals(customVersionTagPattern.toString(), versionRules.tagPattern.toString())
    }

    private fun assertMajorIsDefault(versionRules: VersionRules) {
        assertEquals(
            defaultVersionRules.majorUpdatePatterns.toString(),
            versionRules.majorUpdatePatterns.toString()
        )
    }

    private fun assertMajorIsCustom(versionRules: VersionRules) {
        assertEquals(
            customMajorRulesPatterns.toString(),
            versionRules.majorUpdatePatterns.toString()
        )
    }

    private fun assertMajorIsEmpty(versionRules: VersionRules) {
        assertTrue(versionRules.majorUpdatePatterns.isEmpty())
    }


    private fun assertMinorIsDefault(versionRules: VersionRules) {
        assertEquals(
            defaultVersionRules.minorUpdatePatterns.toString(),
            versionRules.minorUpdatePatterns.toString()
        )
    }

    private fun assertMinorIsCustom(versionRules: VersionRules) {
        assertEquals(
            customMinorRulesPatterns.toString(),
            versionRules.minorUpdatePatterns.toString()
        )
    }

    private fun assertMinorIsEmpty(versionRules: VersionRules) {
        assertTrue(versionRules.minorUpdatePatterns.isEmpty())
    }

    // ====================================================
    @Test
    fun testVersionRulesToString() {
        val string = defaultVersionRules.toString()
        assertTrue(string.contains("Conventional Commits config:"))
    }

    private fun isDefaultConfig(config: ConventionalCommitsVersionConfig) {
        assertNull(config.versionTag)
        assertEquals(0, config.majorRules.size)
        assertEquals(0, config.minorRules.size)

        val versionRules = VersionRules(config)

        assertTagPatternIsDefault(versionRules)
        assertMajorIsDefault(versionRules)
        assertMinorIsDefault(versionRules)

        assertEquals(ConventionalCommitsVersionConfig().toString(), config.toString())
    }

    @Test
    fun testParseNull() {
        // This should result in the default config
        isDefaultConfig(fromXml(null))
    }

    @Test
    fun testParseReallyEmpty() {
        // This should result in the default config
        isDefaultConfig(fromXml("   "))
    }

    @Test
    fun testParseEmptyValid() {
        // This should result in the default config
        isDefaultConfig(fromXml("""
            <projectVersionPolicyConfig>
            </projectVersionPolicyConfig>
            """))
    }

    @Test
    fun testParseValidTag() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customVersionTagXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertEquals(customVersionTagRegex, config.versionTag)
        assertEquals(0, config.majorRules.size)
        assertEquals(0, config.minorRules.size)

        val versionRules = VersionRules(config)
        assertTagPatternIsCustom(versionRules)
        assertMajorIsDefault(versionRules)
        assertMinorIsDefault(versionRules)
    }

    @Test
    fun testParseValidTagMinor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customVersionTagXML + """
            """.trimIndent() + customMinorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertEquals(customVersionTagRegex, config.versionTag)
        assertEquals(0, config.majorRules.size)
        assertEquals(1, config.minorRules.size)
        assertEquals(customMinorRulesRegex, config.minorRules[0])

        val versionRules = VersionRules(config)
        assertTagPatternIsCustom(versionRules)
        assertMajorIsEmpty(versionRules)
        assertMinorIsCustom(versionRules)
    }

    @Test
    fun testParseValidTagMajor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customVersionTagXML + """
            """.trimIndent() + customMajorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()


        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertEquals(customVersionTagRegex, config.versionTag)
        assertEquals(1, config.majorRules.size)
        assertEquals(customMajorRulesRegex, config.majorRules[0])
        assertEquals(0, config.minorRules.size)

        val versionRules = VersionRules(config)
        assertTagPatternIsCustom(versionRules)
        assertMajorIsCustom(versionRules)
        assertMinorIsEmpty(versionRules)
    }

    @Test
    fun testParseValidTagMinorMajor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customVersionTagXML + """
            """.trimIndent() + customMajorRulesXML + """
            """.trimIndent() + customMinorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertEquals(customVersionTagRegex, config.versionTag)
        assertEquals(1, config.majorRules.size)
        assertEquals(customMajorRulesRegex, config.majorRules[0])
        assertEquals(1, config.minorRules.size)
        assertEquals(customMinorRulesRegex, config.minorRules[0])

        val versionRules = VersionRules(config)
        assertTagPatternIsCustom(versionRules)
        assertMajorIsCustom(versionRules)
        assertMinorIsCustom(versionRules)
    }

    @Test
    fun testParseValidMinor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customMinorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertNull(config.versionTag)
        assertEquals(0, config.majorRules.size)
        assertEquals(1, config.minorRules.size)
        assertEquals(customMinorRulesRegex, config.minorRules[0])

        val versionRules = VersionRules(config)
        assertTagPatternIsDefault(versionRules)
        assertMajorIsEmpty(versionRules)
        assertMinorIsCustom(versionRules)
    }

    @Test
    fun testParseValidMajor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """.trimIndent() + customMajorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertNull(config.versionTag)
        assertEquals(1, config.majorRules.size)
        assertEquals(customMajorRulesRegex, config.majorRules[0])
        assertEquals(0, config.minorRules.size)

        val versionRules = VersionRules(config)
        assertTagPatternIsDefault(versionRules)
        assertMajorIsCustom(versionRules)
        assertMinorIsEmpty(versionRules)
    }

    @Test
    fun testParseValidMinorMajor() {
        val versionRulesConfig = """
            <projectVersionPolicyConfig>
            """ + customMajorRulesXML + """
            """ + customMinorRulesXML + """
            </projectVersionPolicyConfig>
            """.trimIndent()

        val config = fromXml(versionRulesConfig)
        LOG.info("Tested config: {}", config)
        assertNotNull(config)

        assertNull(config.versionTag)
        assertEquals(1, config.majorRules.size)
        assertEquals(customMajorRulesRegex, config.majorRules[0])
        assertEquals(1, config.minorRules.size)
        assertEquals(customMinorRulesRegex, config.minorRules[0])

        val versionRules = VersionRules(config)
        assertTagPatternIsDefault(versionRules)
        assertMajorIsCustom(versionRules)
        assertMinorIsCustom(versionRules)
    }

    @Test
    fun testParseBadXmlUnknownTags() {
        val versionRulesConfig = ("<Something><Unexpected></Unexpected></Something>")
        assertFailsWith<ConventionalCommitsConfigException>{ fromXml(versionRulesConfig) }
    }

    @Test
    fun testParseBadXmlUnbalanced() {
        val versionRulesConfig = ("""
            <projectVersionPolicyConfig>
            <versionTag>^The awesome ([0-9]+\.[0-9]+\.[0-9]+) release$
        """.trimIndent())

        assertFailsWith<ConventionalCommitsConfigException> { fromXml(versionRulesConfig) }
    }

    @Test
    fun testVersionRulesIgnoreNullTag() {
        val config = ConventionalCommitsVersionConfig(versionTag = null)
        val versionRules = VersionRules(config)
        assertEquals(
            defaultVersionRules.tagPattern.toString(),
            versionRules.tagPattern.toString()
        )
    }

    @Test
    fun testVersionRulesIgnoreBlankTag() {
        val config = ConventionalCommitsVersionConfig(versionTag = "       ")
        val versionRules = VersionRules(config)
        assertEquals(
            defaultVersionRules.tagPattern.toString(),
            versionRules.tagPattern.toString()
        )
    }
}
