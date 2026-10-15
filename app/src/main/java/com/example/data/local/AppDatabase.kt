package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trade_history")
data class TradeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val marketType: String,
    val side: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val lotSize: Double,
    val pnl: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "CLOSED"
)

@Entity(tableName = "bot_configs")
data class BotConfigEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val strategy: String,
    val allocatedCapital: Double,
    val currentPnl: Double,
    val winRate: Double,
    val totalTrades: Int,
    val isActive: Boolean
)

@Entity(tableName = "system_logs")
data class SystemAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val component: String,
    val details: String,
    val severity: String = "INFO",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "external_accounts")
data class ExternalAccountEntity(
    @PrimaryKey val id: String,
    val brokerName: String,
    val accountNumber: String,
    val balance: Double,
    val currency: String = "USD",
    val isConnected: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val side: String,
    val price: Double,
    val amount: Double,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_api_credentials")
data class UserApiCredentialsEntity(
    @PrimaryKey val exchangeName: String,
    val apiKey: String,
    val secretKey: String,
    val isEnabled: Boolean = true,
    val lastValidated: Long = System.currentTimeMillis()
)

@Dao
interface TradeHistoryDao {
    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeRecordEntity)

    @Query("DELETE FROM trade_history")
    suspend fun clearHistory()
}

@Dao
interface BotConfigDao {
    @Query("SELECT * FROM bot_configs")
    fun getAllBots(): Flow<List<BotConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bots: List<BotConfigEntity>)

    @Update
    suspend fun update(bot: BotConfigEntity)
}

@Dao
interface SystemAuditLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<SystemAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SystemAuditLogEntity)
}

@Dao
interface ExternalAccountDao {
    @Query("SELECT * FROM external_accounts")
    fun getAllAccounts(): Flow<List<ExternalAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: ExternalAccountEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
}

@Dao
interface UserApiCredentialsDao {
    @Query("SELECT * FROM user_api_credentials WHERE exchangeName = :exchangeName LIMIT 1")
    suspend fun getCredentials(exchangeName: String): UserApiCredentialsEntity?

    @Query("SELECT * FROM user_api_credentials")
    fun getAllCredentials(): Flow<List<UserApiCredentialsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: UserApiCredentialsEntity)

    @Delete
    suspend fun deleteCredentials(credentials: UserApiCredentialsEntity)
}

@Database(
    entities = [
        TradeRecordEntity::class,
        BotConfigEntity::class,
        SystemAuditLogEntity::class,
        ExternalAccountEntity::class,
        OrderEntity::class,
        UserApiCredentialsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tradeHistoryDao(): TradeHistoryDao
    abstract fun botConfigDao(): BotConfigDao
    abstract fun systemAuditLogDao(): SystemAuditLogDao
    abstract fun externalAccountDao(): ExternalAccountDao
    abstract fun orderDao(): OrderDao
    abstract fun userApiCredentialsDao(): UserApiCredentialsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esmh_mwas_ai_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
