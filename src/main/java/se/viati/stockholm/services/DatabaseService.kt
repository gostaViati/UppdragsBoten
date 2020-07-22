package se.viati.stockholm.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import javax.annotation.PostConstruct

@Service
class DatabaseService {

    @Value("\${email.db.file}")
    private lateinit var emailsDbFile: String

    private val logger = LoggerFactory.getLogger(SlackService::class.java)

    @PostConstruct
    fun init() {
        logger.info("Using emails DB file at: $emailsDbFile")
        count()?.let { count ->
            logger.info("DB file contains $count emails.")
        }
    }

    fun isDatabaseInitialized(): Boolean {
        return count()?.let { true } ?: false
    }

    private fun dropDatabase(): Boolean {
        return executeUpdate("drop table IF EXISTS email")
    }

    fun createDatabaseIfNotExists(initialId: String): Boolean {
        executeUpdate("create table IF NOT EXISTS email (id TEXT UNIQUE, seen TEXT default CURRENT_TIMESTAMP)")
        return insertId(initialId)
    }

    fun existsId(id: String): Boolean {
        return executeStatement { statement ->
            try {
                val rs = statement.executeQuery("select * from email where id='$id'")
                rs.next()
            } catch (e: SQLException) {
                e.printStackTrace()
                false
            }
        }?: false
    }

    fun count(): Long? {
        return executeStatement { statement ->
            try {
                val rs = statement.executeQuery("select count(*) from email")
                rs.getLong(1)
            } catch (e: SQLException) {
                logger.error(e.message)
                null
            }
        }
    }

    fun insertIdIfNotExists(id: String) {
        if (!existsId(id)) insertId(id)
    }

    fun insertId(id: String): Boolean {
        return executeUpdate("insert into email (id) values('$id')")
    }

    private fun executeUpdate(sql: String): Boolean {
        return executeStatement { statement ->
            try {
                statement.executeUpdate(sql)
                 true
            } catch (e: SQLException) {
                e.printStackTrace()
                false
            }
        }!!
    }

    private fun <T> executeStatement(statementConsumer: (Statement) -> T): T? {
        if (!File(emailsDbFile).exists()) {
            logger.error("No database file at $emailsDbFile")
            return null
        }

        var connection: Connection? = null
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:$emailsDbFile")
            val statement = connection!!.createStatement()
            statement.queryTimeout = 10  // set timeout to 10 sec.

            return statementConsumer(statement)
        } catch (e: SQLException) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            logger.error(e.message)
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                // connection close failed.
                logger.error(e.message)
            }
        }
        return null
    }
}
