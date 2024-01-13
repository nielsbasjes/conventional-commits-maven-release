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

import org.apache.maven.scm.ChangeSet;
import org.semver.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The set of rules that determine from the commit history what the next version should be.
 */
public class VersionRules {
    private final Pattern tagPattern;

    private final List<Pattern> majorUpdatePatterns = new ArrayList<>();
    private final List<Pattern> minorUpdatePatterns = new ArrayList<>();

    public VersionRules(ConventionalCommitsVersionConfig config) {
        int patternFlags = Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES;

        // The default assumes then entire tag is what we need
        String tagRegex = "^(\\d+\\.\\d+\\.\\d+)$";

        // The default rules following https://www.conventionalcommits.org/en/v1.0.0/
        majorUpdatePatterns.add(Pattern.compile("^[a-zA-Z]+(?:\\([a-zA-Z\\d_-]+\\))?!: .*$", patternFlags));
        majorUpdatePatterns.add(Pattern.compile("^BREAKING CHANGE:.*$", patternFlags));
        minorUpdatePatterns.add(Pattern.compile("^feat(?:\\([a-zA-Z\\d_-]+\\))?: .*$", patternFlags));

        if (config != null) {
            String semverConfigVersionTag = config.getVersionTag();
            if (semverConfigVersionTag != null && !semverConfigVersionTag.trim().isEmpty()) {
                tagRegex = semverConfigVersionTag;
            }

            if (!config.getMajorRules().isEmpty() || !config.getMinorRules().isEmpty()) {
                majorUpdatePatterns.clear();
                for (String majorRule : config.getMajorRules()) {
                    majorUpdatePatterns.add(Pattern.compile(majorRule, patternFlags));
                }
                minorUpdatePatterns.clear();
                for (String minorRule : config.getMinorRules()) {
                    minorUpdatePatterns.add(Pattern.compile(minorRule, patternFlags));
                }
            }
        }
        tagPattern = Pattern.compile(tagRegex, Pattern.MULTILINE);
    }

    public Version.Element getMaxElementSinceLastVersionTag(CommitHistory commitHistory) {
        Version.Element maxElement = Version.Element.PATCH;
        for (ChangeSet change : commitHistory.getChanges()) {
            if (isMajorUpdate(change.getComment())) {
                // This is the highest possible: Immediately done
                return Version.Element.MAJOR;
            } else if (isMinorUpdate(change.getComment())) {
                // Have to wait, there may be another MAJOR one.
                maxElement = Version.Element.MINOR;
            }
        }
        return maxElement;
    }

    public boolean isMajorUpdate(String input) {
        return matchesAny(majorUpdatePatterns, input);
    }

    public boolean isMinorUpdate(String input) {
        return matchesAny(minorUpdatePatterns, input);
    }

    private boolean matchesAny(List<Pattern> patterns, String input) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    public List<Pattern> getMajorUpdatePatterns() {
        return majorUpdatePatterns;
    }

    public List<Pattern> getMinorUpdatePatterns() {
        return minorUpdatePatterns;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Conventional Commits config:\n");
        result.append("  VersionTag:\n");
        result.append("    >>>").append(tagPattern).append("<<<\n");
        result.append("  Major Rules:\n");
        for (Pattern majorUpdatePattern : majorUpdatePatterns) {
            result.append("    >>>").append(majorUpdatePattern).append("<<<\n");
        }
        result.append("  Minor Rules:\n");
        for (Pattern minorUpdatePattern : minorUpdatePatterns) {
            result.append("    >>>").append(minorUpdatePattern).append("<<<\n");
        }
        return result.toString();
    }
}
