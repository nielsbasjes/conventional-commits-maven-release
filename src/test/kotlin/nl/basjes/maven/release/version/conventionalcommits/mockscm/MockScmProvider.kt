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
package nl.basjes.maven.release.version.conventionalcommits.mockscm

import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmException
import org.apache.maven.scm.ScmResult
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.scm.command.changelog.ChangeLogScmResult
import org.apache.maven.scm.command.changelog.ChangeLogSet
import org.apache.maven.scm.provider.AbstractScmProvider
import org.apache.maven.scm.provider.ScmProviderRepository
import java.util.Date

class MockScmProvider (comments: List<String>, tags: List<String>) : AbstractScmProvider() {
    var configuredChangeSets: MutableList<ChangeSet>

    init {
        configuredChangeSets = comments
            .map { this.changeSet(it) }
            .toMutableList()
        configuredChangeSets.add(changeSet("Commit for tags", tags))
    }

    override fun getScmType(): String {
        return "dummy"
    }

    override fun makeProviderScmRepository(scmSpecificUrl: String, delimiter: Char): ScmProviderRepository {
        val repository: ScmProviderRepository = object : ScmProviderRepository() {
        }
        repository.user          = "someone"
        repository.password      = "secret"
        repository.isPushChanges = false
        return repository
    }

    private fun changeSet(newComment: String, vararg newTags: String): ChangeSet {
        return changeSet(newComment, listOf(*newTags))
    }

    private fun changeSet(newComment: String, newTags: List<String>): ChangeSet {
        val changeSet = ChangeSet()
        changeSet.comment = newComment
        changeSet.author = "Niels Basjes <niels@basjes.nl>"

        if (newTags.isNotEmpty()) {
            val tags: MutableList<String> = mutableListOf()
            for (newTag in newTags) {
                if (newTag.isNotBlank()) {
                    tags.add(newTag)
                }
            }
            if (tags.isNotEmpty()) {
                changeSet.tags = tags
            }
        }
        return changeSet
    }

    @Throws(ScmException::class)
    override fun changeLog(request: ChangeLogScmRequest): ChangeLogScmResult {
        val from = Date(39817800000L)
        val to = Date(1233451620000L)

        val fullChangeSetList: MutableList<ChangeSet?> = mutableListOf()

        var tagId = 1

        for (i in 0..149) {
            if (fullChangeSetList.size >= request.limit) {
                break
            }
            fullChangeSetList.add(changeSet("Dummy Commit without tags"))
            if (fullChangeSetList.size >= request.limit) {
                break
            }
            fullChangeSetList.add(changeSet("Dummy Commit with tags", "Dummy tag " + tagId++, "Dummy tag " + tagId++))
        }

        for (configuredChangeSet in configuredChangeSets) {
            if (fullChangeSetList.size >= request.limit) {
                break
            }
            fullChangeSetList.add(changeSet("Dummy Commit without tags"))
            if (fullChangeSetList.size >= request.limit) {
                break
            }
            fullChangeSetList.add(configuredChangeSet)
            if (fullChangeSetList.size >= request.limit) {
                break
            }
            fullChangeSetList.add(changeSet("Dummy Commit with tags", "Dummy tag " + tagId++, "Dummy tag " + tagId++))
        }

        val changeLogSet = ChangeLogSet(fullChangeSetList, from, to)

        val scmResult = ScmResult(
            "No command",
            "Special for CCVersionPolicy testing",
            "No command output",
            true
        )
        return ChangeLogScmResult(changeLogSet, scmResult)
    }
}
