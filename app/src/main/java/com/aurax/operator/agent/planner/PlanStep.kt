package com.aurax.operator.agent.planner

/** A single, validated operator action produced by a planner. */
data class PlanStep(
    val id: String,
    val description: String,
    val tool: String,
    val args: Map<String, String> = emptyMap()
)
