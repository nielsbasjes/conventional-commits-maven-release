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

import org.semver.Version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractNextVersionTest {

    public static final VersionRules DEFAULT_VERSION_RULES = new VersionRules(null);

    public void assertNextVersion(VersionRules versionRules, String input, Version.Element element) {
        switch (element) {
            case MAJOR:
                assertTrue(versionRules.isMajorUpdate(input));
                // We do not care about minor and patch
                break;
            case MINOR:
                assertFalse(versionRules.isMajorUpdate(input));
                assertTrue(versionRules.isMinorUpdate(input));
                // We do not care about patch
                break;
            case PATCH:
                assertFalse(versionRules.isMajorUpdate(input));
                assertFalse(versionRules.isMinorUpdate(input));
                break;
            default:
                fail("Unsupported element type:" + element);
        }
    }
}
