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
- **Query Parameters**:
    - `symbol` (required): The trade symbol (e.g., `META`, `AAPL`).

#### Example Request
GET http://localhost:8080/api/trade?symbol=META

### **GET /api/trade/range**

Retrieve trades based on the symbol as well as the timerange.



