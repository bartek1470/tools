package pl.bartek.scripts

import com.github.ajalt.clikt.core.subcommands
import pl.bartek.scripts.executesql.ExecuteSql
import pl.bartek.scripts.jira.ReportWorklog

fun main(args: Array<String>) {
    Scripts()
        .subcommands(
            ExecuteSql(),
            ReportWorklog()
        )
        .main(args)
}
