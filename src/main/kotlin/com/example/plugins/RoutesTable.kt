package com.example.plugins

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object SavedRoutes : Table("saved_routes") {
    val id = varchar("id", 64)
    val userId = varchar("user_id", 64).index()
    val title = varchar("title", 255)
    val type = varchar("type", 64)
    val points = text("points")
    val placeIds = text("place_ids")
    val freeTimeHours = integer("free_time_hours")
    val humanSpeedKmH = integer("human_speed_km_h")
    val routeInfo = text("route_info")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
    override val primaryKey = PrimaryKey(id)
}
