package com.mercado.produtos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class MediaController {
    @Value("${api-tv.diretorio.imagens}")
    private String diretorioImagens;
    @Value("${api-tv.upload.dir}")
    private String diretorioUpload;

    // ENDPOINT PARA SERVIR TODOS OS ARQUIVOS EM /img/uploads/
    @GetMapping("/img/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        System.out.println("🔍 Requisição para arquivo: " + filename);

        try {
            // IMPORTANTE: Descobrir onde estão os arquivos
            String[] possibleBasePaths = {
                    "uploads",                    // ./uploads/
                    "img/uploads",               // ./img/uploads/
                    diretorioUpload,
                    "target/classes/static/uploads",
                    "target/classes/static/img/uploads"
            };

            File foundFile = null;
            String usedPath = null;

            // Procura o arquivo em diferentes localizações
            for (String basePath : possibleBasePaths) {
                Path fullPath = Paths.get(basePath, filename);
                File file = fullPath.toFile();

                System.out.println("📁 Testando: " + file.getAbsolutePath() + " - Existe: " + file.exists());

                if (file.exists() && file.canRead()) {
                    foundFile = file;
                    usedPath = basePath;
                    System.out.println("✅ Arquivo encontrado em: " + usedPath);
                    break;
                }
            }

            if (foundFile == null) {
                System.out.println("❌ Arquivo não encontrado: " + filename);

                // Lista conteúdo dos diretórios para debug
                for (String basePath : possibleBasePaths) {
                    File dir = new File(basePath);
                    if (dir.exists() && dir.isDirectory()) {
                        System.out.println("📂 Conteúdo de " + basePath + ":");
                        String[] files = dir.list();
                        if (files != null) {
                            for (String file : files) {
                                System.out.println("   📄 " + file);
                            }
                        }
                    }
                }

                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(foundFile.toURI());

            if (resource.exists() && resource.isReadable()) {
                // Detecta o tipo de conteúdo baseado na extensão
                String contentType = getContentType(filename);
                System.out.println("✅ Servindo arquivo: " + filename + " (" + foundFile.length() + " bytes) - Tipo: " + contentType);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                        .contentLength(foundFile.length())
                        .body(resource);
            } else {
                System.out.println("❌ Arquivo não é legível: " + foundFile.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.out.println("❌ Erro ao servir arquivo " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Detecta o tipo MIME baseado na extensão do arquivo
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "mp4":
                return "video/mp4";
            case "webm":
                return "video/webm";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream";
        }
    }

    // ENDPOINT DE DEBUG PARA DESCOBRIR ONDE ESTÃO OS ARQUIVOS
    @GetMapping("/debug/list-files")
    public ResponseEntity<String> listFiles() {
        StringBuilder response = new StringBuilder();
        response.append("=== DIAGNÓSTICO DE ARQUIVOS ===\n\n");
        response.append("Diretório de trabalho: ").append(System.getProperty("user.dir")).append("\n\n");

        String[] directories = {
                "uploads",
                "img/uploads",
                diretorioUpload,
                "target/classes/static/uploads",
                "target/classes/static/img/uploads"
        };

        for (String dirPath : directories) {
            File dir = new File(dirPath);
            response.append("📂 ").append(dirPath).append(":\n");
            response.append("   Existe: ").append(dir.exists()).append("\n");
            response.append("   É diretório: ").append(dir.isDirectory()).append("\n");
            response.append("   Caminho absoluto: ").append(dir.getAbsolutePath()).append("\n");

            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    response.append("   Arquivos encontrados:\n");
                    for (File file : files) {
                        response.append("      📄 ").append(file.getName())
                                .append(" (").append(file.length()).append(" bytes)\n");
                    }
                } else {
                    response.append("   Diretório vazio\n");
                }
            }
            response.append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=utf-8")
                .body(response.toString());
    }
}