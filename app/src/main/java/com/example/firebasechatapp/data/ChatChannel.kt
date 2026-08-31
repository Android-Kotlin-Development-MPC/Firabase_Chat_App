package com.example.firebasechatapp.data

/**
 * Available chat channels. Each channel maps to its own Firebase path
 * under /messages, e.g. /messages/general and /messages/mpc.
 */
enum class ChatChannel(val displayName: String, val pathKey: String) {
    GENERAL("General Chat", "general"),
    MPC("MPC Lab Study", "mpc")
}
