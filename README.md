# CSV → Google Form Uploader (Spring Boot + Swing)

A **unified desktop + backend application** that allows users to upload CSV files via a Java Swing UI, processes rows using Spring Boot, submits data to Google Forms, and generates PASS/FAIL reports with timestamps.

This project runs as a **single executable JAR** that launches both:

* Spring Boot REST API
* Embedded Java Swing UI

---

# 📌 Table of Contents

1. Project Overview
2. Features
3. Architecture
4. Tech Stack
5. Project Structure
6. Installation & Setup
7. Running the Application
8. CSV Format Requirements
9. API Documentation
10. Google Forms Integration
11. Mock Mode
12. Rate Limiting
13. UI Guide
14. Logging & Output Reports
15. Error Handling
16. Development Guide
17. Build & Packaging
18. Future Roadmap

---

# 🎯 Project Overview

The system uploads CSV files, parses rows, sends them to Google Forms, and records successful and failed submissions into separate timestamped CSV files.

Use cases include:

* Warehouse validation uploads
* RC consignment processing
* Bulk Google Form automation

---

# ✨ Features

* Unified Spring Boot + Swing UI
* CSV file upload from desktop
* Google Forms submission via HTTP
* Mock Google Forms mode for testing
* Rate limiting to avoid throttling
* PASS/FAIL CSV export with timestamps
* Backend logging for audit trails
* Upload progress & status UI
* Error reporting per row

---

# 🧠 Architecture Overview

## Visual Architecture Diagram

```
+-----------------------------+
|        Java Swing UI        |
|  (CsvUploaderUI Desktop)   |
+--------------+--------------+
               |
               | Multipart HTTP Upload
               v
+--------------+--------------+
|      Spring Boot API        |
|   CsvUploadController       |
+--------------+--------------+
               |
               | Parse CSV Rows
               v
+--------------+--------------+
|     GoogleFormService       |
|  (Rate Limit + Mock Mode)   |
+--------------+--------------+
         |                |
         |                |
         v                v
+----------------+   +----------------+
| PASS CSV File  |   | FAIL CSV File  |
| timestamped    |   | timestamped    |
+----------------+   +----------------+

Google Forms (External)
        ▲
        |
        +---- HTTP Form Submit (RestTemplate)
```

## Flow Summary

1. User uploads CSV via Swing UI
2. File sent to Spring Boot backend
3. CSV parsed row-by-row
4. Each row submitted to Google Forms or Mock Mode
5. Success rows → PASS CSV
6. Failed rows → FAIL CSV with error reason
7. Logs generated for auditing

---

## Logical Flow Diagram

```
User Clicks Upload
        ↓
Choose CSV File
        ↓
Send Multipart Request
        ↓
Spring Boot Receives File
        ↓
Parse CSV Records
        ↓
Loop Through Rows
        ↓
Rate Limited Google Submit
        ↓
┌─────────────┬─────────────┐
│   SUCCESS   │    FAILURE  │
│ Write PASS  │ Write FAIL  │
└─────────────┴─────────────┘
        ↓
Return Summary to UI
        ↓
Show Status in Desktop UI
```

```
Swing UI (Desktop)
      ↓ multipart upload
Spring Boot API (/api/upload)
      ↓ CSV Parser
GoogleFormService
      ↓ Real Google Form OR Mock Mode
PASS / FAIL CSV Writer
```

---

# 🛠 Tech Stack

| Layer       | Technology         |
| ----------- |--------------------|
| Language    | Java 25            |
| Backend     | Spring Boot 4.0.2  |
| UI          | Java Swing         |
| CSV Parsing | Apache Commons CSV |
| HTTP        | RestTemplate       |
| Logging     | SLF4J              |
| Build       | Maven              |

---

# 📁 Project Structure

```
src/main/java/com/example/csvapp/
 ├── CsvApplication.java        # Spring Boot launcher
 ├── controller/
 │    └── CsvUploadController.java
 ├── constants/
 │    └── FormInputIDs.java
 ├── service/
 │    └── GoogleFormService.java
 └── ui/
      └── CsvUploaderUI.java

resources/
 └── application.properties
```

---

