CREATE DATABASE IF NOT EXISTS warehouse_db;
USE warehouse_db;

CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    stock INT NOT NULL,
    price DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS dispatches (
    id VARCHAR(20) PRIMARY KEY,
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    recipient VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS returns (
    id VARCHAR(20) PRIMARY KEY,
    dispatch_id VARCHAR(20) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    reason VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    FOREIGN KEY (dispatch_id) REFERENCES dispatches(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
