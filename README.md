# ESMH.TRADE / MWASA AI — PRODUCTION QUANTITATIVE AGI TRADING SYSTEM

Production-Grade Multi-Asset Autonomous Trading System featuring 35 ML/Deep/Quant Engines, 9 Nobel Prize & MIT Physics Core Equations, 125 DNA Features, and 51 Active Markets.

---

## 🏛️ System Architecture Layers

- **Layer 0 - Data Infrastructure**: Polygon.io (20 Stocks, 10 Forex, Metals, Indices) + Deriv (15 Synthetic Volatility/Crash/Boom) + Binance Crypto (15 Pairs).
- **Layer 1 - Nobel Physics & Quant Core**:
  - Bell Curve Student-t & Pareto Fat-Tail (MLE degrees of freedom, VaR 99)
  - Black-Scholes 1997 Option Pricing & Implied Volatility
  - Ornstein-Uhlenbeck Mean Reversion calibration
  - Engle-Granger Cointegration & Pairs Trading
  - Kyle's Lambda Microstructure & Order Toxicity
  - Ichimoku 5-line Kumo System
  - Wyckoff Phase Accumulation/Distribution Detection
  - Volume Profile (POC, VAH, VAL) & Order Flow CVD
  - Avellaneda-Stoikov Market Making Reservation Spread
  - Kelly Criterion (D.E. Shaw Fat-Tail Vector)
  - Monte Carlo Multiverse Theory (5 Universes)
- **Layer 2 - 35 Engines Ensemble**: Technical (1-10), Deep Learning (11-15), Statistical (16-20), Quant Nobel (21-35).
- **Layer 3 - Execution & Risk Final Boss**: Hard Risk isolation, Kill Switch, Paper Trading, Telegram Alerts.
- **Layer 4 - User Interface**: Android Native (Jetpack Compose M3 Glassmorphism) + Web Dashboard with Tulia 45-min Loader.
- **Layer 5 - Infrastructure & Training**: Python FastAPI Microservice (Port 8001), Model Registry, Feature Store, Drift Detection, Docker & Vercel Cron.

---

## 🚀 Quick Start Guide

### 1. Requirements
- Node.js 18+ or 20+
- Python 3.10+
- Docker & Docker Compose
- Supabase account & API keys

### 2. Environment Setup
Copy the environment variables:
```bash
cp .env.example .env
```
Fill in your `POLYGON_API_KEY`, `DERIV_API_TOKEN`, `BINANCE_API_KEY`, and Supabase credentials.

### 3. Running with Docker Compose (Recommended)
```bash
docker-compose up --build
```
This spins up:
- Next.js Web/API Application on `http://localhost:3000`
- Python ML & Quant Microservice on `http://localhost:8001`

### 4. Running Microservices Manually
**Python Training Service (Port 8001):**
```bash
cd lib/training/python_service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8001
```

---

## 🛡️ Kill Switch & Safety Directives
- **Max Portfolio Heat**: 6%
- **Max Correlated Exposure**: 30%
- **Daily Loss Limit**: -5%
- **Consecutive Loss Cut**: 3 Trades
- **Multiverse Survival Cutoff**: <50% triggers immediate position liquidation and Telegram alerts.
