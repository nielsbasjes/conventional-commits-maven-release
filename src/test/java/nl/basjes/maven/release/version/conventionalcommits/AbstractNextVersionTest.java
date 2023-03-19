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

import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmProvider;
import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.semver.Version;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractNextVersionTest {

    public static final VersionRules DEFAULT_VERSION_RULES = new VersionRules(null);

    public void assertNextVersion(VersionRules versionRules, String input, Version.Element element) {
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
            default:
                fail("Unsupported element type:" + element);
        }
    }

    public void verifyNextVersion(String configXml,
                                  String currentPomVersion,
                                  String expectedReleaseVersion,
                                  String expectedDevelopmentVersion,
                                  String comment,
                                  String... tags) throws VersionParseException, PolicyException, IOException, ScmRepositoryException {
        verifyNextVersion(currentPomVersion,
            singletonList(comment),
            Arrays.asList(tags),
            configXml,
            expectedReleaseVersion,
            expectedDevelopmentVersion);
    }

    public void verifyNextVersion(String currentPomVersion,
                                  List<String> comments,
                                  String tag,
                                  String expectedReleaseVersion) throws VersionParseException, PolicyException, IOException, ScmRepositoryException {
        verifyNextVersion(currentPomVersion,
            comments,
            singletonList(tag),
            "", // Default config
            expectedReleaseVersion,
            null);
    }
    public void verifyNextVersion(String currentPomVersion,
                                  List<String> comments,
                                  List<String> tags,
                                  String configXml,
                                  String expectedReleaseVersion,
                                  String expectedDevelopmentVersion) throws VersionParseException, PolicyException, IOException, ScmRepositoryException {

        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion(currentPomVersion);

        request.setWorkingDirectory("/tmp");

        MockScmProvider scmProvider = new MockScmProvider(comments, tags);
        request.setScmProvider(scmProvider);
        request.setScmRepository(new MockScmRepository(scmProvider));

        request.setConfig(configXml);

        VersionPolicy versionPolicy = new ConventionalCommitsVersionPolicy();

        String suggestedVersion = versionPolicy.getReleaseVersion(request).getVersion();
        assertEquals(expectedReleaseVersion, suggestedVersion);

        if (expectedDevelopmentVersion != null) {
            request.setVersion(suggestedVersion);
            String suggestedDevelopmentVersion = versionPolicy.getDevelopmentVersion(request).getVersion();
            assertEquals(expectedDevelopmentVersion, suggestedDevelopmentVersion);
        }

    }

    public void verifyNextVersionMustFail(
        String versionRulesConfig,
        String pomVersion,
        String comments,
        String... tags
    ) throws PolicyException, IOException, ScmRepositoryException {
        try {
            verifyNextVersion(
                pomVersion,
                singletonList(comments),
                Arrays.asList(tags),
                versionRulesConfig,
                "ignore",
                "ignore");
        } catch (VersionParseException vpe) {
            // Success !
            return;
        }
        fail("Should have failed");
    }

}
