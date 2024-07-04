package com.jcibravo.servercarts.signs

import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.signactions.SignActionType
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions

class SignActionNextStop: SignAction() {
    override fun match(info: SignActionEvent): Boolean {
        return info.isType("scn")
    }

    override fun canSupportRC(): Boolean {
        return false
    }

    override fun build(event: SignChangeActionEvent): Boolean {
        return if (!event.isType("scn")) {
            false
        } else SignBuildOptions.create()
            /*You built a...*/   .setName("ServerCarts™ next stop/ETA getter") /*...sign.*/
            /*This sign can...*/ .setDescription("set the next station and/or the estimated time of arrival (in minutes) of the train or delete it. The changes will be visible on the ServerCarts™ API")
                                 .setPermission("servercarts.signs")
                                 .handle(event.player)
    }

    override fun execute(info: SignActionEvent) {
        if (info.isTrainSign && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (info.isPowered) {
                val group = info.group
                if (group != null) {
                    val argsLine1 = info.getLine(1).split(" ")
                    if (argsLine1.size == 3) {
                        if (argsLine1[1].equals("stop", true)) {
                            val station = info.lines.toList()
                                .subList(2, info.lines.size)
                                .joinToString("")
                                .replace("_", "\\_")
                                .replace(" ", "_")

                            if (argsLine1[2].equals("set", true)) {
                                for (tag in info.group.properties.tags) {
                                    if (tag.startsWith("next@", true)) {
                                        info.group.properties.removeTags(tag)
                                    }
                                }

                                info.group.properties.addTags("next@$station")
                            } else if (argsLine1[2].equals("delete", true)) {
                                for (tag in info.group.properties.tags) {
                                    if (tag.startsWith("next@", true) || tag.startsWith("eta@", true)) {
                                        info.group.properties.removeTags(tag)
                                    }
                                }
                            }
                        } else if (argsLine1[1].equals("eta", true)) {
                            if (argsLine1[2].equals("set", true)) {
                                try {
                                    val time = info.getLine(2).toInt()
                                    for (tag in info.group.properties.tags) {
                                        if (tag.startsWith("eta@", true)) {
                                            info.group.properties.removeTags(tag)
                                        }
                                    }

                                    if (time > 0) info.group.properties.addTags("eta@$time")
                                } catch (_: Exception) {
                                    // Do nothing
                                }
                            } else if (argsLine1[2].equals("delete", true)) {
                                for (tag in info.group.properties.tags) {
                                    if (tag.startsWith("eta@", true)) {
                                        info.group.properties.removeTags(tag)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}