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

    private final VersionRules defaultVersionRules = new VersionRules(null);

    private final String customVersionTagRegex = "^The awesome ([0-9]+\\.[0-9]+\\.[0-9]+) release$";
    private final String customMajorRulesRegex = "^.*Big Change.*$";
    private final String customMinorRulesRegex = "^.*Nice Change.*$";

    private final String customVersionTagXML = "<versionTag>"+ customVersionTagRegex +"</versionTag>";
    private final String customMajorRulesXML = "<majorRules><majorRule>"+ customMajorRulesRegex +"</majorRule></majorRules>";
    private final String customMinorRulesXML = "<minorRules><minorRule>"+ customMinorRulesRegex +"</minorRule></minorRules>";

    private final int patternFlags = Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES;

    private final Pattern customVersionTagPattern =
        Pattern.compile(customVersionTagRegex, patternFlags);
    private final List<Pattern> customMajorRulesPatterns =
        Collections.singletonList(Pattern.compile(customMajorRulesRegex, patternFlags));
    private final List<Pattern> customMinorRulesPatterns =
        Collections.singletonList(Pattern.compile(customMinorRulesRegex, patternFlags));

    private void assertTagPatternIsDefault(VersionRules versionRules) {
        assertEquals(defaultVersionRules.getTagPattern().toString(), versionRules.getTagPattern().toString());
    }

    private void assertTagPatternIsCustom(VersionRules versionRules) {
        assertEquals(customVersionTagPattern.toString(), versionRules.getTagPattern().toString());
    }

    private void assertMajorIsDefault(VersionRules versionRules) {
        assertEquals(
            defaultVersionRules.getMajorUpdatePatterns().toString(),
            versionRules.getMajorUpdatePatterns().toString()
        );
    }

    private void assertMajorIsCustom(VersionRules versionRules) {
        assertEquals(
            customMajorRulesPatterns.toString(),
            versionRules.getMajorUpdatePatterns().toString()
        );
    }

    private void assertMajorIsEmpty(VersionRules versionRules) {
        assertTrue(versionRules.getMajorUpdatePatterns().isEmpty());
    }


    private void assertMinorIsDefault(VersionRules versionRules) {
        assertEquals(
            defaultVersionRules.getMinorUpdatePatterns().toString(),
            versionRules.getMinorUpdatePatterns().toString()
        );
    }

    private void assertMinorIsCustom(VersionRules versionRules) {
        assertEquals(
            customMinorRulesPatterns.toString(),
            versionRules.getMinorUpdatePatterns().toString()
        );
    }

    private void assertMinorIsEmpty(VersionRules versionRules) {
        assertTrue(versionRules.getMinorUpdatePatterns().isEmpty());
    }

    // ====================================================

    @Test
    void testVersionRulesToString() {
        String string = defaultVersionRules.toString();
        assertTrue(string.contains("Conventional Commits config:"));
    }

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
            + customVersionTagXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(customVersionTagRegex, config.getVersionTag());
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
            + customVersionTagXML
            + customMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(customVersionTagRegex, config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(1, config.getMinorRules().size());
        assertEquals(customMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsEmpty(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidTagMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + customVersionTagXML
            + customMajorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(customVersionTagRegex, config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(customMajorRulesRegex, config.getMajorRules().get(0));
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
            + customVersionTagXML
            + customMajorRulesXML
            + customMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertEquals(customVersionTagRegex, config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(customMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(1, config.getMinorRules().size());
        assertEquals(customMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsCustom(versionRules);
        assertMajorIsCustom(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidMinor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + customMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(0, config.getMajorRules().size());
        assertEquals(1, config.getMinorRules().size());
        assertEquals(customMinorRulesRegex, config.getMinorRules().get(0));

        VersionRules versionRules = new VersionRules(config);
        assertTagPatternIsDefault(versionRules);
        assertMajorIsEmpty(versionRules);
        assertMinorIsCustom(versionRules);
    }

    @Test
    void testParseValidMajor() {
        String versionRulesConfig = ""
            + "<projectVersionPolicyConfig>"
            + customMajorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(customMajorRulesRegex, config.getMajorRules().get(0));
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
            + customMajorRulesXML
            + customMinorRulesXML
            + "</projectVersionPolicyConfig>" +
            "";

        ConventionalCommitsVersionConfig config = ConventionalCommitsVersionConfig.fromXml(versionRulesConfig);
        LOG.info("Tested config: {}", config);

        assertNull(config.getVersionTag());
        assertEquals(1, config.getMajorRules().size());
        assertEquals(customMajorRulesRegex, config.getMajorRules().get(0));
        assertEquals(1, config.getMinorRules().size());
        assertEquals(customMinorRulesRegex, config.getMinorRules().get(0));

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

    @Test
    void testVersionRulesIgnoreNullTag(){
        ConventionalCommitsVersionConfig config = new ConventionalCommitsVersionConfig();
        config.setVersionTag(null);
        VersionRules versionRules = new VersionRules(config);
        assertEquals(
            defaultVersionRules.getTagPattern().toString(),
            versionRules.getTagPattern().toString());
    }

    @Test
    void testVersionRulesIgnoreBlankTag(){
        ConventionalCommitsVersionConfig config = new ConventionalCommitsVersionConfig();
        config.setVersionTag("       ");
        VersionRules versionRules = new VersionRules(config);
        assertEquals(
            defaultVersionRules.getTagPattern().toString(),
            versionRules.getTagPattern().toString());
    }


}
