package br.com.techchallenge.relatorio.repository;

import br.com.techchallenge.relatorio.model.Avaliacao;
import br.com.techchallenge.relatorio.model.Urgencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class SqlServerAvaliacaoRepository implements AvaliacaoRepository {

    private static final Pattern NOME_TABELA_VALIDO = Pattern.compile("[A-Za-z0-9_.\\[\\]]+");

    private final String jdbcUrl;
    private final String tabela;

    public SqlServerAvaliacaoRepository(String jdbcUrl, String tabela) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalStateException("SQL_CONNECTION_STRING não configurada");
        }
        if (!NOME_TABELA_VALIDO.matcher(tabela).matches()) {
            throw new IllegalArgumentException("Nome de tabela inválido: " + tabela);
        }
        this.jdbcUrl = jdbcUrl;
        this.tabela = tabela;
    }

    @Override
    public List<Avaliacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT id, descricao, nota, urgencia, data_envio FROM " + tabela
                + " WHERE data_envio >= ? AND data_envio < ? ORDER BY data_envio";

        List<Avaliacao> resultado = new ArrayList<>();
        try (Connection conexao = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(inicio));
            ps.setTimestamp(2, Timestamp.valueOf(fim));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nota = rs.getInt("nota");
                    resultado.add(new Avaliacao(
                            rs.getLong("id"),
                            rs.getString("descricao"),
                            nota,
                            Urgencia.deTexto(rs.getString("urgencia"), nota),
                            rs.getTimestamp("data_envio").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao consultar avaliações no SQL Server: " + e.getMessage(), e);
        }
        return resultado;
    }
}
