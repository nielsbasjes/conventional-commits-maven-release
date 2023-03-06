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

import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.semver.Version;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.semver.Version.Element.MAJOR;
import static org.semver.Version.Element.MINOR;
import static org.semver.Version.Element.PATCH;

public class NextVersionCalculationTest {

    private static ConventionalCommitsVersionPolicy versionPolicy;
    private static final VersionRules DEFAULT_VERSION_RULES = new VersionRules(null);

    @BeforeAll
    public static void setUp() {
        versionPolicy = new ConventionalCommitsVersionPolicy();
    }

    @AfterAll
    public static void tearDown() {
        versionPolicy = null;
    }

    private void assertNextVersion(VersionRules versionRules, String input, Version.Element element) {
        switch (element) {
            case MAJOR:
                assertTrue(versionRules.isMajorUpdate(input));
                // We do not care about minor and patch
                break;
            case MINOR:
                assertFalse(versionRules.isMajorUpdate(input));
                assertTrue(versionRules.isMinorUpdate(input));
                // We do not care about patch
                break;
            case PATCH:
                assertFalse(versionRules.isMajorUpdate(input));
                assertFalse(versionRules.isMinorUpdate(input));
                break;
        }
    }

    @Test
    void testMajorMinorPatchDetection() {
        VersionRules rules = DEFAULT_VERSION_RULES;
        assertNextVersion(rules, "feat!(core): New feature.", MAJOR);
        assertNextVersion(rules, "feat!: New feature.", MAJOR);
        assertNextVersion(rules, "feat(core): Foo.\n\nBREAKING CHANGE: New feature.\n", MAJOR);
        assertNextVersion(rules, "feat: Foo.\n\nBREAKING CHANGE: New feature.\n", MAJOR);

        assertNextVersion(rules, "feat(core): New feature.", MINOR);
        assertNextVersion(rules, "feat: New feature.", MINOR);

        assertNextVersion(rules, "Does not match any pattern.", PATCH);
    }

    @Test
    void testConvertToSnapshot()
        throws Exception {
        String suggestedVersion = versionPolicy.getDevelopmentVersion(new VersionPolicyRequest().setVersion("1.0.0"))
            .getVersion();

        assertEquals("1.0.1-SNAPSHOT", suggestedVersion);
    }

    public void verifyNextVersion(VersionRules versionRules,
                                  String currentPomVersion,
                                  String tag,
                                  List<String> comments,
                                  String expectedNextVersion) throws VersionParseException {
        CommitHistory commitHistory = new CommitHistory();
        commitHistory.addTags(tag);
        commitHistory.addChanges(comments);

        assertEquals(expectedNextVersion, versionPolicy
            .getReleaseVersion(
                new VersionPolicyRequest().setVersion(currentPomVersion),
                versionRules,
                commitHistory
            ).getVersion());
    }

    String patch1 = "Quick patch";
    String patch2 = "fix(core): Another fix.";
    String minor1 = "feat(core): New thingy.";
    String major1 = "fix!(core): Breaking improvement";

    List<String> EMPTY = Collections.emptyList();
    List<String> MAJOR_MESSAGES = Arrays.asList(patch1, patch2, minor1, major1);
    List<String> MINOR_MESSAGES = Arrays.asList(patch1, patch2, minor1);
    List<String> PATCH_MESSAGES = Arrays.asList(patch1, patch2);

    @Test
    void testDefaultVersionRules() throws VersionParseException {
        VersionRules rules = DEFAULT_VERSION_RULES;
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "", EMPTY, "1.2.3"); // No Tag - No CC Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "", PATCH_MESSAGES, "1.2.3"); // No Tag - Patch Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "", MINOR_MESSAGES, "1.3.0"); // No Tag - Minor Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "", MAJOR_MESSAGES, "2.0.0"); // No Tag - Major Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "2.3.4", EMPTY, "2.3.5"); // Tag - No CC Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "2.3.4", PATCH_MESSAGES, "2.3.5"); // Tag - Patch Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "2.3.4", MINOR_MESSAGES, "2.4.0"); // Tag - Minor Comments
        verifyNextVersion(rules, "1.2.3-SNAPSHOT", "2.3.4", MAJOR_MESSAGES, "3.0.0"); // Tag - Major Comments
    }

}
