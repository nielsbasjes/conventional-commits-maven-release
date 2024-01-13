/*
 * Conventional Commits Version Policy
 * Copyright (C) 2022-2024 Niels Basjes
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
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "ProjectVersionPolicyConfig")
public class ConventionalCommitsVersionConfig {
    private static final XmlMapper XML_MAPPER = new XmlMapper();

    public static ConventionalCommitsVersionConfig fromXml(String configXml) {
        if (configXml == null || configXml.trim().isEmpty()) {
            return null;
        }
        // NOTE: If the provided config still contains the property that should have been
        // interpolated there is no option to retrieve it. So we simply assume it is an empty string.
        configXml = configXml.replace("${projectVersionPolicyConfig}", "");
        try {
            LoggerFactory
                .getLogger(ConventionalCommitsVersionConfig.class)
                .debug("ConventionalCommitsVersionConfig XML: \n{}", configXml);
            return XML_MAPPER.readValue(configXml, ConventionalCommitsVersionConfig.class);
        } catch (JsonProcessingException e) {
            throw new ConventionalCommitsConfigException("Unable to read the provided config", e);
        }
    }

    // Only need for tests
    String toXml() throws JsonProcessingException {
        return XML_MAPPER.writeValueAsString(this);
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

    public ConventionalCommitsVersionConfig addMinorRule(String newRule) {
        minorRules.add(newRule);
        return this;
    }

    public ConventionalCommitsVersionConfig addMajorRule(String newRule) {
        majorRules.add(newRule);
        return this;
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
