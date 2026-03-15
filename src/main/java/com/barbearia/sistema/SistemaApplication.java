package com.barbearia.sistema;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.barbearia.sistema.model.Expediente;
import com.barbearia.sistema.repository.ExpedienteRepository;

import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class SistemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaApplication.class, args);
    }

    // O gerador de horários padrão para a barbearia
    @Bean
    CommandLineRunner init(ExpedienteRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<String> dias = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY");

                for (String dia : dias) {
                    Expediente padrao = new Expediente();
                    padrao.setDiaSemana(dia);
                    padrao.setHoraInicio(LocalTime.of(8, 0));
                    padrao.setHoraFim(LocalTime.of(18, 0));
                    padrao.setFechado(false);

                    repository.save(padrao);
                }
                System.out.println(">>> FLOREK CUTS: Horários padrão carregados com sucesso (Seg-Sáb, 08h às 18h).");
            }
        };
    }
}