-- Vincula cada usuario a un único funcionario (relación 1:1)
ALTER TABLE personas.usuario
    ADD COLUMN id_funcionario BIGINT UNIQUE REFERENCES personas.funcionario (id_funcionario);
