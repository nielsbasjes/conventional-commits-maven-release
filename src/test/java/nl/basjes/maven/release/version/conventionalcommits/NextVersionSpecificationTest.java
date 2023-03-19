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

import org.junit.jupiter.api.Test;

import static org.semver.Version.Element.MAJOR;
import static org.semver.Version.Element.MINOR;
import static org.semver.Version.Element.PATCH;

class NextVersionSpecificationTest extends AbstractNextVersionTest {

    @Test
    void testConventionalCommitsExamples() {
        //Verifying the examples show on https://www.conventionalcommits.org/en/v1.0.0/#examples
        VersionRules rules = DEFAULT_VERSION_RULES;

        // Commit message with description and breaking change footer
        assertNextVersion(
            rules,
            "feat: allow provided config object to extend other configs\n" +
            "\n" +
            "BREAKING CHANGE: `extends` key in config file is now used for extending other config files\n",
            MAJOR);

        // Commit message with `!` to draw attention to breaking change
        assertNextVersion(
            rules,
            "feat!: send an email to the customer when a product is shipped",
            MAJOR);

        // Commit message with scope and `!` to draw attention to breaking change
        assertNextVersion(
            rules,
            "feat(api)!: send an email to the customer when a product is shipped",
            MAJOR);

        // Commit message with both `!` and BREAKING CHANGE footer
        assertNextVersion(
            rules,
            "chore!: drop support for Node 6\n" +
            "\n" +
            "BREAKING CHANGE: use JavaScript features not available in Node 6.\n",
            MAJOR);

        // Commit message with no body
        assertNextVersion(
            rules,
            "docs: correct spelling of CHANGELOG",
            PATCH);

        // Commit message with scope
        assertNextVersion(
            rules,
            "feat(lang): add Polish language",
            MINOR);

        // Commit message with multi-paragraph body and multiple footers
        assertNextVersion(
            rules,
            "fix: prevent racing of requests\n" +
            "\n" +
            "Introduce a request id and a reference to latest request. Dismiss\n" +
            "incoming responses other than from latest request.\n" +
            "\n" +
            "    Remove timeouts which were used to mitigate the racing issue but are\n" +
            "obsolete now.\n" +
            "\n" +
            "Reviewed-by: Z\n" +
            "Refs: #123\n",
            PATCH);
    }


}
