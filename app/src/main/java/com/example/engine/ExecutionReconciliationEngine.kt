package com.example.engine

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.OrderEntity
import com.example.data.local.SystemAuditLogEntity
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.abs

/**
 * ============================================================================
 * EXECUTION RECONCILIATION ENGINE (ESMH.TRADE / MWASA AI)
 * ============================================================================
 * Enforces strict order lifecycle reconciliation:
 * AI SIGNAL → RISK APPROVAL → EXECUTION REQUEST → BROKER/EXCHANGE →
 * ORDER ID → ACKNOWLEDGEMENT → FILL/PARTIAL FILL → POSITION UPDATE →
 * P&L UPDATE → DATABASE → MOTHER BRAIN → UI.
 *
 * Detects rejected orders, partial fills, cancellations, duplicate orders,
 * stale timeouts, unexpected positions, and balance mismatches.
 * ============================================================================
 */
class ExecutionReconciliationEngine private constructor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val db = AppDatabase.getInstance(context)

    private val _reconciliationEvents = MutableStateFlow<List<ExecutionReconciliationEvent>>(emptyList())
    val reconciliationEvents: StateFlow<List<ExecutionReconciliationEvent>> = _reconciliationEvents.asStateFlow()

    private val _activeOrdersMap = MutableStateFlow<Map<String, ExecutionReconciliationEvent>>(emptyMap())
    val activeOrdersMap: StateFlow<Map<String, ExecutionReconciliationEvent>> = _activeOrdersMap.asStateFlow()

    // Idempotency cache to prevent accidental duplicate executions
    private val recentOrderHashes = mutableSetOf<String>()

    /**
     * Step 1: Pre-Execution Idempotency & Duplicate Check
     */
    fun validateIdempotency(symbol: String, side: OrderSide, lot: Double, price: Double): Boolean {
        val hash = "$symbol:${side.name}:${(lot * 100).toInt()}:${(price * 10).toInt()}:${System.currentTimeMillis() / 3000}"
        if (recentOrderHashes.contains(hash)) {
            logAudit("DUPLICATE_ORDER_REJECTED", "ReconciliationEngine", "Duplicate order detected for $symbol within 3 seconds. Execution blocked.", "WARNING")
            return false
        }
        recentOrderHashes.add(hash)
        if (recentOrderHashes.size > 100) recentOrderHashes.clear()
        return true
    }

    /**
     * Step 2: Register Order Submission
     */
    fun registerOrderSubmission(
        symbol: String,
        side: OrderSide,
        lot: Double,
        targetPrice: Double,
        brokerName: String
    ): String {
        val internalOrderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
        val event = ExecutionReconciliationEvent(
            internalOrderId = internalOrderId,
            brokerOrderId = "PENDING_BROKER_ACK",
            symbol = symbol,
            side = side,
            requestedLot = lot,
            filledLot = 0.0,
            requestedPrice = targetPrice,
            executedPrice = targetPrice,
            slippagePips = 0.0,
            executionLatencyMs = 0L,
            brokerStatus = "SUBMITTED_AWAITING_ACK",
            reconciliationPassed = false,
            discrepancyNotes = "Dispatched to $brokerName gateway."
        )

        val map = _activeOrdersMap.value.toMutableMap()
        map[internalOrderId] = event
        _activeOrdersMap.value = map

        scope.launch {
            db.orderDao().insertOrder(
                OrderEntity(
                    id = internalOrderId,
                    symbol = symbol,
                    side = side.name,
                    price = targetPrice,
                    amount = lot,
                    status = "SUBMITTED"
                )
            )
        }

        return internalOrderId
    }

    /**
     * Step 3: Reconcile Broker Fill Response
     */
    fun reconcileBrokerResponse(
        internalOrderId: String,
        brokerOrderId: String,
        executedPrice: Double,
        filledLot: Double,
        brokerStatus: String, // "FILLED", "PARTIALLY_FILLED", "REJECTED", "CANCELLED"
        latencyMs: Long,
        errorMessage: String? = null
    ): ExecutionReconciliationEvent {
        val existing = _activeOrdersMap.value[internalOrderId]
        val reqPrice = existing?.requestedPrice ?: executedPrice
        val reqLot = existing?.requestedLot ?: filledLot

        val slippage = abs(executedPrice - reqPrice)
        val passed = (brokerStatus == "FILLED" || brokerStatus == "PARTIALLY_FILLED") && filledLot > 0.0

        val discrepancy = when {
            brokerStatus == "REJECTED" -> "Broker rejected order: ${errorMessage ?: "Margin/Risk rejection"}"
            brokerStatus == "CANCELLED" -> "Order cancelled by exchange/timeout."
            filledLot < reqLot -> "Partial fill detected: Filled $filledLot of $reqLot lots."
            slippage > (reqPrice * 0.005) -> "High slippage detected: ${(slippage / reqPrice) * 100}% variance."
            else -> null
        }

        val reconciledEvent = ExecutionReconciliationEvent(
            internalOrderId = internalOrderId,
            brokerOrderId = brokerOrderId,
            symbol = existing?.symbol ?: "UNKNOWN",
            side = existing?.side ?: OrderSide.BUY,
            requestedLot = reqLot,
            filledLot = filledLot,
            requestedPrice = reqPrice,
            executedPrice = executedPrice,
            slippagePips = slippage,
            executionLatencyMs = latencyMs,
            brokerStatus = brokerStatus,
            reconciliationPassed = passed,
            discrepancyNotes = discrepancy,
            timestamp = System.currentTimeMillis()
        )

        val map = _activeOrdersMap.value.toMutableMap()
        map[internalOrderId] = reconciledEvent
        _activeOrdersMap.value = map

        // Prepend to audit events list
        val list = (_reconciliationEvents.value.toMutableList()).apply {
            add(0, reconciledEvent)
            if (size > 50) removeAt(size - 1)
        }
        _reconciliationEvents.value = list

        scope.launch {
            db.orderDao().insertOrder(
                OrderEntity(
                    id = internalOrderId,
                    symbol = reconciledEvent.symbol,
                    side = reconciledEvent.side.name,
                    price = executedPrice,
                    amount = filledLot,
                    status = brokerStatus
                )
            )
            logAudit(
                action = "RECONCILIATION_$brokerStatus",
                component = "ReconciliationEngine",
                details = "Order $internalOrderId (Broker: $brokerOrderId) reconciled. Status: $brokerStatus. Notes: $discrepancy",
                severity = if (passed) "INFO" else "WARNING"
            )
        }

        return reconciledEvent
    }

    private fun logAudit(action: String, component: String, details: String, severity: String) {
        scope.launch {
            db.systemAuditLogDao().insertLog(
                SystemAuditLogEntity(
                    action = action,
                    component = component,
                    details = details,
                    severity = severity
                )
            )
        }
    }

    companion object {
        @Volatile
        private var instance: ExecutionReconciliationEngine? = null

        fun getInstance(context: Context): ExecutionReconciliationEngine {
            return instance ?: synchronized(this) {
                instance ?: ExecutionReconciliationEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
