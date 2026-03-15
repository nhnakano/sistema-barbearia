package com.barbearia.sistema.dto;

public class ClienteDTO {
    private String nome;
    private String telefone;
    private long totalAtendimentos;

    public ClienteDTO(String nome, String telefone, long totalAtendimentos) {
        this.nome = nome;
        this.telefone = telefone;
        this.totalAtendimentos = totalAtendimentos;
    }

    // Getters (Essenciais para o Thymeleaf conseguir ler os dados no HTML)
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public long getTotalAtendimentos() { return totalAtendimentos; }
}