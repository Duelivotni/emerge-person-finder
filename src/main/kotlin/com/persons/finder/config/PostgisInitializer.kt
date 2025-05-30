package com.persons.finder.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class PostgisInitializer(private val jdbcTemplate: JdbcTemplate) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Ensures the Postgis extension is enabled in the PostgreSQL on app startup.
     * Postgis is required for spatial capabilities to store and query geographical data like points and distances efficiently.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun enablePostgisExtension() {
        val sql = "create extension if not exists postgis;"
        try {
            jdbcTemplate.execute(sql)
            log.info("PostGIS extension ensured to be enabled.")
        } catch (e: Exception) {
            log.error("Failed to enable PostGIS extension: {}", e.message)
        }
    }
}