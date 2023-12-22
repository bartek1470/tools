package pl.bartek.scripts.executesql

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class ExecuteSql : CliktCommand() {

    private val host by option("-h", "--host").required()
    private val port by option("-p", "--port").int().required()
    private val username by option("-u", "--user").required()
    private val password by option("--password").prompt(hideInput = true)
    private val excludeDb by option("-e", "--exclude").multiple()
    private val sql by argument("--sql")

    override fun run() {
        loadDbDriver()
        val dbNames = executeQuery(
            dbName = "postgres",
            query = """
            select datname
            from pg_database
            where datistemplate = false;""".trimIndent()
        ) { resultSet -> resultSet.getString(1) }

        val result = dbNames
            .filter { dbName -> !excludeDb.contains(dbName) }
            .map { dbName ->
                executeQuery(
                    dbName = dbName,
                    query = sql
                ) { resultSet ->
                    val columnCount = resultSet.metaData.columnCount
                    val result = mutableMapOf<String, Any>()
                    for (i in 1..columnCount) {
                        val label = resultSet.metaData.getColumnLabel(i)
                        val value = resultSet.getObject(i)
                        result[label] = value
                    }
                    result.toMap()
                }
            }

        println(result)
    }

    private fun <T> executeQuery(
        dbName: String,
        @Language("postgresql") query: String,
        resultTransformer: (ResultSet) -> T
    ): List<T> {
        val url = "jdbc:postgresql://$host:$port/$dbName";
        return DriverManager
            .getConnection(url, username, password)
            .use { connection: Connection ->
                val resultSet = connection.prepareStatement(query).executeQuery()
                val result = mutableListOf<T>()
                while (resultSet.next()) {
                    val value = resultTransformer.invoke(resultSet)
                    result.add(value)
                }
                return@use result.toList()
            }
    }

    private fun loadDbDriver() {
        Class.forName("org.postgresql.Driver")
    }
}
