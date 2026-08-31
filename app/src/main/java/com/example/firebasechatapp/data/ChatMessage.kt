package com.example.firebasechatapp.data

/**
 * A single chat message stored in Firebase Realtime Database.
 *
 * @param id Firebase auto-generated child key.
 * @param text Message body.
 * @param senderId Stable device/user id used to identify the author.
 * @param senderName Display name for the bubble header.
 * @param timestamp Epoch millis when the message was sent (server clock).
 */
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0L
) {
    /** Whether this message was written by the given user id. */
    fun isOwnedBy(userId: String): Boolean = senderId == userId
}
