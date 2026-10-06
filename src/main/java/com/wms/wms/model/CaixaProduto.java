package com.wms.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "caixa_produto")
public class CaixaProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Caixa getCaixa() { return caixa; }
    public void setCaixa(Caixa caixa) { this.caixa = caixa; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}