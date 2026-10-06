package com.wms.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "posicao")
public class Posicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String mezanino;
    private String rua;
    private String predio;
    private String andar;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMezanino() { return mezanino; }
    public void setMezanino(String mezanino) { this.mezanino = mezanino; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getPredio() { return predio; }
    public void setPredio(String predio) { this.predio = predio; }
    public String getAndar() { return andar; }
    public void setAndar(String andar) { this.andar = andar; }
}