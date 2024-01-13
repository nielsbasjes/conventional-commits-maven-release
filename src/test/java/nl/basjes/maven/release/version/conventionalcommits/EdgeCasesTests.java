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

import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmRepository;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EdgeCasesTests {

    @Test
    void noScmPresent() throws ScmException, VersionParseException, PolicyException {
        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion("1.2.3-SNAPSHOT");
        request.setWorkingDirectory("/tmp");
        ScmProvider scmProvider = new GitExeScmProvider();
        GitUtil.getSettings().setGitCommand("false");

        request.setScmProvider(scmProvider);
        request.setScmRepository(new MockScmRepository(scmProvider));

        CommitHistory commitHistory = new CommitHistory(request, new VersionRules(null));
        assertTrue(commitHistory.getChanges().isEmpty());

        VersionPolicy versionPolicy = new ConventionalCommitsVersionPolicy();

        String suggestedVersion = versionPolicy.getReleaseVersion(request).getVersion();
        assertEquals("1.2.3", suggestedVersion);
    }

}
