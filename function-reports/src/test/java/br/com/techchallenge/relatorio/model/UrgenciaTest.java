package br.com.techchallenge.relatorio.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrgenciaTest {

    @Test
    void classificaPelaNota() {
        assertEquals(Urgencia.CRITICO, Urgencia.daNota(0));
        assertEquals(Urgencia.CRITICO, Urgencia.daNota(3));
        assertEquals(Urgencia.REVISAR, Urgencia.daNota(4));
        assertEquals(Urgencia.REVISAR, Urgencia.daNota(6));
        assertEquals(Urgencia.OK, Urgencia.daNota(7));
        assertEquals(Urgencia.OK, Urgencia.daNota(10));
    }

    @Test
    void rejeitaNotaForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class, () -> Urgencia.daNota(-1));
        assertThrows(IllegalArgumentException.class, () -> Urgencia.daNota(11));
    }

    @Test
    void textoDoBancoTemPrioridadeESeVazioUsaNota() {
        assertEquals(Urgencia.REVISAR, Urgencia.deTexto("revisar", 1));
        assertEquals(Urgencia.CRITICO, Urgencia.deTexto("CRÍTICO", 9));
        assertEquals(Urgencia.OK, Urgencia.deTexto(null, 9));
        assertEquals(Urgencia.CRITICO, Urgencia.deTexto("desconhecido", 2));
    }
}
