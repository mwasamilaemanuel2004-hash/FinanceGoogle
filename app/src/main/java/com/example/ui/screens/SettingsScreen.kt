package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserApiCredentialsEntity
import com.example.data.model.AppLanguageMode
import com.example.data.model.ProfitEngineMode
import com.example.engine.MotherBrainEngine
import com.example.ui.theme.*
import kotlinx.coroutines.launch

private val SurfaceDarker = DarkCharcoal

data class SupportedBrokerItem(
    val id: String,
    val name: String,
    val category: String, // "CRYPTO", "FOREX", "DERIV", "STOCKS"
    val descriptionSwahili: String,
    val descriptionEn: String,
    val minDepositText: String,
    val field1Label: String,
    val field2Label: String?,
    val field3Label: String? = null
)

@Composable
fun SettingsScreen(
    engine: MotherBrainEngine,
    currentTheme: DashboardThemeMode,
    isSwahili: Boolean,
    onSelectTheme: (DashboardThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Top-Level Hub: 0 = API & Brokers, 1 = System Settings, 2 = Portfolio
    var selectedSection by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Section Selector Bar (API vs Settings vs Portfolio)
        Surface(
            color = SurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSwahili) "Kituo cha Mipangilio & Wasifu" else "Settings & Profile Command Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Surface(
                        color = AccentGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "3-IN-1 HUB",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Primary 3-Choice Tabs
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = Color.Transparent,
                    contentColor = AccentGold,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = {
                            Text(
                                text = if (isSwahili) "1. API & Madalali" else "1. API & Brokers",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = {
                            Text(
                                text = if (isSwahili) "2. Mipangilio" else "2. Settings",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = {
                            Text(
                                text = if (isSwahili) "3. Mkoba / Portfolio" else "3. Portfolio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        // Section Content
        when (selectedSection) {
            0 -> ApiAndBrokersSection(engine = engine, isSwahili = isSwahili)
            1 -> SystemSettingsSection(
                engine = engine,
                currentTheme = currentTheme,
                isSwahili = isSwahili,
                onSelectTheme = onSelectTheme
            )
            2 -> PortfolioScreen(
                engine = engine,
                isSwahili = isSwahili,
                onGoToApiSettings = { selectedSection = 0 }
            )
        }
    }
}

// =============================================================================
// SECTION 0: API & BROKERS (Expanded to 16 Brokers & Exchanges)
// =============================================================================
@Composable
fun ApiAndBrokersSection(engine: MotherBrainEngine, isSwahili: Boolean) {
    val userCredentials by engine.userCredentials.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val allBrokers = remember {
        listOf(
            // --- CRYPTO EXCHANGES & PERP HUBS (18) ---
            SupportedBrokerItem(
                id = "Binance",
                name = "Binance",
                category = "CRYPTO",
                descriptionSwahili = "Soko nambari moja duniani la Crypto (Spot, Futures & Margin). Inasaidia mtaji mdogo kuanzia $1 na satoshis.",
                descriptionEn = "World's largest crypto exchange for Spot & Futures. Micro-satoshis supported from $1.",
                minDepositText = "Mtaji: $1+ Micro Satoshis",
                field1Label = "Binance API Key",
                field2Label = "Binance API Secret"
            ),
            SupportedBrokerItem(
                id = "Bybit",
                name = "Bybit",
                category = "CRYPTO",
                descriptionSwahili = "Soko la kimataifa la mikataba ya derivatives na spot yenye execution ya sub-millisecond.",
                descriptionEn = "Top-tier crypto derivatives and spot exchange with ultra-low latency execution.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "Bybit API Key",
                field2Label = "Bybit API Secret"
            ),
            SupportedBrokerItem(
                id = "OKX",
                name = "OKX",
                category = "CRYPTO",
                descriptionSwahili = "Soko la derivatives na Web3 lenye deep liquidity kwa algorithmic trading.",
                descriptionEn = "Institutional crypto exchange with deep liquidity and high API rate limits.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "OKX API Key",
                field2Label = "OKX Secret Key",
                field3Label = "OKX Passphrase"
            ),
            SupportedBrokerItem(
                id = "KuCoin",
                name = "KuCoin",
                category = "CRYPTO",
                descriptionSwahili = "Soko la sarafu zaidi ya 700 za crypto lenye ada ndogo na micro-lot trading.",
                descriptionEn = "Global crypto exchange featuring 700+ altcoins with low fees.",
                minDepositText = "Mtaji: $1+ Spot / Futures",
                field1Label = "KuCoin API Key",
                field2Label = "KuCoin Secret Key",
                field3Label = "KuCoin Passphrase"
            ),
            SupportedBrokerItem(
                id = "Bitget",
                name = "Bitget",
                category = "CRYPTO",
                descriptionSwahili = "Soko la derivatives na copy-trading lenye liquidity kubwa ya BTC, ETH, na SOL.",
                descriptionEn = "High-speed derivatives and copy-trading exchange with deep crypto depth.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "Bitget API Key",
                field2Label = "Bitget Secret Key",
                field3Label = "Bitget Passphrase"
            ),
            SupportedBrokerItem(
                id = "Coinbase",
                name = "Coinbase Advanced",
                category = "CRYPTO",
                descriptionSwahili = "Lango la Coinbase Advanced Trade lenye usalama wa kiwango cha taasisi za kibenki za Marekani.",
                descriptionEn = "Institutional-grade secure crypto connectivity via Coinbase Advanced API.",
                minDepositText = "Mtaji: $2+ USD",
                field1Label = "Coinbase API Key",
                field2Label = "Coinbase Private Secret"
            ),
            SupportedBrokerItem(
                id = "Kraken",
                name = "Kraken Pro",
                category = "CRYPTO",
                descriptionSwahili = "Soko lililosajiliwa lenye usalama wa juu, spot, margin, na futures API ya sub-second.",
                descriptionEn = "Heavily regulated exchange with advanced spot, margin, and futures WebSocket API.",
                minDepositText = "Mtaji: $5+ USD",
                field1Label = "Kraken API Key",
                field2Label = "Kraken Private Key"
            ),
            SupportedBrokerItem(
                id = "GateIO",
                name = "Gate.io",
                category = "CRYPTO",
                descriptionSwahili = "Soko kubwa lenye maelfu ya altcoins, mikataba ya futures, na micro trading.",
                descriptionEn = "Comprehensive multi-tier crypto venue with high leverage and micro altcoin liquidity.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "Gate.io API Key",
                field2Label = "Gate.io Secret Key"
            ),
            SupportedBrokerItem(
                id = "HTX",
                name = "HTX (Huobi)",
                category = "CRYPTO",
                descriptionSwahili = "Soko kuu la Asia la mikataba ya USDT-Margined na Coin-Margined futures.",
                descriptionEn = "Major global liquidity provider for linear and inverse crypto derivatives.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "HTX Access Key",
                field2Label = "HTX Secret Key"
            ),
            SupportedBrokerItem(
                id = "MEXC",
                name = "MEXC Global",
                category = "CRYPTO",
                descriptionSwahili = "Soko lenye ada 0% za maker na mikataba ya haraka ya sarafu mpya za crypto.",
                descriptionEn = "Zero-fee maker liquidity hub with fast-execution futures for emerging tokens.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "MEXC API Key",
                field2Label = "MEXC Secret Key"
            ),
            SupportedBrokerItem(
                id = "BingX",
                name = "BingX",
                category = "CRYPTO",
                descriptionSwahili = "Jukwaa linaloongoza kwa copy trading, standard futures, na spot API.",
                descriptionEn = "Leading crypto social and algorithmic trading platform with multi-asset support.",
                minDepositText = "Mtaji: $1+ USDT",
                field1Label = "BingX API Key",
                field2Label = "BingX Secret Key"
            ),
            SupportedBrokerItem(
                id = "Bitfinex",
                name = "Bitfinex",
                category = "CRYPTO",
                descriptionSwahili = "Soko la taasisi lenye deep liquidity kwa mikakati ya arbitrage na margin flow.",
                descriptionEn = "Professional trading venue for institutional order flow, margin, and OTC liquidity.",
                minDepositText = "Mtaji: $10+ USD",
                field1Label = "Bitfinex API Key",
                field2Label = "Bitfinex API Secret"
            ),
            SupportedBrokerItem(
                id = "CryptoCom",
                name = "Crypto.com Exchange",
                category = "CRYPTO",
                descriptionSwahili = "Lango la taasisi la Crypto.com lenye viwango vya juu vya usalama na deep liquidity.",
                descriptionEn = "High-security institutional exchange API with fast order routing.",
                minDepositText = "Mtaji: $1+ USD",
                field1Label = "Crypto.com API Key",
                field2Label = "Crypto.com Secret Key"
            ),
            SupportedBrokerItem(
                id = "Deribit",
                name = "Deribit (Options & Futures)",
                category = "CRYPTO",
                descriptionSwahili = "Soko namba 1 duniani kwa mikataba ya Crypto Options na BTC/ETH perpetual futures.",
                descriptionEn = "World's leading Bitcoin and Ethereum options and perpetual futures venue.",
                minDepositText = "Mtaji: $5+ BTC/ETH",
                field1Label = "Deribit Client ID",
                field2Label = "Deribit Client Secret"
            ),
            SupportedBrokerItem(
                id = "Hyperliquid",
                name = "Hyperliquid L1 DEX",
                category = "CRYPTO",
                descriptionSwahili = "DEX ya kasi ya juu kwenye L1 blockchain inayotoa zero gas fee na instant perps.",
                descriptionEn = "High-speed decentralized perpetual exchange with custom L1 consensus.",
                minDepositText = "Mtaji: $5+ USDC",
                field1Label = "Hyperliquid Wallet Address",
                field2Label = "API Agent Private Key / Session"
            ),
            SupportedBrokerItem(
                id = "dYdX",
                name = "dYdX v4 Chain",
                category = "CRYPTO",
                descriptionSwahili = "DEX inayoongoza duniani ya perpetuals iliyojengwa juu ya Cosmos SDK yenye sub-second matching.",
                descriptionEn = "Decentralized orderbook exchange for perpetual contracts with high leverage.",
                minDepositText = "Mtaji: $5+ USDC",
                field1Label = "dYdX Account Address",
                field2Label = "dYdX Secret Phrase / Subaccount"
            ),
            SupportedBrokerItem(
                id = "Uniswap",
                name = "Uniswap v3 DEX Router",
                category = "CRYPTO",
                descriptionSwahili = "Lango la Smart Contract la Uniswap kwa ajili ya algorithmic on-chain spot swaps.",
                descriptionEn = "Automated market maker protocol on Ethereum, Arbitrum, Base, and Polygon.",
                minDepositText = "Mtaji: $2+ Web3 Gas",
                field1Label = "EVM Wallet Public Address",
                field2Label = "Web3 RPC Endpoint / Private Key"
            ),
            SupportedBrokerItem(
                id = "PancakeSwap",
                name = "PancakeSwap DEX (BNB Chain)",
                category = "CRYPTO",
                descriptionSwahili = "Lango la automated swaps na micro liquidity pools kwenye BNB Chain na Arbitrum.",
                descriptionEn = "Decentralized DEX aggregator with ultra-low transaction fees on BNB Chain.",
                minDepositText = "Mtaji: $1+ BNB/USDT",
                field1Label = "BNB Chain Wallet Address",
                field2Label = "RPC URL / Web3 Signer Key"
            ),

            // --- FOREX & COMMODITIES BROKERS (20) ---
            SupportedBrokerItem(
                id = "Exness",
                name = "Exness",
                category = "FOREX",
                descriptionSwahili = "Broker mkuu wa Forex na Dhahabu mwenye spreads ndogo na akaunti za Cent kuanzia $1.",
                descriptionEn = "Leading multi-asset broker with tight raw spreads and $1+ cent accounts.",
                minDepositText = "Mtaji: $1+ Cent / Standard",
                field1Label = "Exness Account Login / API",
                field2Label = "Exness Server & Password"
            ),
            SupportedBrokerItem(
                id = "MT5",
                name = "MetaTrader 5 (MT5)",
                category = "FOREX",
                descriptionSwahili = "Unganisha broker yeyote wa Forex (Exness, HFM, IC Markets, FXPro) kupitia MT5 Server na Login.",
                descriptionEn = "Connect any MT5 Forex/CFD broker account via Server Name and Login ID.",
                minDepositText = "Mtaji: $1+ Micro-Lot (0.01)",
                field1Label = "MT5 Account Login ID",
                field2Label = "MT5 Server Name & Password"
            ),
            SupportedBrokerItem(
                id = "MT4",
                name = "MetaTrader 4 (MT4)",
                category = "FOREX",
                descriptionSwahili = "Lango la MetaTrader 4 kwa mawakala wa kitamaduni wa Forex na Dhahabu (XAU/USD).",
                descriptionEn = "Classic MT4 bridge for retail Forex and precious metals brokers.",
                minDepositText = "Mtaji: $1+ Micro-Lot (0.01)",
                field1Label = "MT4 Account Login ID",
                field2Label = "MT4 Server Name & Password"
            ),
            SupportedBrokerItem(
                id = "XM",
                name = "XM Global",
                category = "FOREX",
                descriptionSwahili = "Broker wa kimataifa mwenye akaunti za Ultra Low Micro zenye kuanzia $5 na bila re-quotes.",
                descriptionEn = "Global FX powerhouse with Ultra-Low micro lot execution and zero requotes.",
                minDepositText = "Mtaji: $5+ Micro Lot",
                field1Label = "XM MT4/MT5 Login ID",
                field2Label = "XM Server Name & Password"
            ),
            SupportedBrokerItem(
                id = "ICMarkets",
                name = "IC Markets",
                category = "FOREX",
                descriptionSwahili = "Broker nambari moja wa True ECN duniani mwenye 0.0 pip spread na server za New York Equinix.",
                descriptionEn = "Premier True ECN broker with 0.0 pip spreads and NY4/LD4 ultra-low latency.",
                minDepositText = "Mtaji: $10+ ECN",
                field1Label = "IC Markets Account Number",
                field2Label = "IC Markets Server & Password"
            ),
            SupportedBrokerItem(
                id = "Pepperstone",
                name = "Pepperstone",
                category = "FOREX",
                descriptionSwahili = "Broker maarufu wa Razor Spreads kwa ajili ya scalping ya EUR/USD, GBP/USD na US30.",
                descriptionEn = "Razor-thin spreads with sub-30ms execution for scalping and algorithmic flow.",
                minDepositText = "Mtaji: $10+ Razor",
                field1Label = "Pepperstone Account Login",
                field2Label = "Pepperstone Password & Server"
            ),
            SupportedBrokerItem(
                id = "FXTM",
                name = "FXTM (ForexTime)",
                category = "FOREX",
                descriptionSwahili = "Broker anayeaminika Afrika Mashariki na dunia nzima mwenye akaunti za Micro kuanzia $10.",
                descriptionEn = "Heavily trusted broker in Africa and globally with flexible leverage and micro lots.",
                minDepositText = "Mtaji: $10+ Micro",
                field1Label = "FXTM Account Number",
                field2Label = "FXTM Server & Password"
            ),
            SupportedBrokerItem(
                id = "OctaFX",
                name = "Octa (OctaFX)",
                category = "FOREX",
                descriptionSwahili = "Broker mwenye ada ndogo, hakuna kamisheni, na utekelezaji wa haraka wa 0.01 micro-lot.",
                descriptionEn = "Low-spread commission-free broker with rapid micro-lot trade routing.",
                minDepositText = "Mtaji: $5+ Standard",
                field1Label = "Octa Account Login ID",
                field2Label = "Octa Server Name & Password"
            ),
            SupportedBrokerItem(
                id = "HFM",
                name = "HFM (HotForex)",
                category = "FOREX",
                descriptionSwahili = "Broker mwenye akaunti za Cent kuanzia $5, leverage ya 1:2000, na amana za kienyeji (M-Pesa).",
                descriptionEn = "Multi-regulated broker offering Cent accounts and local mobile money gateways.",
                minDepositText = "Mtaji: $5+ Cent Account",
                field1Label = "HFM Account Number",
                field2Label = "HFM Server & Password"
            ),
            SupportedBrokerItem(
                id = "Tickmill",
                name = "Tickmill",
                category = "FOREX",
                descriptionSwahili = "Broker anayependwa na wataalamu wa algorithmic trading kwa ajili ya kamisheni ndogo zaidi na ECN execution.",
                descriptionEn = "Ultra-low commission broker designed for high-frequency trading & expert advisors.",
                minDepositText = "Mtaji: $10+ Pro",
                field1Label = "Tickmill Account Number",
                field2Label = "Tickmill Server & Password"
            ),
            SupportedBrokerItem(
                id = "FPMarkets",
                name = "FP Markets",
                category = "FOREX",
                descriptionSwahili = "Broker wa Australia aliyeshinda tuzo kwa ECN raw pricing na server za London Equinix LD4.",
                descriptionEn = "Award-winning Australian ASIC regulated broker with direct DMA/ECN liquidity.",
                minDepositText = "Mtaji: $10+ Raw",
                field1Label = "FP Markets Login ID",
                field2Label = "FP Markets Server & Password"
            ),
            SupportedBrokerItem(
                id = "FxPro",
                name = "FxPro",
                category = "FOREX",
                descriptionSwahili = "Broker mwenye uzoefu wa miaka 20+ mwenye NDD (No Dealing Desk) execution na cTrader API.",
                descriptionEn = "Top-tier UK & global broker with NDD execution on MT4, MT5, and cTrader.",
                minDepositText = "Mtaji: $10+ USD",
                field1Label = "FxPro Account Number",
                field2Label = "FxPro Server & Password"
            ),
            SupportedBrokerItem(
                id = "RoboForex",
                name = "RoboForex",
                category = "FOREX",
                descriptionSwahili = "Broker maarufu wa akaunti za Pro-Cent na Micro-lot kwa ajili ya mikakati ya boti za gridi na scalping.",
                descriptionEn = "Cent account leader with low minimum deposit and algorithmic automated execution.",
                minDepositText = "Mtaji: $10+ ProCent",
                field1Label = "RoboForex Login ID",
                field2Label = "RoboForex Server & Password"
            ),
            SupportedBrokerItem(
                id = "ForexCom",
                name = "Forex.com (GAIN Capital)",
                category = "FOREX",
                descriptionSwahili = "Broker mkuu wa masoko ya fedha za kigeni, dhahabu, na fahirisi za kimataifa.",
                descriptionEn = "Regulated global FX leader offering direct market execution on currency pairs.",
                minDepositText = "Mtaji: $10+ Micro",
                field1Label = "Forex.com Username / API ID",
                field2Label = "Forex.com App Key & Secret"
            ),
            SupportedBrokerItem(
                id = "IG",
                name = "IG Markets",
                category = "FOREX",
                descriptionSwahili = "Broker mkubwa wa kimataifa wa CFDs, Indices, na bidhaa za kilimo na nishati.",
                descriptionEn = "Premier global CFD and forex provider with advanced REST & Streaming API.",
                minDepositText = "Mtaji: $10+ CFD",
                field1Label = "IG API Key",
                field2Label = "IG Client Account ID & Password"
            ),
            SupportedBrokerItem(
                id = "AvaTrade",
                name = "AvaTrade",
                category = "FOREX",
                descriptionSwahili = "Broker aliyesajiliwa katika mabara 6 mwenye ulinzi wa AvaProtect na fixed spreads.",
                descriptionEn = "Globally licensed broker supporting FX, options, and automated API integration.",
                minDepositText = "Mtaji: $10+ USD",
                field1Label = "AvaTrade Account Login",
                field2Label = "AvaTrade Server & Password"
            ),
            SupportedBrokerItem(
                id = "FXCM",
                name = "FXCM",
                category = "FOREX",
                descriptionSwahili = "Lango la REST API na FIX Protocol kwa ajili ya algorithmic trading ya Forex na Indices.",
                descriptionEn = "Institutional-grade retail broker with free Python and REST trading API access.",
                minDepositText = "Mtaji: $10+ Micro",
                field1Label = "FXCM API Token",
                field2Label = "FXCM Account Number"
            ),
            SupportedBrokerItem(
                id = "OANDA",
                name = "OANDA",
                category = "FOREX",
                descriptionSwahili = "Broker anayejulikana kwa v20 REST API na biashara ya vipande vidogo bila lot size fasta.",
                descriptionEn = "Pioneer in retail FX offering unit-level precision and powerful v20 REST API.",
                minDepositText = "Mtaji: $1+ Fractional Unit",
                field1Label = "OANDA v20 API Token",
                field2Label = "OANDA Account ID"
            ),
            SupportedBrokerItem(
                id = "SaxoBank",
                name = "Saxo Bank OpenAPI",
                category = "FOREX",
                descriptionSwahili = "Lango la benki ya Saxo kwa ajili ya FX, mikataba ya hatifungani, na masoko ya Ulaya na Asia.",
                descriptionEn = "Institutional Danish investment bank providing institutional multi-asset OpenAPI access.",
                minDepositText = "Mtaji: $50+ Multi-Asset",
                field1Label = "Saxo App Key",
                field2Label = "Saxo OAuth Access Token"
            ),
            SupportedBrokerItem(
                id = "Swissquote",
                name = "Swissquote Bank",
                category = "FOREX",
                descriptionSwahili = "Benki ya Uswisi ya biashara ya mtandaoni yenye usalama wa kiwango cha benki na FIX API.",
                descriptionEn = "Swiss banking security with institutional FIX API connectivity for Forex and metals.",
                minDepositText = "Mtaji: $50+ Bank Standard",
                field1Label = "Swissquote Login ID",
                field2Label = "Swissquote Password & Server"
            ),

            // --- DERIVATIVES & SYNTHETICS (4) ---
            SupportedBrokerItem(
                id = "Deriv",
                name = "Deriv (Synthetic & Multi-Asset)",
                category = "DERIV",
                descriptionSwahili = "Lango la fahirisi za synthetic (Volatility 75, Boom/Crash, Step Index) na CFDs 24/7.",
                descriptionEn = "Continuous 24/7 synthetic indices (Vol 75, Boom/Crash, Step) and CFD gateway.",
                minDepositText = "Mtaji: $0.35+ Micro Stake",
                field1Label = "Deriv API Token (Read & Trade)",
                field2Label = "Deriv App ID (Hiari)"
            ),
            SupportedBrokerItem(
                id = "Quotex",
                name = "Quotex",
                category = "DERIV",
                descriptionSwahili = "Jukwaa la mikataba ya haraka ya masoko ya sarafu na madini kwa amana ndogo kuanzia $10.",
                descriptionEn = "Fast digital contract broker with minimal stakes and quick execution.",
                minDepositText = "Mtaji: $10+ USD",
                field1Label = "Quotex Account Email",
                field2Label = "Quotex Session / API Key"
            ),
            SupportedBrokerItem(
                id = "PocketOption",
                name = "Pocket Option",
                category = "DERIV",
                descriptionSwahili = "Lango la mikataba ya kidijitali na synthetic assets kuanzia dau la $1.",
                descriptionEn = "Micro-contract options platform with $1 minimum trade stakes.",
                minDepositText = "Mtaji: $5+ Micro Stake",
                field1Label = "PocketOption Account ID",
                field2Label = "PocketOption API / Secret Token"
            ),
            SupportedBrokerItem(
                id = "IQOption",
                name = "IQ Option",
                category = "DERIV",
                descriptionSwahili = "Jukwaa la kidijitali la CFDs za sarafu za kigeni, hisa, na options zenye ada ya chini kuanzia $10.",
                descriptionEn = "Digital options and CFD broker with low minimum deposits and fast execution.",
                minDepositText = "Mtaji: $10+ USD",
                field1Label = "IQ Option Account Email",
                field2Label = "IQ Option Password / API Token"
            ),

            // --- STOCKS & EQUITIES (8) ---
            SupportedBrokerItem(
                id = "IBKR",
                name = "Interactive Brokers (IBKR)",
                category = "STOCKS",
                descriptionSwahili = "Lango la taasisi kwa ajili ya hisa za kimataifa, bondi, hatifungani, na masoko 150+ ya dunia.",
                descriptionEn = "Global institutional gateway to 150+ exchanges in 33 countries with Client Portal API.",
                minDepositText = "Mtaji: $20+ Global",
                field1Label = "IBKR Client Portal Gateway ID",
                field2Label = "IBKR OAuth Token / Secret"
            ),
            SupportedBrokerItem(
                id = "Alpaca",
                name = "Alpaca Securities",
                category = "STOCKS",
                descriptionSwahili = "Broker wa hisa za Marekani (NYSE, NASDAQ) na Crypto bila kamisheni, kwa API moja kwa moja.",
                descriptionEn = "Commission-free US equities and crypto API broker with fractional share trading.",
                minDepositText = "Mtaji: $1+ Fractional Share",
                field1Label = "Alpaca API Key ID",
                field2Label = "Alpaca Secret Key"
            ),
            SupportedBrokerItem(
                id = "DSE",
                name = "DSE Tanzania (CSDR)",
                category = "STOCKS",
                descriptionSwahili = "Soko la Hisa la Dar es Salaam (CRDB, NMB, TBL, VODA) kupitia akaunti ya CSDR ya mwekezaji.",
                descriptionEn = "Dar es Salaam Stock Exchange domestic equities gateway via investor CSDR account.",
                minDepositText = "Mtaji: $1+ (TZS 2,500)",
                field1Label = "Investor CSDR Account Number",
                field2Label = "Authorized Broker Code (e.g. Orbit/Solomon)"
            ),
            SupportedBrokerItem(
                id = "NSEKenya",
                name = "NSE Kenya (CDSC)",
                category = "STOCKS",
                descriptionSwahili = "Soko la Hisa la Nairobi (Safaricom, Equity Group, KCB) kupitia nambari ya CDSC.",
                descriptionEn = "Nairobi Securities Exchange gateway for East African blue chips via CDSC account.",
                minDepositText = "Mtaji: $2+ (KES 300)",
                field1Label = "CDSC Account Number",
                field2Label = "Authorized Broker Code"
            ),
            SupportedBrokerItem(
                id = "JSE",
                name = "JSE South Africa (Equities)",
                category = "STOCKS",
                descriptionSwahili = "Soko Kuu la Hisa la Johannesburg (JSE) kwa ajili ya hisa za madini na taasisi za fedha za Afrika Kusini.",
                descriptionEn = "Johannesburg Stock Exchange DMA gateway for African mining and banking blue chips.",
                minDepositText = "Mtaji: $20+ ZAR",
                field1Label = "JSE Broker Account Number",
                field2Label = "JSE API Access Token"
            ),
            SupportedBrokerItem(
                id = "TradeStation",
                name = "TradeStation",
                category = "STOCKS",
                descriptionSwahili = "Lango la kitaalamu la hisa za Marekani, mikataba ya futures, na options kwa WebAPI.",
                descriptionEn = "Professional multi-asset broker providing full WebAPI integration for algorithmic traders.",
                minDepositText = "Mtaji: $25+ USD",
                field1Label = "TradeStation Client ID",
                field2Label = "TradeStation Client Secret"
            ),
            SupportedBrokerItem(
                id = "Schwab",
                name = "Charles Schwab / TD",
                category = "STOCKS",
                descriptionSwahili = "Lango rasmi la Charles Schwab Trader API kwa ajili ya hisa za Marekani na ETFs.",
                descriptionEn = "Official Charles Schwab Trader API for US equities, ETFs, and index options.",
                minDepositText = "Mtaji: $50+ USD",
                field1Label = "Schwab App Key",
                field2Label = "Schwab Secret & Refresh Token"
            ),
            SupportedBrokerItem(
                id = "Robinhood",
                name = "Robinhood Connect",
                category = "STOCKS",
                descriptionSwahili = "Lango la hisa za Marekani na Crypto kwa ajili ya biashara bila kamisheni kupitia Robinhood API.",
                descriptionEn = "Commission-free stock and crypto order routing via Robinhood OAuth API.",
                minDepositText = "Mtaji: $1+ Fractional Share",
                field1Label = "Robinhood Client ID",
                field2Label = "Robinhood OAuth Access Token"
            )
        )
    }

    val filteredBrokers = allBrokers.filter { broker ->
        val matchesCategory = selectedCategory == "ALL" || broker.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() || 
            broker.name.contains(searchQuery, ignoreCase = true) || 
            broker.descriptionSwahili.contains(searchQuery, ignoreCase = true) ||
            broker.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status & Instruction Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSwahili) "MADALALI NA EXCHANGE ZOTE (${allBrokers.size} GATEWAYS)" else "ALL BROKERS & EXCHANGES (${allBrokers.size} GATEWAYS)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AccentGold
                            )
                        }

                        val connectedCount = userCredentials.count { cred -> cred.apiKey.isNotBlank() }
                        Surface(
                            color = if (connectedCount > 0) AccentNeonGreen.copy(alpha = 0.15f) else SurfaceDarker,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (connectedCount > 0) AccentNeonGreen else BorderSubtle)
                        ) {
                            Text(
                                text = if (isSwahili) "$connectedCount Zimeunganishwa" else "$connectedCount Connected",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (connectedCount > 0) AccentNeonGreen else TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSwahili)
                            "Weka taarifa za akaunti yako ya dalali au exchange unayotumia. Mfumo unasaidia mtaji mdogo kuanzia $1 (Micro/Cent lot) hadi $10,000+. Taarifa zako zimehifadhiwa kwa usalama ndani ya kifaa hiki (Room Local DB)."
                        else
                            "Configure your verified broker or crypto exchange accounts. Micro-capital from $1 is fully supported across all 35 gateways with zero local latency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Live Search Input Box
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isSwahili) "Tafuta dalali au exchange (mf. Binance, Exness, Deriv, MT5, XM)..." else "Search any broker or exchange (e.g. Binance, Exness, Deriv, MT5, XM)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = AccentGold, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = SurfaceElevated,
                    unfocusedContainerColor = SurfaceElevated
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Category Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val categories = listOf(
                    "ALL" to (if (isSwahili) "Yote (${allBrokers.size})" else "All (${allBrokers.size})"),
                    "CRYPTO" to (if (isSwahili) "Crypto (${allBrokers.count { it.category == "CRYPTO" }})" else "Crypto (${allBrokers.count { it.category == "CRYPTO" }})"),
                    "FOREX" to (if (isSwahili) "Forex & Madini (${allBrokers.count { it.category == "FOREX" }})" else "Forex & Metals (${allBrokers.count { it.category == "FOREX" }})"),
                    "DERIV" to (if (isSwahili) "Deriv Synthetics (${allBrokers.count { it.category == "DERIV" }})" else "Deriv Synthetics (${allBrokers.count { it.category == "DERIV" }})"),
                    "STOCKS" to (if (isSwahili) "Hisa & DSE (${allBrokers.count { it.category == "STOCKS" }})" else "Stocks & DSE (${allBrokers.count { it.category == "STOCKS" }})")
                )
                items(categories) { (catKey, catLabel) ->
                    val isSel = catKey == selectedCategory
                    Surface(
                        color = if (isSel) AccentGold.copy(alpha = 0.2f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AccentGold else BorderSubtle),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { selectedCategory = catKey }
                    ) {
                        Text(
                            text = catLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AccentGold else TextWhite
                        )
                    }
                }
            }
        }

        // Render each broker gateway
        items(filteredBrokers) { broker ->
            val cred = userCredentials.firstOrNull { it.exchangeName == broker.id }
            SingleBrokerGatewayCard(engine, broker, cred, isSwahili)
        }
    }
}

