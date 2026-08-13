package com.aurax.operator.voice

/** Fast-path commands that bypass the LLM for safety-critical or repetitive actions. */
sealed interface VoiceCommand {
    data object Abort : VoiceCommand
    data object Louder : VoiceCommand
    data object Repeat : VoiceCommand
    data object ForgetLastMemory : VoiceCommand
    data object Capabilities : VoiceCommand
}

object VoiceCommandRouter {
    private val abort = setOf("stop", "ruko", "abort", "cancel", "ruk jao", "stop aura")
    private val louder = setOf("louder", "zor se", "volume up", "awaaz tez")
    private val repeat = setOf("repeat", "dobara", "say again", "phir se")
    private val forget = setOf("forget that", "bhool jao", "forget this", "yeh bhool jao")
    private val capabilities = setOf("what can you do", "kya kar sakte ho", "capabilities", "help")

    fun route(text: String): VoiceCommand? {
        val normalized = text.trim().lowercase().replace(Regex("\\s+"), " ")
        return when {
            normalized in abort -> VoiceCommand.Abort
            normalized in louder -> VoiceCommand.Louder
            normalized in repeat -> VoiceCommand.Repeat
            normalized in forget -> VoiceCommand.ForgetLastMemory
            normalized in capabilities -> VoiceCommand.Capabilities
            else -> null
        }
    }
}
