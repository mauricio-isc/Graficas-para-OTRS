package GRAFICAS.OTRS.GraficasOTRS.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DownloadController {

    private static final Logger log = LoggerFactory.getLogger(DownloadController.class);

    @Value("${app.pdf.dir}")
    private String pdfDir;

    /**
     * Descargar pdf por el nombre del archivo
     */

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String filename){
        log.info("SOLICITUD DE DESCARGA: {}", filename);

        try {
            Path pdfPath = Paths.get(pdfDir).resolve(filename);
            File pdfFile = pdfPath.toFile();

            if (!pdfFile.exists()){
                log.warn("ARCHIVO NO ENCONTADO: {}", filename);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            Resource resource = new FileSystemResource(pdfFile);
            String contentType = Files.probeContentType(pdfPath);

            if (contentType == null){
                contentType = "application/pdf";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        }catch (IOException e){
            log.error("Error al descargar el archivo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


}
