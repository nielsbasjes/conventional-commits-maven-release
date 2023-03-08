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

import org.apache.maven.scm.ScmException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.eclipse.sisu.Description;
import org.semver.Version;
import org.semver.Version.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * Uses SemVer combined with the tags and commit messages to increase the version.
 */
@Singleton
@Named("ConventionalCommitsVersionPolicy")
@Description("A VersionPolicy following the SemVer rules and looks at "
    + "the commit messages following the Conventional Commits convention.")
public class ConventionalCommitsVersionPolicy implements VersionPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ConventionalCommitsVersionPolicy.class);

    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws VersionParseException {
        ConventionalCommitsVersionConfig versionConfig = ConventionalCommitsVersionConfig.fromXml(request.getConfig());
        VersionRules versionRules = new VersionRules(versionConfig);
        try {
            return getReleaseVersion(
                request,
                versionRules,
                new CommitHistory(request, versionRules));
        } catch (ScmException e) {
            throw new VersionParseException("Unable to obtain the information from the SCM history", e);
        }
    }

    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request,
                                                 VersionRules versionRules,
                                                 CommitHistory commitHistory)
        throws VersionParseException {
        boolean usingTag = false;

        String versionString = request.getVersion(); // The current version in the pom
        Version version;

        LOG.debug("--------------------------------------------------------");
        LOG.debug("Determining next ReleaseVersion");
        LOG.debug("VersionRules: \n{}", versionRules);
        LOG.debug("Pom version             : {}", versionString);

        LOG.debug("Commit History          : \n{}", commitHistory);

        Element maxElementSinceLastVersionTag = versionRules.getMaxElementSinceLastVersionTag(commitHistory);

        List<String> commitHistoryTags = commitHistory.getTags();
        if (commitHistoryTags.size() == 1) {
            // Use the latest tag we have
            versionString = commitHistoryTags.get(0);
            usingTag = true;
            LOG.debug("Version from tags       : {}", versionString);
        } else {
            LOG.debug("Version from tags       : NOT FOUND");
        }

        if (maxElementSinceLastVersionTag == null) {
            LOG.debug("Step from commits       : No SCM version tags found");
        } else {
            LOG.debug("Step from commits       : {}", maxElementSinceLastVersionTag.name());
        }

        try {
            version = Version.parse(versionString);
        } catch (IllegalArgumentException e) {
            throw new VersionParseException(e.getMessage(), e);
        }

        LOG.debug("Current version         : {}", version);


        // If we have a version from the tag we use that + the calculated update.
        // If only have the version from the current pom version with -SNAPSHOT removed.
        if (maxElementSinceLastVersionTag != null) {
            version = version.next(maxElementSinceLastVersionTag);
        }

        Version releaseVersion = version.toReleaseVersion();
        LOG.debug("Next version            : {}", releaseVersion);
        LOG.debug("--------------------------------------------------------");

        if (usingTag) {
            LOG.info("From SCM tag with version {} "
                    + "doing a {} version increase based on commit messages to version {}",
                versionString, maxElementSinceLastVersionTag, releaseVersion);
        } else {
            if (maxElementSinceLastVersionTag == null) {
                LOG.info("From project.version {} (because we did not find any valid SCM tags) "
                        + "going to version {} (because we did not find any minor/major commit messages).",
                    versionString, releaseVersion);
            } else {
                LOG.info("From project.version {} (because we did not find any valid SCM tags) "
                        + "doing a {} version increase based on commit messages to version {}",
                    versionString, maxElementSinceLastVersionTag, releaseVersion);
            }
        }

        VersionPolicyResult result = new VersionPolicyResult();
        result.setVersion(releaseVersion.toString());
        return result;
    }

    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request)
        throws VersionParseException {
        Version version;
        try {
            version = Version.parse(request.getVersion());
        } catch (IllegalArgumentException e) {
            throw new VersionParseException(e.getMessage());
        }

        version = version.next(Element.PATCH);
        VersionPolicyResult result = new VersionPolicyResult();
        result.setVersion(version + "-SNAPSHOT");
        return result;
    }
}
