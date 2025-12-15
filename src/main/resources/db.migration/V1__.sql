USE LaundrySystem;  -- Emri i DB-së tënde

-- Fshi FK constraints për të lejuar DROP
ALTER TABLE inventory_transactions DROP CONSTRAINT IF EXISTS FK_INVENTORY_TRANSACTIONS_ON_ORDER;
ALTER TABLE inventory_transactions DROP CONSTRAINT IF EXISTS FK_INVENTORY_TRANSACTIONS_ON_SUPPLY;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS FK_ORDERS_ON_CUSTOMER;
ALTER TABLE order_employees DROP CONSTRAINT IF EXISTS FK_ORDER_EMPLOYEES_ON_EMPLOYEE;
ALTER TABLE order_employees DROP CONSTRAINT IF EXISTS FK_ORDER_EMPLOYEES_ON_ORDER;
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS FK_ORDER_ITEMS_ON_ORDER;
ALTER TABLE order_services DROP CONSTRAINT IF EXISTS FK_ORDER_SERVICES_ON_ORDER_ITEM;
ALTER TABLE order_services DROP CONSTRAINT IF EXISTS FK_ORDER_SERVICES_ON_SERVICE;
ALTER TABLE payments DROP CONSTRAINT IF EXISTS FK_PAYMENTS_ON_ORDER;

-- DROP tables nëse ekzistojnë
DROP TABLE IF EXISTS inventory_transactions;
DROP TABLE IF EXISTS order_employees;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS order_services;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS supplies;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS customers;

-- Krijo tables me DECIMAL (script-i yt i rregulluar)
CREATE TABLE customers
(
    id             int IDENTITY (1, 1) NOT NULL,
    first_name     varchar(50)         NOT NULL,
    last_name      varchar(50)         NOT NULL,
    phone          varchar(20)         NOT NULL,
    email          varchar(100),
    address        varchar(MAX),
    loyalty_points int
        CONSTRAINT DF_customers_loyalty_points DEFAULT 0,
    created_at     datetime            NOT NULL,
    updated_at     datetime            NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id)
)
GO

CREATE TABLE employees
(
    id         int IDENTITY (1, 1) NOT NULL,
    first_name varchar(50)         NOT NULL,
    last_name  varchar(50)         NOT NULL,
    role       varchar(20)         NOT NULL,
    phone      varchar(20),
    password   varchar(255)        NOT NULL,
    salary     decimal(10, 2)      NOT NULL
        CONSTRAINT DF_employees_salary DEFAULT 0.00,
    hire_date  date,
    is_active  bit
        CONSTRAINT DF_employees_is_active DEFAULT 1,
    created_at datetime            NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (id)
)
GO

CREATE TABLE inventory_transactions
(
    id               int IDENTITY (1, 1) NOT NULL,
    supply_id        int                 NOT NULL,
    order_id         int,
    quantity         int                 NOT NULL,
    transaction_type varchar(20),
    transaction_date datetime            NOT NULL,
    notes            varchar(MAX),
    CONSTRAINT pk_inventory_transactions PRIMARY KEY (id)
)
GO

CREATE TABLE order_employees
(
    id            int IDENTITY (1, 1) NOT NULL,
    order_id      int                 NOT NULL,
    employee_id   int                 NOT NULL,
    assigned_role varchar(20),
    assigned_at   datetime            NOT NULL,
    CONSTRAINT pk_order_employees PRIMARY KEY (id)
)
GO

CREATE TABLE order_items
(
    id               int IDENTITY (1, 1) NOT NULL,
    order_id         int                 NOT NULL,
    item_description varchar(100)        NOT NULL,
    quantity         int
        CONSTRAINT DF_order_items_quantity DEFAULT 1,
    unit_price       decimal(5, 2),  -- DECIMAL
    subtotal         decimal(6, 2),  -- DECIMAL
    tag_id           varchar(100),
    CONSTRAINT pk_order_items PRIMARY KEY (id)
)
GO

