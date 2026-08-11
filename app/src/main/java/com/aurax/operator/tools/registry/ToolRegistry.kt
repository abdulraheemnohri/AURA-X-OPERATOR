package com.aurax.operator.tools.registry
interface AgentTool{val id:String;val riskLevel:RiskLevel;suspend fun execute(args:Map<String,String>):com.aurax.operator.core.common.ToolResult}
enum class RiskLevel{LOW,MEDIUM,HIGH}
class ToolRegistry(private val tools:List<AgentTool>){fun get(id:String)=tools.firstOrNull{it.id==id}}