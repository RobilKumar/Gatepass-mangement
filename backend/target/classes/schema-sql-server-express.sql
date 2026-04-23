IF DB_ID('GatepassDB') IS NULL
BEGIN
    CREATE DATABASE GatepassDB;
END;
GO

USE GatepassDB;
GO

IF OBJECT_ID('dbo.gatepass_request', 'U') IS NOT NULL DROP TABLE dbo.gatepass_request;
IF OBJECT_ID('dbo.employee_master', 'U') IS NOT NULL DROP TABLE dbo.employee_master;
IF OBJECT_ID('dbo.plant_master', 'U') IS NOT NULL DROP TABLE dbo.plant_master;
IF OBJECT_ID('dbo.department_master', 'U') IS NOT NULL DROP TABLE dbo.department_master;
GO

CREATE TABLE dbo.department_master (
    department_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE TABLE dbo.plant_master (
    plant_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    plant_code VARCHAR(50) NOT NULL UNIQUE,
    plant_name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE TABLE dbo.employee_master (
    employee_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    employee_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL DEFAULT '123456',
    mobile VARCHAR(20) NULL,
    designation VARCHAR(100) NULL,
    department_id BIGINT NULL,
    plant_id BIGINT NULL,
    reporting_manager_id BIGINT NULL,
    role VARCHAR(30) NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id) REFERENCES dbo.department_master(department_id),
    CONSTRAINT fk_employee_plant
        FOREIGN KEY (plant_id) REFERENCES dbo.plant_master(plant_id),
    CONSTRAINT fk_employee_reporting_manager
        FOREIGN KEY (reporting_manager_id) REFERENCES dbo.employee_master(employee_id),
    CONSTRAINT ck_employee_role
        CHECK (role IN ('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN', 'SECURITY'))
);
GO

CREATE TABLE dbo.gatepass_request (
    gatepass_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    request_no VARCHAR(50) NOT NULL UNIQUE,
    employee_id BIGINT NOT NULL,
    manager_id BIGINT NOT NULL,
    gatepass_type VARCHAR(30) NOT NULL,
    request_date DATE NOT NULL,
    from_time TIME NOT NULL,
    to_time TIME NOT NULL,
    half_day_session VARCHAR(20) NULL,
    from_plant_id BIGINT NULL,
    to_plant_id BIGINT NULL,
    out_location VARCHAR(200) NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING_MANAGER_APPROVAL',
    manager_remarks VARCHAR(500) NULL,
    manager_action_date DATETIME2 NULL,
    checkout_time DATETIME2 NULL,
    checkin_time DATETIME2 NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),

    CONSTRAINT fk_gatepass_employee
        FOREIGN KEY (employee_id) REFERENCES dbo.employee_master(employee_id),
    CONSTRAINT fk_gatepass_manager
        FOREIGN KEY (manager_id) REFERENCES dbo.employee_master(employee_id),
    CONSTRAINT fk_gatepass_from_plant
        FOREIGN KEY (from_plant_id) REFERENCES dbo.plant_master(plant_id),
    CONSTRAINT fk_gatepass_to_plant
        FOREIGN KEY (to_plant_id) REFERENCES dbo.plant_master(plant_id),
    CONSTRAINT ck_gatepass_type
        CHECK (gatepass_type IN ('PLANT_TO_PLANT', 'OUT_DUTY', 'HALF_DAY', 'PERSONAL')),
    CONSTRAINT ck_gatepass_status
        CHECK (status IN (
            'PENDING_MANAGER_APPROVAL',
            'APPROVED_BY_MANAGER',
            'REJECTED_BY_MANAGER',
            'CHECKED_OUT',
            'CHECKED_IN',
            'CANCELLED'
        )),
    CONSTRAINT ck_half_day_session
        CHECK (half_day_session IS NULL OR half_day_session IN ('FIRST_HALF', 'SECOND_HALF')),
    CONSTRAINT ck_time_range
        CHECK (from_time < to_time)
);
GO

CREATE INDEX ix_employee_manager ON dbo.employee_master(reporting_manager_id);
CREATE INDEX ix_employee_role_active ON dbo.employee_master(role, is_active);
CREATE INDEX ix_employee_name_code ON dbo.employee_master(employee_name, employee_code);
CREATE INDEX ix_gatepass_manager_status ON dbo.gatepass_request(manager_id, status);
CREATE INDEX ix_gatepass_employee ON dbo.gatepass_request(employee_id);
CREATE INDEX ix_gatepass_date_status ON dbo.gatepass_request(request_date, status);
CREATE INDEX ix_gatepass_status_date ON dbo.gatepass_request(status, request_date);
CREATE INDEX ix_gatepass_type_date ON dbo.gatepass_request(gatepass_type, request_date);
GO

INSERT INTO dbo.department_master (department_name) VALUES
('HR'),
('Production'),
('Maintenance'),
('IT');
GO

INSERT INTO dbo.plant_master (plant_code, plant_name, location) VALUES
('P1', 'Plant 1', 'Main Campus'),
('P2', 'Plant 2', 'Industrial Area'),
('P3', 'Plant 3', 'Warehouse Side');
GO

INSERT INTO dbo.employee_master
(employee_code, employee_name, email, password, mobile, designation, department_id, plant_id, reporting_manager_id, role)
VALUES
('ADM001', 'System Admin', 'admin@company.com', 'admin123', '9000000000', 'System Administrator', 4, 1, NULL, 'ADMIN'),
('MGR001', 'Rajesh Manager', 'rajesh@company.com', 'manager123', '9000000001', 'Production Manager', 2, 1, NULL, 'MANAGER'),
('HR001', 'Neha HR', 'neha.hr@company.com', 'hr123', '9000000002', 'HR Executive', 1, 1, NULL, 'HR'),
('SEC001', 'Gate Security', 'security@company.com', 'security123', '9000000003', 'Security Officer', 1, 1, NULL, 'SECURITY');
GO

INSERT INTO dbo.employee_master
(employee_code, employee_name, email, password, mobile, designation, department_id, plant_id, reporting_manager_id, role)
VALUES
('EMP001', 'Amit Employee', 'amit@company.com', 'emp123', '9000000011', 'Operator', 2, 1, 2, 'EMPLOYEE'),
('EMP002', 'Priya Employee', 'priya@company.com', 'emp123', '9000000012', 'Technician', 3, 1, 2, 'EMPLOYEE'),
('EMP003', 'Suresh Employee', 'suresh@company.com', 'emp123', '9000000013', 'Operator', 2, 2, 2, 'EMPLOYEE');
GO

-- Useful approval query for manager dashboard:
-- SELECT * FROM dbo.gatepass_request
-- WHERE manager_id = @LoggedInManagerId
-- AND status = 'PENDING_MANAGER_APPROVAL'
-- ORDER BY created_at DESC;
