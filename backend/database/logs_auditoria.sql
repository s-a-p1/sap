CREATE TABLE IF NOT EXISTS logs_auditoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NULL,
    email VARCHAR(150) NULL,
    acao VARCHAR(50) NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    data_hora DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);