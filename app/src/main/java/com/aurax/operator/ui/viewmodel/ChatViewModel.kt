package com.aurax.operator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurax.operator.agent.execution.TaskExecutor
import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.entities.MessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val dao: AuraDao,
    private val executor: TaskExecutor
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch { refresh() }
    }

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
                dao.addMessage(userMessage)
                _messages.value = _messages.value + userMessage

                val response = executor.execute(input)
                val aiMessage = MessageEntity(conversationId = 0L, role = "assistant", content = response)
                dao.addMessage(aiMessage)
                _messages.value = _messages.value + aiMessage
            } catch (error: Throwable) {
                _error.value = error.message ?: "Unable to process message"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
