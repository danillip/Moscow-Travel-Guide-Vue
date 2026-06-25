package com.example.plugins

import com.example.model.AuthResponse
import com.example.model.UserResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(private val jwtService: JwtService) {

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

    fun signOut(refreshToken: String) {
        val deleted = transaction {
            RefreshTokens.deleteWhere { RefreshTokens.token eq refreshToken }
        }
        if (deleted == 0) throw IllegalArgumentException("Refresh token not found or already used")
    }

    fun refresh(refreshToken: String): AuthResponse {
        val decoded = jwtService.verifier().verify(refreshToken)
        val type = decoded.getClaim("type").asString()
        if (type != "refresh") throw IllegalArgumentException("Invalid token type")

        val userId = decoded.subject

        val deleted = transaction {
            RefreshTokens.deleteWhere { RefreshTokens.token eq refreshToken }
        }
        if (deleted == 0) throw IllegalArgumentException("Refresh token already used or expired")

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
        val access = jwtService.makeAccessToken(id, email)
        val refresh = jwtService.makeRefreshToken(id)
        val now = LocalDateTime.now()
        val expiresAt = now.plusSeconds(JwtService.REFRESH_TOKEN_EXPIRY / 1000)

        transaction {
            RefreshTokens.insert {
                it[RefreshTokens.id] = UUID.randomUUID().toString()
                it[RefreshTokens.userId] = id
                it[RefreshTokens.token] = refresh
                it[RefreshTokens.expiresAt] = expiresAt
                it[RefreshTokens.createdAt] = now
            }
        }

        val iso = createdAt.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        return AuthResponse(
            accessToken = access,
            refreshToken = refresh,
            user = UserResponse(id = id, name = name, email = email, createdAt = iso)
        )
    }
}
