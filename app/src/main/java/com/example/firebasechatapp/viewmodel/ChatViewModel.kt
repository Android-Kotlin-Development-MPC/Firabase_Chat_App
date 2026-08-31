package com.example.firebasechatapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebasechatapp.data.ChatChannel
import com.example.firebasechatapp.data.ChatMessage
import com.example.firebasechatapp.data.UserSession
import com.example.firebasechatapp.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the chat screen.
 *
 * - [selectedChannel] / [onChannelSelected] drive the multi-channel selector.
 * - Switching channels cancels the previous Firebase listener via
 *   [flatMapLatest] and starts streaming from the new node.
 * - [deleteMessage] removes a message a user owns (triggered by long-press).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val userSession = UserSession(application)

    val currentUserId: String get() = userSession.userId
    val currentUserName: String get() = userSession.displayName

    // --- Channel selection ---

    private val _selectedChannel = MutableStateFlow(ChatChannel.GENERAL)
    val selectedChannel: StateFlow<ChatChannel> = _selectedChannel.asStateFlow()

    /** Switches the active channel and re-points all reads/writes to it. */
    fun onChannelSelected(channel: ChatChannel) {
        if (channel == _selectedChannel.value) return
        repository.switchChannel(channel)
        _selectedChannel.value = channel
    }

    // --- Message stream (auto-resubscribes when the channel changes) ---

    val messages: StateFlow<List<ChatMessage>> = _selectedChannel
        .flatMapLatest { channel ->
            repository.switchChannel(channel)
            repository.observeMessages()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // --- Composer input ---

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    fun onDraftChange(value: String) {
        _draft.value = value
    }

    // --- Feedback (Snackbar) ---

    private val _feedback = MutableSharedFlow<String>()
    val feedback: SharedFlow<String> = _feedback.asSharedFlow()

    // --- Actions ---

    fun sendMessage() {
        val text = _draft.value.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(text, userSession.userId, userSession.displayName)
                _draft.value = ""
            } catch (e: Exception) {
                _feedback.emit("Failed to send message: ${e.message}")
            }
        }
    }

    /** Deletes a message the current user owns. No-op for other users' messages. */
    fun deleteMessage(message: ChatMessage) {
        if (!message.isOwnedBy(userSession.userId)) return
        viewModelScope.launch {
            try {
                repository.deleteMessage(message.id)
                _feedback.emit("Message deleted")
            } catch (e: Exception) {
                _feedback.emit("Failed to delete message: ${e.message}")
            }
        }
    }
}
