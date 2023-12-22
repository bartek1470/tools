package pl.bartek.scripts.jira.rest.latest

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pl.bartek.scripts.jira.rest.SslSettings
import pl.bartek.scripts.jira.rest.latest.model.IssueFieldType
import pl.bartek.scripts.jira.rest.latest.model.SearchRequest
import pl.bartek.scripts.jira.rest.latest.model.SearchResult
import pl.bartek.scripts.jira.rest.latest.model.UserData

class JiraRestApiLatestClient(
    protocol: String = "https",
    host: String,
    private val token: String,
    private val email: String,
) {

    private val apiVersion = "latest"
    private val baseUrl = "$protocol://$host/rest/api/$apiVersion/"
    private val httpClient = HttpClient(Java) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
            sanitizeHeader { header ->
                setOf(
                    HttpHeaders.Authorization.lowercase(),
                    HttpHeaders.SetCookie.lowercase(),
                    HttpHeaders.Cookie.lowercase(),
                    "x-asessionid",
                    "x-ausername",
                ).contains(header.lowercase())
            }
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(username = email, password = token)
                }
                realm = "Access to the '/' path"
                sendWithoutRequest { true }
            }
        }
        defaultRequest {
            url(baseUrl)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(json)
        }
        engine {
            config {
                sslContext(SslSettings.getSslContext())
            }
        }
    }

    suspend fun searchByJql(
        jql: String,
        fields: List<IssueFieldType> = listOf(IssueFieldType.SUMMARY, IssueFieldType.WORKLOG)
    ): SearchResult {
        val response = httpClient.post("search") {
            setBody(
                SearchRequest(
                    jql = jql,
                    fields = fields.map { it.id },
                )
            )
        }
        return response.body<SearchResult>()
    }

    suspend fun getCurrentUser(): UserData {
        return httpClient.get("myself").body()
    }

    companion object {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
