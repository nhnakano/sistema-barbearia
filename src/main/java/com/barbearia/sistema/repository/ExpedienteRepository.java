package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Expediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpedienteRepository extends JpaRepository<Expediente, Long> {
    
    // ESTA É A MÁGICA: Busca o horário de funcionamento de um dia específico (ex: "MONDAY")
    // para o calendário saber a que horas a barbearia abre e fecha antes de mostrar as vagas.
    Expediente findByDiaSemana(String diaSemana);
}