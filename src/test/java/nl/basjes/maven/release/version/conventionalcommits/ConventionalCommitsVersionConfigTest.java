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

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConventionalCommitsVersionConfigTest {

    String versionRulesConfig = ""
        + "<projectVersionPolicyConfig>"
        + "  <minorRules>"
        + "    <minorRule>Minor One</minorRule>"
        + "    <minorRule>Minor Two</minorRule>"
        + "    <minorRule>Minor Three</minorRule>"
        + "  </minorRules>"
        + "  <majorRules>"
        + "    <majorRule>Major One</majorRule>"
        + "    <majorRule>Major Two</majorRule>"
        + "    <majorRule>Major Three</majorRule>"
        + "  </majorRules>"
        + "  <versionTag>My Version Tag</versionTag>"
        + "</projectVersionPolicyConfig>";

    @Test
    void readXMLTest() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        assertEquals("My Version Tag", config.getVersionTag());
        assertEquals(Arrays.asList("Minor One", "Minor Two", "Minor Three"), config.getMinorRules());
        assertEquals(Arrays.asList("Major One", "Major Two", "Major Three"), config.getMajorRules());
    }

    @Test
    void createAndSerdeLoopTest() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        ConventionalCommitsVersionConfig config1 = new ConventionalCommitsVersionConfig();

        config1.setVersionTag("My Version Tag")
            .addMinorRule("Minor One")
            .addMinorRule("Minor Two")
            .addMinorRule("Minor Three")
            .addMajorRule("Major One")
            .addMajorRule("Major Two")
            .addMajorRule("Major Three");

        assertEquals(config.toString(), config1.toString());

        String configXml = config1.toXml();

        ConventionalCommitsVersionConfig config2 = ConventionalCommitsVersionConfig.fromXml(configXml);

        assertEquals(config1.toString(), config2.toString());
    }
}
