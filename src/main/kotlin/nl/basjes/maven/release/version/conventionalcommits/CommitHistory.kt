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

import org.apache.maven.scm.ChangeSet
import org.apache.maven.scm.ScmFileSet
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest
import org.apache.maven.shared.release.versions.VersionParseException
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Helper class to manage the commit history of the SCM repository.
 */
class CommitHistory(request: VersionPolicyRequest, versionRules: VersionRules) {

    val changes: MutableList<ChangeSet> = mutableListOf()
    var lastVersionTag: String? = null
        private set

    fun addChanges(change: ChangeSet?) {
        this.changes.add(change!!)
    }

    private fun load(request: VersionPolicyRequest, versionRules: VersionRules) {
        val scmRepository = request.scmRepository
        val scmProvider = request.scmProvider
        val workingDirectory = request.workingDirectory

        if (scmRepository == null || scmProvider == null || workingDirectory == null) {
            // We do not have a commit history so the changes and tags will remain empty
            return
        }

        val changeLogRequest = ChangeLogScmRequest(
            scmRepository,
            ScmFileSet(File(workingDirectory))
        )

        val logger = LoggerFactory.getLogger(CommitHistory::class.java)

        var limit = 0
        while (this.lastVersionTag == null) {
            limit += 100 // Read the repository in incremental steps of 100
            changeLogRequest.setLimit(null) // Cannot set new value if it is already set
            changeLogRequest.limit = limit
            changes.clear()

            logger.debug("Checking the last {} commits.", limit)

            val changeLogSet = scmProvider.changeLog(changeLogRequest).changeLog ?: return
            val changeSets = changeLogSet.changeSets

            for (changeSet in changeSets) {
                addChanges(changeSet)

                val changeSetTags = changeSet.getTags()
                val versionTags = changeSetTags
                    .map { tag: String ->
                        val matcher = versionRules.tagPattern.matcher(tag)
                        if (matcher.find()) {
                            return@map matcher.group(1)
                        }
                        null
                    }
                    .filterNotNull()
                    .toList()

                if (versionTags.isNotEmpty()) {
                    // Found the previous release tag
                    if (versionTags.size > 1) {
                        throw VersionParseException(
                            "Most recent commit with tags has multiple version tags: "
                                    + versionTags
                        )
                    }
                    this.lastVersionTag = versionTags.first()
                    logger.debug("Found tag")
                    break // We have the last version tag
                }
            }
            if (this.lastVersionTag == null &&
                changeSets.size < limit
            ) {
                // Apparently there are simply no more commits.
                logger.debug("Did not find any tag")
                break
            }
        }
    }

    init {
        load(request, versionRules)
    }

    override fun toString(): String {
        val logLines: MutableList<String> = mutableListOf()

        logLines.add("Filtered commit history:")
        for (changeSet in changes) {
            logLines.add("-- Comment: \"${changeSet.comment}\"")
            logLines.add("   Tags   : ${changeSet.tags}")
        }

        val sb = StringBuilder()
        for (logLine in logLines) {
            sb.append(logLine).append('\n')
        }
        return sb.toString()
    }
}
