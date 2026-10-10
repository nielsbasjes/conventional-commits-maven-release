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

import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmProvider
import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmRepository
import org.apache.maven.scm.repository.ScmRepositoryException
import org.apache.maven.shared.release.policy.PolicyException
import org.apache.maven.shared.release.policy.version.VersionPolicy
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest
import org.apache.maven.shared.release.versions.VersionParseException
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

val DEFAULT_VERSION_RULES: VersionRules = VersionRules()

fun assertNextVersion(versionRules: VersionRules, input: String, step: VersionStep) {
    when (step) {
        VersionStep.MAJOR -> {
            assertTrue(versionRules.isMajorUpdate(input))
        }
        VersionStep.MINOR -> {
            assertFalse(versionRules.isMajorUpdate(input))
            assertTrue(versionRules.isMinorUpdate(input))
        }

        VersionStep.PATCH -> {
            assertFalse(versionRules.isMajorUpdate(input))
            assertFalse(versionRules.isMinorUpdate(input))
        }

    }
}


@Throws(VersionParseException::class, PolicyException::class, IOException::class, ScmRepositoryException::class)
fun verifyNextVersion(
    configXml: String,
    currentPomVersion: String,
    expectedReleaseVersion: String,
    expectedDevelopmentVersion: String,
    comment: String,
    vararg tags: String,
) {
    verifyNextVersion(
        configXml,
        currentPomVersion,
        listOf(comment),
        listOf(*tags),
        expectedReleaseVersion,
        expectedDevelopmentVersion
    )
}

@Throws(VersionParseException::class, PolicyException::class, IOException::class, ScmRepositoryException::class)
fun verifyNextVersion(
    currentPomVersion: String,
    comments: List<String>,
    tag: String,
    expectedReleaseVersion: String,
) {
    verifyNextVersion(
        "",  // Default config
        currentPomVersion,
        comments,
        listOf(tag),
        expectedReleaseVersion,
        null
    )
}

@Throws(VersionParseException::class, PolicyException::class, IOException::class, ScmRepositoryException::class)
fun verifyNextVersion(
    configXml: String = "",
    currentPomVersion: String? = null,
    comments: List<String> = listOf(),
    tags: List<String> = listOf(),
    expectedReleaseVersion: String,
    expectedDevelopmentVersion: String? = null,
) {
    val request = VersionPolicyRequest()
    request.setVersion(currentPomVersion)

    request.workingDirectory = "/tmp"

    val scmProvider = MockScmProvider(comments, tags)
    request.scmProvider = scmProvider
    request.scmRepository = MockScmRepository(scmProvider)

    request.config = configXml

    val versionPolicy: VersionPolicy = ConventionalCommitsVersionPolicy()

    val suggestedVersion = versionPolicy.getReleaseVersion(request).version
    assertEquals(expectedReleaseVersion, suggestedVersion)

    if (expectedDevelopmentVersion != null) {
        request.version = suggestedVersion
        val suggestedDevelopmentVersion = versionPolicy.getDevelopmentVersion(request).version
        assertEquals(expectedDevelopmentVersion, suggestedDevelopmentVersion)
    }
}

@Throws(PolicyException::class, IOException::class, ScmRepositoryException::class)
fun verifyNextVersionMustFail(
    configXml: String?,
    pomVersion: String?,
    comments: String,
    vararg tags: String,
) {
    try {
        verifyNextVersion(
            configXml ?: "",
            pomVersion,
            listOf(comments),
            listOf(*tags),
            "ignore",
            "ignore"
        )
    } catch (_: VersionParseException) {
        // Success !
        return
    }
    fail("Should have failed")
}
