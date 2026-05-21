package GRAFICAS.OTRS.GraficasOTRS.controller;

import GRAFICAS.OTRS.GraficasOTRS.dto.StatisticsDto;
import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import GRAFICAS.OTRS.GraficasOTRS.service.ChartGeneratorService;
import GRAFICAS.OTRS.GraficasOTRS.service.FileParserService;
import GRAFICAS.OTRS.GraficasOTRS.service.PdfGeneratorService;
import GRAFICAS.OTRS.GraficasOTRS.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileParserService fileParserService;
    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ChartGeneratorService chartGeneratorService;
    @MockBean
    private PdfGeneratorService pdfGeneratorService;

    @Test
    void testUploadSuccess() throws Exception{
        MockMultipartFile file = new MockMultipartFile
                ("file", "test.csv", "text/csv", "dummy".getBytes());

        when(fileParserService.parseFile(any())).thenReturn(List.of( new Ticket()));
        when(statisticsService.calculateStatistics(any()))
                .thenReturn(new StatisticsDto());
        when(chartGeneratorService.generateAllCharts(any(), any()))
                .thenReturn(Map.of("estados", new byte[10], "tendencia", new byte[10]));
        when(pdfGeneratorService.generateReport(any(), any(), any(), any(), any(), any()))
                .thenReturn(new byte[100]);

        mockMvc.perform(multipart("/api/upload")
                .file(file)
                .param("cliente", "Banorte"))
                .andExpect(status().isOk());
    }

    @Test
    void TestUploadFailed() throws Exception{
        MockMultipartFile file = new MockMultipartFile
                ("file", "test.csv", "test/csv", "dummy".getBytes());
        //ticket vacio
        when(fileParserService.parseFile(any())).thenReturn(List.of());
        when(statisticsService.calculateStatistics(any()))
                .thenReturn(new StatisticsDto());
        when(chartGeneratorService.generateAllCharts(any(), any()))
                .thenReturn(Map.of("estados", new byte[10], "tendencia", new byte[10]));
        when(pdfGeneratorService.generateReport(any(), any(), any(), any(), any(), any()))
                .thenReturn(new byte[100]);

        mockMvc.perform(multipart("/api/upload")
                        .file(file)
                        .param("cliente", "SEDENA"))
                .andExpect(status().isBadRequest());
    }
}
