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

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TestSemanticVersion {

    private val log = LoggerFactory.getLogger(TestSemanticVersion::class.java)

    private fun assertVersion(
        input: String,
        major: Int,
        minor: Int,
        patch: Int,
        preRelease: String? = null,
        buildMetadata: String? = null,
    ) = assertSoftly(input.toVersion()) {
        major              shouldBe major
        minor              shouldBe minor
        patch              shouldBe patch
        preRelease         shouldBe preRelease
        buildMetadata      shouldBe buildMetadata
    }

    @Test
    fun checkSemanticVersionRelease() {
        assertVersion("1",              major = 1, minor = 0, patch = 0)
        assertVersion("1.2",            major = 1, minor = 2, patch = 0)
        assertVersion("1.2.3",          major = 1, minor = 2, patch = 3)
    }

    @Test
    fun checkSemanticVersionSnapshot() {
        assertVersion("0-SNAPSHOT",     major = 0, minor = 0, patch = 0, preRelease = "SNAPSHOT")
        assertVersion("0.2-SNAPSHOT",   major = 0, minor = 2, patch = 0, preRelease = "SNAPSHOT")
        assertVersion("0.2.3-SNAPSHOT", major = 0, minor = 2, patch = 3, preRelease = "SNAPSHOT")
    }

    @Test
    fun checkSemanticVersionReleaseCandidate() {
        assertVersion("1-RC4",          major = 1, minor = 0, patch = 0, preRelease = "RC4")
        assertVersion("1.2-RC4",        major = 1, minor = 2, patch = 0, preRelease = "RC4")
        assertVersion("1.2.3-RC4",      major = 1, minor = 2, patch = 3, preRelease = "RC4")
    }

    @Test
    fun checkSortingByMajorMinorPatch() {
        val versions = listOf(
            "2.0.0",
            "1.0.0",
            "1.2.0",
            "1.1.0",
            "1.1.5",
            "1.1.3",
        ).map { it.toVersion() }
        val sorted = versions.sorted().map { it.toString() }
        sorted shouldBe listOf(
            "1.0.0",
            "1.1.0",
            "1.1.3",
            "1.1.5",
            "1.2.0",
            "2.0.0",
        )
    }

    @Test
    fun checkSortingWithQualifiers() {
        // For same version, SNAPSHOT < RC < stable
        val versions = listOf(
            "1.0.0-RC1",
            "1.0.0",
            "1.0.0-SNAPSHOT",
        ).map { it.toVersion() }
        val sorted = versions.sorted().map { it.toString() }
        sorted shouldBe listOf(
            "1.0.0-RC1",
            "1.0.0-SNAPSHOT",
            "1.0.0",
        )
    }

    @Test
    fun checkSortingReleaseCandidates() {
        val versions = listOf(
            "1.0.0-RC3",
            "1.0.0-RC1",
            "1.0.0-RC2",
        ).map { it.toVersion() }
        val sorted = versions.sorted().map { it.toString() }
        sorted shouldBe listOf(
            "1.0.0-RC1",
            "1.0.0-RC2",
            "1.0.0-RC3",
        )
    }

    @Test
    fun checkComprehensiveSorting() {
        val unsorted = listOf(
            "2.0.0",
            "1.0.0",
            "1.0.0-RC1",
            "1.0.0-RC2",
            "1.0.0-SNAPSHOT",
            "1.1.0",
            "1.1.0-SNAPSHOT",
            "0.9.0",
        ).map { it.toVersion() }

        val sorted = unsorted.sorted().map { it.toString() }

        sorted shouldBe listOf(
            "0.9.0",
            "1.0.0-RC1",
            "1.0.0-RC2",
            "1.0.0-SNAPSHOT",
            "1.0.0",
            "1.1.0-SNAPSHOT",
            "1.1.0",
            "2.0.0",
        )
    }

    private fun assertValid(version: SemanticVersion) {
        assertTrue(version.isValid(), "Version $version should be valid but isn't (isValid=${version.isValid()})")
    }

    private fun assertInvalid(version: SemanticVersion) {
        if (version.isValid()) {
            fail("Version $version should be valid but isn't (isValid=${version.isValid()})")
        } else {
            log.info("INvalid version: $version")
        }
    }

    @Test
    fun checkIsValid() {
        assertValid(SemanticVersion(major = 0, minor = 0, patch = 0))
        assertValid(SemanticVersion(major = 0, minor = 0, patch = 0, preRelease = "SNAPSHOT"))
        assertValid(SemanticVersion(major = 0, minor = 0, patch = 0, preRelease = "RC1"))
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3))
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "SNAPSHOT"))
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "RC1"))

        // Negative versions
        assertInvalid(SemanticVersion(major = -1, minor = 2, patch = 3))
        assertInvalid(SemanticVersion(major = -1, minor = 2, patch = 3, preRelease = "SNAPSHOT"))
        assertInvalid(SemanticVersion(major = -1, minor = 2, patch = 3, preRelease = "RC1"))
        assertInvalid(SemanticVersion(major = 1, minor = -2, patch = 3))
        assertInvalid(SemanticVersion(major = 1, minor = -2, patch = 3, preRelease = "SNAPSHOT"))
        assertInvalid(SemanticVersion(major = 1, minor = -2, patch = 3, preRelease = "RC1"))
        assertInvalid(SemanticVersion(major = 1, minor = 2, patch = -3))
        assertInvalid(SemanticVersion(major = 1, minor = 2, patch = -3, preRelease = "SNAPSHOT"))
        assertInvalid(SemanticVersion(major = 1, minor = 2, patch = -3, preRelease = "RC1"))

        // Good combinations of shapshot and release candidate
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "SNAPSHOT"))
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3))
        assertValid(SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "RC1"))
    }

    @Test
    fun checkEquals() {
        assertEquals("1.2.3".toVersion(), SemanticVersion(1, 2, 3))
        assertEquals("1.2.3-SNAPSHOT".toVersion(), SemanticVersion(1, 2, 3, "SNAPSHOT"))
        assertEquals("1.2.3-RC1".toVersion(),
            SemanticVersion(major = 1, minor = 2, patch = 3, preRelease = "RC1"),
        )
    }

    @Test
    fun verifySemVer2OrderingExample() {

        // The example shown on https://semver.org/spec/v2.0.0.html about the ordering of versions
        val expected = listOf(
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-alpha.beta",
            "1.0.0-beta",
            "1.0.0-beta.2",
            "1.0.0-beta.11",
            "1.0.0-rc.1",
            "1.0.0",
        )

        val semanticVersions = expected.map { version -> version.toVersion() }.toList()

        val shuffledVersions = mutableListOf<SemanticVersion>()
        shuffledVersions.addAll(semanticVersions)
        while (semanticVersions == shuffledVersions) {
            shuffledVersions.clear()
            semanticVersions.shuffled().forEach { shuffledVersions.add(it) }
        }

        assertNotEquals(semanticVersions, shuffledVersions)
        assertEquals(expected, shuffledVersions.sorted().map { it.toString() }.toList())
    }

    @Test
    fun verifyAllSpecificationExamples() {
        // In the Semver 2.0.0 specification some examples are shown.
        // Here we verify that parsing and serializing back to a String is around trip
        listOf(
            "1.0.0",
            "1.0.0-0.3.7",
            "1.0.0+20130313144700",
            "1.0.0+21AF26D3----117B344092BD",
            "1.0.0-alpha",
            "1.0.0-alpha+001",
            "1.0.0-alpha.1",
            "1.0.0-alpha.beta",
            "1.0.0-beta",
            "1.0.0-beta.11",
            "1.0.0-beta.2",
            "1.0.0-beta+exp.sha.5114f85",
            "1.0.0-rc.1",
            "1.0.0-x.7.z.92",
            "1.0.0-x-y-z.--",
            "1.9.0",
            "1.10.0",
            "1.11.0",
            "2.0.0",
            "2.1.0",
            "2.1.1",
        ). forEach { input ->
            val semanticVersion = input.toVersion()
            assertValid(semanticVersion)
            assertEquals(input, semanticVersion.toString())
        }
    }

    @Test
    fun verifySemVer2AllOrdering() {
        val expected = listOf(
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-alpha.beta",
            "1.0.0",
            "1.1.0-alpha",
            "1.1.0-alpha.1",
            "1.1.0-alpha.beta",
            "1.1.0",
            "1.1.1-alpha",
            "1.1.1-alpha.1",
            "1.1.1-alpha.beta",
            "1.1.1",
            "2.0.0-alpha",
            "2.0.0-alpha.1",
            "2.0.0-alpha.beta",
            "2.0.0",
            "2.1.0-alpha",
            "2.1.0-alpha.1",
            "2.1.0-alpha.beta",
            "2.1.0",
            "2.1.1-alpha",
            "2.1.1-alpha.1",
            "2.1.1-alpha.beta",
            "2.1.1",
        )
        val semanticVersions = expected.map { version -> version.toVersion() }.toList()

        val shuffledVersions = mutableListOf<SemanticVersion>()
        shuffledVersions.addAll(semanticVersions)
        while (semanticVersions == shuffledVersions) {
            shuffledVersions.clear()
            semanticVersions.shuffled().forEach { shuffledVersions.add(it) }
        }
        assertNotEquals(semanticVersions, shuffledVersions)

        // Now sort them and we should have the original list again
        assertEquals(expected, shuffledVersions.sorted().map { it.toString() }.toList())
    }


    @Test
    fun verifySemVer2Equals() {
        val expected = listOf(
            "1.0.0-alpha.beta",
            "1.0.0-alpha",
            "1.0.0",
        )
        val semanticVersions = expected.map { version -> version.toVersion() }.toList()

        assertFalse(semanticVersions.first().equals("A String is not a version instance."))
        assertNotEquals(semanticVersions.first().hashCode(), semanticVersions.last().hashCode())

        semanticVersions.forEach { version ->
            assertTrue(version.equals(version))
        }

        val semanticVersionsPairs = semanticVersions.flatMap { left ->
            semanticVersions
                .filter { left != it }
                .map { right -> left to right }
        }

        semanticVersionsPairs.forEach { pair ->
            assertNotEquals(pair.first, pair.second, "Should be different: ${pair.first} -> ${pair.second}")
        }
    }

}
