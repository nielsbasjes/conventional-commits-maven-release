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

import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.semver.Version.Element.MAJOR;
import static org.semver.Version.Element.MINOR;
import static org.semver.Version.Element.PATCH;

public class NextVersionCalculationTest extends AbstractNextVersionTest {

    private static ConventionalCommitsVersionPolicy versionPolicy;

    @BeforeAll
    public static void setUp() {
        versionPolicy = new ConventionalCommitsVersionPolicy();
    }

    @AfterAll
    public static void tearDown() {
        versionPolicy = null;
    }

    @Test
    void testMajorMinorPatchDetection() {
        VersionRules rules = DEFAULT_VERSION_RULES;
        // Major
        assertNextVersion(rules, "feat(core)!: New feature.", MAJOR);
        assertNextVersion(rules, "feat!: New feature.", MAJOR);
        assertNextVersion(rules, "feat(core): Foo.\n\nBREAKING CHANGE: New feature.\n", MAJOR);
        assertNextVersion(rules, "feat: Foo.\n\nBREAKING CHANGE: New feature.\n", MAJOR);

        // Minor
        assertNextVersion(rules, "feat(core): New feature.", MINOR);
        assertNextVersion(rules, "feat: New feature.", MINOR);

        // Patch
        assertNextVersion(rules, "Does not match any pattern.", PATCH);
    }

    @Test
    void testConvertToSnapshot()
        throws Exception {
        String suggestedVersion = versionPolicy.getDevelopmentVersion(new VersionPolicyRequest().setVersion("1.0.0"))
            .getVersion();

        assertEquals("1.0.1-SNAPSHOT", suggestedVersion);
    }

    @Test
    void testConvertToSnapshotBadVersion() {
        assertThrows(VersionParseException.class, () ->
            versionPolicy.getDevelopmentVersion(new VersionPolicyRequest().setVersion("Really Bad"))
        );
    }

    private static final String PATCH_1 = "Quick patch";
    private static final String PATCH_2 = "fix(core): Another fix.";
    private static final String MINOR_1 = "feat(core): New thingy.";
    private static final String MAJOR_1 = "fix(core)!: Breaking improvement";

    private static final List<String> EMPTY = Collections.emptyList();
    private static final List<String> MAJOR_MESSAGES = Arrays.asList(PATCH_1, PATCH_2, MINOR_1, MAJOR_1);
    private static final List<String> MINOR_MESSAGES = Arrays.asList(PATCH_1, PATCH_2, MINOR_1);
    private static final List<String> PATCH_MESSAGES = Arrays.asList(PATCH_1, PATCH_2);

    @Test
    void testDefaultVersionRules() throws VersionParseException, PolicyException, IOException, ScmRepositoryException {
        verifyNextVersion("1.2.3-SNAPSHOT", EMPTY,          "",      "1.2.3"); // No Tag - No CC Comments
        verifyNextVersion("1.2.3-SNAPSHOT", PATCH_MESSAGES, "",      "1.2.3"); // No Tag - Patch Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MINOR_MESSAGES, "",      "1.3.0"); // No Tag - Minor Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MAJOR_MESSAGES, "",      "2.0.0"); // No Tag - Major Comments
        verifyNextVersion("1.2.3-SNAPSHOT", EMPTY,          "2.3.4", "2.3.5"); // Tag - No CC Comments
        verifyNextVersion("1.2.3-SNAPSHOT", PATCH_MESSAGES, "2.3.4", "2.3.5"); // Tag - Patch Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MINOR_MESSAGES, "2.3.4", "2.4.0"); // Tag - Minor Comments
        verifyNextVersion("1.2.3-SNAPSHOT", MAJOR_MESSAGES, "2.3.4", "3.0.0"); // Tag - Major Comments
    }

    @Test
    void testInvalidPomVersion() {
        assertThrows(VersionParseException.class, () ->
            verifyNextVersion("Bad", Collections.singletonList("Nothing"), "Unmatched value", "Should fail")
        );
    }

    @Test
    void testInvalidTagVersion() {
        assertThrows(VersionParseException.class, () ->
            verifyNextVersion("1.2.3",
                Collections.singletonList("Nothing"),
                singletonList("Bad"),
                "<projectVersionPolicyConfig>\n" +
                "  <versionTag>^(Bad)$</versionTag>\n" +
                "</projectVersionPolicyConfig>",
                "Should fail",
                null)
        );
    }
}
