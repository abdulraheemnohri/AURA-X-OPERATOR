package com.aurax.operator.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurax.operator.agent.execution.TaskExecutor
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.entities.MessageEntity
import com.aurax.operator.memory.KnowledgeBaseManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dao: AuraDao,
    private val executor: TaskExecutor,
    @ApplicationContext context: Context
) : ViewModel() {
    private val rag = KnowledgeBaseManager(context)
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { viewModelScope.launch { refresh() } }

    suspend fun refresh() {
        _messages.value = dao.getRecentMessages(50).asReversed()
    }

    fun sendMessage(text: String) {
        val input = text.trim()
        if (input.isBlank() || _isLoading.value) return
        viewModelScope.launch {
            _error.value = null
            _isLoading.value = true
            try {
                val userMessage = MessageEntity(conversationId = 0L, role = "user", content = input)
                val userId = dao.addMessage(userMessage)
                _messages.value = _messages.value + userMessage.copy(id = userId)

                val ragContext = withContext(Dispatchers.IO) {
                    rag.search(input, 5).joinToString("\n\n") {
                        "[Knowledge: ${it.chunk.source} | score=${"%.2f".format(it.score)}]\n${it.chunk.text}"
                    }
                }
                val executionInput = if (ragContext.isBlank()) input else "Use the following local knowledge when relevant:\n$ragContext\n\nUser request:\n$input"
                val response = executor.execute(executionInput)
                val aiMessage = MessageEntity(conversationId = 0L, role = "assistant", content = response)
                val aiId = dao.addMessage(aiMessage)
                _messages.value = _messages.value + aiMessage.copy(id = aiId)
            } catch (error: Throwable) {
                _error.value = error.message ?: "Unable to process message"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
