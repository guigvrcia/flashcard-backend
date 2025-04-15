package com.example

import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import models.Flashcards
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import org.slf4j.event.Level
import models.Users

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureDatabase()
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging) {
        level = Level.INFO
    }
}

fun Application.configureDatabase() {
    Database.connect("jdbc:sqlite:data.db", driver = "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(Flashcards)
        SchemaUtils.create(Users)
    }
}
