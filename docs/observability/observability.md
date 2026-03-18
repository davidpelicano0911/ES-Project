# Observability — Defined SLOs and SLIs

## 🔹 1. Operimus API Availability

**Objective:**  
Measures the uptime and availability of the Marketing API service in production. Target is 99% successful requests over a 30-day rolling window.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Availability |
| **SLI metric** | Ratio of successful requests to total requests |
| **Target** | 99 % |
| **Time window** | 30-day rolling |

---

## 🔹 2. Campaign POST Latency

**Objective:**  
Ensures 95% of POST /api/v3/campaigns/ responses complete in under 600 ms over a 30-day rolling window.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Latency |
| **SLI metric** | Percentage of `POST /api/v3/campaigns` requests completed in ≤ 600 ms |
| **Target** | 95 % |
| **Time window** | 30-day rolling |

---

## 🔹 3. Event Index Freshness

**Objective:**  
≥99% of events appear in Elasticsearch within 120s of publication over a 30-day rolling window.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Latency |
| **SLI metric** | Percentage of events with `index_lag_seconds ≤ 120` |
| **Target** | 99.9 % |
| **Time window** | 30-day rolling |

---

## 🔹 4. Login Success Latency

**Objective:**  
Ensures 99 % of successful login attempts complete in under 1 second over a 30-day rolling window.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Latency |
| **SLI metric** | Percentage of successful login attempts completed in ≤ 1 s |
| **Target** | 99.99 % |
| **Time window** | 30-day rolling |

## 🔹 5. Frontend Navigation Latency

**Objective:**  
Ensures that 95% of page navigation actions complete within 650 ms.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Latency |
| **SLI metric** | Percentage of frontend navigation transactions with duration ≤ 650 ms |
| **Target** | 95 % |
| **Time window** | 30-day rolling |

---

## 🔹 6. Frontend Page Load Time

**Objective:**  
Ensures that 99% of frontend page-load transactions complete within 800 ms.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Performance |
| **SLI metric** | Percentage of page-load transactions completed in ≤ 800 ms |
| **Target** | 99 % |
| **Time window** | 30-day rolling |

---

## 🔹 7. Frontend API Call Success Rate

**Objective:**  
Ensures that at least 95% of frontend-initiated HTTP requests succeed (from the user's perspective).

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Availability |
| **SLI metric** | Ratio of successful frontend HTTP spans to total frontend HTTP spans |
| **Target** | 95 % |
| **Time window** | 30-day rolling |

---

## 🔹 9. User Interaction Responsiveness

**Objective:**  
Ensures that 99% of user-triggered actions (button clicks, form submissions, modal open actions) complete within 50 ms.

| Attribute | Definition |
|------------|-------------|
| **SLI type** | Latency |
| **SLI metric** | Percentage of user-interaction spans completed in ≤ 50 ms |
| **Target** | 99 % |
| **Time window** | 30-day rolling |

---