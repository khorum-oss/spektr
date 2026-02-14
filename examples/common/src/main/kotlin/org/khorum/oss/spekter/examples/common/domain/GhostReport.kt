package org.khorum.oss.spekter.examples.common.domain

data class GhostReport(
    val numberOfGhosts: Int,
    val evidence: String? = null
)

data class CreateGhostReportRequest(
    val numberOfGhosts: Int = 1,
    val evidence: String? = null
)