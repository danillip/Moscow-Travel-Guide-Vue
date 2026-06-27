package com.example.database.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

internal object RefreshTokens : Table("refresh_tokens") {
    val id: Column<String> = varchar("id", 64)
    val userId: Column<String> = varchar("user_id", 64)
    val token: Column<String> = varchar("token", 2048)
    val expiresAt: Column<LocalDateTime> = datetime("expires_at")
    val createdAt: Column<LocalDateTime> = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
