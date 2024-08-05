CREATE TABLE Creditor (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          max_financing_rate_in_bps INT NOT NULL
);

CREATE TABLE Purchaser (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(255) NOT NULL
);

CREATE TABLE PurchaserFinancingSettings (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            purchaser_id BIGINT NOT NULL,
                                            creditor_id BIGINT NOT NULL,
                                            minimum_financing_term_in_days INT NOT NULL,
                                            annual_rate_in_bps INT NOT NULL,
                                            FOREIGN KEY (purchaser_id) REFERENCES Purchaser(id),
                                            FOREIGN KEY (creditor_id) REFERENCES Creditor(id)
);

CREATE TABLE Invoice (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         creditor_id BIGINT NOT NULL,
                         debtor_id BIGINT NOT NULL,
                         amount DECIMAL(19, 2) NOT NULL,
                         maturity_date DATE NOT NULL,
                         financed BOOLEAN DEFAULT FALSE,
                         purchaser_id BIGINT,
                         early_payment_amount DECIMAL(19, 2),
                         financing_date DATE,
                         financing_rate INT,
                         FOREIGN KEY (creditor_id) REFERENCES Creditor(id),
                         FOREIGN KEY (purchaser_id) REFERENCES Purchaser(id)
);

CREATE INDEX idx_financed ON Invoice (financed);


