# Introduction
When using the [maven release plugin](https://maven.apache.org/maven-release/) you can add custom rules on how the next version is determined. Such a "next version calculation" is called a Version Policy.

This is a version policy that enforces the Semantic Versioning format and upgrades major/minor/patch element for next development version depending on the commit messages since the previous release (actually tag in the SCM).

The default configuration follows the [Conventional Commits](https://www.conventionalcommits.org/) standard.




* CCSemVerVersionPolicy (since 3.0.0-M8): \
  A version policy that enforces the SemVer format and uses Conventional Commits to determine the next version.


* CCSemVerVersionPolicy

  The CCSemVerVersionPolicy is different in the sense that it goes beyond the project version specified in the pom.xml
  and tries to analyze the SCM history and from the tags and commit messages it calculates the next version.
  For this a few additional configuration options are available.

  The calculation works as follows:

    * It tries to find the previously released version by searching for the previous release tag.
      Here the configured <<<versionTag>>> regex is used to locate and extract (the regex MUST have exactly
      1 capture group) the correct value.
      If no matching tags are found the project version from the pom.xml is used as the fallback reference point.

    * By default it assumes that only a patch release (i.e. <<<?.?.+1>>> ) is needed.

    * If any of the commit messages since the previous release tag match any of the of <<<minorRules>>> regexes then
      it assumes the next release is a minor release (i.e. <<<?.+1.0>>> ).

    * If any of the commit messages since the previous release tag match any of the of <<<majorRules>>> regexes then
      it assumes the next release is a major release (i.e. <<<+1.0.0>>> ).

  The next development version is always calculated as the release version and then <<<?.?.+1-SNAPSHOT>>>.

  When doing <<<mvn release:prepare -X>>> the details about the found tags, commit messages and the used patterns
  are output to the console.

  The default configuration assumes a version tag that is just the version (i.e. something like <<<1.2.3>>>) and
  the rules from the {{{https://www.conventionalcommits.org/en/v1.0.0/}Conventional Commits v1.0.0}} are followed.

  If you want a different tag format this will usually result in setting it both when creating the tag and
  for finding it again:

+-------------------
<configuration>
<tagNameFormat>v@{project.version}</tagNameFormat>
<projectVersionPolicyId>CCSemVerVersionPolicy</projectVersionPolicyId>
<projectVersionPolicyConfig>
<versionTag>^v([0-9]+\.[0-9]+\.[0-9]+)$</versionTag>
</projectVersionPolicyConfig>
</configuration>
+-------------------

If either the minor or major rules are specified then all default rules (both minor and major) are dropped
in favour of the configuration.

+-------+
<project>
...
<build>
<plugins>
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-release-plugin</artifactId>
<version>${project.version}</version>
<configuration>
<tagNameFormat>v@{project.version}</tagNameFormat>

          <projectVersionPolicyId>CCSemVerVersionPolicy</projectVersionPolicyId>

          <!-- projectVersionPolicyConfig for the CCSemVerVersionPolicy is an XML structure:  -->
          <!-- versionTag: A regex with 1 capture group that MUST extract the project.version from the SCM tag. -->
          <!-- minorRules: A list regexes that will be matched against all lines in each commit message since   -->
          <!--             the last tag. If matched the next version is at least a MINOR update.                -->
          <!-- majorRules: A list regexes that will be matched against all lines in each commit message since   -->
          <!--             the last tag. If matched the next version is at least a MAJOR update.                -->
          <!-- If a match is found the commit will trigger either a minor or major version increase             -->
          <!-- instead of only a patch increase.                                                                -->
          <projectVersionPolicyConfig>
            <versionTag>^v([0-9]+\.[0-9]+\.[0-9]+)$</versionTag>
            <majorRules>
              <majorRule>^[a-zA-Z]+!(?:\([a-zA-Z0-9_-]+\))?: .*$</majorRule>
              <majorRule>^BREAKING CHANGE:.*$</majorRule>
            </majorRules>
            <minorRules>
              <minorRule>^feat(?:\([a-zA-Z0-9_-]+\))?: .*$</minorRule>
            </minorRules>
          </projectVersionPolicyConfig>
        </configuration>
      </plugin>
    </plugins>
    ...
  </build>
  ...
</project>
+-------+



License
=======

    Conventional Commits Version Policy
    Copyright (C) 2013-2023 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
