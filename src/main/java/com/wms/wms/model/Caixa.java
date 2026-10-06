package com.wms.wms.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "caixa")
public class Caixa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_qr", nullable = false, unique = true)
    private String codigoQr;

    @Enumerated(EnumType.STRING)
    private StatusCaixa status = StatusCaixa.DISPONIVEL;

    @OneToMany(mappedBy = "caixa", cascade = CascadeType.ALL)
    private List<CaixaProduto> produtos;

    public enum StatusCaixa {
        DISPONIVEL, EM_TRABALHO, VAZIA
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public StatusCaixa getStatus() { return status; }
    public void setStatus(StatusCaixa status) { this.status = status; }
    public List<CaixaProduto> getProdutos() { return produtos; }
    public void setProdutos(List<CaixaProduto> produtos) { this.produtos = produtos; }
}