package br.com.techchallenge.relatorio.model;


public enum Urgencia {
    CRITICO,
    REVISAR,
    OK;

    public static Urgencia daNota(int nota) {
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10: " + nota);
        }
        if (nota <= 3) {
            return CRITICO;
        }
        if (nota <= 6) {
            return REVISAR;
        }
        return OK;
    }

    public static Urgencia deTexto(String texto, int nota) {
        if (texto != null) {
            String normalizado = texto.trim().toUpperCase()
                    .replace("Í", "I").replace("Á", "A");
            for (Urgencia u : values()) {
                if (u.name().equals(normalizado)) {
                    return u;
                }
            }
        }
        return daNota(nota);
    }
}
