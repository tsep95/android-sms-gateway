package me.capcom.smsgateway.modules.keepalive

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

// Пустая служба спец-возможностей. Единственная задача — быть включённой,
// чтобы ОС (в т.ч. MIUI) не убивала процесс и не усыпляла его в doze.
class KeepAliveAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
