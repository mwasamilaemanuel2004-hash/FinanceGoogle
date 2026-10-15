package com.example.engine.lib.quant_shared

import com.example.data.model.PriceCandle

interface MarketEngine {
    fun pastEngine(history: List<PriceCandle>): Map<String, Double>
    fun futureEngine(history: List<PriceCandle>): Double
    fun agentDecision(dna: Map<String, Double>): String
}
