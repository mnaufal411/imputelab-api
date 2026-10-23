# ImputeLab

**A web-based tool for dataset profiling and missing-value imputation benchmarking.**

ImputeLab is a full-stack application designed to help users inspect datasets, understand missing-value patterns, and compare the performance of different imputation strategies before deciding how to handle missing data.

Instead of applying an imputation method without evaluating its suitability, ImputeLab provides dataset profiling and comparative benchmarking to support more informed data preprocessing decisions.

## Features

### 1. Dataset Profiling
- Upload a CSV dataset for analysis.
- Inspect column types and dataset structure.
- Identify missing values and calculate missing-value statistics.
- Review column-level profiling results.

### 2. Imputation Benchmarking
- Evaluate multiple missing-value imputation strategies.
- Compare imputation results using relevant evaluation metrics.
- Inspect benchmark results at the strategy and column levels.
- Identify the best-performing strategy based on the benchmark results.
- Review skipped columns and their associated reasons when a strategy cannot be evaluated.

### 3. Analysis History
- Persist profiling and benchmarking results.
- Browse previous analyses.
- Inspect detailed results for individual analysis runs.
- Delete saved analysis records.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React, Vite, JavaScript, CSS |
| Backend | Java, Spring Boot |
| Build Tool | Maven |
| Database | PostgreSQL |
| API Style | REST API |
| Testing | JUnit 5, Spring Boot Test |
| Version Control | Git |

## Architecture

ImputeLab separates its user interface, backend processing, and persistent storage.

```text
┌──────────────────────────┐
│     React Frontend       │
│   Dataset & Results UI   │
└────────────┬─────────────┘
             │ HTTP / REST
             ▼
┌──────────────────────────┐
│   Spring Boot Backend    │
│                          │
│  Dataset Profiling       │
│  Imputation Benchmarking │
│  History Management      │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       PostgreSQL         │
│   Persistent Results     │
└──────────────────────────┘
```

The frontend communicates with the backend through REST endpoints. The backend handles dataset processing and benchmarking, while PostgreSQL stores analysis history and associated results.

## Getting Started

### Prerequisites

Make sure the following software is installed:

- Java 21 or a compatible version required by the project
- Node.js and npm
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/mnaufal411/imputelab-api.git
cd imputelab-api
```

### 2. Configure PostgreSQL

Create a PostgreSQL database and a dedicated database user for the application.

Example SQL:

```sql
CREATE USER imputelab_user WITH PASSWORD 'your_password';
CREATE DATABASE imputelab OWNER imputelab_user;
```

If the database or user already exists, reuse the existing configuration instead.

### 3. Configure Environment Variables

Create a `.env` file in the project root:

```env
DB_PASSWORD=your_password
```

The backend reads this value through the `DB_PASSWORD` environment variable. Keep `.env` private and never commit it to version control.

The database URL and username are configured in:

`api/src/main/resources/application.properties`

### 4. Install Frontend Dependencies

```bash
cd frontend
npm install
cd ..
```

### 5. Start the Application

Run both the backend and frontend with the development script:

```bash
./dev.sh
```

Once the services have started, open:

**Frontend:** http://localhost:5173

**Backend:** http://localhost:8080

Press `Ctrl+C` in the terminal to stop the development processes.

## REST API

The backend exposes endpoints for dataset profiling, benchmarking, and analysis history.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/datasets/profile` | Profile an uploaded dataset |
| `POST` | `/api/datasets/benchmark` | Run an imputation benchmark |
| `GET` | `/api/history` | Retrieve analysis history |
| `GET` | `/api/history/{id}` | Retrieve details of an analysis |
| `DELETE` | `/api/history/{id}` | Delete an analysis record |

For request formats, response schemas, and validation rules, refer to the backend source code and DTO definitions.

## Testing

Run the backend test suite:

```bash
cd api
./mvnw clean verify
```

Build the frontend to check production compilation:

```bash
cd frontend
npm run build
```

## Project Structure

```text
imputelab-api/
├── api/
│   ├── src/
│   │   ├── main/java/com/imputlab/api/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── validation/
│   │   ├── main/resources/
│   │   └── test/
│   ├── pom.xml
│   └── mvnw
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   └── App.jsx
│   └── package.json
├── .env                 # Local configuration; not committed
├── .gitignore
├── dev.sh
└── README.md
```

## Project Goals

ImputeLab explores the practical application of backend development, data preprocessing, and database persistence in a full-stack application.

Its main goals are to:

- Make missing-value patterns easier to inspect.
- Support comparison of alternative imputation strategies.
- Keep benchmark results available for later review.
- Provide a foundation for future improvements to data preprocessing workflows.

## Future Improvements

Potential areas for further development include:

- Applying the selected imputation strategy to the original dataset.
- Exporting the processed dataset as a downloadable CSV file.
- Expanding benchmark metrics and supported imputation methods.
- Improving visualization and interpretability of benchmark results.
- Adding more comprehensive API documentation.

## License

No license has been specified yet. Add a `LICENSE` file if you intend to distribute the project under an open-source license.

