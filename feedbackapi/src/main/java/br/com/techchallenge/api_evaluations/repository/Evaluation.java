package br.com.techchallenge.api_evaluations.repository;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false, length = 1000)
    private String description;

    @Column(name = "nota", nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia", nullable = false, length = 20)
    private Urgency urgency;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Evaluation(String description, Integer score, Urgency urgency) {
        this.description = description;
        this.score = score;
        this.urgency = urgency;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
