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
package nl.basjes.maven.release.policy.ccsemver;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.Arrays;
import java.util.Date;

public class MockScmProvider
    extends AbstractScmProvider {
    String comment;
    String[] tags;

    public MockScmProvider(String comment, String... tags) {
        this.comment = comment;
        this.tags = tags;
    }

    @Override
    public String getScmType() {
        return "dummy";
    }

    @Override
    public ScmProviderRepository makeProviderScmRepository(String scmSpecificUrl, char delimiter) {
        ScmProviderRepository repository = new ScmProviderRepository() {
        };
        repository.setUser("someone");
        repository.setPassword("secret");
        repository.setPushChanges(false);
        return repository;
    }

    private ChangeSet changeSet(String comment, String... tags) {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setComment(comment);
        changeSet.setAuthor("Someone <someone@example.nl>");
        changeSet.setTags(Arrays.asList(tags));
        return changeSet;
    }

    @Override
    public ChangeLogScmResult changeLog(ChangeLogScmRequest request) {
        Date from = new Date(39817800000L);
        Date to = new Date(1644768534785L);
        ChangeLogSet changeLogSet = new ChangeLogSet(
            Arrays.asList(
                changeSet("Commit 19"),
                changeSet("Commit 18"),
                changeSet("Commit 17"),
                changeSet("Commit 16"),
                changeSet("Commit 15", "tag 1", "tag 2"),
                changeSet(comment), // The comment that should make the difference
                changeSet("Commit 13"),
                changeSet("Commit 12", "tag 3"),
                changeSet("Commit 11"),
                changeSet("Commit 10"),
                changeSet("Commit  9"),
                changeSet("Commit  8"),
                changeSet("Commit  7"),
                changeSet("Commit  6", "tag 4"),
                changeSet("Commit  5"),
                changeSet("Commit  4"),
                changeSet("Commit  3"),
                changeSet("Commit  2", tags), // The first tag that looks like a version
                changeSet("Commit  1"),
                changeSet("Commit  0")
            ), from, to
        );

        ScmResult scmResult = new ScmResult(
            "No command",
            "Special for CCSemVer testing",
            "No command output",
            true
        );
        return new ChangeLogScmResult(changeLogSet, scmResult);
    }
}
