package com.example.engine

import com.example.data.model.MarketType

data class ContractSpecification(
    val marketType: MarketType,
    val contractSize: Double, // 100,000 for Forex, 1 for Crypto, 100 for Gold, etc.
    val minSpreadPips: Double,
    val commissionPerLotUsd: Double,
    val baseMarginRequirementPct: Double, // e.g. 0.01 for 1:100 leverage
    val maxLeverageAllowed: Int,
    val marketHoursSwahili: String,
    val marketHoursEn: String,
    val liquidityProviderModel: String,
    val typicalVolatilityBps: Double, // Volatility in Basis Points
    val riskFactorMultiplier: Double,
    val executionPipelineRequirement: String
)

object MarketSpecificationRegistry {

    fun getSpecificationForMarket(marketType: MarketType): ContractSpecification {
        return when (marketType) {
            MarketType.FOREX -> ContractSpecification(
                marketType = MarketType.FOREX,
                contractSize = 100000.0, // 1 Standard Lot = 100,000 Base Units
                minSpreadPips = 0.5,
                commissionPerLotUsd = 3.50,
                baseMarginRequirementPct = 0.005, // 1:200 leverage
                maxLeverageAllowed = 200,
                marketHoursSwahili = "Saa 24/5 - Kuanzia Jumatatu Asubuhi hadi Ijumaa Usiku EST",
                marketHoursEn = "24/5 Interbank - Monday 00:00 UTC to Friday 22:00 UTC",
                liquidityProviderModel = "Tier-1 Prime Broker LP Aggregator (ECN Connect)",
                typicalVolatilityBps = 45.0,
                riskFactorMultiplier = 1.0,
                executionPipelineRequirement = "STP/NDD Sub-15ms Direct DMA Routing"
            )
            MarketType.CRYPTO -> ContractSpecification(
                marketType = MarketType.CRYPTO,
                contractSize = 1.0, // 1 Coin fraction
                minSpreadPips = 10.0, // Standard tight retail spreads
                commissionPerLotUsd = 0.0, // Zero taker fees for VIP maker tier
                baseMarginRequirementPct = 0.05, // 1:20 leverage
                maxLeverageAllowed = 20,
                marketHoursSwahili = "Saa 24/7/365 Bila Kusimama",
                marketHoursEn = "24/7/365 Continuous Decentralized Order Book Flow",
                liquidityProviderModel = "Binance/Bybit Unified Spot & Perpetuals Order Book",
                typicalVolatilityBps = 320.0,
                riskFactorMultiplier = 2.4,
                executionPipelineRequirement = "Binance Web-Socket Direct WS-API Stream"
            )
            MarketType.METALS -> ContractSpecification(
                marketType = MarketType.METALS,
                contractSize = 100.0, // 1 Lot Gold = 100 Troy Ounces
                minSpreadPips = 1.2,
                commissionPerLotUsd = 5.00,
                baseMarginRequirementPct = 0.01, // 1:100 leverage
                maxLeverageAllowed = 100,
                marketHoursSwahili = "Muda wa CME/London, hupumzika saa 1 kila siku",
                marketHoursEn = "LBMA OTC Hours - daily 1-hour maintenance break at 22:00 GMT",
                liquidityProviderModel = "OTC Bullion Banks Syndicated Liquidity (London Pool)",
                typicalVolatilityBps = 85.0,
                riskFactorMultiplier = 1.5,
                executionPipelineRequirement = "Direct Metal Fix Pricing Feed"
            )
            MarketType.COMMODITIES -> ContractSpecification(
                marketType = MarketType.COMMODITIES,
                contractSize = 1000.0, // 1 Lot Crude Oil = 1,000 Barrels
                minSpreadPips = 2.0,
                commissionPerLotUsd = 6.00,
                baseMarginRequirementPct = 0.02, // 1:50 leverage
                maxLeverageAllowed = 50,
                marketHoursSwahili = "Masaa ya CME Globex",
                marketHoursEn = "NYMEX / ICE exchange trading sessions",
                liquidityProviderModel = "CME/ICE Clearing House Consolidated Book",
                typicalVolatilityBps = 150.0,
                riskFactorMultiplier = 1.8,
                executionPipelineRequirement = "DMA Exchange Gateway Router"
            )
            MarketType.INDICES -> ContractSpecification(
                marketType = MarketType.INDICES,
                contractSize = 10.0, // 1 Lot = 10 Contracts Multiplier
                minSpreadPips = 0.8,
                commissionPerLotUsd = 2.00,
                baseMarginRequirementPct = 0.01, // 1:100 leverage
                maxLeverageAllowed = 100,
                marketHoursSwahili = "Saa za ufunguzi wa masoko ya hisa ya kikanda",
                marketHoursEn = "Regional Stock Exchange open sessions with CME futures pre-market",
                liquidityProviderModel = "Index Futures Market Makers Syndicate",
                typicalVolatilityBps = 110.0,
                riskFactorMultiplier = 1.2,
                executionPipelineRequirement = "Sub-10ms Index Futures Bridge"
            )
            MarketType.DERIV -> ContractSpecification(
                marketType = MarketType.DERIV,
                contractSize = 1.0, // Synthetic units
                minSpreadPips = 0.1,
                commissionPerLotUsd = 0.0, // Zero transaction commissions
                baseMarginRequirementPct = 0.01, // 1:100 leverage
                maxLeverageAllowed = 100,
                marketHoursSwahili = "Saa 24/7/365 Kiotomatiki na Endelevu",
                marketHoursEn = "24/7/365 Continuous Cryptographically Secured Algorithmic Tick Feed",
                liquidityProviderModel = "Deriv Cryptographic Synthetic Engine (Exclusive Market Maker)",
                typicalVolatilityBps = 450.0, // Extreme synthetic volatility
                riskFactorMultiplier = 3.0, // Extreme risk setting
                executionPipelineRequirement = "Deriv Web-Socket Direct Token Protocol"
            )
            MarketType.STOCKS -> ContractSpecification(
                marketType = MarketType.STOCKS,
                contractSize = 1.0, // 1 Share
                minSpreadPips = 1.5,
                commissionPerLotUsd = 2.50,
                baseMarginRequirementPct = 0.20, // 1:5 leverage
                maxLeverageAllowed = 5,
                marketHoursSwahili = "Saa 17:30 - 23:00 TZS (Soko la Marekani)",
                marketHoursEn = "US Cash Market Hours - 09:30 to 16:00 EST (Monday-Friday)",
                liquidityProviderModel = "NASDAQ/NYSE/DSE Exchange Consolidated Book",
                typicalVolatilityBps = 120.0,
                riskFactorMultiplier = 1.1,
                executionPipelineRequirement = "Equity Clearing DMA Route"
            )
            MarketType.ETFS -> ContractSpecification(
                marketType = MarketType.ETFS,
                contractSize = 1.0, // 1 Share of Fund
                minSpreadPips = 1.0,
                commissionPerLotUsd = 1.50,
                baseMarginRequirementPct = 0.20, // 1:5 leverage
                maxLeverageAllowed = 5,
                marketHoursSwahili = "Saa za kawaida za soko la hisa Marekani",
                marketHoursEn = "Standard US equity market sessions (Monday-Friday)",
                liquidityProviderModel = "Consolidated Equity/ETF Authorized Participants AP Pool",
                typicalVolatilityBps = 75.0,
                riskFactorMultiplier = 0.9,
                executionPipelineRequirement = "Stock Clearing DMA Route"
            )
        }
    }
}
