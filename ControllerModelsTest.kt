package com.aicody.virtualxbox

import org.junit.Assert.*
import org.junit.Test

class ControllerModelsTest {
    @Test fun axesClampToRange() {
        assertEquals(1f, clampAxis(2f))
        assertEquals(-1f, clampAxis(-2f))
    }
    @Test fun triggersClampToRange() {
        assertEquals(0f, clampTrigger(-1f))
        assertEquals(1f, clampTrigger(2f))
    }
    @Test fun deadzoneZerosSmallInput() {
        assertEquals(0f, applyDeadzone(0.04f, 0.08f))
    }
    @Test fun deadzoneRescalesOutsideInput() {
        assertTrue(applyDeadzone(0.54f, 0.08f) > 0.4f)
    }
    @Test fun profileMappingRejectsUnknownOutput() {
        val profile = MappingProfile("test", mapOf("A" to "UNKNOWN"))
        assertNull(mapButton("A", profile))
    }
    @Test fun jsonIncludesTypeAndButtons() {
        val line = ControllerState(buttons=setOf("A")).toJsonLine()
        assertTrue(line.contains("\"type\":\"state\""))
        assertTrue(line.contains("\"A\""))
    }
}
