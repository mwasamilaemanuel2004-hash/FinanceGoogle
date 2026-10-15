import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { market = 'BTC/USDT', timeframe = '1m' } = body;
    
    // In production, fetch via Polygon SDK client
    const fetchedCount = 1000;
    
    return NextResponse.json({
      status: 'SUCCESS',
      market,
      timeframe,
      candlesSaved: fetchedCount,
      timestamp: new Date().toISOString()
    });
  } catch (error: any) {
    return NextResponse.json({ status: 'ERROR', message: error.message }, { status: 500 });
  }
}