# ⚙️ Installation & Setup

### Requirements

* Java 17+
* Maven

### Clone & Build

```bash
git clone <repo-url>
cd project
mvn clean install
```

---

# ▶️ Running the Application

```bash
mvn spring-boot:run
```

### Startup Behavior

✔ Spring Boot server starts
✔ API listens on port `8030`
✔ Swing UI launches automatically

---

# 📄 CSV Format Requirements

### Required Columns

| Column Name                   | Required |
|-------------------------------| -------- |
| WSN                           | Yes      |
| Brand Name                    | Yes      |
| Seller ID                     | Yes      |
| Vertical                      | Yes      |
| Refinishing task performed    | Yes      |
| PV remarks after Refinishing  | Yes      |

### Example CSV

```csv
WSN,Brand Name,Seller ID,Vertical,Refinishing task performed,PV remarks after Refinishing
1IY4PS_I,RED TAPE,e5c763d953f74a07,Footwear/Sandal,Spotting,No issues (PASS)
```

---

# 🌐 API Documentation

## Upload CSV Endpoint

```
POST /api/upload
Content-Type: multipart/form-data
```

### Request

| Field | Type     |
| ----- | -------- |
| file  | CSV File |

### Response Example

```
Uploaded rows: 100 | Success=92 | Failed=8
```

---

# ☁ Google Forms Integration

### Google Form POST URL

```
https://docs.google.com/forms/d/e/<FORM_ID>/formResponse
```

### CSV → Google Form Mapping

| CSV Column                    | Google Entry ID |
|-------------------------------| --------------- |
| WSN                           | entry.xxxxxx    |
| Brand Name                    | entry.xxxxxx    |
| Seller ID                     | entry.xxxxxx    |
| Refinishing task performed    | entry.xxxxxx    |
| PV remarks after Refinishing  | entry.xxxxxx    |

---

# 🧪 Mock Google Forms Mode

Used for testing without real submissions.

### Enable in `application.properties`

```
mock.google.forms=true
```

### Behavior

* Simulates success/failure
* No external HTTP calls

---

# 🚦 Rate Limiting

Protects Google Forms from being flooded.

### Example Limit

* Max 5 requests/second

### Implemented In

`GoogleFormService`

---

# 🖥 Swing UI Guide

### UI Features

* Upload CSV button
* Upload progress messages
* HTTP response display
* Status label feedback

### Workflow

1. Click Upload CSV
2. Choose file
3. Upload starts
4. Status updates live
5. Completion result shown

---

# 🧾 Logging & Output Reports

### Generated Files

| File                          | Purpose                    |
| ----------------------------- | -------------------------- |
| pass_rows_YYYYMMDD_HHMMSS.csv | Successful rows            |
| fail_rows_YYYYMMDD_HHMMSS.csv | Failed rows + error reason |

### Example FAIL Output

```csv
RC Kolkata,WSN123,CN9982,CPR111,Fail,"Missing WSN"
```

---

# ❌ Error Handling

| Scenario           | Behavior     |
| ------------------ | ------------ |
| Missing CSV fields | FAIL row     |
| HTTP errors        | FAIL row     |
| Google timeout     | FAIL row     |
| Invalid file       | Upload abort |

---

# 👨‍💻 Development Guide

### Key Classes

| Class               | Responsibility                 |
|---------------------|--------------------------------|
| CsvApplication      | App launcher                   |
| CsvUploaderUI       | Desktop UI                     |
| CsvUploadController | File handling API              |
| GoogleFormService   | Google Form submission         |
| FormInputIDs        | Stores Google form's input IDs |

---

# 📦 Build & Packaging

### Build JAR

```bash
mvn clean package
```

### Run JAR

```bash
java -Djava.awt.headless=false -jar target/csv-google-form-app-0.0.1-SNAPSHOT.jar
```

---

# 🗺 Future Roadmap

* Web UI version
* Database persistence
* Retry failed rows automatically
* Export ZIP reports
* Docker deployment
* Authentication layer
* Multi-form routing

---

# 📣 Support & Contribution

Contributions welcome.

If issues arise, submit logs + sample CSV for debugging.

---

# 🎉 End of Documentation