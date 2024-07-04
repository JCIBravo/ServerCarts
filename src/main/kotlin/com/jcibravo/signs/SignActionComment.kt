package com.jcibravo.signs

import com.bergerkiller.bukkit.tc.events.SignActionEvent
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent
import com.bergerkiller.bukkit.tc.signactions.SignAction
import com.bergerkiller.bukkit.tc.signactions.SignActionType
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions

class SignActionComment: SignAction() {
    override fun match(info: SignActionEvent): Boolean {
        return info.isType("scc")
    }

    override fun canSupportRC(): Boolean {
        return false
    }

    override fun build(event: SignChangeActionEvent): Boolean {
        return if (!event.isType("scc")) {
            false
        } else SignBuildOptions.create()
            /*You built a...*/   .setName("ServerCarts™ comment getter") /*...sign.*/
            /*This sign can...*/ .setDescription("add, set or delete a comment that will be visible on the ServerCarts™ API")
                                 .setPermission("servercarts.signs")
                                 .handle(event.player)
    }

    override fun execute(info: SignActionEvent) {
        if (info.isTrainSign && info.isAction(SignActionType.GROUP_ENTER, SignActionType.REDSTONE_ON)) {
            if (info.isPowered) {
                val group = info.group
                if (group != null) {
                    val argsLine1 = info.getLine(1).split(" ")
                    if (argsLine1.size == 2) {
                        val text = info.lines.toList()
                            .subList(2, info.lines.size)
                            .joinToString("")
                            .replace("_", "\\_")
                            .replace(" ", "_")
                        
                        if (argsLine1[1].equals("add", true)) {
                            info.group.properties.addTags("comment@$text")
                        } else if (argsLine1[1].equals("set", true)) {
                            for (tag in info.group.properties.tags) {
                                if (tag.startsWith("comment@", true)) {
                                    info.group.properties.removeTags(tag)
                                }
                            }

                            info.group.properties.addTags("comment@$text")
                        } else if (argsLine1[1].equals("delete", true)) {
                            for (tag in info.group.properties.tags) {
                                if (tag.startsWith("comment@", true)) {
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