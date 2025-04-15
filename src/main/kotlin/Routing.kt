package com.example

import com.example.DTO.FlashcardDTO
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Flashcards
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.*
import DTO.RegisterRequest
import DTO.AuthResponse
import com.example.DTO.FlashcardResponse
import com.example.DTO.FlashcardResponseDTO
import com.example.DTO.LoginResponse
import models.Users
import org.jetbrains.exposed.sql.*
import com.example.models.FlashcardType
import org.jetbrains.exposed.dao.id.EntityID


fun Application.configureRouting() {
    routing {

        get("/flashcards") {
            val userId = call.request.queryParameters["userId"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing userId")
                return@get
            }

            val flashcards = transaction {
                Flashcards.select { Flashcards.userId eq userId }
                    .map {
                        FlashcardResponse(
                            id = it[Flashcards.id].value,
                            question = it[Flashcards.question],
                            answer = it[Flashcards.answer],
                            type = it[Flashcards.type].toString(),
                            options = it[Flashcards.options]?.split(";") ?: emptyList(),
                            locations = it[Flashcards.locations]?.split(";") ?: emptyList(),
                            isCorrect = it[Flashcards.isCorrect]
                        )
                    }
            }

            call.respond(flashcards)
        }



        post("/flashcards") {
            val request = call.receive<FlashcardDTO>()
            transaction {
                Flashcards.insert {
                    it[question] = request.question
                    it[answer] = request.answer
                    it[userId] = request.userId
                    it[type] = request.type
                    it[options] = request.options?.joinToString(";")
                    val lLocations = request.locations?.joinToString(";")
                    if(!lLocations.isNullOrBlank() && lLocations.length > 6){
                        it[locations] = lLocations
                    }
                    it[isCorrect] = request.isCorrect
                }
            }
            call.respond(HttpStatusCode.Created)
        }


        post("/register") {
            val request = call.receive<RegisterRequest>()

            val passwordHash = request.password.hashCode().toString()

            val exists = transaction {
                Users.select { Users.username eq request.username }.any()
            }

            if (exists) {
                call.respond(HttpStatusCode.Conflict, AuthResponse("Usuário já existe"))
                return@post
            }

            transaction {
                Users.insert {
                    it[Users.username] = request.username
                    it[Users.passwordHash] = request.password.hashCode().toString()
                }
            }

            call.respond(HttpStatusCode.Created, AuthResponse("Usuário registrado com sucesso"))
        }

        post("/login") {
            val request = call.receive<RegisterRequest>()
            val passwordHash = request.password.hashCode().toString()

            val user = transaction {
                Users.select {
                    Users.username eq request.username and (Users.passwordHash eq passwordHash)
                }.singleOrNull()
            }

            if (user != null) {
                call.respond(LoginResponse("Login bem-sucedido", user[Users.id]))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse("Credenciais inválidas"))
            }

        }

        put("/flashcards/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid ID")
                return@put
            }

            val request = call.receive<FlashcardDTO>()

            transaction {
                Flashcards.update({ Flashcards.id eq id }) {
                    it[question] = request.question
                    it[answer] = request.answer
                    it[userId] = request.userId
                    it[type] = FlashcardType.valueOf(request.type.name)
                    it[options] = request.options?.joinToString(";")
                    val lLocations = request.locations?.joinToString(";")
                    if(!lLocations.isNullOrBlank() && lLocations.length > 6){
                        it[locations] = lLocations
                    }
                    it[isCorrect] = request.isCorrect
                }
            }

            call.respond(HttpStatusCode.OK)
        }


    }
}
