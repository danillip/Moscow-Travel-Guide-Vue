package com.example.plugins

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object RefreshTokens : Table("refresh_tokens") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64)
    val token = varchar("token", 2048)
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(id)
}
