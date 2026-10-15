import crypto from 'crypto';

/**
 * Binance API Connector (Real)
 * HMAC-SHA256 Signature Logic & Async Fetchers
 */

const BASE_URL_SPOT = 'https://api.binance.com';
const BASE_URL_FUTURES = 'https://fapi.binance.com';

function getSignature(query: string, secret: string): string {
    return crypto.createHmac('sha256', secret).update(query).digest('hex');
}

async function fetchBinanceData(
    baseUrl: string,
    endpoint: string,
    apiKey: string,
    secret: string,
    query: string
): Promise<any> {
    const timestamp = Date.now();
    const fullQuery = `${query}&timestamp=${timestamp}`;
    const signature = getSignature(fullQuery, secret);
    const url = `${baseUrl}${endpoint}?${fullQuery}&signature=${signature}`;

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'X-MBX-APIKEY': apiKey
        }
    });

    const data = await response.json();
    
    if (!response.ok) {
        throw {
            status: response.status,
            code: data.code,
            message: data.msg
        };
    }
    
    return data;
}

export async function getBinanceBalances(apiKey: string, secret: string) {
    try {
        const spotData = await fetchBinanceData(BASE_URL_SPOT, '/api/v3/account', apiKey, secret, 'recvWindow=5000');
        const futuresData = await fetchBinanceData(BASE_URL_FUTURES, '/fapi/v2/balance', apiKey, secret, 'recvWindow=5000');
        
        return {
            status: 'CONNECTED',
            spot: spotData.balances,
            futures: futuresData
        };
    } catch (error: any) {
        return {
            status: 'ERROR',
            code: error.code || 'UNKNOWN',
            message: error.message || 'Connection failed'
        };
    }
}

export async function getBinancePositions(apiKey: string, secret: string) {
    try {
        return await fetchBinanceData(BASE_URL_FUTURES, '/fapi/v2/positionRisk', apiKey, secret, 'recvWindow=5000');
    } catch (error: any) {
        return {
            status: 'ERROR',
            code: error.code || 'UNKNOWN',
            message: error.message || 'Failed to fetch positions'
        };
    }
}
