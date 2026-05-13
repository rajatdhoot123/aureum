# Groww API - Indian Indices (Intercepted)

**Source Page:** https://groww.in/indices/indian-indices  
**Date intercepted:** 2026-05-13

Groww uses internal `stocks_data` APIs (publicly callable with minimal headers) that provide excellent real-time Indian index data directly from NSE/BSE. This is a strong alternative / complement to the current Yahoo Finance wrapper for Indian indices (`^NSEI`, `^BSESN`, `^NSEBANK` etc).

---

## 1. Global Instruments (featured on Indian Indices page)

**Used for:** GIFT NIFTY + major global indices (shown in widgets even on Indian page).

### Minimal cURL
```bash
curl 'https://groww.in/v1/api/stocks_data/v1/global_instruments?instrumentType=GLOBAL_INSTRUMENTS' \
  -H 'accept: application/json' \
  -H 'referer: https://groww.in/indices/indian-indices' \
  -H 'x-app-id: growwWeb' \
  -H 'x-platform: web'
```

### Response Structure
```json
{
  "aggregatedGlobalInstrumentDto": [
    {
      "livePriceDto": {
        "value": 23417,
        "open": 23399.5,
        "high": 23608,
        "low": 23302.5,
        "close": 23404,
        "dayChange": 13,
        "dayChangePerc": 0.06,
        "tsInMillis": 1778679137
      },
      "instrumentDetailDto": {
        "weight": 100,
        "symbol": "SGX NIFTY",
        "name": "GIFT NIFTY",
        "country": "SINGAPORE",
        "continent": "ASIA",
        "gsin": "GFUTSGX32NIFTY",
        "logoUrl": "...",
        "searchId": "sgx-nifty"
      }
    }
    // ... Dow, S&P, Nikkei, Hang Seng, DAX, CAC, FTSE, etc.
  ]
}
```

---

## 2. Index Search / Discovery

**Used for:** Searching and listing Indian indices (NIFTY 50, Bank Nifty, Sensex, sectoral, etc.).

### Minimal cURL
```bash
curl 'https://groww.in/v1/api/stocks_data/v1/company/search_id/nifty?fields=ALL_ASSETS&page=0&size=30' \
  -H 'accept: application/json' \
  -H 'referer: https://groww.in/indices/indian-indices' \
  -H 'x-app-id: growwWeb' \
  -H 'x-platform: web'
```

**Key fields returned per index:**
- `header.searchId` → "nifty", "nifty-bank", "sp-bse-sensex"
- `header.growwCompanyId` → "GIDXNIFTY", "GIDXNIFTYBANK", "GIDXBSESN"
- `header.nseScriptCode` or `bseScriptCode` → "NIFTY", "BANKNIFTY", "1" (for Sensex)
- `header.displayName`, `shortName`
- `yearLowPrice`, `yearHighPrice`

Returns 20+ popular Indian indices when searching "nifty".

---

## 3. Live Indian Index Prices (Recommended for RateWatch)

**Best endpoint discovered:** `accord_points` latest_indices_ohlc

Gives clean LTP + OHLC + change for any Indian index using its NSE/BSE symbol code.

### Examples

**NIFTY 50**
```bash
curl 'https://groww.in/v1/api/stocks_data/v1/accord_points/exchange/NSE/segment/CASH/latest_indices_ohlc/NIFTY' \
  -H 'accept: application/json' \
  -H 'x-app-id: growwWeb' \
  -H 'x-platform: web'
```

**Response:**
```json
{
  "value": 23412.6,
  "open": 23362.45,
  "high": 23582.95,
  "low": 23262.55,
  "close": 23379.55,
  "dayChange": 33.05,
  "dayChangePerc": 0.141,
  "symbol": "NIFTY",
  "tsInMillis": 1778610600,
  "yearHighPrice": 26373.2,
  "yearLowPrice": 22182.55,
  "type": "LIVE_INDEX"
}
```

**BANK NIFTY**
```bash
.../exchange/NSE/segment/CASH/latest_indices_ohlc/BANKNIFTY
```

**SENSEX**
```bash
.../exchange/BSE/segment/CASH/latest_indices_ohlc/SENSEX
```
(Note: SENSEX uses BSE exchange + script code "1", but the ohlc endpoint accepts "SENSEX" symbol.)

**Other popular ones that work:**
- NIFTY IT → `NIFTYIT`
- NIFTY AUTO → `NIFTYAUTO`
- FINNIFTY → `FINNIFTY`
- INDIA VIX → `INDIAVIX`

---

## Mapping from Current RateWatch Symbols

| Current (Yahoo) | Groww Equivalent                          |
|-----------------|-------------------------------------------|
| ^NSEI           | NSE/CASH/NIFTY (searchId: nifty)          |
| ^BSESN          | BSE/CASH/SENSEX (searchId: sp-bse-sensex) |
| ^NSEBANK        | NSE/CASH/BANKNIFTY (searchId: nifty-bank) |
| ^NSEIT          | NSE/CASH/NIFTYIT                          |
| ^CNXAUTO        | NSE/CASH/NIFTYAUTO                        |

---

## Other Useful Groww Endpoints Found

- `GET /v1/api/stocks_data/v1/company/search_id/{searchId}` — full index profile + constituents (for NIFTY 50 stocks)
- `GET /v1/api/stocks_data/v1/global_instruments?instrumentType=GLOBAL_INSTRUMENTS` — GIFT Nifty + world indices
- `GET /v1/api/stocks_data/explore/v2/indices/market_trends/filters` — universe filters (NIFTY 100, 500 etc.)

---

## Recommendation for KwikTwik Tunnel / RateWatch

We can add a **Groww Indian Indices provider** (or primary source for Indian symbols) in the stocks proxy:

1. Maintain a small mapping table: `^NSEI` → `{exchange:"NSE", segment:"CASH", symbol:"NIFTY", searchId:"nifty"}`
2. For unknown symbols, fall back to Yahoo (current behavior).
3. Use the `latest_indices_ohlc` endpoint — it's lightweight, accurate, and low latency for Indian market hours.
4. The `search_id` endpoint can be used to auto-discover new indices or validate symbols.

This would give RateWatch users **faster and more reliable** Indian index updates compared to Yahoo Finance (which often lags or has timezone/previous-close issues for Indian markets).

---

**Headers that are sufficient** (no cookies needed):
- `accept: application/json`
- `referer: https://groww.in/indices/indian-indices`
- `x-app-id: growwWeb`
- `x-platform: web`
- Normal browser User-Agent

All endpoints above return valid JSON without authentication.

