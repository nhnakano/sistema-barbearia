package com.barbearia.sistema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbearia.sistema.model.Agendamento;
import com.barbearia.sistema.model.Expediente;
import com.barbearia.sistema.dto.ClienteDTO;
import com.barbearia.sistema.repository.AgendamentoRepository;
import com.barbearia.sistema.repository.ExpedienteRepository;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ExpedienteRepository repository;
    @Autowired
    private AgendamentoRepository agendamentoRepository; 

    @GetMapping("/painel")
    public String painel(@RequestParam(name = "dataFiltro", required = false) String dataFiltro, Model model) {
        model.addAttribute("horarios", repository.findAll());
        
        // --- LÓGICA DO FILTRO DE BUSCA ---
        List<Agendamento> agendamentosExibicao;
        if (dataFiltro != null && !dataFiltro.isEmpty()) {
            LocalDate dataParaFiltrar = LocalDate.parse(dataFiltro);
            agendamentosExibicao = agendamentoRepository.findByData(dataParaFiltrar);
            model.addAttribute("dataSelecionada", dataFiltro);
        } else {
            agendamentosExibicao = agendamentoRepository.findAll();
        }
        model.addAttribute("agendamentos", agendamentosExibicao);
        
        // --- MÓDULO CRM (Histórico de Fidelidade) ---
        List<Agendamento> todosParaCalculo = agendamentoRepository.findAll();
        List<ClienteDTO> crmClientes = new ArrayList<>();
        List<String> telefonesVistos = new ArrayList<>();
        
        for (Agendamento ag : todosParaCalculo) {
            if (!telefonesVistos.contains(ag.getTelefone())) {
                telefonesVistos.add(ag.getTelefone());
                long totalAtendimentos = todosParaCalculo.stream()
                    .filter(a -> a.getTelefone().equals(ag.getTelefone()) && "ATENDIDO".equals(a.getStatus()))
                    .count();
                crmClientes.add(new ClienteDTO(ag.getNomeCliente(), ag.getTelefone(), totalAtendimentos));
            }
        }
        model.addAttribute("crmClientes", crmClientes);

        // --- MÓDULO FINANCEIRO (Dashboards) ---
        double fatDiario = 0, fatSemanal = 0, fatMensal = 0, fatTrimestral = 0, fatAnual = 0;
        LocalDate hoje = LocalDate.now();
        int mesAtual = hoje.getMonthValue();
        int anoAtual = hoje.getYear();
        int trimestreAtual = (mesAtual - 1) / 3 + 1;

        for (Agendamento ag : todosParaCalculo) {
            if ("ATENDIDO".equals(ag.getStatus())) {
                LocalDate dataAg = ag.getData();
                double valor = ag.getValorTotal();

                if (dataAg.getYear() == anoAtual) {
                    fatAnual += valor;
                    if (dataAg.getMonthValue() == mesAtual) fatMensal += valor;
                    int trimestreAg = (dataAg.getMonthValue() - 1) / 3 + 1;
                    if (trimestreAg == trimestreAtual) fatTrimestral += valor;
                }
                if (!dataAg.isBefore(hoje.minusDays(7)) && !dataAg.isAfter(hoje)) fatSemanal += valor;
                if (dataAg.isEqual(hoje)) fatDiario += valor;
            }
        }
        
        model.addAttribute("fatDiario", fatDiario);
        model.addAttribute("fatSemanal", fatSemanal);
        model.addAttribute("fatMensal", fatMensal);
        model.addAttribute("fatTrimestral", fatTrimestral);
        model.addAttribute("fatAnual", fatAnual);

        // Formulário de Novo Agendamento (Admin)
        Agendamento novoAg = new Agendamento();
        novoAg.setServicoCorte(true);
        model.addAttribute("novoAgendamento", novoAg);
        
        return "index";
    }

    // --- MÓDULO DE EXPORTAÇÃO (CSV) ---
    @GetMapping("/admin/exportar-relatorio")
    public void exportarRelatorio(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=relatorio_florek_cuts.csv");

        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        PrintWriter writer = response.getWriter();
        writer.println("Data;Hora;Cliente;Servicos;Valor;Status");

        for (Agendamento ag : agendamentos) {
            writer.println(ag.getData() + ";" + ag.getHora() + ";" + ag.getNomeCliente() + ";" +
                           ag.getServicosFormatados() + ";" + "R$ " + ag.getValorTotal() + ";" + ag.getStatus());
        }
    }

    @PostMapping("/admin/agendar")
    public String adminAgendar(@ModelAttribute Agendamento agendamento, RedirectAttributes attributes) {
        int duracaoTotal = 0;
        if (agendamento.isServicoCorte()) duracaoTotal += 30;
        if (agendamento.isServicoBarba()) duracaoTotal += 15;
        if (agendamento.isServicoSobrancelha()) duracaoTotal += 15;
        if (duracaoTotal == 0) duracaoTotal = 30; 
        
        agendamento.setHoraFim(agendamento.getHora().plusMinutes(duracaoTotal));

        List<Agendamento> marcadosDoDia = agendamentoRepository.findByData(agendamento.getData());
        boolean temConflito = marcadosDoDia.stream()
            .filter(ag -> !"CANCELADO".equals(ag.getStatus()))
            .anyMatch(ag -> agendamento.getHora().isBefore(ag.getHoraFim()) && agendamento.getHoraFim().isAfter(ag.getHora()));

        if (temConflito) {
            attributes.addFlashAttribute("erroAdmin", "Ops! Horário em conflito com outra reserva.");
            return "redirect:/painel";
        }

        agendamento.setStatus("AGENDADO");
        agendamentoRepository.save(agendamento);
        attributes.addFlashAttribute("sucessoAdmin", "Agendamento manual criado com sucesso!");
        return "redirect:/painel";
    }

    @GetMapping("/status-agendamento/{id}/{novoStatus}")
    public String alterarStatusAgendamento(@PathVariable Long id, @PathVariable String novoStatus) {
        Agendamento agendamento = agendamentoRepository.findById(id).orElse(null);
        if (agendamento != null) {
            agendamento.setStatus(novoStatus);
            agendamentoRepository.save(agendamento);
        }
        return "redirect:/painel";
    }

    @GetMapping("/alternar-status/{id}")
    public String alternarStatus(@PathVariable Long id) {
        Expediente exp = repository.findById(id).orElse(null);
        if (exp != null) {
            exp.setFechado(!exp.isFechado());
            repository.save(exp);
        }
        return "redirect:/painel";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Expediente exp = repository.findById(id).orElse(null);
        if (exp != null) {
            model.addAttribute("horario", exp);
            return "editar"; 
        }
        return "redirect:/painel";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Expediente formExpediente) {
        Expediente existente = repository.findById(formExpediente.getId()).orElse(null);
        if (existente != null) {
            existente.setHoraInicio(formExpediente.getHoraInicio());
            existente.setHoraFim(formExpediente.getHoraFim());
            repository.save(existente);
        }
        return "redirect:/painel";
    }
}