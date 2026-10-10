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

import nl.basjes.maven.release.version.conventionalcommits.VersionStep.MAJOR
import nl.basjes.maven.release.version.conventionalcommits.VersionStep.MINOR
import nl.basjes.maven.release.version.conventionalcommits.VersionStep.PATCH
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.MULTILINE
import kotlin.text.RegexOption.UNIX_LINES

/**
 * The set of rules that determine from the commit history what the next version should be.
 */
class VersionRules(config: ConventionalCommitsVersionConfig = ConventionalCommitsVersionConfig()) {
    val tagPattern: Regex
    val majorUpdatePatterns: MutableList<Regex> = mutableListOf()
    val minorUpdatePatterns: MutableList<Regex> = mutableListOf()

    init {
        val regexFlags = setOf(MULTILINE, DOT_MATCHES_ALL, UNIX_LINES)

        val semverConfigVersionTag = config.versionTag
        val tagRegex = if (semverConfigVersionTag.isNullOrBlank()) {
            // The default assumes then entire tag is what we need
            // This is the SemVer 2.0.0 regex with only a single capture group for the entire thing.
            """^((?:0|[1-9]\d*)(?:\.(?:0|[1-9]\d*)(?:\.(?:0|[1-9]\d*))?)?(?:-(?:(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+(?:[0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?)$"""
        } else {
            semverConfigVersionTag
        }
        tagPattern = Regex(tagRegex)

        if (config.majorRules.isEmpty() && config.minorRules.isEmpty()) {
            // The default rules following https://www.conventionalcommits.org/en/v1.0.0/
            majorUpdatePatterns.add(Regex("^[a-zA-Z]+(?:\\([a-zA-Z\\d_-]+\\))?!: .*$", regexFlags))
            majorUpdatePatterns.add(Regex("^BREAKING CHANGE:.*$", regexFlags))
            minorUpdatePatterns.add(Regex("^feat(?:\\([a-zA-Z\\d_-]+\\))?: .*$", regexFlags))
        } else {
            for (majorRule in config.majorRules) {
                majorUpdatePatterns.add(Regex(majorRule, regexFlags))
            }
            for (minorRule in config.minorRules) {
                minorUpdatePatterns.add(Regex(minorRule, regexFlags))
            }
        }
    }

    /**
     * Extract th version string or null if it did not match the configured extraction expression.
     */
    fun extractVersionString(tag: String): String? {
        val matchResult = tagPattern.find(tag)
        return matchResult?.groupValues?.get(1)
    }

    fun getMaxElementSinceLastVersionTag(commitHistory: CommitHistory): VersionStep {
        var maxElement = PATCH
        for (change in commitHistory.changes) {
            if (isMajorUpdate(change.comment)) {
                // This is the highest possible: Immediately done
                return MAJOR
            } else if (isMinorUpdate(change.comment)) {
                // Have to wait, there may be another MAJOR one.
                maxElement = MINOR
            }
        }
        return maxElement
    }

    fun isMajorUpdate(input: String): Boolean {
        return matchesAny(majorUpdatePatterns, input)
    }

    fun isMinorUpdate(input: String): Boolean {
        return matchesAny(minorUpdatePatterns, input)
    }

    private fun matchesAny(patterns: MutableList<Regex>, input: String): Boolean {
        for (pattern in patterns) {
            if (pattern.find(input) != null) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("Conventional Commits config:\n")
        result.append("  VersionTag:\n")
        result.append("    >>>").append(tagPattern).append("<<<\n")
        result.append("  Major Rules:\n")
        for (majorUpdatePattern in majorUpdatePatterns) {
            result.append("    >>>").append(majorUpdatePattern).append("<<<\n")
        }
        result.append("  Minor Rules:\n")
        for (minorUpdatePattern in minorUpdatePatterns) {
            result.append("    >>>").append(minorUpdatePattern).append("<<<\n")
        }
        return result.toString()
    }
}
