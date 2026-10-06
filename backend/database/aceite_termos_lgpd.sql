ALTER TABLE usuarios
    ADD COLUMN aceitou_termos BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_aceite_termos DATETIME(6) NULL,
    ADD COLUMN versao_termos VARCHAR(20) NULL;
