package com.example.engine

import kotlin.math.exp
import kotlin.random.Random

/**
 * A simple Feedforward Neural Network with Backpropagation.
 * Used for price trend prediction and pattern recognition.
 */
class BackpropNeuralNetwork(
    private val inputSize: Int,
    private val hiddenSize: Int,
    private val outputSize: Int,
    private val learningRate: Double = 0.01
) {
    private var weightsInputHidden: Array<DoubleArray> = Array(inputSize) { DoubleArray(hiddenSize) { Random.nextDouble(-1.0, 1.0) } }
    private var weightsHiddenOutput: Array<DoubleArray> = Array(hiddenSize) { DoubleArray(outputSize) { Random.nextDouble(-1.0, 1.0) } }
    
    private var biasHidden: DoubleArray = DoubleArray(hiddenSize) { Random.nextDouble(-1.0, 1.0) }
    private var biasOutput: DoubleArray = DoubleArray(outputSize) { Random.nextDouble(-1.0, 1.0) }

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))
    private fun sigmoidDerivative(x: Double): Double = x * (1.0 - x)

    /**
     * Forward pass to predict output from input.
     */
    fun predict(inputs: DoubleArray): DoubleArray {
        val hiddenLayer = DoubleArray(hiddenSize)
        for (j in 0 until hiddenSize) {
            var sum = biasHidden[j]
            for (i in 0 until inputSize) {
                sum += inputs[i] * weightsInputHidden[i][j]
            }
            hiddenLayer[j] = sigmoid(sum)
        }

        val outputLayer = DoubleArray(outputSize)
        for (k in 0 until outputSize) {
            var sum = biasOutput[k]
            for (j in 0 until hiddenSize) {
                sum += hiddenLayer[j] * weightsHiddenOutput[j][k]
            }
            outputLayer[k] = sigmoid(sum)
        }
        return outputLayer
    }

    /**
     * Backpropagation algorithm to train the network.
     */
    fun train(inputs: DoubleArray, targets: DoubleArray) {
        // 1. Forward Pass
        val hiddenLayer = DoubleArray(hiddenSize)
        for (j in 0 until hiddenSize) {
            var sum = biasHidden[j]
            for (i in 0 until inputSize) {
                sum += inputs[i] * weightsInputHidden[i][j]
            }
            hiddenLayer[j] = sigmoid(sum)
        }

        val outputLayer = DoubleArray(outputSize)
        for (k in 0 until outputSize) {
            var sum = biasOutput[k]
            for (j in 0 until hiddenSize) {
                sum += hiddenLayer[j] * weightsHiddenOutput[j][k]
            }
            outputLayer[k] = sigmoid(sum)
        }

        // 2. Calculate Output Error & Gradient
        val outputErrors = DoubleArray(outputSize)
        val outputGradients = DoubleArray(outputSize)
        for (k in 0 until outputSize) {
            outputErrors[k] = targets[k] - outputLayer[k]
            outputGradients[k] = outputErrors[k] * sigmoidDerivative(outputLayer[k])
        }

        // 3. Calculate Hidden Error & Gradient
        val hiddenErrors = DoubleArray(hiddenSize)
        val hiddenGradients = DoubleArray(hiddenSize)
        for (j in 0 until hiddenSize) {
            var error = 0.0
            for (k in 0 until outputSize) {
                error += outputGradients[k] * weightsHiddenOutput[j][k]
            }
            hiddenErrors[j] = error
            hiddenGradients[j] = hiddenErrors[j] * sigmoidDerivative(hiddenLayer[j])
        }

        // 4. Update Hidden-Output Weights and Biases
        for (k in 0 until outputSize) {
            biasOutput[k] += outputGradients[k] * learningRate
            for (j in 0 until hiddenSize) {
                weightsHiddenOutput[j][k] += outputGradients[k] * hiddenLayer[j] * learningRate
            }
        }

        // 5. Update Input-Hidden Weights and Biases
        for (j in 0 until hiddenSize) {
            biasHidden[j] += hiddenGradients[j] * learningRate
            for (i in 0 until inputSize) {
                weightsInputHidden[i][j] += hiddenGradients[j] * inputs[i] * learningRate
            }
        }
    }
}