@Composable
fun SingleBrokerGatewayCard(
    engine: MotherBrainEngine,
    broker: SupportedBrokerItem,
    cred: UserApiCredentialsEntity?,
    isSwahili: Boolean
) {
    var field1 by remember(cred) { mutableStateOf(cred?.apiKey ?: "") }
    var field2 by remember(cred) { mutableStateOf(cred?.secretKey ?: "") }
    var saveFeedback by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(cred != null && cred.apiKey.isNotBlank()) }
    val isConnected = cred != null && cred.apiKey.isNotBlank()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isConnected) AccentNeonGreen else BorderSubtle),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) AccentNeonGreen else DangerRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = broker.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SurfaceDarker,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = broker.category,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentCyan,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = broker.minDepositText,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentGold,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isConnected) AccentNeonGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isConnected) AccentNeonGreen else DangerRed)
                    ) {
                        Text(
                            text = if (isConnected) (if (isSwahili) "IMEUNGANISHWA" else "CONNECTED") else (if (isSwahili) "BILA MUUNGANISHO" else "NOT CONFIGURED"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) AccentNeonGreen else DangerRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }

            // Description
            Text(
                text = if (isSwahili) broker.descriptionSwahili else broker.descriptionEn,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 11.sp
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))

                // Input Field 1
                OutlinedTextField(
                    value = field1,
                    onValueChange = {
                        field1 = it
                        saveFeedback = null
                    },
                    label = { Text(broker.field1Label) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = AccentCyan
                    )
                )

                // Input Field 2 (if present)
                if (broker.field2Label != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = field2,
                        onValueChange = {
                            field2 = it
                            saveFeedback = null
                        },
                        label = { Text(broker.field2Label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = AccentCyan
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Button
                Button(
                    onClick = {
                        engine.saveApiCredentials(broker.id, field1, field2)
                        saveFeedback = if (isSwahili) "✅ ${broker.name} Imehifadhiwa & Kuunganishwa!" else "✅ ${broker.name} Connected & Verified!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (field1.isNotBlank()) AccentNeonGreen else SurfaceElevated,
                        contentColor = if (field1.isNotBlank()) Color.Black else TextWhite
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "HIFADHI & UNGANISHA ${broker.name.uppercase()}" else "SAVE & CONNECT ${broker.name.uppercase()}",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                saveFeedback?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentNeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// =============================================================================
// SECTION 1: SYSTEM SETTINGS (Language, Engine Mode, Themes, APK Center)
// =============================================================================
@Composable
fun SystemSettingsSection(
    engine: MotherBrainEngine,
    currentTheme: DashboardThemeMode,
    isSwahili: Boolean,
    onSelectTheme: (DashboardThemeMode) -> Unit
) {
    val telemetry by engine.telemetry.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Language Switcher
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSwahili) "Lugha ya Mfumo" else "Application Language",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguageMode.values().forEach { mode ->
                            val isSelected = telemetry.languageMode == mode
                            Button(
                                onClick = { engine.setLanguageMode(mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                }
            }
        }

        // GAM MODE (Grid & Accumulation Momentum - 200 Trades on M5)
        item {
            GamModeSettingsCard(engine = engine, isSwahili = isSwahili)
        }

        // Engine Optimization Mode
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSwahili) "Hali ya Uendeshaji wa Boti" else "Algorithmic Engine Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ProfitEngineMode.values().forEach { mode ->
                        val isSelected = telemetry.engineMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceElevated else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { engine.setEngineMode(mode) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = mode.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(text = mode.description, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { engine.setEngineMode(mode) }
                            )
                        }
                    }
                }
            }
        }

        // High-Tech Color Themes (10 Themes)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = "Themes", tint = currentTheme.primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Mada za Rangi za Dashibodi (10)" else "Sovereign High-Tech Color Themes (10)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DashboardThemeMode.values().forEach { theme ->
                        val isSelected = currentTheme == theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceElevated else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) theme.primaryColor else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { onSelectTheme(theme) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(theme.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = theme.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.primaryColor else TextWhite
                                )
                            }

                            if (isSelected) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Email Notifications & Resend Integration Card
        item {
            EmailNotificationSettingsCard(engine = engine, isSwahili = isSwahili)
        }

        // APK Download & Export Center
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = "APK", tint = AccentCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSwahili) "Kituo cha Kupakua APK & Export" else "APK Download & Packaging Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isSwahili)
                            "Unaweza kupakua faili la APK la ESMH.TRADE / MWASA AI moja kwa moja kutoka kwenye menyu ya pembeni au mipangilio ya juu ya AI Studio. Bofya kitufe cha 'Export APK' au 'Download Project' kwenye menyu ya AI Studio platform hapo juu."
                        else
                            "You can download the ESMH.TRADE / MWASA AI APK directly from the AI Studio platform header/sidebar menu. Click 'Export APK' or 'Download Project' in the top-right AI Studio control panel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// =============================================================================
