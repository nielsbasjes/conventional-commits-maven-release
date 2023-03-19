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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigParsingTest {

    private static final Logger LOG = LogManager.getLogger();

    private static final VersionRules DEFAULT_VERSION_RULES = new VersionRules(null);

    private static final String CustomVersionTagRegex = "^The awesome ([0-9]+\\.[0-9]+\\.[0-9]+) release$";
    private static final String CustomMajorRulesRegex = "^.*Big Change.*$";
    private static final String CustomMinorRulesRegex = "^.*Nice Change.*$";

    private static final String CustomVersionTagXML = "<versionTag>"+CustomVersionTagRegex+"</versionTag>";
    private static final String CustomMajorRulesXML = "<majorRules><majorRule>"+CustomMajorRulesRegex+"</majorRule></majorRules>";
    private static final String CustomMinorRulesXML = "<minorRules><minorRule>"+CustomMinorRulesRegex+"</minorRule></minorRules>";

    private static final int patternFlags = Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES;

    private static final Pattern CustomVersionTagPattern =
        Pattern.compile(CustomVersionTagRegex, patternFlags);
    private static final List<Pattern> CustomMajorRulesPatterns =
        Collections.singletonList(Pattern.compile(CustomMajorRulesRegex, patternFlags));
    private static final List<Pattern> CustomMinorRulesPatterns =
        Collections.singletonList(Pattern.compile(CustomMinorRulesRegex, patternFlags));

    private void assertTagPatternIsDefault(VersionRules versionRules) {
        assertEquals(DEFAULT_VERSION_RULES.getTagPattern().toString(), versionRules.getTagPattern().toString());
    }

    private void assertTagPatternIsCustom(VersionRules versionRules) {
        assertEquals(CustomVersionTagPattern.toString(), versionRules.getTagPattern().toString());
    }

    private void assertMajorIsDefault(VersionRules versionRules) {
        assertEquals(
            DEFAULT_VERSION_RULES.getMajorUpdatePatterns().toString(),
            versionRules.getMajorUpdatePatterns().toString()
        );
    }

    private void assertMajorIsCustom(VersionRules versionRules) {
        assertEquals(
            CustomMajorRulesPatterns.toString(),
            versionRules.getMajorUpdatePatterns().toString()
        );
    }

    private void assertMajorIsEmpty(VersionRules versionRules) {
        assertTrue(versionRules.getMajorUpdatePatterns().isEmpty());
    }


    private void assertMinorIsDefault(VersionRules versionRules) {
        assertEquals(
            DEFAULT_VERSION_RULES.getMinorUpdatePatterns().toString(),
            versionRules.getMinorUpdatePatterns().toString()
        );
    }

    private void assertMinorIsCustom(VersionRules versionRules) {
        assertEquals(
            CustomMinorRulesPatterns.toString(),
            versionRules.getMinorUpdatePatterns().toString()
        );
    }

    private void assertMinorIsEmpty(VersionRules versionRules) {
        assertTrue(versionRules.getMinorUpdatePatterns().isEmpty());
    }

    // ====================================================

    @Test
    void testParseNull() {
        assertNull(ConventionalCommitsVersionConfig.fromXml(null));
    }

    @Test
    void testParseReallyEmpty() {
        assertNull(ConventionalCommitsVersionConfig.fromXml(""));
    }

    @Test
    void testParseEmptyValid() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(0, config.getMinorRules().size());

        VersionRules versionRules = new VersionRules(config);

        assertTagPatternIsDefault(versionRules);
        assertMajorIsDefault(versionRules);
        assertMinorIsDefault(versionRules);
    }

    @Test
    void testParseValidTag() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomVersionTagXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(CustomVersionTagRegex, config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(0, config.getMinorRules().size());

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsDefault(versionRules);
        assertMinorIsDefault(versionRules);
    }

    @Test
    void testParseValidTagMinor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomVersionTagXML
            + CustomMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(CustomVersionTagRegex, config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(1, config.getMinorRules().size());
        assertEquals(CustomMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsEmpty(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidTagMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomVersionTagXML
            + CustomMajorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(CustomVersionTagRegex, config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(CustomMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(0, config.getMinorRules().size());

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsCustom(versionRules);
        assertMinorIsEmpty(versionRules);
    }

    @Test
    void testParseValidTagMinorMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomVersionTagXML
            + CustomMajorRulesXML
            + CustomMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(CustomVersionTagRegex, config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(CustomMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(1, config.getMinorRules().size());
        assertEquals(CustomMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsCustom(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidMinor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(1, config.getMinorRules().size());
        assertEquals(CustomMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsDefault(versionRules);
        assertMajorIsEmpty(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomMajorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(CustomMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(0, config.getMinorRules().size());

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsDefault(versionRules);
        assertMajorIsCustom(versionRules);
        assertMinorIsEmpty(versionRules);
    }

    @Test
    void testParseValidMinorMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + CustomMajorRulesXML
            + CustomMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(CustomMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(1, config.getMinorRules().size());
        assertEquals(CustomMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsDefault(versionRules);
        assertMajorIsCustom(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseBadXmlUnknownTags() {
        String versionRulesConfig = ""
            + "<Something><Unexpected></Unexpected></Something>";

        assertThrows(ConventionalCommitsConfigException.class, () ->
            ConventionalCommitsVersionConfig.fromXml(versionRulesConfig));
    }

    @Test
    void testParseBadXmlUnbalanced() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            // Unbalanced XML tag
            + "  <versionTag>^The awesome ([0-9]+\\.[0-9]+\\.[0-9]+) release$";

        assertThrows(ConventionalCommitsConfigException.class, () ->
            ConventionalCommitsVersionConfig.fromXml(versionRulesConfig));
    }

}