CREATE TABLE order_services
(
    id            int IDENTITY (1, 1) NOT NULL,
    order_item_id int                 NOT NULL,
    service_id    int                 NOT NULL,
    quantity      int
        CONSTRAINT DF_order_services_quantity DEFAULT 1,
    price         decimal(5, 2),  -- DECIMAL
    CONSTRAINT pk_order_services PRIMARY KEY (id)
)
GO

CREATE TABLE orders
(
    id           int IDENTITY (1, 1) NOT NULL,
    customer_id  int                 NOT NULL,
    dropoff_date date                NOT NULL,
    pickup_date  date,
    status       varchar(20)
        CONSTRAINT DF_orders_status DEFAULT 'received',
    total_amount decimal(8, 2)
        CONSTRAINT DF_orders_total_amount DEFAULT 0,
    notes        varchar(MAX),
    created_at   datetime            NOT NULL,
    updated_at   datetime            NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
)
GO

CREATE TABLE payments
(
    id             int IDENTITY (1, 1) NOT NULL,
    order_id       int                 NOT NULL,
    amount         decimal(8, 2)       NOT NULL,  -- DECIMAL
    payment_method varchar(20),
    payment_date   datetime            NOT NULL,
    status         varchar(20)
        CONSTRAINT DF_payments_status DEFAULT 'pending',
    CONSTRAINT pk_payments PRIMARY KEY (id)
)
GO

CREATE TABLE services
(
    id             int IDENTITY (1, 1) NOT NULL,
    name           varchar(50)         NOT NULL,
    description    varchar(MAX),
    price_per_unit decimal(5, 2)       NOT NULL,  -- DECIMAL
    unit_type      varchar(10)
        CONSTRAINT DF_services_unit_type DEFAULT 'item',
    estimated_time int,
    CONSTRAINT pk_services PRIMARY KEY (id)
)
GO

CREATE TABLE supplies
(
    id            int IDENTITY (1, 1) NOT NULL,
    name          varchar(50)         NOT NULL,
    description   varchar(MAX),
    unit          varchar(10),
    reorder_level int
        CONSTRAINT DF_supplies_reorder_level DEFAULT 10,
    current_stock int
        CONSTRAINT DF_supplies_current_stock DEFAULT 0,
    CONSTRAINT pk_supplies PRIMARY KEY (id)
)
GO

-- Shto unique constraints
ALTER TABLE customers
    ADD CONSTRAINT uc_customers_email UNIQUE (email)
GO

ALTER TABLE customers
    ADD CONSTRAINT uc_customers_phone UNIQUE (phone)
GO

ALTER TABLE order_items
    ADD CONSTRAINT uc_order_items_tag UNIQUE (tag_id)
GO

-- Shto foreign keys
ALTER TABLE inventory_transactions
    ADD CONSTRAINT FK_INVENTORY_TRANSACTIONS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id)
GO

ALTER TABLE inventory_transactions
    ADD CONSTRAINT FK_INVENTORY_TRANSACTIONS_ON_SUPPLY FOREIGN KEY (supply_id) REFERENCES supplies (id)
GO

ALTER TABLE orders
    ADD CONSTRAINT FK_ORDERS_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES customers (id)
GO

ALTER TABLE order_employees
    ADD CONSTRAINT FK_ORDER_EMPLOYEES_ON_EMPLOYEE FOREIGN KEY (employee_id) REFERENCES employees (id)
GO

ALTER TABLE order_employees
    ADD CONSTRAINT FK_ORDER_EMPLOYEES_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id)
GO

ALTER TABLE order_items
    ADD CONSTRAINT FK_ORDER_ITEMS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id)
GO

ALTER TABLE order_services
    ADD CONSTRAINT FK_ORDER_SERVICES_ON_ORDER_ITEM FOREIGN KEY (order_item_id) REFERENCES order_items (id)
GO

ALTER TABLE order_services
    ADD CONSTRAINT FK_ORDER_SERVICES_ON_SERVICE FOREIGN KEY (service_id) REFERENCES services (id)
GO

ALTER TABLE payments
    ADD CONSTRAINT FK_PAYMENTS_ON_ORDER FOREIGN KEY (order_id) REFERENCES orders (id)
GO
