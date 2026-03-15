package com.barbearia.sistema.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCliente;
    private String telefone;
    private LocalDate data;
    private LocalTime hora;
    private LocalTime horaFim; 

    private boolean servicoCorte;
    private boolean servicoBarba;
    private boolean servicoSobrancelha;

    private String status = "AGENDADO";

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }
    public boolean isServicoCorte() { return servicoCorte; }
    public void setServicoCorte(boolean servicoCorte) { this.servicoCorte = servicoCorte; }
    public boolean isServicoBarba() { return servicoBarba; }
    public void setServicoBarba(boolean servicoBarba) { this.servicoBarba = servicoBarba; }
    public boolean isServicoSobrancelha() { return servicoSobrancelha; }
    public void setServicoSobrancelha(boolean servicoSobrancelha) { this.servicoSobrancelha = servicoSobrancelha; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getServicosFormatados() {
        List<String> servicos = new ArrayList<>();
        if (servicoCorte) servicos.add("Corte");
        if (servicoBarba) servicos.add("Barba");
        if (servicoSobrancelha) servicos.add("Sobrancelha");
        return String.join(" + ", servicos);
    }

    // >>> NOVA MÁGICA: Calcula o valor total do agendamento
    public double getValorTotal() {
        double total = 0.0;
        if (servicoCorte) total += 35.0;
        if (servicoBarba) total += 25.0;
        if (servicoSobrancelha) total += 15.0;
        return total;
    }
}