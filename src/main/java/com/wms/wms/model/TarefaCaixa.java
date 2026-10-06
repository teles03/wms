package com.wms.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tarefa_caixa")
public class TarefaCaixa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @ManyToOne
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @Column(nullable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    private StatusTarefaCaixa status = StatusTarefaCaixa.PENDENTE;

    public enum StatusTarefaCaixa {
        PENDENTE, EM_ANDAMENTO, CONCLUIDA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }
    public Caixa getCaixa() { return caixa; }
    public void setCaixa(Caixa caixa) { this.caixa = caixa; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public StatusTarefaCaixa getStatus() { return status; }
    public void setStatus(StatusTarefaCaixa status) { this.status = status; }
}