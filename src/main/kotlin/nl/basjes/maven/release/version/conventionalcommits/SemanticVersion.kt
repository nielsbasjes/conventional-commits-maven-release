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

import kotlinx.serialization.Serializable

@Serializable
class SemanticVersion internal constructor(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null,
    val buildMetadata: String? = null
) : Comparable<SemanticVersion> {
    fun isValid() =
        (major >= 0 && minor >= 0 && patch >= 0)

    /**
     * Implements full SemVer 2.0.0 precedence rules for sorting and comparisons.
     */
    override fun compareTo(other: SemanticVersion): Int {
        // Compare major.minor.patch
        major.compareTo(other.major).takeIf { it != 0 }?.let { return it }
        minor.compareTo(other.minor).takeIf { it != 0 }?.let { return it }
        patch.compareTo(other.patch).takeIf { it != 0 }?.let { return it }

        // Pre-release versions have lower precedence than normal versions
        if (this.preRelease == null){
            return if (other.preRelease == null) 0 else 1
        }
        if (other.preRelease == null) return -1

        // Both have a prerelease specified
        val preReleaseParts1 = this.preRelease.split(".")
        val preReleaseParts2 = other.preRelease.split(".")
        val minLength = minOf(preReleaseParts1.size, preReleaseParts2.size)

        for (i in 0 until minLength) {
            val preReleasePart1 = preReleaseParts1[i]
            val preReleasePart2 = preReleaseParts2[i]

            if (preReleasePart1 != preReleasePart2) {
                val p1Int = preReleasePart1.toIntOrNull()
                val p2Int = preReleasePart2.toIntOrNull()

                return when {
                    // Numeric identifiers always have lower precedence than non-numeric identifiers
                    p1Int != null && p2Int != null -> p1Int.compareTo(p2Int)
                    p1Int != null -> -1
                    p2Int != null -> 1
                    else -> preReleasePart1.compareTo(preReleasePart2) // Identifiers with letters or hyphens are compared lexically
                }
            }
        }

        // If all identical up to the shorter length, the longer list has higher precedence
        return preReleaseParts1.size.compareTo(preReleaseParts2.size)
    }

    override fun toString() =
        "$major.$minor.$patch" +
        (if (preRelease    != null) "-$preRelease"      else "") +
        (if (buildMetadata != null) "+$buildMetadata"   else "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SemanticVersion
        if (major         != other.major         ) return false
        if (minor         != other.minor         ) return false
        if (patch         != other.patch         ) return false
        if (preRelease    != other.preRelease    ) return false
        if (buildMetadata != other.buildMetadata ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + preRelease.hashCode()
        result = 31 * result + buildMetadata.hashCode()
        return result
    }

}

// Official SemVer 2.0.0 regex pattern
// https://semver.org/spec/v2.0.0.html#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
private val SEMVER_REGEX = Regex(
    """^(0|[1-9]\d*)(?:\.(0|[1-9]\d*)(?:\.(0|[1-9]\d*))?)?(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?$"""
)

class SemanticVersionParseException(msg: String): IllegalArgumentException(msg)

fun String.toVersion(): SemanticVersion {
    val matchResult = SEMVER_REGEX.matchEntire(this.trim())
        ?: throw SemanticVersionParseException("Invalid SemanticVersion format: '$this'")
    val (major, minor, patch, preRelease, buildMetadata) = matchResult.destructured
    return SemanticVersion(
        major         = major         .takeIf { it.isNotBlank() }?.toInt() ?: 0 ,
        minor         = minor         .takeIf { it.isNotBlank() }?.toInt() ?: 0 ,
        patch         = patch         .takeIf { it.isNotBlank() }?.toInt() ?: 0 ,
        preRelease    = preRelease    .takeIf { it.isNotBlank() },
        buildMetadata = buildMetadata .takeIf { it.isNotBlank() }
    )
}

fun SemanticVersion.next(step: VersionStep) =
    when (step) {
        VersionStep.MAJOR -> this.nextMajor()
        VersionStep.MINOR -> this.nextMinor()
        VersionStep.PATCH -> this.nextPatch()
    }

fun SemanticVersion.nextMajor() = SemanticVersion(major+1, 0,       0)
fun SemanticVersion.nextMinor() = SemanticVersion(major,   minor+1, 0)
fun SemanticVersion.nextPatch() = SemanticVersion(major,   minor,   patch+1)

fun SemanticVersion.toReleaseVersion() = SemanticVersion(major, minor, patch)
fun SemanticVersion.toSnapshot() = SemanticVersion(major,   minor,   patch, "SNAPSHOT")

