package com.jcibravo.servercarts

import com.bergerkiller.bukkit.tc.properties.CartProperties
import com.bergerkiller.bukkit.tc.properties.TrainProperties
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.block.Sign
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.command.ConsoleCommandSender as Console
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ServerCartsCommands(private val plugin: ServerCarts): CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        /* Command map:
            [command]       args[0]     args[1]     args[2]     args[3]
            /servercarts    reloadsigns
            /servercarts    nextstop
            /servercarts    nextstop    delete
            /servercarts    nextstop    eta         <Int>
            /servercarts    nextstop    set         <String>
            /servercarts    comment
            /servercarts    comment     delete
            /servercarts    comment     set         <String>
            /servercarts    comment     add         <String>
        */

        if (sender is Player ) {
            if (sender.hasPermission("servercarts.commands")) {
                val selectedTrain = runCatching{ CartProperties.getEditing(sender).group}.getOrNull()
                if (selectedTrain != null) {
                    if (args.size == 1) {
                        if (args[0].equals("nextstop", true)) {
                            //Display the command map of nextstop.
                            sender.sendMessage("${ChatColor.GOLD}" + "Next stop (SCN) commands:")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop ${ChatColor.RESET}Displays the help menu (this one).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop delete ${ChatColor.RESET}Deletes the next stop and it's ETA.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop eta <number> ${ChatColor.RESET}Set an estimated time of arrival (in minutes).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop set <message> ${ChatColor.RESET}Set the name of the next stop.")
                            return true
                        } else if (args[0].equals("comment", true)) {
                            //Display the command map of comment.
                            sender.sendMessage("${ChatColor.GOLD}" + "Comments (SCC) commands:")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment ${ChatColor.RESET}Displays the help menu (this one).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment delete ${ChatColor.RESET}Delete all the comments of the train.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment set <message> ${ChatColor.RESET}Replace all comments with a new one.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment add <message> ${ChatColor.RESET}Add an extra comment.")
                            return true
                        } else if (args[0].equals("reloadsigns", true)) {
                            //Reload the signs endpoint
                            SignLogs(plugin, sender).reload()
                            return true
                        } else if (args[0].equals("help", true)) {
                            sender.sendMessage("${ChatColor.YELLOW}" + "ServerCarts help:")
                            sender.sendMessage("${ChatColor.GOLD}" + "Config:")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● apiPort ${ChatColor.RESET}(Default value: 25570) Set the port you want your ServerCarts' API to start. Make sure another program is NOT using this port!")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● trackerUpdate ${ChatColor.RESET}(Default value: 5) Set how many seconds you want to wait before the tracker updates again.")

                            sender.sendMessage("${ChatColor.GOLD}" + "Commands:")
                            sender.sendMessage("${ChatColor.LIGHT_PURPLE}" + "Permission node: 'servercarts.commands'")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts reloadsigns ${ChatColor.RESET}Reload the signs endpoint")

                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop ${ChatColor.RESET}Displays the help menu (this one).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop delete ${ChatColor.RESET}Deletes the next stop and it's ETA.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop eta <number> ${ChatColor.RESET}Set an estimated time of arrival (in minutes).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts nextstop set <message> ${ChatColor.RESET}Set the name of the next stop.")

                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment ${ChatColor.RESET}Displays the help menu (this one).")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment delete ${ChatColor.RESET}Delete all the comments of the train.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment set <message> ${ChatColor.RESET}Replace all comments with a new one.")
                            sender.sendMessage("${ChatColor.DARK_GREEN}● /servercarts comment add <message> ${ChatColor.RESET}Add an extra comment.")

                            return true
                        } else return false
                    } else if (args.size == 2) {
                        if (args[0].equals("nextstop", true) && args[1].equals("delete", true)) {
                            // Command /servercarts nextstop delete
                            for (tag in selectedTrain.properties.tags) {
                                //Search the next station and delete it, the ETA will be deleted too
                                if (tag.startsWith("next@", true)) {
                                    selectedTrain.properties.removeTags(tag)
                                } else if (tag.startsWith("eta@", true)) {
                                    selectedTrain.properties.removeTags(tag)
                                }
                            }

                            return true
                        } else if (args[0].equals("comment", true) && args[1].equals("delete", true)) {
                            // Command /servercarts comment delete
                            for (tag in selectedTrain.properties.tags) {
                                //Search all comments and delete it
                                if (tag.startsWith("comment@", true)) {
                                    selectedTrain.properties.removeTags(tag)
                                }
                            }

                            return true
                        } else return false
                    } else if (args.size > 2) {
                        val text = args.toList()
                            .subList(2, args.size)
                            .joinToString(" ")
                            .replace("_", "\\_")
                            .replace(" ", "_")

                        if (args[0].equals("nextstop", true) && args[1].equals("set", true)) {
                            // Command /servercarts nextstop set <message>
                            for (tag in selectedTrain.properties.tags) {
                                // Remove the previous next stop
                                if (tag.startsWith("next@", true)) {
                                    selectedTrain.properties.removeTags(tag)
                                }
                            }

                            // Add the new next stop
                            selectedTrain.properties.addTags("next@$text")

                            return true
                        } else if (args[0].equals("nextstop", true) && args[1].equals("eta", true)) {
                            // Command /servercarts nextstop eta <Int>
                            if (selectedTrain.properties.tags.any { it.startsWith("next@") }) {
                                // Setting ETA:
                                try {
                                    // We need to check if args[2] is a number.
                                    val number = args[2].toInt()

                                    // Remove the previous ETA
                                    for (tag in selectedTrain.properties.tags) {
                                        if (tag.startsWith("eta@", true)) {
                                            selectedTrain.properties.removeTags(tag)
                                        }
                                    }

                                    if (number < 1) {
                                        // If the new time remaining is set to 0 or a negative number, then remove the ETA.
                                        sender.sendMessage("${ChatColor.DARK_AQUA}" + "The Estimated Time of Arrival (ETA) has been removed.")
                                    } else {
                                        // Else, we set the new ETA.
                                        selectedTrain.properties.addTags("eta@${args[2]}")
                                        sender.sendMessage("${ChatColor.DARK_AQUA}" + "Estimated Time of Arrival (ETA) has been set to ${args[2]} min.")
                                    }
                                } catch (_: Exception) {
                                    // If args[2] is not a number, catch the error and send a warning to the user.
                                    sender.sendMessage("${ChatColor.DARK_AQUA} Please, type a number bigger than zero. Value '0' will remove the ETA")
                                }
                            } else {
                                // When setting ETA without a next station:
                                sender.sendMessage("${ChatColor.DARK_AQUA}" + "You can't set the Estimated Time of Arrival (ETA) if you don't have any next stop station set.")
                            }

                            return true
                        } else if (args[0].equals("comment", true) && args[1].equals("set", true)) {
                            // Command /servercarts comment set <message>
                            for (tag in selectedTrain.properties.tags) {
                                // Remove the previous comments
                                if (tag.startsWith("comment@", true)) {
                                    selectedTrain.properties.removeTags(tag)
                                }
                            }

                            // Set the new (and only) comment.
                            selectedTrain.properties.addTags("comment@${text}")
                            sender.sendMessage("${ChatColor.DARK_AQUA}" + "The new comment has been set to the train.")

                            return true
                        } else if (args[0].equals("comment", true) && args[1].equals("add", true)) {
                            // Command /servercarts comment add <message>
                            selectedTrain.properties.addTags("comment@${text}")
                            sender.sendMessage("${ChatColor.DARK_AQUA}" + "Another comment has been set to the train.")

                            return true
                        }
                    } else return false
                } else {
                    sender.sendMessage("${ChatColor.RED}" + "You're not editing any train, select a train by typing the '/train edit' command.")
                    return true
                }
            } else {
                sender.sendMessage("${ChatColor.RED}" + "You don't have permission to use this command. You will need the 'servercarts.commands' permission node")
                return true
            }
        } else {
            plugin.logger.info("This command can only be executed by a player.")
            return true
        }

        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        val completions = mutableListOf<String>()
        when(args.size){
            1 -> {
                val argPartial = args[0].lowercase()
                val options = listOf("comment", "nextstop", "reloadsigns")
                completions.addAll(options.filter { it.startsWith(argPartial) })
            }

            2 -> {
                val argPartial = args[1].lowercase()
                val options = if (args[0].equals("comment", true)) {
                    listOf("set", "add", "delete")
                } else if (args[0].equals("nextstop", true)) {
                    listOf("set", "delete", "eta")
                } else {
                    listOf()
                }

                completions.addAll(options.filter { it.startsWith(argPartial) })
            }

            3 -> {
                val argPartial = args[2].lowercase()
                val options = if (args[1].equals("eta", true)) {
                    (1..128).toList().map { it.toString() }
                } else if (args[1].equals("set", true) || args[1].equals("add", true)) {
                    listOf("<message>")
                } else {
                    listOf()
                }

                completions.addAll(options.filter { it.startsWith(argPartial) })
            }
        }

        if (args.size > 3) {
            val options = listOf("<message>")
            completions.addAll(options)
        }

        return completions
    }
}