package com.jcibravo.servercarts
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.jcibravo.signs.SignActionComment
import com.jcibravo.signs.SignActionNextStop
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin


class ServerCarts: JavaPlugin() {
    private lateinit var apiServer: APIServer
    private val signs = SignLogs(this)
    private var dmapTracker: DynmapTracker? = null

    override fun onEnable() {
        this.saveDefaultConfig()

        val version = Bukkit.getServer().version
        if (
            version.contains("1.20") ||
            version.contains("1.21") ||
            version.contains("1.22")
        ) {
            this
                .getCommand("servercarts")!!
                .setExecutor(ServerCartsCommands(this))

            SignAction.register(SignActionComment())
            SignAction.register(SignActionNextStop())

            logger.info("ServerCarts plugin started.")
            logger.warning("The ServerCarts' server will start after the server is completely loaded!")
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, {
                //Get the signs
                signs.start()

                //Start API server
                logger.info("Starting ServerCarts server...")
                apiServer = APIServer(this, signs)
                apiServer.start(
                    this.config.getInt("apiPort", 25570)
                )

                //Start Dynmap server
                logger.info("Starting ServerCarts' Dynmap train tracker...")
                val dynmap = server.pluginManager.getPlugin("dynmap")
                if (dynmap != null) {
                    dmapTracker = DynmapTracker(this, dynmap)
                    dmapTracker!!.start()
                } else logger.warning("ServerCarts' Dynmap train tracker has been disabled: No dynmap plugin detected.")
            }, 1L)
        } else {
            logger.severe("This plugin is not compatible with your current minecraft version $version. Disabling ServerCarts...")
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        apiServer.stop()
        dmapTracker?.stop()

        SignAction.unregister(SignActionComment())
        SignAction.unregister(SignActionNextStop())
        logger.info("ServerCarts plugin disabled.")
    }
}