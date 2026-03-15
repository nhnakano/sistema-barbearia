package com.barbearia.sistema.controller;

import com.barbearia.sistema.model.Agendamento;
import com.barbearia.sistema.model.Expediente;
import com.barbearia.sistema.repository.AgendamentoRepository;
import com.barbearia.sistema.repository.ExpedienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository repository;
    @Autowired
    private ExpedienteRepository expedienteRepository;

    @GetMapping("/agendar")
    public String telaAgendar(Model model) {
        Agendamento ag = new Agendamento();
        ag.setServicoCorte(true); 
        model.addAttribute("agendamento", ag);
        return "agendar";
    }

    @PostMapping("/confirmar-agendamento")
    public String confirmarAgendamento(@ModelAttribute Agendamento agendamento, RedirectAttributes attributes) {
        int duracaoTotal = 0;
        if (agendamento.isServicoCorte()) duracaoTotal += 30;
        if (agendamento.isServicoBarba()) duracaoTotal += 15;
        if (agendamento.isServicoSobrancelha()) duracaoTotal += 15;
        if (duracaoTotal == 0) duracaoTotal = 30; 
        
        agendamento.setHoraFim(agendamento.getHora().plusMinutes(duracaoTotal));

        List<Agendamento> marcadosDoDia = repository.findByData(agendamento.getData());
        
        boolean temConflito = marcadosDoDia.stream()
            // IGNORA OS CANCELADOS NA HORA DE VERIFICAR CONFLITOS!
            .filter(ag -> !"CANCELADO".equals(ag.getStatus()))
            .anyMatch(ag -> 
                agendamento.getHora().isBefore(ag.getHoraFim()) && agendamento.getHoraFim().isAfter(ag.getHora())
            );

        if (temConflito) {
            attributes.addFlashAttribute("erro", "Ops! O tempo necessário para esses serviços invade outro horário reservado.");
            return "redirect:/agendar";
        }

        repository.save(agendamento);
        attributes.addFlashAttribute("sucesso", "Agendamento confirmado, " + agendamento.getNomeCliente() + "!");
        return "redirect:/agendar";
    }

    @GetMapping("/horarios-disponiveis")
    @ResponseBody
    public List<String> getHorariosDisponiveis(@RequestParam String data) {
        LocalDate dataEscolhida = LocalDate.parse(data);
        String diaSemana = dataEscolhida.getDayOfWeek().name();
        Expediente expediente = expedienteRepository.findByDiaSemana(diaSemana);
        if (expediente == null || expediente.isFechado()) return new ArrayList<>(); 

        List<Agendamento> marcados = repository.findByData(dataEscolhida);
        List<String> disponiveis = new ArrayList<>();
        LocalTime horaAtual = expediente.getHoraInicio();
        
        while (horaAtual.isBefore(expediente.getHoraFim())) {
            boolean ocupado = false;
            for (Agendamento ag : marcados) {
                // SÓ BLOQUEIA SE NÃO ESTIVER CANCELADO
                if (!"CANCELADO".equals(ag.getStatus()) && !horaAtual.isBefore(ag.getHora()) && horaAtual.isBefore(ag.getHoraFim())) {
                    ocupado = true;
                    break;
                }
            }
            if (!ocupado) disponiveis.add(horaAtual.toString());
            horaAtual = horaAtual.plusMinutes(30); 
        }
        return disponiveis;
    }
}