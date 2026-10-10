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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlChildrenName
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.slf4j.LoggerFactory

@Serializable
@XmlSerialName("projectVersionPolicyConfig", namespace = "", prefix = "")
class ConventionalCommitsVersionConfig {
    // Only need for tests
    fun toXml(): String {
        return XML_MAPPER.encodeToString(this)
    }

    /**
     * The regex with exactly 1 capture group that extracts the version from the SCM tag.
     * Or null if no version tag was defined.
     */
    @XmlElement
    @XmlSerialName("versionTag", namespace = "", prefix = "")
    var versionTag: String? = null
        private set

    /*
    * The list of regexes that must be classified as "minor" version changes.
    */
    @XmlSerialName("minorRules", namespace = "", prefix = "")
    @XmlChildrenName("minorRule", namespace = "", prefix = "")
    var minorRules: MutableList<String> = mutableListOf()
        private set

    /*
    * The list of regexes that must be classified as "major" version changes.
    */
    @XmlSerialName("majorRules", namespace = "", prefix = "")
    @XmlChildrenName("majorRule", namespace = "", prefix = "")
    var majorRules: MutableList<String> = mutableListOf()
        private set

    // Used for testing only
    fun setVersionTag(newVersionTag: String?): ConventionalCommitsVersionConfig {
        this.versionTag = newVersionTag
        return this
    }

    // Used for testing only
    fun addMinorRule(newRule: String): ConventionalCommitsVersionConfig {
        minorRules.add(newRule)
        return this
    }

    // Used for testing only
    fun addMajorRule(newRule: String): ConventionalCommitsVersionConfig {
        majorRules.add(newRule)
        return this
    }

    override fun toString(): String {
        return "ConventionalCommitsVersionConfig {" +
                "versionTag='" + versionTag + '\'' +
                ", minorRules=" + minorRules +
                ", majorRules=" + majorRules +
                '}'
    }

    companion object {
        private val XML_MAPPER = XML.v1 {
            policy {
                verifyElementOrder = false
            }
        }

        @JvmStatic
        fun fromXml(configXml: String?): ConventionalCommitsVersionConfig {
            if (configXml.isNullOrBlank()) {
                return ConventionalCommitsVersionConfig() // No changes from the default
            }
            // NOTE: If the provided config still contains the property that should have been
            // interpolated there is no option to retrieve it. So we simply assume it is an empty string.
            val sanitizedXml = configXml.replace($$"${projectVersionPolicyConfig}", "")
            try {
                LoggerFactory
                    .getLogger(ConventionalCommitsVersionConfig::class.java)
                    .debug("ConventionalCommitsVersionConfig XML: \n{}", sanitizedXml)
                return XML_MAPPER.decodeFromString(sanitizedXml, null)
            } catch (e: nl.adaptivity.xmlutil.XmlException) {
                throw ConventionalCommitsConfigException("Unable to read the provided config", e)
            } catch (e: SerializationException) {
                throw ConventionalCommitsConfigException("Unable to read the provided config", e)
            }
        }
    }

}
