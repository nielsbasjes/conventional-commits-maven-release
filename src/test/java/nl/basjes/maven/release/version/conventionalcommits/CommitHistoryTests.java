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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommitHistoryTests {

    private static final Logger LOG = LogManager.getLogger();

    private ScmProvider createScmProvider() {
        List<String> comments = new ArrayList<>();
        comments.add("One");
        comments.add("Two");

        List<String> tags = new ArrayList<>();
        tags.add("My Tag 1");
        return new MockScmProvider(comments, tags);
    }

    @Test
    void checkCustomHistory() throws ScmException, VersionParseException {
        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion("1.2.3");

        request.setWorkingDirectory("/tmp");

        ScmProvider scmProvider = createScmProvider();
        request.setScmProvider(scmProvider);
        request.setScmRepository(new MockScmRepository(scmProvider));
        CommitHistory commitHistory = new CommitHistory(request, new VersionRules(null));

        String commitHistoryString = commitHistory.toString();
        assertTrue(commitHistoryString.contains("Comment: \"One\""));
        assertTrue(commitHistoryString.contains("Comment: \"Two\""));
        assertTrue(commitHistoryString.contains("Tags   : [My Tag 1]"));
        LOG.info("Commit history: {}", commitHistoryString);
    }

    @Test
    void badRequestNoWorkingDirectory() throws ScmException, VersionParseException {
        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion("1.2.3");
//        request.setWorkingDirectory("/tmp");
        ScmProvider scmProvider = createScmProvider();
        request.setScmProvider(scmProvider);
        request.setScmRepository(new MockScmRepository(scmProvider));

        CommitHistory commitHistory = new CommitHistory(request, new VersionRules(null));
        assertTrue(commitHistory.getChanges().isEmpty());
    }

    @Test
    void badRequestNoScmProvider() throws ScmException, VersionParseException {
        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion("1.2.3");
        request.setWorkingDirectory("/tmp");
        ScmProvider scmProvider = createScmProvider();
//        request.setScmProvider(scmProvider);
        request.setScmRepository(new MockScmRepository(scmProvider));

        CommitHistory commitHistory = new CommitHistory(request, new VersionRules(null));
        assertTrue(commitHistory.getChanges().isEmpty());
    }

    @Test
    void badRequestNoScmRepository() throws ScmException, VersionParseException {
        VersionPolicyRequest request = new VersionPolicyRequest();
        request.setVersion("1.2.3");
        request.setWorkingDirectory("/tmp");
        ScmProvider scmProvider = createScmProvider();
        request.setScmProvider(scmProvider);
//        request.setScmRepository(new MockScmRepository(scmProvider));

        CommitHistory commitHistory = new CommitHistory(request, new VersionRules(null));
        assertTrue(commitHistory.getChanges().isEmpty());
    }


}
