# Gatepass Management Backend

Spring Boot backend for a gatepass workflow using SQL Server Express.

## What This First Version Includes

- Department master
- Plant master
- Employee master with reporting manager mapping
- Employee gatepass request creation
- Manager approval and rejection
- HR gatepass list/report view
- Security approved list, checkout, and checkin

## SQL Server Express Setup

Open SQL Server Management Studio and run:

```sql
src/main/resources/schema-sql-server-express.sql
```

The script creates:

- `GatepassDB`
- `department_master`
- `plant_master`
- `employee_master`
- `gatepass_request`
- sample manager, HR, security, and employees

## Backend Configuration

Update this file with your SQL Server username/password:

```text
src/main/resources/application.properties
```

Default SQL Server Express instance:

```properties
spring.datasource.url=jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=GatepassDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourStrongPassword
```

If your SQL Server Express runs on port `1433`, use:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=GatepassDB;encrypt=true;trustServerCertificate=true
```

## Run

```bash
mvn spring-boot:run
```

The backend starts on:

```text
http://localhost:8080
```

## React Frontend

The frontend is inside:

```text
frontend
```

Run backend first:

```bash
mvn spring-boot:run
```

Then open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

For network/LAN access, run frontend with host binding:

```bash
npm run dev -- --host 0.0.0.0
```

Then open it from another computer using your machine IP:

```text
http://YOUR_PC_IP:5173
```

If frontend and backend are on different addresses, create this file:

```text
frontend/.env
```

Example:

```properties
VITE_API_BASE_URL=http://YOUR_BACKEND_PC_IP:8080
```

After changing `.env`, restart frontend.

Backend CORS is controlled from:

```properties
app.cors.allowed-origin-patterns=http://localhost:*,http://127.0.0.1:*,http://192.168.*.*
```

If your frontend is on a fixed IP/domain, add it there and restart backend.

The frontend uses a simple role/user selector for first testing:

- Admin manages masters and views all records
- Employee creates request
- Manager approves or rejects
- HR views all requests
- Security performs checkout and checkin

Real login/JWT can be added after this workflow is confirmed.

## Login Credentials

Run this migration once if your database already exists:

```sql
src/main/resources/migration-add-login.sql
```

Default users:

| Role | Login ID | Password |
|---|---|---|
| Admin | `ADM001` | `admin123` |
| Employee | `EMP001` | `emp123` |
| Manager | `MGR001` | `manager123` |
| HR | `HR001` | `hr123` |
| Security | `SEC001` | `security123` |

Role behavior:

- Employee can create and view own requests.
- Manager can view only requests assigned to that manager.
- HR can view all requests.
- Security can view approved and checked-out requests.
- Admin can manage masters and access all frontend portals.

## Excel Movement Report

HR and Admin can download a filtered Excel movement report from the frontend.

Backend endpoint:

```http
GET /api/reports/gatepass-movement.xlsx
```

Optional filters:

```text
fromDate=2026-04-01
toDate=2026-04-30
fromTime=09:00:00
toTime=18:00:00
employeeId=5
managerId=2
gatepassType=OUT_DUTY
status=APPROVED_BY_MANAGER
```

Example:

```http
GET http://localhost:8080/api/reports/gatepass-movement.xlsx?fromDate=2026-04-01&toDate=2026-04-30&managerId=2
```

The Excel contains employee, manager, date/time, plant movement, out location, reason, approval status, checkout, and checkin details.

## Large Data Handling

The UI is designed for large master/report data such as 10,000+ employees or gatepass records.

- Employee Master uses server-side search and pagination.
- HR movement list uses server-side search and pagination.
- Manager approvals load only that manager's pending requests.
- Security queues load only approved/checked-out records by page.
- Reporting Manager assignment uses manager search instead of loading all managers.
- Excel download is still generated from backend filters, so the browser does not render the full dataset.

Useful migrations for existing databases:

```sql
src/main/resources/migration-performance-indexes.sql
```

If your existing database was created before the Admin user was added to the seed script, run this once in SSMS:

```sql
USE GatepassDB;

IF NOT EXISTS (SELECT 1 FROM dbo.employee_master WHERE employee_code = 'ADM001')
BEGIN
    INSERT INTO dbo.employee_master
    (employee_code, employee_name, email, mobile, designation, department_id, plant_id, reporting_manager_id, role)
    VALUES
    ('ADM001', 'System Admin', 'admin@company.com', '9000000000', 'System Administrator', 4, 1, NULL, 'ADMIN');
END;
```

## Important Header-Based User Simulation

This first backend does not include JWT login yet. For testing:

- Employee APIs use `X-Employee-Id`
- Manager APIs use `X-Manager-Id`

From the latest seed data:

- System Admin has `employee_id = 1`
- Manager Rajesh has `employee_id = 2`
- Employee Amit has `employee_id = 5`
- Employee Priya has `employee_id = 6`
- Employee Suresh has `employee_id = 7`

If your database was already created before the Admin seed was added, your existing IDs may still be:

- Manager Rajesh has `employee_id = 1`
- Employee Amit has `employee_id = 4`
- Employee Priya has `employee_id = 5`
- Employee Suresh has `employee_id = 6`

## Workflow

1. Employee creates a gatepass.
2. System assigns it to employee's reporting manager.
3. Manager sees it in pending approval.
4. Manager approves or rejects.
5. HR can see all records.
6. Security can checkout/checkin approved gatepasses.
