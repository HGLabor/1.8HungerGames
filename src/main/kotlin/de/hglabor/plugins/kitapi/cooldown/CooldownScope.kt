package de.hglabor.plugins.kitapi.cooldown

class CooldownScope {
    var shouldApply = true

    /**
     * Cancels the cooldown (it won't be applied).
     */
    fun cancelCooldown() {
        shouldApply = false
    }

    /**
     * Reapplies the cooldown, if it was previously cancelled.
     */
    fun applyCooldown() {
        shouldApply = true
    }
}
