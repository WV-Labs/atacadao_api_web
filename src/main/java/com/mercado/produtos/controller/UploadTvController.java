package com.mercado.produtos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
public class UploadTvController {

    @Value("${api-tv.upload.dir}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api-tv}")
    private String contextPath;

    @Value("${server.port:8081}")
    private String serverPort;

    @PostConstruct
    public void init() {
        System.out.println("====================================");
        System.out.println("UPLOAD TV CONTROLLER INICIALIZADO");
        System.out.println("Porta: " + serverPort);
        System.out.println("Context Path: " + contextPath);
        System.out.println("Upload Dir: " + uploadDir);
        System.out.println("Endpoint: http://localhost:" + serverPort + contextPath + "/upload");
        System.out.println("====================================");

        // Criar diretório de upload se não existir
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Diretório de upload criado: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("Diretório de upload já existe: " + uploadPath.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Erro ao criar diretório de upload: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== UPLOAD TV CONTROLLER ===");
        System.out.println("Arquivo recebido: " + (arquivo != null ? arquivo.getOriginalFilename() : "null"));
        System.out.println("Tamanho: " + (arquivo != null ? arquivo.getSize() : "0"));
        System.out.println("Content-Type: " + (arquivo != null ? arquivo.getContentType() : "null"));

        try {
            // Validações básicas
            if (arquivo.isEmpty()) {
                System.err.println("Arquivo está vazio");
                response.put("erro", "Arquivo não pode estar vazio");
                return ResponseEntity.badRequest().body(response);
            }

            // Validar tipo de arquivo
            String contentType = arquivo.getContentType();
            if (!isValidFileType(contentType)) {
                System.err.println("Tipo de arquivo inválido: " + contentType);
                response.put("erro", "Tipo de arquivo não permitido");
                return ResponseEntity.badRequest().body(response);
            }

            // Criar diretório se não existir
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Diretório criado: " + uploadPath.toAbsolutePath());
            }

            // Gerar nome único para o arquivo
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = getFileExtension(nomeOriginal);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String nomeUnico = timestamp + "_" + uuid + extensao;

            System.out.println("Nome original: " + nomeOriginal);
            System.out.println("Nome único: " + nomeUnico);

            // Salvar arquivo no diretório static da api-tv
            Path caminhoArquivo = uploadPath.resolve(nomeUnico);
            Files.copy(arquivo.getInputStream(), caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Arquivo salvo em: " + caminhoArquivo.toAbsolutePath());

            // Construir URL de acesso via api-tv
            String urlArquivo = "http://localhost:" + serverPort + contextPath + "/img/uploads/" + nomeUnico;

            System.out.println("URL de acesso: " + urlArquivo);

            // Resposta de sucesso
            response.put("sucesso", true);
            response.put("nomeArquivo", nomeUnico);
            response.put("nomeOriginal", nomeOriginal);
            response.put("urlArquivo", urlArquivo);
            response.put("tamanho", arquivo.getSize());
            response.put("tipo", contentType);
            response.put("servidor", "api-tv");

            System.out.println("Upload realizado com sucesso: " + response);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("Erro de IO: " + e.getMessage());
            e.printStackTrace();
            response.put("erro", "Erro ao salvar arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            System.err.println("Erro geral: " + e.getMessage());
            e.printStackTrace();
            response.put("erro", "Erro interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/upload/{nomeArquivo}")
    public ResponseEntity<Map<String, Object>> deletarArquivo(@PathVariable String nomeArquivo) {
        Map<String, Object> response = new HashMap<>();

        System.out.println("=== DELETE ARQUIVO ===");
        System.out.println("Arquivo a deletar: " + nomeArquivo);

        try {
            Path caminhoArquivo = Paths.get(uploadDir).resolve(nomeArquivo);
            System.out.println("Caminho completo: " + caminhoArquivo.toAbsolutePath());

            if (Files.exists(caminhoArquivo)) {
                Files.delete(caminhoArquivo);
                System.out.println("Arquivo deletado com sucesso");

                response.put("sucesso", true);
                response.put("mensagem", "Arquivo deletado com sucesso");
                response.put("servidor", "api-tv");
            } else {
                System.err.println("Arquivo não encontrado");
                response.put("erro", "Arquivo não encontrado");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("Erro ao deletar: " + e.getMessage());
            e.printStackTrace();
            response.put("erro", "Erro ao deletar arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/upload/info/{nomeArquivo}")
    public ResponseEntity<Map<String, Object>> infoArquivo(@PathVariable String nomeArquivo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Path caminhoArquivo = Paths.get(uploadDir).resolve(nomeArquivo);

            if (Files.exists(caminhoArquivo)) {
                String urlArquivo = "http://localhost:" + serverPort + contextPath + "/img/uploads/" + nomeArquivo;

                response.put("sucesso", true);
                response.put("nomeArquivo", nomeArquivo);
                response.put("urlArquivo", urlArquivo);
                response.put("tamanho", Files.size(caminhoArquivo));
                response.put("existe", true);
                response.put("servidor", "api-tv");
            } else {
                response.put("existe", false);
                response.put("erro", "Arquivo não encontrado");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("erro", "Erro ao verificar arquivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;

        boolean isValid = contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("video/mp4") ||
                contentType.equals("video/mov") ||
                contentType.equals("video/avi") ||
                contentType.equals("video/quicktime");

        System.out.println("Content-Type '" + contentType + "' é válido: " + isValid);
        return isValid;
    }

    private String getFileExtension(String nomeArquivo) {
        if (nomeArquivo == null || nomeArquivo.lastIndexOf(".") == -1) {
            return "";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }
}