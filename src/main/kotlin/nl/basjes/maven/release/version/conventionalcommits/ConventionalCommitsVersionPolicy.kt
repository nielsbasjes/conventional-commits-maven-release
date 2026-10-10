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
import nl.basjes.maven.release.version.conventionalcommits.VersionStep.PATCH
import org.apache.maven.scm.ScmException
import org.apache.maven.shared.release.policy.PolicyException
import org.apache.maven.shared.release.policy.version.VersionPolicy
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest
import org.apache.maven.shared.release.policy.version.VersionPolicyResult
import org.apache.maven.shared.release.versions.VersionParseException
import org.eclipse.sisu.Description
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Uses SemVer combined with the tags and commit messages to increase the version.
 */
@Singleton
@Named("ConventionalCommitsVersionPolicy")
@Description("A VersionPolicy following the SemVer rules and looks at the commit messages following the Conventional Commits convention.")
class ConventionalCommitsVersionPolicy : VersionPolicy {
    @Throws(VersionParseException::class, PolicyException::class)
    override fun getReleaseVersion(request: VersionPolicyRequest): VersionPolicyResult {
        val versionConfig = fromXml(request.config)
        val versionRules = VersionRules(versionConfig)
        val commitHistory: CommitHistory
        try {
            commitHistory = CommitHistory(request, versionRules)
        } catch (e: ScmException) {
            throw PolicyException("Something went wrong fetching the commit history", e)
        }

        var usingTag = false

        var versionString = request.version // The current version in the pom
        var version: SemanticVersion

        LOG.debug("--------------------------------------------------------")
        LOG.debug("Determining next ReleaseVersion")
        LOG.debug("VersionRules: \n{}", versionRules)
        LOG.debug("Pom version             : {}", versionString)

        LOG.debug("Commit History          : \n{}", commitHistory)

        val maxElementSinceLastVersionTag = versionRules.getMaxElementSinceLastVersionTag(commitHistory)

        val latestVersionTag = commitHistory.lastVersionTag
        if (latestVersionTag != null) {
            // Use the latest tag we have
            versionString = latestVersionTag
            usingTag = true
            LOG.debug("Version from tags       : {}", versionString)
        } else {
            LOG.debug("Version from tags       : NOT FOUND")
        }

        LOG.debug("Step from commits       : {}", maxElementSinceLastVersionTag.name)

        try {
            version = versionString.toVersion()
        } catch (e: IllegalArgumentException) {
            throw VersionParseException(e.message, e)
        }

        LOG.debug("Current version         : {}", version)

        // If we have a version from the tag we use that + the calculated update.
        // If only have the version from the current pom version with -SNAPSHOT removed IF it is only a PATCH.
        if (!(latestVersionTag == null && maxElementSinceLastVersionTag == PATCH)) {
            version = version.next(maxElementSinceLastVersionTag)
        }

        val releaseVersion = version.toReleaseVersion()
        LOG.debug("Next version            : {}", releaseVersion)
        LOG.debug("--------------------------------------------------------")


        LOG.info("Version and SCM analysis result:")
        if (usingTag) {
            LOG.info("- Starting from SCM tag with version {}", versionString)
        } else {
            LOG.info("- Starting from project.version {} (because we did not find any valid SCM tags)", versionString)
        }

        LOG.info(
            "- Doing a {} version increase{}.",
            maxElementSinceLastVersionTag,
            if (maxElementSinceLastVersionTag == PATCH)
                " (because we did not find any minor/major commit messages)"
            else
                ""
        )

        LOG.info("- Next release version : {}", releaseVersion)

        val result = VersionPolicyResult()
        result.version = releaseVersion.toString()
        return result
    }

    @Throws(VersionParseException::class)
    override fun getDevelopmentVersion(request: VersionPolicyRequest): VersionPolicyResult {
        var version: SemanticVersion
        try {
            version = request.version.toVersion()
        } catch (e: IllegalArgumentException) {
            throw VersionParseException(e.message)
        }

        version = version.nextPatch()
        val result = VersionPolicyResult()
        result.version = version.toSnapshot().toString()
        return result
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ConventionalCommitsVersionPolicy::class.java)
    }
}
