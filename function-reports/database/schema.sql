-- Esquema esperado pela função de relatório (Azure SQL / SQL Server).
-- A tabela é criada/alimentada pela API de avaliações (Pessoa 2); alinhe os nomes das colunas com ela.

IF OBJECT_ID('dbo.Avaliacoes', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Avaliacoes (
        id          BIGINT IDENTITY(1,1) PRIMARY KEY,
        descricao   NVARCHAR(1000) NOT NULL,
        nota        INT            NOT NULL CHECK (nota BETWEEN 0 AND 10),
        urgencia    VARCHAR(10)    NOT NULL,            -- CRITICO | REVISAR | OK
        data_envio  DATETIME2      NOT NULL DEFAULT SYSUTCDATETIME()
    );

    CREATE INDEX IX_Avaliacoes_data_envio ON dbo.Avaliacoes (data_envio);
END
GO

-- Usuário somente leitura para a função de relatório (princípio do menor privilégio).
-- Execute no banco (não no master). Troque a senha por uma forte e guarde-a no Key Vault / App Settings.
-- CREATE USER relatorio_reader WITH PASSWORD = '<senha-forte>';
-- GRANT SELECT ON dbo.Avaliacoes TO relatorio_reader;
