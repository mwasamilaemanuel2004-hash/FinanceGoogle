-- ==============================================================================
-- ESMH.TRADE / MWASA AI — PRODUCTION SUPABASE SCHEMAS & REALTIME MIGRATIONS
-- ==============================================================================

-- 1. Market Data OHLCV
CREATE TABLE IF NOT EXISTS public.market_data_ohlcv (
    id BIGSERIAL PRIMARY KEY,
    market VARCHAR(64) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    open NUMERIC(18, 8) NOT NULL,
    high NUMERIC(18, 8) NOT NULL,
    low NUMERIC(18, 8) NOT NULL,
    close NUMERIC(18, 8) NOT NULL,
    volume NUMERIC(24, 8) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_market_time ON public.market_data_ohlcv (market, timestamp DESC);

-- 2. Feature Store
CREATE TABLE IF NOT EXISTS public.feature_store (
    id BIGSERIAL PRIMARY KEY,
    market VARCHAR(64) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL,
    dna_features JSONB NOT NULL,
    bell_stats JSONB,
    ichimoku_vals JSONB,
    wyckoff_vals JSONB,
    vp_vals JSONB,
    of_vals JSONB,
    ou_vals JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_features_market_time ON public.feature_store (market, timestamp DESC);

-- 3. Model Registry
CREATE TABLE IF NOT EXISTS public.model_registry (
    id SERIAL PRIMARY KEY,
    engine VARCHAR(64) NOT NULL,
    market VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    accuracy NUMERIC(8, 4) NOT NULL,
    f1 NUMERIC(8, 4) NOT NULL,
    sharpe_backtest NUMERIC(8, 4),
    model_path TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    trained_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_model_registry_engine ON public.model_registry (engine, market, status);

-- 4. Paper Trades
CREATE TABLE IF NOT EXISTS public.paper_trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    market VARCHAR(64) NOT NULL,
    side VARCHAR(16) NOT NULL,
    lot NUMERIC(12, 4) NOT NULL,
    entry_price NUMERIC(18, 8) NOT NULL,
    exit_price NUMERIC(18, 8),
    pnl NUMERIC(18, 4) DEFAULT 0.0,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    universe_results JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_paper_market ON public.paper_trades (market, created_at DESC);

-- 5. Audit Logs
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id BIGSERIAL PRIMARY KEY,
    market VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    final_prob NUMERIC(8, 4),
    kelly_lot NUMERIC(12, 4),
    multiverse_survival NUMERIC(8, 4),
    risk_score NUMERIC(8, 4),
    decision_payload JSONB NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_audit_time ON public.audit_logs (timestamp DESC);

-- 6. Kill Events
CREATE TABLE IF NOT EXISTS public.kill_events (
    id BIGSERIAL PRIMARY KEY,
    reason TEXT NOT NULL,
    triggered_by VARCHAR(64) NOT NULL,
    multiverse_survival NUMERIC(8, 4),
    positions_closed INT DEFAULT 0,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.market_data_ohlcv ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.feature_store ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.model_registry ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.paper_trades ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.kill_events ENABLE ROW LEVEL SECURITY;

-- Production RLS Policies: Read allowed for all, Write allowed for service role
DO $$ BEGIN
    DROP POLICY IF EXISTS "Public read market_data" ON public.market_data_ohlcv;
    CREATE POLICY "Public read market_data" ON public.market_data_ohlcv FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public read model_registry" ON public.model_registry;
    CREATE POLICY "Public read model_registry" ON public.model_registry FOR SELECT USING (true);

    DROP POLICY IF EXISTS "Public read paper_trades" ON public.paper_trades;
    CREATE POLICY "Public read paper_trades" ON public.paper_trades FOR SELECT USING (true);
END $$;
