# Trade API

The **Trade API** provides endpoints to interact with trade data, including querying trades by symbol and retrieving trades within a specific date and time range.

## Table of Contents

- [Getting Started](#getting-started)
- [Endpoints](#endpoints)
    - [GET /api/trade](#get-apitrade)
    - [GET /api/trade/range](#get-apitraderange)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)
- [License](#license)

---

## Getting Started

### Prerequisites

Ensure you have the following preconfigured:

- Java
- Elasticsearch 
- Kafka 

### Setup

## Endpoints

### **GET /api/trade**

Retrieve trades based on the symbol.

#### Request

- **URL**: `/api/trade`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type     | Required | Description                                      | Example                 |
|-----------|----------|----------|--------------------------------------------------|-------------------------|
| `symbol`  | `string` | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`). | `META`                 |

---

#### Example Request
```http
GET http://localhost:8080/api/trade?symbol=META
```

### **GET /api/trade/range**

Retrieve trades based on the symbol and a specified time range.

#### Request

- **URL**: `/api/trade/range`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type     | Required | Description                                      | Example                 |
|-----------|----------|----------|--------------------------------------------------|-------------------------|
| `symbol`  | `string` | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`). | `META`                 |
| `start`   | `string` (ISO 8601 DateTime) | Yes      | The start of the time range in `YYYY-MM-DD HH:mm` format. | `2025-01-15 00:00`     |
| `end`     | `string` (ISO 8601 DateTime) | Yes      | The end of the time range in `YYYY-MM-DD HH:mm` format.   | `2025-01-15 12:00`     |

---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/range?symbol=META&start=2025-01-15 00:00&end=2025-01-15 12:00
```

### **GET /api/trade/volume/less**

Retrieve trade orders filtered by symbol, time range, and a maximum trade volume.

#### Request

- **URL**: `/api/trade/volume/less`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type                        | Required | Description                                                      | Example                 |
|-----------|-----------------------------|----------|------------------------------------------------------------------|-------------------------|
| `symbol`  | `string`                    | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`).               | `META`                 |
| `start`   | `string` (ISO 8601 DateTime) | Yes      | The start of the time range in `YYYY-MM-DD HH:mm` format.         | `2025-01-14 00:00`     |
| `end`     | `string` (ISO 8601 DateTime) | Yes      | The end of the time range in `YYYY-MM-DD HH:mm` format.           | `2025-01-15 23:59`     |
| `volume`  | `number`                    | Yes      | The maximum trade volume to filter.                              | `200`                  |

---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/volume/less?start=2025-01-14 00:00&end=2025-01-15 23:59&symbol=META&volume=200
```
### **GET /api/trade/aggregate**

Retrieve aggregated statistics on trade volume filtered by symbol and time range.

#### Request

- **URL**: `/api/trade/aggregate`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type                        | Required | Description                                                      | Example                 |
|-----------|-----------------------------|----------|------------------------------------------------------------------|-------------------------|
| `symbol`  | `string`                    | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`).               | `META`                 |
| `start`   | `string` (ISO 8601 DateTime) | Yes      | The start of the time range in `YYYY-MM-DD HH:mm` format.         | `2025-01-14 00:00`     |
| `end`     | `string` (ISO 8601 DateTime) | Yes      | The end of the time range in `YYYY-MM-DD HH:mm` format.           | `2025-01-15 23:59`     |
---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/aggregate?start=2025-01-17 00:00&end=2025-01-17 23:59&symbol=META
```

### **GET /api/trade/significant**

Retrieve trade orders where the price difference (high - low) exceeds a specified threshold.

#### Request

- **URL**: `/api/trade/significant`
- **Method**: `GET`
### **Query Parameters**

| Parameter   | Type       | Required | Description                                                      | Example |
|-------------|------------|----------|------------------------------------------------------------------|--------|
| `symbol`    | `string`   | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`).               | `META` |
| `threshold` | `integer`  | Yes      | The minimum price difference (high - low) for filtering.        | `10`   |
---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/significant?threshold=10&symbol=META
```

### **GET /api/trade/volume**

Retrieve a daily trade volume histogram, calculating the total volume traded for each day.

#### Request

- **URL**: `/api/trade/volume`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type     | Required | Description                                      | Example                 |
|-----------|----------|----------|--------------------------------------------------|-------------------------|
| `symbol`  | `string` | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`). | `META`                 |
---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/volume?symbol=META
```

### **GET /api/trade/volatile**

Retrieve the most significant price differences per day for a given trade symbol, including the high, low, date, and volume for each day with the largest price difference.
#### Request

- **URL**: `/api/trade/volatile`
- **Method**: `GET`
### **Query Parameters**

| Parameter | Type     | Required | Description                                      | Example                 |
|-----------|----------|----------|--------------------------------------------------|-------------------------|
| `symbol`  | `string` | Yes      | The trade symbol to filter (e.g., `META`, `AAPL`). | `META`                 |
---

#### **Example Request**
```http
GET http://localhost:8080/api/trade/volatile?symbol=META
```