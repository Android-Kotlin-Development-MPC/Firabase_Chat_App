package com.example.firebasechatapp.repository

import com.example.firebasechatapp.data.ChatChannel
import com.example.firebasechatapp.data.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository layer wrapping the Firebase Realtime Database.
 *
 * Messages live under /messages/{channelKey}. The active channel can be
 * switched at runtime so reads and writes target the correct node.
 */
class ChatRepository(private val database: FirebaseDatabase = FirebaseDatabase.getInstance()) {

    private var _activeChannel: ChatChannel = ChatChannel.GENERAL
    val activeChannel: ChatChannel get() = _activeChannel

    /** Changes the active channel. Called on drop-down selection changes. */
    fun switchChannel(channel: ChatChannel) {
        _activeChannel = channel
    }

    /** Reference to the current channel's message node, e.g. /messages/general. */
    private fun channelRef(): DatabaseReference =
        database.getReference("messages").child(_activeChannel.pathKey)

    /**
     * Streams messages for the currently selected channel in real time.
     * Emits the latest full snapshot whenever a child changes. The flow is
     * cancelled automatically when the collect scope is cancelled.
     */
    fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children
                    .mapNotNull { child ->
                        val text = child.child("text").getValue(String::class.java).orEmpty()
                        val senderId = child.child("senderId").getValue(String::class.java).orEmpty()
                        val senderName = child.child("senderName").getValue(String::class.java).orEmpty()
                        val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                        ChatMessage(
                            id = child.key.orEmpty(),
                            text = text,
                            senderId = senderId,
                            senderName = senderName,
                            timestamp = timestamp
                        )
                    }
                    .sortedBy { it.timestamp }
                trySendBlocking(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        channelRef().addValueEventListener(listener)
        awaitClose { channelRef().removeEventListener(listener) }
    }

    /**
     * Writes a new message to the active channel using the server clock for
     * the timestamp so ordering stays consistent across devices.
     */
    suspend fun sendMessage(text: String, senderId: String, senderName: String) {
        val ref = channelRef().push()
        ref.setValue(
            mapOf(
                "text" to text,
                "senderId" to senderId,
                "senderName" to senderName,
                "timestamp" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    /** Deletes a message from the active channel by its Firebase child key. */
    suspend fun deleteMessage(messageId: String) {
        channelRef().child(messageId).removeValue().await()
    }
}
