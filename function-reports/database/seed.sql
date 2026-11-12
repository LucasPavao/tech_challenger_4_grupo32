-- Dados de exemplo para testar o relatório (usa as datas dos últimos dias).
INSERT INTO dbo.Avaliacoes (descricao, nota, urgencia, data_envio) VALUES
(N'A aula foi excelente, conteúdo muito claro.',        10, 'OK',      DATEADD(DAY, -1, SYSUTCDATETIME())),
(N'Gostei, mas faltaram exemplos práticos.',             7, 'OK',      DATEADD(DAY, -2, SYSUTCDATETIME())),
(N'O áudio estava ruim durante toda a aula.',            2, 'CRITICO', DATEADD(DAY, -2, SYSUTCDATETIME())),
(N'Material de apoio desatualizado.',                    5, 'REVISAR', DATEADD(DAY, -3, SYSUTCDATETIME())),
(N'Aula confusa e sem organização.',                     1, 'CRITICO', DATEADD(DAY, -4, SYSUTCDATETIME())),
(N'Ótimo ritmo e boas dinâmicas.',                       8, 'OK',      DATEADD(DAY, -5, SYSUTCDATETIME())),
(N'Conteúdo bom, poderia ter mais exercícios.',          6, 'REVISAR', DATEADD(DAY, -6, SYSUTCDATETIME()));
