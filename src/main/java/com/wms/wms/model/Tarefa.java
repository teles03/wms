package com.wms.wms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tarefa")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private StatusTarefa status = StatusTarefa.EM_ANDAMENTO;

    private LocalDateTime inicio = LocalDateTime.now();
    private LocalDateTime fim;

    @OneToMany(mappedBy = "tarefa", cascade = CascadeType.ALL)
    private List<TarefaCaixa> caixasDaTarefa;

    public enum StatusTarefa {
        EM_ANDAMENTO, FINALIZADA, CANCELADA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public StatusTarefa getStatus() { return status; }
    public void setStatus(StatusTarefa status) { this.status = status; }
    public LocalDateTime getInicio() { return inicio; }
    public void setInicio(LocalDateTime inicio) { this.inicio = inicio; }
    public LocalDateTime getFim() { return fim; }
    public void setFim(LocalDateTime fim) { this.fim = fim; }
    public List<TarefaCaixa> getCaixasDaTarefa() { return caixasDaTarefa; }
    public void setCaixasDaTarefa(List<TarefaCaixa> caixasDaTarefa) { this.caixasDaTarefa = caixasDaTarefa; }
}