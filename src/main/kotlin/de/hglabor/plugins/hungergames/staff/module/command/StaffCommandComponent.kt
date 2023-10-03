package de.hglabor.plugins.hungergames.staff.module.command

import org.bukkit.entity.Player

class StaffCommandComponent(val name: String, var commandCallback : (sender: Player, args: List<String>) -> Unit)

inline fun staffCommand(name: String, noinline commandCallback : (sender: Player, args: List<String>) -> Unit) = StaffCommandComponent(name, commandCallback)