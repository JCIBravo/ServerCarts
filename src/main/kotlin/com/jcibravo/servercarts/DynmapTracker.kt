package com.jcibravo.servercarts

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.dynmap.DynmapAPI
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.Marker
import org.dynmap.markers.MarkerIcon
import org.dynmap.markers.MarkerSet


class DynmapTracker(private val plugin: Plugin, dynmap: Plugin) {
    private var firstRun = true
    private val reloadTimeSec = plugin.config.getInt("trackerUpdate", 5).toLong()

    private val tcConfig = TCConfigs()
    private val dynmapMarkers = mutableListOf<Marker>()
    private val textTrainIcon = "&#128651;"
    private var taskId: Int = -1

    private val dmAPI: DynmapAPI = dynmap as DynmapAPI
    private val dmarker: MarkerAPI = dmAPI.markerAPI
    private lateinit var dmarkerSet: MarkerSet
    private lateinit var dmarkerTrainIcon: MarkerIcon

    fun start() {
        taskId = object : BukkitRunnable() {
            override fun run() {
                try {
                    if (firstRun) {
                        // First run of the Runnable, set on false so this block will run only once!
                        plugin.logger.info("Creating and loading markers...")
                        plugin.logger.info("The markers will reload after $reloadTimeSec seconds as stated in the configuration file!")
                        dmarkerSet = dmarker.getMarkerSet("servercarts.map.tracker") ?: dmarker.createMarkerSet("servercarts.map.tracker", "SC Tracker", null, false)
                        dmarkerTrainIcon = dmarker.getMarkerIcon("servercarts.map.tracker.icon") ?: dmarker.createMarkerIcon("servercarts.map.tracker.icon", "SC Tracker icon", plugin.getResource("trackerIcon.png"))

                        firstRun = false
                    } else {
                        //plugin.logger.info("Reloading markers...")
                        dmarkerSet = dmarker.getMarkerSet("servercarts.map.tracker")
                        dmarkerTrainIcon = dmarker.getMarkerIcon("servercarts.map.tracker.icon")
                        clean()
                    }

                    for (train in tcConfig.getTrains()) {
                        val label = """
                        <div style="font-family: sans-serif; margin: 2px;">
                            <h3 style="background: #303030; color: white; padding: 3px; text-align: center;">${textTrainIcon.repeat(train.carriages)} ${train.name?.capitalize() ?: train.id}</h3>
                            
                            <p><b>Train properties:</b></p>
                            <ul style="font-size: 12px;">
                                <li><b>ID:</b> ${train.id}</li>
                                ${if (train.name != null && train.name.isNotBlank()){"<li><b>Name:</b> ${train.name}</li>"} else ""}
                                <li><b>Speed:</b> ${train.speed.blocksPerTick}b/T<br><i style="font-size: 9px;">(Max. speed: ${train.speedLimit.blocksPerTick}b/T)</i></li>
                                <li><b>Nº of cars:</b> ${train.carriages}</li>
                                <li><b>Passengers:</b> ${train.passengers}</li>
                            </ul>
                            
                            ${if (train.destination != null || train.nextStop != null) "<p><b>ServerCarts' line properties:</b></p>" else ""}
                            <ul style="font-size: 12px;">
                                ${if (train.destination == null && train.nextStop == null) "<li><i>No data</i></li>" else ""}
                                ${if (train.destination != null && train.destination.isNotBlank()){"<li><b>Destination:</b> ${train.destination}</li>"} else ""}
                                ${if (train.nextStop != null && train.nextStop.isNotBlank()){"<li><b>Next stop:</b> ${train.nextStop}</li>"} else ""}                   
                            </ul>
                            
                            ${if (train.route.isNotEmpty() || train.nextDestinationRoute != null) "<p><b>TC Route properties:</b></p>" else ""}
                            <ul style="font-size: 12px;">
                                ${if (train.route.isEmpty() && train.nextDestinationRoute == null) "<li><i>No route has been set</i></li>" else ""}
                                ${if (train.route.isNotEmpty()){"<li>This train ends at ${train.route.last()}</li>"} else ""}
                                ${if (train.nextDestinationRoute != null && train.nextDestinationRoute.isNotBlank()){"<li>The next stop in the route is ${train.nextDestinationRoute}</li>"} else ""}
                            </ul>
                        </div>
                        """.trimIndent()

                        dynmapMarkers.add(
                            dmarkerSet.createMarker("servercarts.map.tracker.train.${train.id}", label, true, train.world, train.x.toDouble(), train.y.toDouble(), train.z.toDouble(), dmarkerTrainIcon, false)
                        )
                    }
                } catch (e: Exception) {
                    plugin.logger.warning("EXCEPTION FOUND ON DYNMAP TRAIN TRACKER!")
                    e.printStackTrace()

                    plugin.logger.info("In order to avoid future problems and console spam, the ServerCarts' Dynmap Train tracker functionality will be stopped and it will not be available until the server starts again.")
                    stop()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * reloadTimeSec).taskId
    }

    fun stop() {
        plugin.logger.info("Stopping ServerCarts' Dynmap train tracker...")
        if (taskId != -1) {
            plugin.logger.info("(runnable task id was $taskId)")
            plugin.server.scheduler.cancelTask(taskId)

            taskId = -1
        }

        clean()
    }

    private fun clean() {
        dynmapMarkers.forEach{ marker -> marker.deleteMarker()}
        dynmapMarkers.clear()
    }
}