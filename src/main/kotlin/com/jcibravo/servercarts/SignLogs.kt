package com.jcibravo.servercarts

import com.jcibravo.servercarts.data.SignData
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class SignLogs(private val plugin: ServerCarts, private val informer: Player? = null) {
    private val signs = mutableListOf<SignData>()

    fun getSigns(): List<SignData> {
        return signs
    }

    fun getTCSigns(): List<SignData> {
        return signs.filter { it.isATrainCartsSign }
    }

    fun isLoaded(): Boolean {
        return signs.isNotEmpty()
    }

    fun start() {
        plugin.logger.info("Starting sign loading...")
        informer?.sendMessage("${ChatColor.DARK_AQUA} Starting sign loading...")
        load()
    }

    fun reload() {
        plugin.logger.info("Reloading...")
        informer?.sendMessage("${ChatColor.DARK_AQUA} Reloading...")
        signs.clear()
        start()
    }

    private fun load() {
        if (isLoaded()) {
            plugin.logger.warning("For performance reasons, you cannot reload the data until you turn off the server, if you really want to reload the sign data, run /servercarts reloadsigns command")
            informer?.sendMessage("${ChatColor.YELLOW} For performance reasons, you cannot reload the data until you turn off the server, if you really want to reload the sign data, run /servercarts reloadsigns command")
        } else {
            object : BukkitRunnable() {
                override fun run() {
                    plugin.logger.info("Loading the signs...")
                    informer?.sendMessage("${ChatColor.DARK_AQUA} Loading the signs...")

                    for (world in plugin.server.worlds) {
                        for (chunk in world.loadedChunks) {
                            for (blockState in chunk.tileEntities) {
                                if (blockState.block.state is Sign) {
                                    val sign = blockState.block.state as Sign
                                    val isTCSign = isTCSign(
                                        listOf(
                                            sign.getSide(Side.FRONT).lines[0],
                                            sign.getSide(Side.BACK).lines[0],
                                        )
                                    )

                                    signs.add(
                                        SignData(
                                            x = sign.x,
                                            y = sign.y,
                                            z = sign.z,
                                            isATrainCartsSign = isTCSign,
                                            world = sign.world.name,
                                            linesFront = sign.getSide(Side.FRONT).lines,
                                            linesBack = sign.getSide(Side.BACK).lines,
                                        )
                                    )
                                }
                            }
                        }
                    }

                    plugin.logger.info("Signs loaded!")
                    informer?.sendMessage("${ChatColor.DARK_AQUA} Signs loaded!")
                }
            }.runTask(plugin)
        }
    }

    private fun isTCSign(firstLines: List<String>): Boolean {
        val result = mutableListOf(false, false)
        for (i in 0..1) {
            val finalLine = firstLines[i].lowercase()
            val regex = """\[(?=(?:[^\[\]]*\b(train|cart)\b[^\[\]]*)?\][^\[\]]*)[^\[\]]*\]|\((?=(?:[^\(\)]*\b(train|cart)\b[^\(\)]*)?\)[^\(\)]*)[^\(\)]*\)""".toRegex()
            val hasOnlyBrackets = finalLine.count { it == '[' } == 1 && finalLine.count { it == ']' } == 1 && finalLine.count { it == '(' } == 0 && finalLine.count { it == ')' } == 0
            val hasOnlyParentheses = finalLine.count { it == '[' } == 0 && finalLine.count { it == ']' } == 0 && finalLine.count { it == '(' } == 1 && finalLine.count { it == ')' } == 1
            result[i] = regex.matches(finalLine) && (
                (finalLine.startsWith('[') && finalLine.endsWith(']') && hasOnlyBrackets) ||
                (finalLine.startsWith('(') && finalLine.endsWith(')') && hasOnlyParentheses)
            )
        }

        return true in result
    }
}