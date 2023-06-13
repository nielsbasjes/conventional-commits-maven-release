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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Helper class to manage the commit history of the SCM repository.
 */
public class CommitHistory {
    private final List<ChangeSet> changes = new ArrayList<>();
    private String latestVersionTag = null;

    public List<ChangeSet> getChanges() {
        return changes;
    }

    public void addChanges(ChangeSet change) {
        this.changes.add(change);
    }

    public String getLastVersionTag() {
        return latestVersionTag;
    }

    public CommitHistory(VersionPolicyRequest request, VersionRules versionRules)
        throws ScmException, VersionParseException {
        ScmRepository scmRepository = request.getScmRepository();
        ScmProvider scmProvider = request.getScmProvider();
        String workingDirectory = request.getWorkingDirectory();

        if (scmRepository == null || scmProvider == null || workingDirectory == null) {
            // We do not have a commit history so the changes and tags will remain empty
            return;
        }

        ChangeLogScmRequest changeLogRequest = new ChangeLogScmRequest(
            scmRepository,
            new ScmFileSet(new File(workingDirectory))
        );

        Logger logger = LoggerFactory.getLogger(CommitHistory.class);

        int limit = 0;
        while (latestVersionTag == null) {
            limit += 100; // Read the repository in incremental steps of 100
            changeLogRequest.setLimit(null);
            changeLogRequest.setLimit(limit);
            changes.clear();

            logger.debug("Checking the last {} commits.", limit);

            ChangeLogSet changeLogSet = scmProvider.changeLog(changeLogRequest).getChangeLog();
            if (changeLogSet == null) {
                return;
            }
            List<ChangeSet> changeSets = changeLogSet.getChangeSets();

            for (ChangeSet changeSet : changeSets) {
                addChanges(changeSet);

                List<String> changeSetTags = changeSet.getTags();
                List<String> versionTags = changeSetTags
                    .stream()
                    .map(tag -> {
                        Matcher matcher = versionRules.getTagPattern().matcher(tag);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                        return null;
                    }
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!versionTags.isEmpty()) {
                    // Found the previous release tag
                    if (versionTags.size() > 1) {
                        throw new VersionParseException("Most recent commit with tags has multiple version tags: "
                            + versionTags);
                    }
                    latestVersionTag = versionTags.get(0);
                    logger.debug("Found tag");
                    break; // We have the last version tag
                }
            }
            if (latestVersionTag == null &&
                changeSets.size() < limit) {
                // Apparently there are simply no more commits.
                logger.debug("Did not find any tag");
                break;
            }
        }
    }

    @Override
    public String toString() {
        List<String> logLines = new ArrayList<>();

        logLines.add("Filtered commit history:");
        for (ChangeSet changeSet : changes) {
            logLines.add("-- Comment: \"" + changeSet.getComment() + "\"");
            logLines.add("   Tags   : " + changeSet.getTags());
        }

        StringBuilder sb = new StringBuilder();
        for (String logLine : logLines) {
            sb.append(logLine).append('\n');
        }
        return sb.toString();
    }
}
