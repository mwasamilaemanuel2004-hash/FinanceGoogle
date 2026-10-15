import { Candle } from './smc_detector';

export interface VolumeProfileResult {
  poc: number; // Point of Control (Highest volume node)
  vah: number; // Value Area High (70% Volume boundary top)
  val: number; // Value Area Low (70% Volume boundary bottom)
  vpin: number; // Volume-Synchronized Probability of Informed Trading (0.0 - 1.0)
  totalVolume: number;
  valueAreaVolume: number;
  bins: Array<{ price: number; volume: number; pctOfTotal: number }>;
  latencyMs: number;
}

export class VolumeProfileCalculator {
  /**
   * Fast Volume Profile & VPIN analysis (< 10ms for 1000 candles).
   */
  public static calculate(candles: Candle[], binCount: number = 30): VolumeProfileResult {
    const t0 = performance.now();
    if (candles.length === 0) {
      return {
        poc: 0,
        vah: 0,
        val: 0,
        vpin: 0,
        totalVolume: 0,
        valueAreaVolume: 0,
        bins: [],
        latencyMs: 0
      };
    }

    let minPrice = Infinity;
    let maxPrice = -Infinity;
    let totalVol = 0;

    candles.forEach(c => {
      if (c.low < minPrice) minPrice = c.low;
      if (c.high > maxPrice) maxPrice = c.high;
      totalVol += c.volume;
    });

    const priceRange = maxPrice - minPrice || 1.0;
    const binSize = priceRange / binCount;

    const volumeBins = new Float64Array(binCount);

    // Distribute candle volume across intersecting price bins
    candles.forEach(c => {
      const candleRange = c.high - c.low || 0.0001;
      const volPerUnit = c.volume / candleRange;

      const startBin = Math.max(0, Math.min(binCount - 1, Math.floor((c.low - minPrice) / binSize)));
      const endBin = Math.max(0, Math.min(binCount - 1, Math.floor((c.high - minPrice) / binSize)));

      if (startBin === endBin) {
        volumeBins[startBin] += c.volume;
      } else {
        const spreadBins = endBin - startBin + 1;
        const portion = c.volume / spreadBins;
        for (let b = startBin; b <= endBin; b++) {
          volumeBins[b] += portion;
        }
      }
    });

    // Find Point of Control (POC)
    let maxBinIdx = 0;
    let maxBinVol = 0;
    for (let i = 0; i < binCount; i++) {
      if (volumeBins[i] > maxBinVol) {
        maxBinVol = volumeBins[i];
        maxBinIdx = i;
      }
    }

    const poc = minPrice + (maxBinIdx + 0.5) * binSize;

    // Calculate Value Area (70% Volume around POC)
    const targetAreaVol = totalVol * 0.70;
    let currentAreaVol = maxBinVol;
    let upperIdx = maxBinIdx;
    let lowerIdx = maxBinIdx;

    while (currentAreaVol < targetAreaVol && (upperIdx < binCount - 1 || lowerIdx > 0)) {
      const nextUpperVol = upperIdx < binCount - 1 ? volumeBins[upperIdx + 1] : 0;
      const nextLowerVol = lowerIdx > 0 ? volumeBins[lowerIdx - 1] : 0;

      if (nextUpperVol >= nextLowerVol && upperIdx < binCount - 1) {
        upperIdx++;
        currentAreaVol += nextUpperVol;
      } else if (lowerIdx > 0) {
        lowerIdx--;
        currentAreaVol += nextLowerVol;
      } else if (upperIdx < binCount - 1) {
        upperIdx++;
        currentAreaVol += nextUpperVol;
      } else {
        break;
      }
    }

    const vah = minPrice + (upperIdx + 1) * binSize;
    const val = minPrice + lowerIdx * binSize;

    // VPIN estimation: Order toxicity = |BuyVol - SellVol| / TotalVol across buckets
    let buyVolTotal = 0;
    let sellVolTotal = 0;
    candles.forEach(c => {
      // Tick rule approximation: Close > Open implies buyer initiated
      if (c.close >= c.open) {
        buyVolTotal += c.volume;
      } else {
        sellVolTotal += c.volume;
      }
    });

    const vpin = totalVol > 0 ? Math.abs(buyVolTotal - sellVolTotal) / totalVol : 0.15;

    const bins = [];
    for (let i = 0; i < binCount; i++) {
      bins.push({
        price: Number((minPrice + (i + 0.5) * binSize).toFixed(2)),
        volume: Number(volumeBins[i].toFixed(1)),
        pctOfTotal: totalVol > 0 ? Number(((volumeBins[i] / totalVol) * 100).toFixed(1)) : 0
      });
    }

    const latencyMs = Number((performance.now() - t0).toFixed(3));
    return {
      poc: Number(poc.toFixed(2)),
      vah: Number(vah.toFixed(2)),
      val: Number(val.toFixed(2)),
      vpin: Number(vpin.toFixed(3)),
      totalVolume: Math.round(totalVol),
      valueAreaVolume: Math.round(currentAreaVol),
      bins,
      latencyMs
    };
  }
}