// GAM MODE (GRID & ACCUMULATION MOMENTUM - ~200 TRADES M5 SCALPER)
// =============================================================================
@Composable
fun GamModeSettingsCard(
    engine: MotherBrainEngine,
    isSwahili: Boolean
) {
    val gamConfig by engine.gamModeConfig.collectAsState()
    var showInfoDialog by remember { mutableStateOf(false) }
    var customProfitInput by remember { mutableStateOf("") }

    val progress = if (gamConfig.targetTradesPerCycle > 0) {
        (gamConfig.executedTradesInCycle.toFloat() / gamConfig.targetTradesPerCycle.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isGamActive = gamConfig.isEnabled

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isGamActive) 2.dp else 1.dp,
            color = if (isGamActive) AccentGold else BorderSubtle
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title, Status Badge, Info Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGamActive) AccentGold.copy(alpha = 0.2f) else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "GAM Mode",
                            tint = if (isGamActive) AccentGold else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSwahili) "MODE YA GAM (M5 SCALPER)" else "GAM MODE (M5 SCALPER)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isGamActive) AccentGold else TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showInfoDialog = true },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isSwahili)
                                "Hutrade takribani trade ${gamConfig.targetTradesPerCycle} kwenye chati ya M5 kwa faida unayochagua"
                            else
                                "Executes ~${gamConfig.targetTradesPerCycle} trades on M5 timeframe with chosen profit per trade",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Timeframe Badge
                Surface(
                    color = AccentCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "CHATI: M5",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AccentCyan,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activation Switch Banner
            Surface(
                color = if (isGamActive) AccentGold.copy(alpha = 0.12f) else SurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isGamActive) AccentGold else BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGamActive)
                                (if (isSwahili) "🟢 GAM MODE INAFANYA KAZI (M5)" else "🟢 GAM MODE ACTIVE (M5)")
                            else
                                (if (isSwahili) "⚪ GAM MODE IMEZIMWA (STANDBY)" else "⚪ GAM MODE PAUSED (STANDBY)"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = if (isGamActive) AccentGold else TextWhite
                        )
                        Text(
                            text = if (isSwahili)
                                "Hali: ${gamConfig.status} • Faida kwa kila trade: $${String.format("%.2f", gamConfig.profitTargetPerTradeUsd)}"
                            else
                                "Status: ${gamConfig.status} • Target profit/trade: $${String.format("%.2f", gamConfig.profitTargetPerTradeUsd)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = isGamActive,
                        onCheckedChange = { engine.toggleGamMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = AccentGold,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SurfaceDarker
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. SELECT TARGET PROFIT PER TRADE ($ per trade)
            Text(
                text = if (isSwahili) "1. CHAGUA FAIDA KWA KILA TRADE (PROFIT PER TRADE):" else "1. SELECT TARGET PROFIT PER TRADE:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = AccentGold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val profitPresets = listOf(0.20, 0.50, 1.00, 2.00, 5.00, 10.00, 25.00)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(profitPresets) { preset ->
                    val isSelected = kotlin.math.abs(gamConfig.profitTargetPerTradeUsd - preset) < 0.01
                    Surface(
                        color = if (isSelected) AccentGold.copy(alpha = 0.25f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentGold else BorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable {
                            engine.setGamProfitTarget(preset)
                        }
                    ) {
                        Text(
                            text = "$${String.format("%.2f", preset)}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) AccentGold else TextWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Profit Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = customProfitInput,
                    onValueChange = { customProfitInput = it },
                    placeholder = {
                        Text(
                            text = if (isSwahili) "Weka kiasi maalum (mf. $1.50 au $3.00)..." else "Custom profit (e.g. $1.50 or $3.00)...",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        val parsed = customProfitInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            engine.setGamProfitTarget(parsed)
                            customProfitInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isSwahili) "Weka" else "Set",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. SELECT CYCLE TRADES TARGET (~200 TRADES)
            Text(
                text = if (isSwahili) "2. IDADI YA TRADE KWA MZUNGUKO WA M5 (CYCLE TARGET):" else "2. TRADES PER M5 CYCLE TARGET:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = AccentCyan
            )
            Spacer(modifier = Modifier.height(6.dp))

            val tradeCountPresets = listOf(50, 100, 200, 500)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tradeCountPresets.forEach { count ->
                    val isSelected = gamConfig.targetTradesPerCycle == count
                    Surface(
                        color = if (isSelected) AccentCyan.copy(alpha = 0.25f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AccentCyan else BorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                engine.setGamTargetTradesCount(count)
                            }
                    ) {
                        Text(
                            text = if (count == 200) "$count (GAM)" else "$count",
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) AccentCyan else TextWhite,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. LIVE CYCLE PROGRESS & ACCUMULATED PROFIT METER
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSwahili) "Maendeleo ya Mzunguko (Cycle Progress):" else "M5 Cycle Execution Meter:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${gamConfig.executedTradesInCycle} / ${gamConfig.targetTradesPerCycle} Trades (${String.format("%.1f", progress * 100)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = AccentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentGold,
                        trackColor = SurfaceDarker,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSwahili) "Jumla ya Faida Iliyopatikana:" else "Accumulated Cycle Profit:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "+$${String.format("%,.2f", gamConfig.totalAccumulatedProfitUsd)} USD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = AccentNeonGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isSwahili) "Kiwango cha Ushindi:" else "Win Rate:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "${gamConfig.winRatePct}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Toggle & Reset Cycle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { engine.toggleGamMode(!isGamActive) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGamActive) DangerRed else AccentGold
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(
                        imageVector = if (isGamActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isGamActive) Color.White else Color.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGamActive)
                            (if (isSwahili) "SIMAMISHA GAM" else "PAUSE GAM")
                        else
                            (if (isSwahili) "ANZA GAM MODE (M5)" else "START GAM (M5)"),
                        fontWeight = FontWeight.Black,
                        color = if (isGamActive) Color.White else Color.Black,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = { engine.resetGamCycle() },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp), tint = TextWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSwahili) "WEKA UPYA MZUNGUKO" else "RESET CYCLE",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        GamModeInfoDialog(
            isSwahili = isSwahili,
            targetTrades = gamConfig.targetTradesPerCycle,
            profitPerTrade = gamConfig.profitTargetPerTradeUsd,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun GamModeInfoDialog(
    isSwahili: Boolean,
    targetTrades: Int,
    profitPerTrade: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSwahili) "Jinsi Mode ya GAM Inavyofanya Kazi" else "How GAM Mode Operates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = AccentGold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isSwahili)
                        "• **Mzunguko wa Takribani Trade 200:** Mode ya GAM (Grid & Accumulation Momentum) imetengenezwa kufanya takribani trade $targetTrades kwa mzunguko mmoja kwa kasi ya juu bila kuhatarisha mtaji."
                    else
                        "• **~200 Trades Cycle:** GAM Mode (Grid & Accumulation Momentum) is engineered to execute ~$targetTrades high-frequency trades per cycle without taking unhedged directional risks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite
                )

                Text(
                    text = if (isSwahili)
                        "• **Chati ya M5 (Dakika 5):** Inatumia mikondo ya dakika 5 (M5 Candlesticks) kusoma mawimbi mafupi na kuingia wakati soko linapobadilika mwelekeo (micro-mean reversion)."
                    else
                        "• **M5 Timeframe (5-Minutes):** Operates strictly on 5-minute bar oscillations to exploit intraday micro-reversals and momentum bursts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite
                )

                Text(
                    text = if (isSwahili)
                        "• **Faida Iliyochaguliwa kwa Kila Trade ($${String.format("%.2f", profitPerTrade)}):** Kila trade ikifikia faida uliyochagua ($${String.format("%.2f", profitPerTrade)} USD), inafungwa papo hapo (Instant Take-Profit) na faida kuingizwa kwenye salio lako."
                    else
                        "• **Target Profit per Trade ($${String.format("%.2f", profitPerTrade)}):** Once each individual trade reaches your configured profit ($${String.format("%.2f", profitPerTrade)} USD), it closes instantly with automatic profit lock-in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentNeonGreen
                )

                Text(
                    text = if (isSwahili)
                        "• **Ulinzi wa Mtaji:** Inatumia micro-lots (0.01) na inajumuisha Stop-loss guard inayofunga trades zote endapo hasara ya jumla itagusa 1.5% ya mtaji wako."
                    else
                        "• **Capital Protection:** Employs 0.01 micro-lots and dynamic risk governor with a hard stop-loss guard at 1.5% max drawdown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
            ) {
                Text(
                    text = if (isSwahili) "NIMELEWA" else "UNDERSTOOD",
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
}

// =============================================================================
// EMAIL NOTIFICATION SETTINGS (RESEND.COM INTEGRATION)
// =============================================================================
@Composable
fun EmailNotificationSettingsCard(
    engine: MotherBrainEngine,
    isSwahili: Boolean
) {
    val emailStatus by engine.emailNotificationSystem.status.collectAsState()
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf(engine.emailNotificationSystem.getApiKey()) }
    var recipientEmailInput by remember { mutableStateOf(engine.emailNotificationSystem.getRecipientEmail()) }
    var resultBanner by remember { mutableStateOf<String?>(null) }
    var isDispatching by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email Alerts", tint = AccentCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSwahili) "Arifa za Email (Resend API)" else "Email Notifications (Resend API)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = if (engine.emailNotificationSystem.isConfigured()) AccentNeonGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (engine.emailNotificationSystem.isConfigured()) "CONFIGURED" else "OPTIONAL",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (engine.emailNotificationSystem.isConfigured()) AccentNeonGreen else WarningOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSwahili)
                    "Pokea arifa za kiotomatiki za Biashara (Trade Alerts >70%), Ripoti za Kila Siku (18:00), na Ulinzi wa Hatari (Circuit Breaker) kupitia barua pepe yako."
                else
                    "Receive automated real-time institutional Trade Alerts (>70% confidence), Daily Performance Reports (18:00), and Risk Circuit-Breaker notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = recipientEmailInput,
                onValueChange = {
                    recipientEmailInput = it
                    engine.configureEmailNotification(apiKeyInput, it)
                },
                label = { Text("Recipient Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = {
                    apiKeyInput = it
                    engine.configureEmailNotification(it, recipientEmailInput)
                },
                label = { Text("Resend API Key (re_...)") },
                placeholder = { Text("Enter your Resend API token") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isDispatching = true
                        resultBanner = null
                        scope.launch {
                            val res = engine.sendTestEmailNotification(recipientEmailInput)
                            isDispatching = false
                            resultBanner = if (res.isSuccess) {
                                "✅ Test email dispatched to $recipientEmailInput"
                            } else {
                                "❌ Failed: ${res.exceptionOrNull()?.message}"
                            }
                        }
                    },
                    enabled = !isDispatching,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isDispatching) {
                        CircularProgressIndicator(color = DarkCharcoal, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isSwahili) "Jaribu Email Sasa" else "Send Test Email",
                        color = DarkCharcoal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        engine.configureEmailNotification(apiKeyInput, recipientEmailInput)
                        resultBanner = "💾 Saved for $recipientEmailInput"
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", color = TextWhite, fontSize = 12.sp)
                }
            }

            resultBanner?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (msg.startsWith("✅") || msg.startsWith("💾")) AccentNeonGreen.copy(alpha = 0.12f) else DangerRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (msg.startsWith("✅") || msg.startsWith("💾")) AccentNeonGreen else DangerRed)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWhite,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Sent: ${emailStatus.totalSentCount}", color = AccentNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Failed: ${emailStatus.totalFailedCount}", color = if (emailStatus.totalFailedCount > 0) DangerRed else TextMuted, fontSize = 10.sp)
                Text(
                    text = if (emailStatus.lastAttemptTimestamp > 0) "Last: ${(System.currentTimeMillis() - emailStatus.lastAttemptTimestamp) / 1000}s ago" else "No attempts",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

