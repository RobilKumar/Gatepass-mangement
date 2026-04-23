USE GatepassDB;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_employee_role_active' AND object_id = OBJECT_ID('dbo.employee_master'))
    CREATE INDEX ix_employee_role_active ON dbo.employee_master(role, is_active);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_employee_name_code' AND object_id = OBJECT_ID('dbo.employee_master'))
    CREATE INDEX ix_employee_name_code ON dbo.employee_master(employee_name, employee_code);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_gatepass_status_date' AND object_id = OBJECT_ID('dbo.gatepass_request'))
    CREATE INDEX ix_gatepass_status_date ON dbo.gatepass_request(status, request_date);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_gatepass_type_date' AND object_id = OBJECT_ID('dbo.gatepass_request'))
    CREATE INDEX ix_gatepass_type_date ON dbo.gatepass_request(gatepass_type, request_date);
GO




