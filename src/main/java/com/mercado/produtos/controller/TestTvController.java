package com.mercado.produtos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")  // Removido o /api-tv pois já está no context-path
public class TestTvController {

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api-tv}")
    private String contextPath;

    @Value("${app.upload.dir:src/main/resources/static/img/uploads}")
    private String uploadDir;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API-TV");
        response.put("timestamp", LocalDateTime.now());
        response.put("port", serverPort);
        response.put("context", contextPath);
        response.put("upload_dir", uploadDir);
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service_name", "API-TV Upload Service");
        response.put("version", "1.0.0");
        response.put("endpoints", new String[]{
                "/api-tv/upload [POST] - Upload de arquivos",
                "/api-tv/upload/{nome} [DELETE] - Deletar arquivo",
                "/api-tv/upload/info/{nome} [GET] - Info do arquivo",
                "/api-tv/health [GET] - Status do serviço",
                "/api-tv/info [GET] - Informações do serviço"
        });
        response.put("upload_config", Map.of(
                "max_file_size", "50MB",
                "allowed_types", "image/*, video/*",
                "upload_directory", uploadDir
        ));
        return response;
    }
}