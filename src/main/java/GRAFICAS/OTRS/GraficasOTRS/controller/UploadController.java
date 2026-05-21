package GRAFICAS.OTRS.GraficasOTRS.controller;

import GRAFICAS.OTRS.GraficasOTRS.dto.ReportResponseDto;
import GRAFICAS.OTRS.GraficasOTRS.dto.StatisticsDto;
import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import GRAFICAS.OTRS.GraficasOTRS.service.ChartGeneratorService;
import GRAFICAS.OTRS.GraficasOTRS.service.FileParserService;
import GRAFICAS.OTRS.GraficasOTRS.service.PdfGeneratorService;
import GRAFICAS.OTRS.GraficasOTRS.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins= "*")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);

    private final FileParserService fileParserService;
    private final StatisticsService statisticsService;
    private final ChartGeneratorService chartGeneratorService;
    private final PdfGeneratorService pdfGeneratorService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.pdf.dir}")
    private String pdfDir;

    public UploadController(FileParserService fileParserService, StatisticsService statisticsService,
                            ChartGeneratorService chartGeneratorService, PdfGeneratorService pdfGeneratorService){
        this.fileParserService = fileParserService;
        this.statisticsService = statisticsService;
        this.chartGeneratorService = chartGeneratorService;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API funcionando ");
    }

    /**
     * Endpoint para subir archivo y generar reporte
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDto> uploadAndGenerateReport(@RequestParam("file")MultipartFile file,
                                                                     @RequestParam("cliente") String cliente){
        log.info("Recibida peticion de generacion de reporte para cliente: {}", cliente);
        log.info("Archivo: {}, tamano: {}, bytes", file.getOriginalFilename(), file.getSize());

        try{
            //1. validar archivo
            if (file.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(new ReportResponseDto(false, "No se encontro el archivo", null, null, 0));

            }
            if (cliente == null || cliente.trim().isEmpty()){
                return ResponseEntity.badRequest()
                        .body(new ReportResponseDto(false, "El nombre del cliente es requerido", null, null, 0));
            }
            //2. Parsear archio a lista de tickets
            List<Ticket> tickets = fileParserService.parseFile(file);
            log.info("Tickets parseados: {}", tickets.size());

            if (tickets.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(new ReportResponseDto(false, "No se encontraron tickets validos en el archivo", null, null, 0));
            }

            //3. calcular estadisticas
            StatisticsDto statisticsDto = statisticsService.calculateStatistics(tickets);
            log.info("Estadisticas calculadas: {}", statisticsDto);

            //4. Generar graficos
            Map<String, byte[]> charts = chartGeneratorService.generateAllCharts(
                    statisticsDto.getTicketsPorEstado(),
                    statisticsDto.getTicketsPorMes()
            );

            byte[] graficosEstados = charts.get("estados");
            byte[] graficoTendencia = charts.get("tendencia");
            byte[] graficoPrioridad = chartGeneratorService.generatePrioridadBarChart(statisticsDto.getTicketsPorPrioridad());

            //5. Generar PDF
            byte[] pdfBytes = pdfGeneratorService.generateReport(
                    tickets, cliente, statisticsDto, graficosEstados, graficoTendencia, graficoPrioridad
            );

            //6. Guardar PDF en el servidor
            String filename = generateFilename(cliente);
            savePdfToDisk(pdfBytes, filename);

            //7. Construir respuesta
            String downloadUrl = "/api/download/" + filename;
            ReportResponseDto response = new ReportResponseDto(
                    true,
                    "Reporte generado exitosamente",
                    filename,
                    downloadUrl,
                    tickets.size()
            );

            log.info("Reporte generado y guardado: {}", filename);
            return ResponseEntity.ok(response);

        }catch (Exception e){
            log.error("Error al generar el reporte: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReportResponseDto(false, "ERROR AL GENERAR EL REPORTE", null, null, 0));
        }
    }

    /**
     * Generar nombre unico para el archivo PDF
     */
    private String generateFilename(String cliente){
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String clienteSanitizado = cliente.replaceAll("[^a-zA-Z0-9]", "_").substring(0, Math.min(20, cliente.length()));
        return "reporte_" + clienteSanitizado + "_" + timestamp + ".pdf";
    }

    /**
     * guardar el PDF en el disco
     * TODO: estos metodos los pondre en otro carpeta con otro archivo
     */
    private void savePdfToDisk(byte[] pdfBytes, String filename) throws IOException{
        Path pdfPath = Paths.get(pdfDir);
        if (!Files.exists(pdfPath)){
            Files.createDirectories(pdfPath);
        }

        File pdfFile = new File(pdfDir + File.separator + filename);
        try(FileOutputStream fos = new FileOutputStream(pdfFile)){
            fos.write(pdfBytes);
        }
        log.info("PDF guardado en: {} ", pdfFile.getAbsolutePath());
    }
}
