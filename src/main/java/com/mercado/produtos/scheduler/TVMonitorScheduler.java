package com.mercado.produtos.scheduler;

import com.mercado.produtos.service.TVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class TVMonitorScheduler {

    @Autowired
    private TVService tvService;

    /**
     * Monitora status das TVs conectadas
     */
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void monitorarTVs() {
        Map<String, LocalDateTime> tvsOnline = tvService.getTVsOnline();

        if (!tvsOnline.isEmpty()) {
            System.out.println("=== TVs ONLINE ===");
            tvsOnline.forEach((tv, ultimaVerificacao) -> {
                System.out.println("TV " + tv + " - Última verificação: " + ultimaVerificacao);
            });
            System.out.println("Total de TVs online: " + tvsOnline.size());
        }
    }
}
