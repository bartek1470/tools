package pl.bartek.scripts.jira

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.validate
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import pl.bartek.scripts.isEqualOrAfter
import pl.bartek.scripts.isEqualOrBefore
import pl.bartek.scripts.jira.rest.latest.JiraRestApiLatestClient
import pl.bartek.scripts.jira.rest.latest.model.Issue
import pl.bartek.scripts.jira.rest.latest.model.IssueFieldType
import pl.bartek.scripts.jira.rest.latest.model.WorklogItem
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

private val jiraDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
private val clock = Clock.systemDefaultZone()

class ReportWorklog : CliktCommand() {

    private val host by option("-h", "--host").required()
    private val email by option("-e", "--email").required()
    private val token by option("-t", "--token").prompt(hideInput = true)
    private val startDateOption by option("--start-date").help("Date in format yyyy-MM-dd").validate { isValidDate(it) }
    private val endDateOption by option("--end-date").help("Date in format yyyy-MM-dd").validate { isValidDate(it) }

    override fun run() {
        val currentDate = LocalDate.now(clock)
        val startDate = findStartDate()
        val endDate = findEndDate(startDate)
        if (startDate.isAfter(endDate)) {
            println("Start date cannot be after end date")
            return
        }

        val client = JiraRestApiLatestClient(host = host, token = token, email = email)
        runBlocking {
            val issuesWithTime = fetchIssuesWithTotalTime(client, startDate, endDate)
            val totalTime = issuesWithTime.sumOf { it.second.sum() }
            val daysUntilCurrentDate = startDate.until(currentDate, ChronoUnit.DAYS) + 1
            val meetingsTime = TimeUnit.MINUTES.toSeconds(15 * 2 * daysUntilCurrentDate)
            val expectedTotalTime = TimeUnit.HOURS.toSeconds(daysUntilCurrentDate * 8) - meetingsTime
            val timeDifferenceBetweenExpectedAndActual = expectedTotalTime - totalTime
            val issuesWithTimeMessage = issuesWithTime.joinToString("\n") {
                val key = it.first.key
                val summary = it.first.fields["summary"]!!
                val time = secondsToFullTime(it.second.sum())
                "$key $summary - $time"
            }
            println(
                """
                Meetings take ${secondsToFullTime(meetingsTime)}
                Total time should be ${secondsToFullTime(expectedTotalTime)} (including today, minus meetings)
                Total time spend in issues: ${secondsToFullTime(totalTime)}
                Time difference: ${secondsToFullTime(timeDifferenceBetweenExpectedAndActual)}
                Issue list with time:
                """.trimIndent()
            )
            println(issuesWithTimeMessage)
        }
    }

    private suspend fun fetchIssuesWithTotalTime(
        client: JiraRestApiLatestClient,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Pair<Issue, List<Long>>> {
        val authorAccountId = client.getCurrentUser().accountId
        val searchResult =
            client.searchByJql("worklogDate >= \"$startDate\" AND worklogDate <= \"$endDate\" AND worklogAuthor = currentUser()")
        return searchResult.issues.asSequence()
            .map { issue -> Pair(issue, issue.fields) }
            .map { issue -> Pair(issue.first, issue.second[IssueFieldType.WORKLOG.id]!!) }
            .map { issue -> Pair(issue.first, extractWorklogItems(issue.second)) }
            .map { issue ->
                val totalTimeInIssue =
                    calculateTotalTime(issue.second, startDate, endDate, authorAccountId)
                Pair(issue.first, totalTimeInIssue)
            }.toList()
    }

    private fun extractWorklogItems(issueFields: JsonElement): List<WorklogItem> {
        val worklogsJson = issueFields.jsonObject["worklogs"]!!
        val worklogs: List<WorklogItem> = JiraRestApiLatestClient.json.decodeFromJsonElement(worklogsJson)
        return worklogs
    }

    private fun findStartDate(): LocalDate =
        startDateOption?.let { LocalDate.parse(it) }
            ?: LocalDate.now(clock)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private fun findEndDate(startDate: LocalDate): LocalDate {
        if (startDateOption != null && endDateOption == null) {
            return startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
        }

        return endDateOption?.let { LocalDate.parse(it) }
            ?: LocalDate.now(clock)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
    }

    private fun calculateTotalTime(
        worklogArray: List<WorklogItem>,
        startDate: LocalDate,
        endDate: LocalDate,
        authorAccountId: String
    ) = worklogArray.asSequence()
        .filter { worklog -> isBetweenDates(worklog, startDate, endDate) }
        .filter { worklog -> worklog.author.accountId == authorAccountId }
        .map { worklog -> worklog.timeSpentSeconds }
        .map { it.toLong() }
        .toList()

    private fun isBetweenDates(
        worklog: WorklogItem,
        startDate: LocalDate,
        endDate: LocalDate
    ): Boolean {
        val started = LocalDate.parse(worklog.started, jiraDateTimeFormat)
        return started.isEqualOrAfter(startDate)
                && startDate.isEqualOrBefore(endDate)
    }

    private fun secondsToFullTime(seconds: Long): String {
        var time = seconds
        val minutesInSeconds = TimeUnit.MINUTES.toSeconds(1)
        val hoursInSeconds = TimeUnit.HOURS.toSeconds(1)

        val elapsedHours = time / hoursInSeconds
        time %= hoursInSeconds

        val elapsedMinutes = time / minutesInSeconds
        time %= minutesInSeconds

        val elapsedSeconds = time

        return "${elapsedHours}h ${elapsedMinutes}m ${elapsedSeconds}s"
    }

    private fun isValidDate(value: String) {
        //TODO [bartek1470] validate date by parsing or in another way
        value.matches("\\d{4}-\\d{2}-\\d{2}".toRegex())
    }
}


