package com.barbearia.sistema.repository;

import com.barbearia.sistema.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Método fundamental para o filtro por data no Painel ADM 
    // e para a lógica de bloqueio de horários duplicados.
    List<Agendamento> findByData(LocalDate data);
}