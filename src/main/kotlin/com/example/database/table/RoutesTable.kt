package com.example.database.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

internal object SavedRoutes : Table("saved_routes") {
    val id: Column<String> = varchar("id", 64)
    val userId: Column<String> = varchar("user_id", 64).index()
    val title: Column<String> = varchar("title", 255)
    val type: Column<String> = varchar("type", 64)
    val points: Column<String> = text("points")
    val placeIds: Column<String> = text("place_ids")
    val freeTimeHours: Column<Int> = integer("free_time_hours")
    val humanSpeedKmH: Column<Int> = integer("human_speed_km_h")
    val routeInfo: Column<String> = text("route_info")
    val createdAt: Column<LocalDateTime> = datetime("created_at").default(LocalDateTime.now())
    val updatedAt: Column<LocalDateTime> = datetime("updated_at").default(LocalDateTime.now())
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
