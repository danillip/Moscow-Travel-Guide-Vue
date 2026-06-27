package com.example.database.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

internal object Users : Table("users") {
    val id: Column<String> = varchar("id", 64)
    val name: Column<String> = varchar("name", 255)
    val email: Column<String> = varchar("email", 255).index(isUnique = true)
    val passwordHash: Column<String> = varchar("password_hash", 255)
    val createdAt: Column<LocalDateTime> = datetime("created_at").default(LocalDateTime.now())
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
