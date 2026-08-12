package com.aurax.operator

import com.aurax.operator.agent.planner.OperatorPlanner
import org.junit.Assert.assertEquals
import org.junit.Test

class PlannerTest {
    private val planner = OperatorPlanner()

    @Test
    fun chromeSearchIsPlanned() {
        val step = planner.plan("open Chrome and search weather").single()
        assertEquals("chrome_automation", step.tool)
        assertEquals("weather", step.args["query"])
    }

    @Test
    fun youtubeSearchIsPlanned() {
        val step = planner.plan("search youtube cats").single()
        assertEquals("youtube_automation", step.tool)
        assertEquals("cats", step.args["query"])
    }

    @Test
    fun youtubeSearchWithForIsPlanned() {
        val step = planner.plan("youtube search for funny cats").single()
        assertEquals("youtube_automation", step.tool)
        assertEquals("funny cats", step.args["query"])
    }

    @Test
    fun blankCommandsProduceNoSteps() {
        assertEquals(0, planner.plan("   ").size)
    }

    @Test
    fun packageLaunchIsPlanned() {
        val step = planner.plan("launch package com.example.app").single()
        assertEquals("android_open", step.tool)
        assertEquals("com.example.app", step.args["package"])
    }

    @Test
    fun unknownCommandsDoNotBecomeAutomation() {
        val step = planner.plan("send money").single()
        assertEquals("none", step.tool)
    }
}
