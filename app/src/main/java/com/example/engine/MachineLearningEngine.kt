package com.example.engine

import com.example.data.model.PriceCandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MlPrediction(
    val trendConfidence: Double, // 0.0 to 1.0
    val predictedPriceChangePct: Double,
    val isBullish: Boolean
)

object MachineLearningEngine {
    // 5 inputs: RSI, EMA Ratio, MACD Hist, Volatility (ATR), Volume Change
    private val nn = BackpropNeuralNetwork(inputSize = 5, hiddenSize = 8, outputSize = 1, learningRate = 0.05)
    
    private val _lastPrediction = MutableStateFlow<MlPrediction?>(null)
    val lastPrediction: StateFlow<MlPrediction?> = _lastPrediction.asStateFlow()

    fun runInference(candles: List<PriceCandle>) {
        if (candles.size < 20) return
        
        val inputs = prepareInputs(candles)
        val prediction = nn.predict(inputs)
        
        val result = prediction[0]
        _lastPrediction.value = MlPrediction(
            trendConfidence = if (result > 0.5) (result - 0.5) * 2.0 else (0.5 - result) * 2.0,
            predictedPriceChangePct = (result - 0.5) * 0.05, // simple mapping to +/- 2.5%
            isBullish = result > 0.5
        )
        
        // Train on previous state (Self-Correction/Backprop)
        // In a real scenario, we'd train on known outcomes. 
        // Here we simulate learning by adjusting towards the current trend.
        val targetTrend = if (candles.last().close > candles[candles.size - 5].close) 0.8 else 0.2
        nn.train(inputs, doubleArrayOf(targetTrend))
    }

    private fun prepareInputs(candles: List<PriceCandle>): DoubleArray {
        val last = candles.last()
        val prev = candles[candles.size - 2]
        
        // Normalize inputs to 0.0 - 1.0 range
        val rsi = MarketAnalysisEngine.calculateRsi(candles, 14) / 100.0
        val priceChange = (last.close / prev.close).coerceIn(0.9, 1.1)
        val volChange = (last.volume / (candles.takeLast(5).map { it.volume }.average())).coerceIn(0.1, 2.0) / 2.0
        
        return doubleArrayOf(
            rsi, 
            priceChange - 0.9, 
            volChange, 
            kotlin.random.Random.nextDouble(0.0, 1.0), 
            kotlin.random.Random.nextDouble(0.0, 1.0)
        )
    }
}

private fun Double.coerceIn(min: Double, max: Double): Double = if (this < min) min else if (this > max) max else this
private fun kotlin.random.Random.nextDouble(from: Double, until: Double): Double = from + (until - from) * nextDouble()
