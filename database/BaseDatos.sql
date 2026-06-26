CREATE SCHEMA "db-clients";
CREATE SCHEMA "db-accounts";
CREATE SCHEMA "db-movements";

-- ==========================================
-- SCHEMAS & COMMON TABLES
-- ==========================================

-- db-clients common tables
CREATE TABLE "db-clients".history (id SERIAL PRIMARY KEY, entity_name VARCHAR(100), entity_id VARCHAR(100), action VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-clients".audit (id SERIAL PRIMARY KEY, user_id VARCHAR(100), action VARCHAR(255), details TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-clients".events (id SERIAL PRIMARY KEY, event_type VARCHAR(100), payload TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-clients".errors (id SERIAL PRIMARY KEY, error_code VARCHAR(50), error_message TEXT, stack_trace TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

-- db-accounts common tables
CREATE TABLE "db-accounts".history (id SERIAL PRIMARY KEY, entity_name VARCHAR(100), entity_id VARCHAR(100), action VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-accounts".audit (id SERIAL PRIMARY KEY, user_id VARCHAR(100), action VARCHAR(255), details TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-accounts".events (id SERIAL PRIMARY KEY, event_type VARCHAR(100), payload TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-accounts".errors (id SERIAL PRIMARY KEY, error_code VARCHAR(50), error_message TEXT, stack_trace TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

-- db-movements common tables
CREATE TABLE "db-movements".history (id SERIAL PRIMARY KEY, entity_name VARCHAR(100), entity_id VARCHAR(100), action VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-movements".audit (id SERIAL PRIMARY KEY, user_id VARCHAR(100), action VARCHAR(255), details TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-movements".events (id SERIAL PRIMARY KEY, event_type VARCHAR(100), payload TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE "db-movements".errors (id SERIAL PRIMARY KEY, error_code VARCHAR(50), error_message TEXT, stack_trace TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

-- ==========================================
-- DOMAIN ENTITIES
-- ==========================================

-- db-clients Entities
CREATE TABLE "db-clients".person (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(50),
    age INT,
    identification VARCHAR(50) UNIQUE NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50)
);

CREATE TABLE "db-clients".client (
    id INT PRIMARY KEY REFERENCES "db-clients".person(id),
    client_id INT UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    status BOOLEAN NOT NULL
);

-- db-accounts Entities
CREATE TABLE "db-accounts".account (
    account_number VARCHAR(50) PRIMARY KEY,
    account_id VARCHAR(36) UNIQUE NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    initial_balance NUMERIC(15, 2) NOT NULL,
    status BOOLEAN NOT NULL,
    client_id INT NOT NULL -- Logical reference to client_id in db-clients.client
);

-- db-movements Entities
CREATE TABLE "db-movements".movement (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    value NUMERIC(15, 2) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL,
    account_number VARCHAR(50) NOT NULL -- Logical reference to account_number in db-accounts.account
);

-- ==========================================
-- SAMPLE DATA
-- ==========================================

-- Insert Persons
INSERT INTO "db-clients".person (id, name, gender, age, identification, address, phone) VALUES
(1, 'Jose Lema', 'Masculino', 30, '1710034065', 'Otavalo sn y principal', '098254785'),
(2, 'Marianela Montalvo', 'Femenino', 28, '0910000017', 'Amazonas y NNUU', '097548965'),
(3, 'Juan Osorio', 'Masculino', 35, '1720000007', '13 junio y Equinoccial', '098874587');

-- Insert Clients
INSERT INTO "db-clients".client (id, client_id, password, status) VALUES
(1, 1, '1234', TRUE),
(2, 2, '5678', TRUE),
(3, 3, '1245', TRUE);

-- Insert Accounts (account_number stored AES-encrypted, same as AccountMapper)
INSERT INTO "db-accounts".account (account_number, account_id, account_type, initial_balance, status, client_id) VALUES
('p/BVPruk9O1bAlfdgQuFiA==', '11111111-1111-1111-1111-111111111111', 'Ahorro', 2000.00, TRUE, 1),
('yasFB2UOU2VfylHfDkn3jw==', '22222222-2222-2222-2222-222222222222', 'Corriente', 100.00, TRUE, 2),
('/qXeeCuUHRUqwC9jfFyUGg==', '33333333-3333-3333-3333-333333333333', 'Ahorros', 0.00, TRUE, 3),
('uPowMyo3o5fIS11p3Zr/cA==', '44444444-4444-4444-4444-444444444444', 'Ahorros', 540.00, TRUE, 2);

-- Note: The sample movements are left empty as they should be created via the API to properly calculate balances and send events.

-- Update sequences
SELECT setval(pg_get_serial_sequence('"db-clients".person', 'id'), coalesce(max(id),0) + 1, false) FROM "db-clients".person;
SELECT setval(pg_get_serial_sequence('"db-movements".movement', 'id'), coalesce(max(id),0) + 1, false) FROM "db-movements".movement;
