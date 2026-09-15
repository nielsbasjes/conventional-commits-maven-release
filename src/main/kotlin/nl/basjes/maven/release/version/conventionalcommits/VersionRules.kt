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
import java.util.regex.Pattern

/**
 * The set of rules that determine from the commit history what the next version should be.
 */
class VersionRules(config: ConventionalCommitsVersionConfig?) {
    val tagPattern: Pattern
    val majorUpdatePatterns: MutableList<Pattern> = mutableListOf()
    val minorUpdatePatterns: MutableList<Pattern> = mutableListOf()

    init {
        val patternFlags = Pattern.MULTILINE or Pattern.DOTALL or Pattern.UNIX_LINES

        // The default assumes then entire tag is what we need
        var tagRegex = "^(\\d+\\.\\d+\\.\\d+)$"

        // The default rules following https://www.conventionalcommits.org/en/v1.0.0/
        majorUpdatePatterns.add(Pattern.compile("^[a-zA-Z]+(?:\\([a-zA-Z\\d_-]+\\))?!: .*$", patternFlags))
        majorUpdatePatterns.add(Pattern.compile("^BREAKING CHANGE:.*$", patternFlags))
        minorUpdatePatterns.add(Pattern.compile("^feat(?:\\([a-zA-Z\\d_-]+\\))?: .*$", patternFlags))

        if (config != null) {
            val semverConfigVersionTag = config.versionTag
            if (!semverConfigVersionTag.isNullOrBlank()) {
                tagRegex = semverConfigVersionTag
            }

            if (config.majorRules.isNotEmpty() || config.minorRules.isNotEmpty()) {
                majorUpdatePatterns.clear()
                for (majorRule in config.majorRules) {
                    majorUpdatePatterns.add(Pattern.compile(majorRule, patternFlags))
                }
                minorUpdatePatterns.clear()
                for (minorRule in config.minorRules) {
                    minorUpdatePatterns.add(Pattern.compile(minorRule, patternFlags))
                }
            }
        }
        tagPattern = Pattern.compile(tagRegex, Pattern.MULTILINE)
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

    private fun matchesAny(patterns: MutableList<Pattern>, input: String): Boolean {
        for (pattern in patterns) {
            val matcher = pattern.matcher(input)
            if (matcher.find()) {
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
