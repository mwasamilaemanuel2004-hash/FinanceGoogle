package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavigationGroup(val labelEn: String, val labelSw: String) {
    MAIN("MAIN HYBRID CONTROL", "UDHIBITI MKUU"),
    QUANT_ENGINE("INTELLIGENCE, FUNDI & TASKS", "AKILI, FUNDI & KAZI"),
    GOVERNANCE("GOVERNANCE, SEC & OPS", "ULINZI & UENDESHAJI")
}

enum class EsmhDestination(
    val route: String,
    val titleEn: String,
    val titleSw: String,
    val icon: ImageVector,
    val group: NavigationGroup
) {
    HOME("home", "Command Center", "Kituo Kikuu", Icons.Default.Home, NavigationGroup.MAIN),
    TRADE("trade", "Trading Terminal", "Biashara & Masoko", Icons.Default.SwapHoriz, NavigationGroup.MAIN),
    MARKETS("markets", "Market Radar", "Masoko", Icons.Default.ShowChart, NavigationGroup.MAIN),
    BOTS("bots", "Bots Registry", "Nodi za Boti", Icons.Default.SmartToy, NavigationGroup.QUANT_ENGINE),
    MOTHER_BRAIN("mother_brain", "Mother Brain AI", "Ubongo Mkuu", Icons.Default.Psychology, NavigationGroup.QUANT_ENGINE),
    OPPORTUNITIES("opportunities", "Opportunities", "Fursa za Soko", Icons.Default.TrendingUp, NavigationGroup.QUANT_ENGINE),
    PORTFOLIO("portfolio", "Portfolio", "Mkoba wa Mali", Icons.Default.AccountBalanceWallet, NavigationGroup.MAIN),
    RISK_CENTER("risk", "Risk Center", "Kituo cha Hatari", Icons.Default.Security, NavigationGroup.GOVERNANCE),
    SYSTEM_HEALTH("system_health", "System Health", "Afya ya Mfumo", Icons.Default.HealthAndSafety, NavigationGroup.GOVERNANCE),
    BACKTEST_LAB("backtest", "Backtest Lab", "Ripoti ya Jaribio", Icons.Default.Assessment, NavigationGroup.QUANT_ENGINE),
    QUANT_LAB("quant_lab", "Quant Lab", "Maabara ya Quant", Icons.Default.Science, NavigationGroup.QUANT_ENGINE),
    QUANTUM_FINANCE("quantum_finance", "Quantum Finance & 24/6", "Quantum & Wikendi", Icons.Default.AllInclusive, NavigationGroup.QUANT_ENGINE),
    INFINITE_CLUSTERS("infinite_clusters", "50K+ Pairs & Clusters", "Makundi & 50K+ Pair", Icons.Default.Hub, NavigationGroup.QUANT_ENGINE),
    COMMANDERS("commanders", "Commanders Hub", "Kituo cha Makamanda", Icons.Default.SmartToy, NavigationGroup.MAIN),
    SETTINGS("settings", "Settings", "Mipangilio", Icons.Default.Settings, NavigationGroup.GOVERNANCE),
    ADMIN_COMMAND("admin_command", "Admin Center", "Kituo cha Admin", Icons.Default.AdminPanelSettings, NavigationGroup.GOVERNANCE)
}
