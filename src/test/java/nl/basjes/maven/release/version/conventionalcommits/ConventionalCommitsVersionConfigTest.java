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
package nl.basjes.maven.release.version.conventionalcommits;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConventionalCommitsVersionConfigTest {

    String versionRulesConfig =
        """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>
            """.stripIndent();

    @Test
    void createAndSerdeLoopTest() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        assertNotNull(config);

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
        assertNotNull(config2);

        assertEquals(config1.toString(), config2.toString());
    }

    @Test
    void readXMLTestVersionMajorMinor() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>
            """
        );
        assertNotNull(config);
        assertEquals("My Version Tag", config.getVersionTag());
        assertEquals(List.of("Minor One", "Minor Two", "Minor Three"), config.getMinorRules());
        assertEquals(List.of("Major One", "Major Two", "Major Three"), config.getMajorRules());
    }

    @Test
    void readXMLTestVersionMajorEmptyMinorEmpty() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
              </minorRules>
              <majorRules>
              </majorRules>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>
            """
        );
        assertNotNull(config);
        assertEquals("My Version Tag", config.getVersionTag());
        assertEquals(List.of(), config.getMinorRules());
        assertEquals(List.of(), config.getMajorRules());
    }

    @Test
    void readXMLTestVersion() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(
            """
            <projectVersionPolicyConfig>
              <versionTag>My Version Tag</versionTag>
            </projectVersionPolicyConfig>
            """
        );
        assertNotNull(config);
        assertEquals("My Version Tag", config.getVersionTag());
        assertEquals(List.of(), config.getMinorRules());
        assertEquals(List.of(), config.getMajorRules());
    }


    @Test
    void readXMLTestMinor() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(
            """
            <projectVersionPolicyConfig>
              <minorRules>
                <minorRule>Minor One</minorRule>
                <minorRule>Minor Two</minorRule>
                <minorRule>Minor Three</minorRule>
              </minorRules>
            </projectVersionPolicyConfig>
            """
        );
        assertNotNull(config);
        assertNull(config.getVersionTag());
        assertEquals(List.of("Minor One", "Minor Two", "Minor Three"), config.getMinorRules());
        assertEquals(List.of(), config.getMajorRules());
    }

    @Test
    void readXMLTestMajor() {
        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(
            """
            <projectVersionPolicyConfig>
              <majorRules>
                <majorRule>Major One</majorRule>
                <majorRule>Major Two</majorRule>
                <majorRule>Major Three</majorRule>
              </majorRules>
            </projectVersionPolicyConfig>
            """
        );
        assertNotNull(config);
        assertNull(config.getVersionTag());
        assertEquals(List.of(), config.getMinorRules());
        assertEquals(List.of("Major One", "Major Two", "Major Three"), config.getMajorRules());
    }
}
