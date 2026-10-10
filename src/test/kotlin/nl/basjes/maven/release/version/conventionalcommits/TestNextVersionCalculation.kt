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

import org.apache.maven.scm.repository.ScmRepositoryException
import org.apache.maven.shared.release.policy.PolicyException
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest
import org.apache.maven.shared.release.versions.VersionParseException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestNextVersionCalculation {
    @Test
    fun testMajorMinorPatchDetection() {
        val rules: VersionRules = DEFAULT_VERSION_RULES
        // Major
        assertNextVersion(rules, "feat(core)!: New feature.", VersionStep.MAJOR)
        assertNextVersion(rules, "feat!: New feature.", VersionStep.MAJOR)
        assertNextVersion(rules, "feat(core): Foo.\n\nBREAKING CHANGE: New feature.\n", VersionStep.MAJOR)
        assertNextVersion(rules, "feat: Foo.\n\nBREAKING CHANGE: New feature.\n", VersionStep.MAJOR)

        // Minor
        assertNextVersion(rules, "feat(core): New feature.", VersionStep.MINOR)
        assertNextVersion(rules, "feat: New feature.", VersionStep.MINOR)

        // Patch
        assertNextVersion(rules, "Does not match any pattern.", VersionStep.PATCH)
    }

    @Test
    @Throws(Exception::class)
    fun testConvertToSnapshot() {
        val suggestedVersion: String? =
            versionPolicy.getDevelopmentVersion(VersionPolicyRequest().setVersion("1.0.0"))
                .version

        assertEquals("1.0.1-SNAPSHOT", suggestedVersion)
    }

    @Test
    fun testConvertToSnapshotBadVersion() {
        assertFailsWith<VersionParseException> {
            versionPolicy.getDevelopmentVersion(VersionPolicyRequest().setVersion("Really Bad"))
        }
    }

    @Test
    @Throws(VersionParseException::class, PolicyException::class, IOException::class, ScmRepositoryException::class)
    fun testDefaultVersionRules() {
        verifyNextVersion("1.2.3-SNAPSHOT", EMPTY,          "",      "1.2.3") // No Tag - No CC Comments
        verifyNextVersion("1.2.3-SNAPSHOT", PATCH_MESSAGES, "",      "1.2.3") // No Tag - Patch Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MINOR_MESSAGES, "",      "1.3.0") // No Tag - Minor Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MAJOR_MESSAGES, "",      "2.0.0") // No Tag - Major Comments
        verifyNextVersion("1.2.3-SNAPSHOT", EMPTY,          "2.3.4", "2.3.5") // Tag - No CC Comments
        verifyNextVersion("1.2.3-SNAPSHOT", PATCH_MESSAGES, "2.3.4", "2.3.5") // Tag - Patch Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MINOR_MESSAGES, "2.3.4", "2.4.0") // Tag - Minor Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MAJOR_MESSAGES, "2.3.4", "3.0.0") // Tag - Major Comments
    }

    @Test
    fun testInvalidPomVersion() {
        assertFailsWith<VersionParseException> {
            verifyNextVersion("Bad", mutableListOf("Nothing"), "Unmatched value", "Should fail")
        }
    }

    @Test
    fun testInvalidTagVersion() {
        assertFailsWith<VersionParseException> {
            verifyNextVersion(
                """
                    <projectVersionPolicyConfig>
                      <versionTag>^(Bad)$</versionTag>
                    </projectVersionPolicyConfig>
                """.trimIndent(),
                "1.2.3",
                mutableListOf("Nothing"),
                mutableListOf("Bad"),
                "Should fail",
                null
            )
        }
    }

    companion object {
        private var versionPolicy: ConventionalCommitsVersionPolicy = ConventionalCommitsVersionPolicy()

        private const val PATCH_1 = "Quick patch"
        private const val PATCH_2 = "fix(core): Another fix."
        private const val MINOR_1 = "feat(core): New thingy."
        private const val MAJOR_1 = "fix(core)!: Breaking improvement"

        private val EMPTY = mutableListOf<String>()
        private val MAJOR_MESSAGES: List<String> = listOf(PATCH_1, MINOR_1, MAJOR_1, PATCH_2)
        private val MINOR_MESSAGES: List<String> = listOf(PATCH_1, MINOR_1, PATCH_2)
        private val PATCH_MESSAGES: List<String> = listOf(PATCH_1, PATCH_2)
    }
}
