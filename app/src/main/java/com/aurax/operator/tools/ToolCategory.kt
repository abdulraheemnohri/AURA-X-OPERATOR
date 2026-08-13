package com.aurax.operator.tools

enum class ToolCategory {
    ANDROID, FILE, MEDIA, COMMUNICATION, WEB, SYSTEM, CALENDAR, CONTACTS,
    LOCATION, CLIPBOARD, CALCULATOR, TIMER, NOTES, KNOWLEDGE, CUSTOM
}

enum class ToolRisk { LOW, MEDIUM, HIGH, BLOCKED }

data class ToolDescriptor(
    val id: String,
    val title: String,
    val category: ToolCategory,
    val risk: ToolRisk,
    val requiresConfirmation: Boolean,
    val description: String
)

object BuiltInToolCatalog {
    val tools = listOf(
        ToolDescriptor("take_photo", "Take photo", ToolCategory.MEDIA, ToolRisk.LOW, false, "Open the camera and let the user capture an image."),
        ToolDescriptor("read_sms", "Read SMS", ToolCategory.COMMUNICATION, ToolRisk.HIGH, true, "Read recent messages after explicit confirmation."),
        ToolDescriptor("create_calendar_event", "Create calendar event", ToolCategory.CALENDAR, ToolRisk.MEDIUM, true, "Create a calendar event with user confirmation."),
        ToolDescriptor("set_alarm", "Set alarm", ToolCategory.TIMER, ToolRisk.LOW, false, "Create a local alarm."),
        ToolDescriptor("start_timer", "Start timer", ToolCategory.TIMER, ToolRisk.LOW, false, "Start a countdown timer."),
        ToolDescriptor("get_location", "Get coarse location", ToolCategory.LOCATION, ToolRisk.MEDIUM, true, "Read the device's coarse location."),
        ToolDescriptor("read_contact", "Read contact", ToolCategory.CONTACTS, ToolRisk.HIGH, true, "Search contacts after explicit confirmation."),
        ToolDescriptor("calculate", "Calculate", ToolCategory.CALCULATOR, ToolRisk.LOW, false, "Evaluate a bounded mathematical expression."),
        ToolDescriptor("convert_unit", "Convert unit", ToolCategory.CALCULATOR, ToolRisk.LOW, false, "Convert common units locally."),
        ToolDescriptor("translate", "Translate", ToolCategory.KNOWLEDGE, ToolRisk.LOW, false, "Translate text with the local model."),
        ToolDescriptor("summarize_text", "Summarize", ToolCategory.KNOWLEDGE, ToolRisk.LOW, false, "Summarize supplied text locally."),
        ToolDescriptor("create_folder", "Create folder", ToolCategory.FILE, ToolRisk.LOW, false, "Create a folder through Android's Storage Access Framework."),
        ToolDescriptor("move_file", "Move file", ToolCategory.FILE, ToolRisk.MEDIUM, true, "Move a selected file after confirmation."),
        ToolDescriptor("delete_file", "Delete file", ToolCategory.FILE, ToolRisk.HIGH, true, "Delete a selected file; confirmation is mandatory."),
        ToolDescriptor("share_text", "Share text", ToolCategory.CLIPBOARD, ToolRisk.LOW, false, "Open Android's share sheet with supplied text."),
        ToolDescriptor("scan_qr", "Scan QR", ToolCategory.MEDIA, ToolRisk.LOW, false, "Open a QR scanning flow."),
        ToolDescriptor("open_app", "Open app", ToolCategory.ANDROID, ToolRisk.LOW, false, "Launch an installed application."),
        ToolDescriptor("device_info", "Device info", ToolCategory.SYSTEM, ToolRisk.LOW, false, "Read non-sensitive device capabilities."),
        ToolDescriptor("search_memory", "Search memory", ToolCategory.KNOWLEDGE, ToolRisk.LOW, false, "Search local semantic memory."),
        ToolDescriptor("search_knowledge", "Search knowledge base", ToolCategory.KNOWLEDGE, ToolRisk.LOW, false, "Retrieve local document chunks."),
        ToolDescriptor("send_sms", "Send SMS", ToolCategory.COMMUNICATION, ToolRisk.BLOCKED, true, "Reserved for an explicit future implementation; never auto-send."),
        ToolDescriptor("payment", "Payment action", ToolCategory.SYSTEM, ToolRisk.BLOCKED, true, "Payments and financial transactions are permanently blocked."),
        ToolDescriptor("password_input", "Password input", ToolCategory.WEB, ToolRisk.BLOCKED, true, "Password and credential entry is permanently blocked.")
    )

    fun find(id: String): ToolDescriptor? = tools.firstOrNull { it.id == id }
}
