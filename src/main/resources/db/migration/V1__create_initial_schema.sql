-- Create customer table
CREATE TABLE CUS (
     id CHAR(12) NOT NULL,
     name VARCHAR(100) NOT NULL,
     PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- Create account table
CREATE TABLE ACC (
     number_account VARCHAR(8) NOT NULL,
     cus_id CHAR(12) NOT NULL,
     balance DECIMAL(19,2) NOT NULL DEFAULT '0.00',

     PRIMARY KEY (number_account),

     KEY fk_cus_id (cus_id),

     CONSTRAINT fk_cus_id
         FOREIGN KEY (cus_id)
             REFERENCES CUS(id),

     CONSTRAINT ck_bal
         CHECK (balance >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


-- Create transaction table
CREATE TABLE TRANS (
       id_trans VARCHAR(50) NOT NULL,
       acc_source VARCHAR(8) NOT NULL,
       acc_receive VARCHAR(8) DEFAULT NULL,
       type_trans ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL,
       amount DECIMAL(19,2) NOT NULL,
       time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       status ENUM('SUCCESS', 'FAILED') NOT NULL,

       PRIMARY KEY (id_trans),

       KEY fk_acc_source (acc_source),
       KEY fk_acc_receive (acc_receive),

       CONSTRAINT fk_acc_receive
           FOREIGN KEY (acc_receive)
               REFERENCES ACC(number_account),

       CONSTRAINT fk_acc_source
           FOREIGN KEY (acc_source)
               REFERENCES ACC(number_account),

       CONSTRAINT ck_acc_diff
           CHECK (
               acc_receive IS NULL
                   OR acc_receive <> acc_source
               ),

       CONSTRAINT ck_amount
           CHECK (amount >= 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
