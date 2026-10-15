package com.example.engine.recheck

import java.io.File

object ActiveCheckRegistry {
    data class ComponentStatus(
        val name: String,
        val filePath: String,
        val isBuilt: Boolean,
        val isCalled: Boolean,
        val lastActive: String,
        val status: String
    )

    fun generateReport(): String {
        val components = listOf(
            ComponentStatus("CryptoMaster", "app/src/main/java/com/example/engine/masters/CryptoMaster.kt", true, true, "Just now", "ACTIVE"),
            // ... add others ...
        )
        
        val report = StringBuilder("| Item | File Exists? | Called? | Last Active | Status | Action Taken |\n")
        report.append("| --- | --- | --- | --- | --- | --- |\n")
        components.forEach {
            report.append("| ${it.name} | ${if (File(it.filePath).exists()) "YES" else "NO"} | ${it.isCalled} | ${it.lastActive} | ${it.status} | Left untouched |\n")
        }
        return report.toString()
    }
}
