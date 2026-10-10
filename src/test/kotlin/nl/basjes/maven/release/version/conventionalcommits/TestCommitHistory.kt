/*
 * Conventional Commits Version Policy
 * Copyright (C) 2022-2026 Niels Basjes
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
package nl.basjes.maven.release.version.conventionalcommits

import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmProvider
import nl.basjes.maven.release.version.conventionalcommits.mockscm.MockScmRepository
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.maven.scm.ScmException
import org.apache.maven.scm.provider.ScmProvider
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest
import org.apache.maven.shared.release.versions.VersionParseException
import kotlin.test.Test
import kotlin.test.assertTrue

private val LOG: Logger = LogManager.getLogger()

internal class CommitHistoryTests {
    private fun createScmProvider(): ScmProvider {
        val comments: MutableList<String> = mutableListOf()
        comments.add("One")
        comments.add("Two")

        val tags: MutableList<String> = mutableListOf()
        tags.add("My Tag 1")
        return MockScmProvider(comments, tags)
    }

    @Test
    @Throws(ScmException::class, VersionParseException::class)
    fun checkCustomHistory() {
        val request = VersionPolicyRequest()
        request.version = "1.2.3"
        request.workingDirectory = "/tmp"

        val scmProvider = createScmProvider()
        request.scmProvider = scmProvider
        request.scmRepository = MockScmRepository(scmProvider)
        val commitHistory = CommitHistory(request, VersionRules())

        val commitHistoryString = commitHistory.toString()
        assertTrue(commitHistoryString.contains("Comment: \"One\""))
        assertTrue(commitHistoryString.contains("Comment: \"Two\""))
        assertTrue(commitHistoryString.contains("Tags   : [My Tag 1]"))
        LOG.info("Commit history: {}", commitHistoryString)
    }

    @Test
    @Throws(ScmException::class, VersionParseException::class)
    fun badRequestNoWorkingDirectory() {
        val request = VersionPolicyRequest()
        request.version = "1.2.3"
        //        request.setWorkingDirectory("/tmp");

        val scmProvider = createScmProvider()
        request.scmProvider = scmProvider
        request.scmRepository = MockScmRepository(scmProvider)

        val commitHistory = CommitHistory(request, VersionRules())
        assertTrue(commitHistory.changes.isEmpty())
    }

    @Test
    @Throws(ScmException::class, VersionParseException::class)
    fun badRequestNoScmProvider() {
        val request = VersionPolicyRequest()
        request.version = "1.2.3"
        request.workingDirectory = "/tmp"

        val scmProvider = createScmProvider()
        //        request.setScmProvider(scmProvider);
        request.scmRepository = MockScmRepository(scmProvider)

        val commitHistory = CommitHistory(request, VersionRules())
        assertTrue(commitHistory.changes.isEmpty())
    }

    @Test
    @Throws(ScmException::class, VersionParseException::class)
    fun badRequestNoScmRepository() {
        val request = VersionPolicyRequest()
        request.version = "1.2.3"
        request.workingDirectory = "/tmp"

        val scmProvider = createScmProvider()
        request.scmProvider = scmProvider

        //        request.setScmRepository(new MockScmRepository(scmProvider));
        val commitHistory = CommitHistory(request, VersionRules())
        assertTrue(commitHistory.changes.isEmpty())
    }
}
