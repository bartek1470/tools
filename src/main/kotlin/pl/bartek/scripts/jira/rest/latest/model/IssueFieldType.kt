package pl.bartek.scripts.jira.rest.latest.model

enum class IssueFieldType(val id: String) {

    SUMMARY("summary"),
    DESCRIPTION("description"),
    WORKLOG("worklog");
}
