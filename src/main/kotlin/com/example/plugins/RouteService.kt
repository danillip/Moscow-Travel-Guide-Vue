package com.example.plugins

import com.example.model.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteService @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    fun getRoutes(userId: String, page: Int, limit: Int): SavedRoutesResponse {
        val rows = transaction {
            SavedRoutes.selectAll()
                .where { SavedRoutes.userId eq userId }
                .orderBy(SavedRoutes.createdAt)
                .toList()
        }

        val total = rows.size
        val offset = (page - 1) * limit
        val items = rows.drop(offset).take(limit).map { rowToRoute(it) }

        return SavedRoutesResponse(
            items = items,
            meta = PlacesMeta(
                page = page,
                limit = limit,
                total = total,
                hasMore = page * limit < total
            )
        )
    }

    fun getRouteById(userId: String, routeId: String): SavedRoute? {
        val row = transaction {
            SavedRoutes.selectAll()
                .where { (SavedRoutes.userId eq userId) and (SavedRoutes.id eq routeId) }
                .firstOrNull()
        } ?: return null
        return rowToRoute(row)
    }

    fun createRoute(userId: String, request: CreateRouteRequest): SavedRoute {
        val id = "route_${UUID.randomUUID()}"
        val now = LocalDateTime.now()
        val pointsJson = json.encodeToString(request.points)
        val placeIdsJson = json.encodeToString(request.placeIds)
        val routeInfoJson = json.encodeToString(request.routeInfo)

        transaction {
            SavedRoutes.insert {
                it[SavedRoutes.id] = id
                it[SavedRoutes.userId] = userId
                it[SavedRoutes.title] = request.title
                it[SavedRoutes.type] = request.type
                it[SavedRoutes.points] = pointsJson
                it[SavedRoutes.placeIds] = placeIdsJson
                it[SavedRoutes.freeTimeHours] = request.freeTimeHours
                it[SavedRoutes.humanSpeedKmH] = request.humanSpeedKmH
                it[SavedRoutes.routeInfo] = routeInfoJson
                it[SavedRoutes.createdAt] = now
                it[SavedRoutes.updatedAt] = now
            }
        }

        return getRouteById(userId, id)!!
    }

    fun updateRoute(userId: String, routeId: String, request: UpdateRouteRequest): SavedRoute? {
        val existing = getRouteById(userId, routeId) ?: return null
        val now = LocalDateTime.now()

        transaction {
            SavedRoutes.update({ (SavedRoutes.userId eq userId) and (SavedRoutes.id eq routeId) }) {
                request.title?.let { v -> it[SavedRoutes.title] = v }
                request.type?.let { v -> it[SavedRoutes.type] = v }
                request.points?.let { v -> it[SavedRoutes.points] = json.encodeToString(v) }
                request.placeIds?.let { v -> it[SavedRoutes.placeIds] = json.encodeToString(v) }
                request.freeTimeHours?.let { v -> it[SavedRoutes.freeTimeHours] = v }
                request.humanSpeedKmH?.let { v -> it[SavedRoutes.humanSpeedKmH] = v }
                request.routeInfo?.let { v -> it[SavedRoutes.routeInfo] = json.encodeToString(v) }
                it[SavedRoutes.updatedAt] = now
            }
        }

        return getRouteById(userId, routeId)
    }

    fun deleteRoute(userId: String, routeId: String): Boolean {
        val deleted = transaction {
            SavedRoutes.deleteWhere { (SavedRoutes.userId eq userId) and (SavedRoutes.id eq routeId) }
        }
        return deleted > 0
    }

    private fun rowToRoute(row: org.jetbrains.exposed.sql.ResultRow): SavedRoute {
        val iso = { dt: LocalDateTime ->
            dt.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        }
        return SavedRoute(
            id = row[SavedRoutes.id],
            userId = row[SavedRoutes.userId],
            title = row[SavedRoutes.title],
            type = row[SavedRoutes.type],
            points = json.decodeFromString(row[SavedRoutes.points]),
            placeIds = json.decodeFromString(row[SavedRoutes.placeIds]),
            freeTimeHours = row[SavedRoutes.freeTimeHours],
            humanSpeedKmH = row[SavedRoutes.humanSpeedKmH],
            routeInfo = json.decodeFromString(row[SavedRoutes.routeInfo]),
            createdAt = iso(row[SavedRoutes.createdAt]),
            updatedAt = iso(row[SavedRoutes.updatedAt])
        )
    }
}
