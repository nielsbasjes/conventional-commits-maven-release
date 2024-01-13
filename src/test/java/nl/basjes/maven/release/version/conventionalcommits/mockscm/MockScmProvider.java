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
package nl.basjes.maven.release.version.conventionalcommits.mockscm;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MockScmProvider
    extends AbstractScmProvider {
    List<ChangeSet> configuredChangeSets;

    public MockScmProvider(List<String> comments, List<String> tags) {
        configuredChangeSets = comments.stream().map(this::changeSet).collect(Collectors.toList());
        configuredChangeSets.add(changeSet("Commit for tags", tags));
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

    private ChangeSet changeSet(String newComment, String... newTags) {
        return changeSet(newComment, Arrays.asList(newTags));
    }

    private ChangeSet changeSet(String newComment, List<String> newTags) {
        ChangeSet changeSet = new ChangeSet();
        changeSet.setComment(newComment);
        changeSet.setAuthor("Niels Basjes <niels@basjes.nl>");

        if (!newTags.isEmpty()) {
            List<String> tags = new ArrayList<>();
            for (String newTag : newTags) {
                if (newTag != null && !newTag.trim().isEmpty()) {
                    tags.add(newTag);
                }
            }
            if (!tags.isEmpty()) {
                changeSet.setTags(tags);
            }
        }
        return changeSet;
    }

    @Override
    public ChangeLogScmResult changeLog(ChangeLogScmRequest request) throws ScmException {
        Date from = new Date(39817800000L);
        Date to   = new Date(1233451620000L);

        List<ChangeSet> fullChangeSetList = new ArrayList<>();

        int tagId = 1;

        for (int i = 0; i < 150; i++) {
            if (fullChangeSetList.size() >= request.getLimit()) {
                break;
            }
            fullChangeSetList.add(changeSet("Dummy Commit without tags"));
            if (fullChangeSetList.size() >= request.getLimit()) {
                break;
            }
            fullChangeSetList.add(changeSet("Dummy Commit with tags", "Dummy tag " + tagId++, "Dummy tag " + tagId++));
        }

        for (ChangeSet configuredChangeSet : configuredChangeSets) {
            if (fullChangeSetList.size() >= request.getLimit()) {
                break;
            }
            fullChangeSetList.add(changeSet("Dummy Commit without tags"));
            if (fullChangeSetList.size() >= request.getLimit()) {
                break;
            }
            fullChangeSetList.add(configuredChangeSet);
            if (fullChangeSetList.size() >= request.getLimit()) {
                break;
            }
            fullChangeSetList.add(changeSet("Dummy Commit with tags", "Dummy tag "+ tagId++, "Dummy tag "+ tagId++));
        }

        ChangeLogSet changeLogSet = new ChangeLogSet(fullChangeSetList, from, to);

        ScmResult scmResult = new ScmResult(
            "No command",
            "Special for CCVersionPolicy testing",
            "No command output",
            true
        );
        return new ChangeLogScmResult(changeLogSet, scmResult);
    }
}
