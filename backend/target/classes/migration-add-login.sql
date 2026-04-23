USE GatepassDB;
GO

IF COL_LENGTH('dbo.employee_master', 'password') IS NULL
BEGIN
    ALTER TABLE dbo.employee_master
    ADD password VARCHAR(100) NOT NULL CONSTRAINT df_employee_password DEFAULT '123456';
END;
GO

UPDATE dbo.employee_master SET password = 'admin123' WHERE employee_code = 'ADM001';
UPDATE dbo.employee_master SET password = 'manager123' WHERE employee_code = 'MGR001';
UPDATE dbo.employee_master SET password = 'hr123' WHERE employee_code = 'HR001';
UPDATE dbo.employee_master SET password = 'security123' WHERE employee_code = 'SEC001';
UPDATE dbo.employee_master SET password = 'emp123' WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.employee_master WHERE employee_code = 'ADM001')
BEGIN
    INSERT INTO dbo.employee_master
    (employee_code, employee_name, email, password, mobile, designation, department_id, plant_id, reporting_manager_id, role)
    VALUES
    ('ADM001', 'System Admin', 'admin@company.com', 'admin123', '9000000000', 'System Administrator', 4, 1, NULL, 'ADMIN');
END;
GO
