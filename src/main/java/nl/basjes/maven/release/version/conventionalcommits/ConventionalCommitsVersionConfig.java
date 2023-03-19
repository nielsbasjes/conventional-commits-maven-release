/*
 * Conventional Commits Version Policy
 * Copyright (C) 2022-2023 Niels Basjes
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
package nl.basjes.maven.release.version.conventionalcommits;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JacksonXmlRootElement(localName = "ProjectVersionPolicyConfig")
public class ConventionalCommitsVersionConfig {
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static ConventionalCommitsVersionConfig fromXml(String configXml) {
        if (configXml == null || configXml.trim().isEmpty()) {
            return null;
        }
        try {
            return XML_MAPPER.readValue(configXml, ConventionalCommitsVersionConfig.class);
        } catch (JsonProcessingException e) {
            throw new ConventionalCommitsConfigException("Unable to read the provided config", e);
        }
    }

    public String toXml() {
        try {
            return XML_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ConventionalCommitsConfigException("Unable to convert the config into XML", e);
        }
    }

    /**
     * The regex with exactly 1 capture group that extracts the version from the SCM tag.
     */
    private String versionTag;

    /*
     * The list of regexes that must be classified as "minor" version changes.
     */
    private final List<String> minorRules = new ArrayList<>();

    /*
     * The list of regexes that must be classified as "major" version changes.
     */
    private final List<String> majorRules = new ArrayList<>();

    public String getVersionTag() {
        return versionTag;
    }

    public List<String> getMinorRules() {
        return minorRules;
    }

    public List<String> getMajorRules() {
        return majorRules;
    }

    public ConventionalCommitsVersionConfig setVersionTag(String newVersionTag) {
        this.versionTag = newVersionTag;
        return this;
    }

    public ConventionalCommitsVersionConfig clearMinorRules() {
        minorRules.clear();
        return this;
    }

    public ConventionalCommitsVersionConfig clearMajorRules() {
        majorRules.clear();
        return this;
    }

    public ConventionalCommitsVersionConfig addMinorRule(String newRule) {
        minorRules.add(newRule);
        return this;
    }

    public ConventionalCommitsVersionConfig addMajorRule(String newRule) {
        majorRules.add(newRule);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConventionalCommitsVersionConfig)) {
            return false;
        }
        ConventionalCommitsVersionConfig that = (ConventionalCommitsVersionConfig) o;
        return Objects.equals(versionTag, that.versionTag)
            && Objects.equals(minorRules,  that.minorRules)
            && Objects.equals(majorRules,  that.majorRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionTag, minorRules, majorRules);
    }

    @Override
    public String toString() {
        return "ConventionalCommitsVersionConfig {" +
            "versionTag='" + versionTag + '\'' +
            ", minorRules=" + minorRules +
            ", majorRules=" + majorRules +
            '}';
    }
}
