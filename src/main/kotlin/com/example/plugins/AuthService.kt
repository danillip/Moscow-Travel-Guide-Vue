package com.example.plugins

import com.example.model.AuthResponse
import com.example.model.UserResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

object AuthService {
    fun signUp(name: String, email: String, password: String): AuthResponse {
        val existing =
            transaction { Users.selectAll().where { Users.email eq email }.firstOrNull() }
        if (existing != null) throw IllegalArgumentException("Email already registered")

        val id = "user_${UUID.randomUUID()}"
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val now = LocalDateTime.now()

        transaction {
            Users.insert {
                it[Users.id] = id
                it[Users.name] = name
                it[Users.email] = email
                it[Users.passwordHash] = hash
                it[Users.createdAt] = now
            }
        }

        return buildResponse(id, name, email, now)
    }

    fun signIn(email: String, password: String): AuthResponse {
        val row = transaction { Users.selectAll().where { Users.email eq email }.firstOrNull() }
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!BCrypt.checkpw(password, row[Users.passwordHash])) {
            throw IllegalArgumentException("Invalid email or password")
        }

        return buildResponse(
            row[Users.id],
            row[Users.name],
            row[Users.email],
            row[Users.createdAt]
        )
    }

    fun refresh(refreshToken: String): AuthResponse {
        val decoded = JWTConfig.verifier().verify(refreshToken)
        val type = decoded.getClaim("type").asString()
        if (type != "refresh") throw IllegalArgumentException("Invalid token type")

        val userId = decoded.subject
        val row = transaction { Users.selectAll().where { Users.id eq userId }.firstOrNull() }
            ?: throw IllegalArgumentException("User not found")

        return buildResponse(
            row[Users.id],
            row[Users.name],
            row[Users.email],
            row[Users.createdAt]
        )
    }

    private fun buildResponse(
        id: String,
        name: String,
        email: String,
        createdAt: LocalDateTime
    ): AuthResponse {
        val access = JWTConfig.makeAccessToken(id, email)
        val refresh = JWTConfig.makeRefreshToken(id)
        val iso = createdAt.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        return AuthResponse(
            accessToken = access,
            refreshToken = refresh,
            user = UserResponse(id = id, name = name, email = email, createdAt = iso)
        )
    }
}
