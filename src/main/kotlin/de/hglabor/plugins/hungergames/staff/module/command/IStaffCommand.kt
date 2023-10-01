package de.hglabor.plugins.hungergames.staff.module.command

interface IStaffCommand {
    val command: StaffCommandComponent
    val commandUsage: String
    val description: String
}