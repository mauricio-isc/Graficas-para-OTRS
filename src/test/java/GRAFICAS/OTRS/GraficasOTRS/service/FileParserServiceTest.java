package GRAFICAS.OTRS.GraficasOTRS.service;

import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileParserServiceTest {
    private final FileParserService service = new FileParserService();

    @Test
    void parseCSV_conDatosValidos_devuelveListaTickets() throws Exception {
        String csv = """
                N° Ticket,Estado,Cliente,Empresa,Asunto,Cola,Prioridad,Creación,Notificación Inicial,Última Actualización,Cierre,Cerrado por,Tiempo Total,Minutos Total,Propietario Actual,SLA
                2026041710000019,ABIERTO,CARLOS,Grupo Financiero Banorte,"Falla switch",Cola1,1 Baja,17/04/2026 18:36,17/04/2026 18:36,20/04/2026 06:46,,,17d 2h 48m,24648,daniel.antonio,Baja prioridad
                """;
        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        List<Ticket> tickets = service.parseFile(file);
        assertNotNull(tickets);
        assertEquals(1, tickets.size());
        Ticket t = tickets.get(0);
        assertEquals("2026041710000019", t.getNumeroTicket());
        assertEquals("ABIERTO", t.getEstado());
        assertEquals("CARLOS", t.getCliente());
        assertEquals("Grupo Financiero Banorte", t.getEmpresa());
        assertEquals("1 Baja", t.getPrioridad());
        assertEquals(24648, t.getMinutosTotal());
    }

    @Test
    void parseCSV_sinNumeroTicket_filaIgnorada() throws Exception {
        String csv = """
                N° Ticket,Estado
                ,ABIERTO
                """;
        MockMultipartFile file = new MockMultipartFile("file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        List<Ticket> tickets = service.parseFile(file);
        assertTrue(tickets.isEmpty());
    }

    @Test
    void parseArchivoInvalido_lanzaExcepcion() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "contenido".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.parseFile(file));

    }
}