package pl.bartek.scripts.jira.rest.latest.model

enum class Expand(val id: String) {

    SUMMARY("summary"),
    OPERATIONS("operations"),
    VERSIONED_REPRESENTATIONS("versionedRepresentations"),
    EDIT_META("editmeta"),
    CHANGELOG("changelog"),
    RENDERED_FIELDS("renderedFields"),
    NAMES("names"),
    SCHEMA("schema");

    companion object {
        fun byId(id: String): Expand? {
            return entries.firstOrNull { it.id == id }
        }
    }
}
