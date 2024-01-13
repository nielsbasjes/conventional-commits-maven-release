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

import org.apache.maven.scm.ScmException;
import org.apache.maven.shared.release.policy.PolicyException;
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

/**
 * Uses SemVer combined with the tags and commit messages to increase the version.
 */
@Singleton
@Named("ConventionalCommitsVersionPolicy")
@Description("A VersionPolicy following the SemVer rules and looks at "
    + "the commit messages following the Conventional Commits convention.")
public class ConventionalCommitsVersionPolicy implements VersionPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(ConventionalCommitsVersionPolicy.class);

    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws VersionParseException, PolicyException {
        ConventionalCommitsVersionConfig versionConfig = ConventionalCommitsVersionConfig.fromXml(request.getConfig());
        VersionRules versionRules = new VersionRules(versionConfig);
        CommitHistory commitHistory;
        try {
            commitHistory = new CommitHistory(request, versionRules);
        } catch (ScmException e) {
            throw new PolicyException("Something went wrong fetching the commit history", e);
        }

        boolean usingTag = false;

        String versionString = request.getVersion(); // The current version in the pom
        Version version;

        LOG.debug("--------------------------------------------------------");
        LOG.debug("Determining next ReleaseVersion");
        LOG.debug("VersionRules: \n{}", versionRules);
        LOG.debug("Pom version             : {}", versionString);

        LOG.debug("Commit History          : \n{}", commitHistory);

        Element maxElementSinceLastVersionTag = versionRules.getMaxElementSinceLastVersionTag(commitHistory);

        String latestVersionTag = commitHistory.getLastVersionTag();
        if (latestVersionTag != null) {
            // Use the latest tag we have
            versionString = latestVersionTag;
            usingTag = true;
            LOG.debug("Version from tags       : {}", versionString);
        } else {
            LOG.debug("Version from tags       : NOT FOUND");
        }

        LOG.debug("Step from commits       : {}", maxElementSinceLastVersionTag.name());

        try {
            version = Version.parse(versionString);
        } catch (IllegalArgumentException e) {
            throw new VersionParseException(e.getMessage(), e);
        }

        LOG.debug("Current version         : {}", version);


        // If we have a version from the tag we use that + the calculated update.
        // If only have the version from the current pom version with -SNAPSHOT removed IF it is only a PATCH.
        if (!(latestVersionTag == null && maxElementSinceLastVersionTag == Element.PATCH)) {
            version = version.next(maxElementSinceLastVersionTag);
        }

        Version releaseVersion = version.toReleaseVersion();
        LOG.debug("Next version            : {}", releaseVersion);
        LOG.debug("--------------------------------------------------------");


        LOG.info("Version and SCM analysis result:");
        if (usingTag) {
            LOG.info("- Starting from SCM tag with version {}", versionString);
        } else {
            LOG.info("- Starting from project.version {} (because we did not find any valid SCM tags)", versionString);
        }

        LOG.info("- Doing a {} version increase{}.",
            maxElementSinceLastVersionTag,
            maxElementSinceLastVersionTag == Element.PATCH
                ? " (because we did not find any minor/major commit messages)"
                : "");

        LOG.info("- Next release version : {}", releaseVersion);

        VersionPolicyResult result = new VersionPolicyResult();
        result.setVersion(releaseVersion.toString());
        return result;
    }

    @Override
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
