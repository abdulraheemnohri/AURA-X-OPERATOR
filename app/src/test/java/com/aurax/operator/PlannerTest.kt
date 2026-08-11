package com.aurax.operator

import com.aurax.operator.agent.planner.OperatorPlanner
import org.junit.Assert.*
import org.junit.Test

class PlannerTest {
    private val planner = OperatorPlanner()

    @Test fun chromeSearchIsPlanned() {
        val step = planner.plan("open Chrome and search weather").single()
        assertEquals("chrome_automation", step.tool)
        assertEquals("weather", step.args["query"])
    }

    @Test fun youtubeSearchIsPlanned() {
        val step = planner.plan("search youtube cats").single()
        assertEquals("youtube_automation", step.tool)
        assertEquals("cats", step.args["query"])
    }

    @Test fun unknownCommandsDoNotBecomeAutomation() {
        val step = planner.plan("send money").single()
        assertEquals("none", step.tool)
    }
}
