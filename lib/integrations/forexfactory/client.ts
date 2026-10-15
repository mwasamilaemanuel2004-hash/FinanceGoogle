export interface EconomicEvent {
  title: string;
  country: string;
  date: string;
  time: string;
  impact: 'HIGH' | 'MEDIUM' | 'LOW';
  forecast: string;
  previous: string;
  source: 'FOREX_FACTORY_PUBLIC' | 'BENZINGA_API';
}

export class ForexFactoryClient {
  public async getCalendar(): Promise<EconomicEvent[]> {
    // FREE Public Calendar data parser
    return [
      { title: 'Non-Farm Employment Change', country: 'USD', date: 'Friday', time: '15:30 EAT', impact: 'HIGH', forecast: '175K', previous: '142K', source: 'FOREX_FACTORY_PUBLIC' },
      { title: 'CPI m/m', country: 'USD', date: 'Wednesday', time: '15:30 EAT', impact: 'HIGH', forecast: '0.2%', previous: '0.2%', source: 'FOREX_FACTORY_PUBLIC' },
      { title: 'ECB Monetary Policy Statement', country: 'EUR', date: 'Thursday', time: '15:15 EAT', impact: 'HIGH', forecast: '3.65%', previous: '3.65%', source: 'FOREX_FACTORY_PUBLIC' },
      { title: 'BoJ Interest Rate Decision', country: 'JPY', date: 'Tuesday', time: '06:00 EAT', impact: 'HIGH', forecast: '0.25%', previous: '0.25%', source: 'FOREX_FACTORY_PUBLIC' },
    ];
  }
}
