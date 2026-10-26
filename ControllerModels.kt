package com.aicody.virtualxbox

import org.json.JSONObject

val XBOX_BUTTONS = listOf("A", "B", "X", "Y", "LB", "RB", "BACK", "START", "L3", "R3", "UP", "DOWN", "LEFT", "RIGHT", "GUIDE")

data class ControllerState(
    val profile: String = "Default",
    val buttons: Set<String> = emptySet(),
    val leftX: Float = 0f, val leftY: Float = 0f,
    val rightX: Float = 0f, val rightY: Float = 0f,
    val leftTrigger: Float = 0f, val rightTrigger: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJsonLine(): String = JSONObject()
        .put("type", "state").put("profile", profile)
        .put("buttons", org.json.JSONArray(buttons.sorted()))
        .put("leftX", clampAxis(leftX)).put("leftY", clampAxis(leftY))
        .put("rightX", clampAxis(rightX)).put("rightY", clampAxis(rightY))
        .put("leftTrigger", clampTrigger(leftTrigger)).put("rightTrigger", clampTrigger(rightTrigger))
        .put("timestamp", timestamp).toString() + "\n"
}

data class MappingProfile(
    val name: String,
    val mapping: Map<String, String> = XBOX_BUTTONS.associateWith { it },
    val leftDeadzone: Float = 0.08f,
    val rightDeadzone: Float = 0.08f,
    val sensitivity: Float = 1f
)

fun clampAxis(value: Float): Float = value.coerceIn(-1f, 1f)
fun clampTrigger(value: Float): Float = value.coerceIn(0f, 1f)
fun applyDeadzone(value: Float, deadzone: Float): Float {
    val v = clampAxis(value)
    val d = deadzone.coerceIn(0f, 0.95f)
    return if (kotlin.math.abs(v) <= d) 0f else (kotlin.math.sign(v) * ((kotlin.math.abs(v) - d) / (1f - d))).coerceIn(-1f, 1f)
}
fun mapButton(input: String, profile: MappingProfile): String? = profile.mapping[input]
    ?.takeIf { it in XBOX_BUTTONS }
