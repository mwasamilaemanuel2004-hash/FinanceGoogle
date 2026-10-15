/**
 * ESMH.TRADE — DERIV XML BOT GENERATOR
 * Generates XML blocks for app.deriv.com / bot.deriv.com
 * Supports: Volatility Indices (V75, V100, V25), Crash/Boom, Step Index.
 */

export interface DerivXmlConfig {
  market: string;
  tradeType: 'CALL' | 'PUT' | 'DIGITEVEN' | 'DIGITODD';
  stake: number;
  martingaleMultiplier: number;
  takeProfit: number;
  stopLoss: number;
}

export function generateDerivBotXml(config: DerivXmlConfig): string {
  return `<?xml version="1.0" encoding="UTF-8"?>
<xml xmlns="http://www.w3.org/1999/xhtml" collection="false">
  <block type="trade" id="trade_block" x="0" y="0">
    <field name="MARKET">${config.market}</field>
    <field name="SUBMARKET">synthetic_index</field>
    <field name="SYMBOL">${config.market}</field>
    <field name="TRADETYPE">${config.tradeType}</field>
    <field name="CURRENCY">USD</field>
    <statement name="SUBMARKET">
      <block type="trade_definition_market" id="market_def">
        <value name="DURATION">
          <shadow type="math_number" id="dur_val">
            <field name="NUM">5</field>
          </shadow>
        </value>
        <value name="AMOUNT">
          <shadow type="math_number" id="amount_val">
            <field name="NUM">${config.stake}</field>
          </shadow>
        </value>
      </block>
    </statement>
  </block>
  <!-- RISK MANAGEMENT & MARTINGALE MULTIPLIER ${config.martingaleMultiplier} -->
  <!-- TAKE PROFIT: $${config.takeProfit} | STOP LOSS: $${config.stopLoss} -->
</xml>`;
}
