package GRAFICAS.OTRS.GraficasOTRS.service;

import GRAFICAS.OTRS.GraficasOTRS.dto.StatisticsDto;
import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import GRAFICAS.OTRS.GraficasOTRS.util.DateUtils;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);
    @Value("${app.emails.fijos}")
    private String emailsFijos;

    private PdfFont normalFont;
    private PdfFont boldFont;
    private PdfFont italicFont;

    @PostConstruct
    public void init() throws Exception{
        //fuentes
        normalFont = PdfFontFactory.createFont("Helvetica", "UTF-8");
        boldFont = PdfFontFactory.createFont("Helvetica-Bold", "UTF-8");
        italicFont = PdfFontFactory.createFont("Helvetica-Oblique", "UTF-8");
    }
    /**
     * Genera el reporte PDF completo
     * @param tickets Lista de tickets
     * @param cliente Nombre del cliente (input usuario)
     * @param statisticsDto Estadísticas calculadas
     * @param graficoEstados byte[] del gráfico circular
     * @param graficoTendencia byte[] del gráfico de barras
     * @return byte[] del archivo PDF
     */
    public byte[] generateReport(List<Ticket> tickets, String cliente,
                                 StatisticsDto statisticsDto,
                                 byte[] graficoEstados, byte[] graficoTendencia,
                                 byte[]  graficoPrioridad) throws Exception{
        log.info("Generando reporte PDF para cliente: {}, tickets: {}", cliente, tickets.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50,50,50,50);

        //=========================== PAGINA 1 ==========================================
        //logo de ptn
        addLogo(document);

        //titulo principal
        addTitle(document, "REPORTE DE TICKETS MENSUALES");

        //Subtitulo con cliente
        addSubtitle(document, "Tickets de: \"" + cliente +"\"");

        //nombre de la empresa
        addCompanyName(document, "Palo Tinto Networks s.a de c.v");

        addSpacing(document, 2);

        // Linea separadora
        addSeparatorLine(document);

        // Datos del cliente
        addClientData(document, cliente);

        // Tabla de estadísticas principales (KPIs)
        addKPITable(document, statisticsDto);

        document.add(new AreaBreak());
        // Gráfico de estados
        addChart(document, graficoEstados, "Estado de Tickets");

        // Salto a página 2
        document.add(new AreaBreak());

        // ===================== PAGINA 2 =====================
        // grafico de tendencia mensual
        addChart(document, graficoTendencia, "Tendencia Mensual - Tickets Por Mes");

        // SALTO DE PAGGINA
        document.add(new AreaBreak());
        addChart(document, graficoPrioridad, "Tickets por Prioridad");

        document.add(new AreaBreak());
        // Tabla de informacion principal
        addMainInfoTable(document, tickets);

        //==================== PAGINA 3 ==========================================
        //Tabla de fechas y tiempos
        addDatesTable(document, tickets);

        //Tabla de detalles y responsables
        addDetailsTable(document, tickets);

        //cerrar documento
        document.close();
        log.info("PDF generado correctamente, tamaño: {}", baos.size());
        return baos.toByteArray();
    }

    /**
     * Agregar el logo de la empresa
     */
    private void addLogo(Document document){
        try{
            ClassPathResource logoResource = new ClassPathResource("logo/palotinto_logo.png");
            if (logoResource.exists()){
                InputStream logoStream = logoResource.getInputStream();
                byte[] logoBytes = logoStream.readAllBytes();

                Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(logoBytes));
                logo.setWidth(200);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);

                document.add(logo);
                addSpacing(document, 1);
            } else {
                log.warn("Logo no encontrado en classpath: logo/palotinto_logo.png");
            }
        } catch (Exception e){
            log.warn("Error al cargar el logo: {}", e.getMessage());
        }
    }

    /**
     * Agrega título principal
     */
    private void addTitle(Document document, String title) {
        Paragraph titleParagraph = new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(titleParagraph);
    }
    /**
     * Agrega subtítulo
     */
    private void addSubtitle(Document document, String subtitle) {
        Paragraph subtitleParagraph = new Paragraph(subtitle)
                .setFont(normalFont)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(subtitleParagraph);
    }

    /**
     * Agrega nombre de la empresa
     */
    private void addCompanyName(Document document, String companyName) {
        Paragraph companyParagraph = new Paragraph(companyName)
                .setFont(italicFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(companyParagraph);
    }

    /**
     * Agrega línea separadora
     */
    private void addSeparatorLine(Document document) {
        LineSeparator lineSeparator = new LineSeparator(new SolidLine(1f));
        lineSeparator.setMarginTop(10);
        lineSeparator.setMarginBottom(10);
        document.add(lineSeparator);
    }

    /**
     * Agrega datos del cliente
     */
    private void addClientData(Document document, String cliente) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(15);
        table.setBorder(Border.NO_BORDER);

        // Cliente
        addTableRow(table, "CLIENTE:", cliente);

        // Emails
        //addTableRow(table, "EMAILS:", emailsFijos);
        Cell emailLabelCell = new Cell()
                .add(new Paragraph("EMAILS:").setFont(boldFont).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
        Cell emailValueCell = new Cell()
                .setBorder(Border.NO_BORDER);
        //dividir emails por coma y espacio o por coma
        String[] emailArray = emailsFijos.split(",");
        for (String email : emailArray){
            emailValueCell.add(new Paragraph(email.trim()).setFont(normalFont).setFontSize(10));
        }
        table.addCell(emailLabelCell);
        table.addCell(emailValueCell);

        // Fecha de generación
        String fechaGeneracion = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        addTableRow(table, "FECHA GENERACION:", fechaGeneracion);

        document.add(table);
    }

    /**
     * Agrega una fila a la tabla de datos del cliente
     */
    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(normalFont).setFontSize(10))
                .setBorder(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Agrega tabla de KPIs (estadísticas principales)
     */
    private void addKPITable(Document document, StatisticsDto stats) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        // Encabezado
        Cell headerCell = new Cell(1, 2)
                .add(new Paragraph("ESTADISTICAS PRINCIPALES").setFont(boldFont).setFontSize(12))
                .setBackgroundColor(ColorConstants.DARK_GRAY)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(headerCell);

        // Filas de KPI
        addKPIRow(table, "Total de Tickets", String.valueOf(stats.getTotalTickets()));
        addKPIRow(table, "Tickets Cerrados", String.valueOf(stats.getTicketsCerrados()));
        addKPIRow(table, "Tickets Abiertos", String.valueOf(stats.getTicketsAbiertos()));
        addKPIRow(table, "Tasa de Cierre", stats.getTasaCierre() + "%");
        addKPIRow(table, "Periodo", stats.getPeriodoFormatted());
        addKPIRow(table, "Tiempo de Solucion Promedio", stats.getTiempoPromedioDias() + " días");

        document.add(table);
    }

    /**
     * Agrega una fila a la tabla de KPI
     */
    private void addKPIRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(normalFont).setFontSize(10))
                .setBorder(Border.NO_BORDER);

        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(boldFont).setFontSize(10))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Agrega un gráfico al documento
     */
    private void addChart(Document document, byte[] chartBytes, String title) throws Exception {
        if (chartBytes != null && chartBytes.length > 0) {
            Paragraph titleParagraph = new Paragraph(title)
                    .setFont(boldFont)
                    .setFontSize(14)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(titleParagraph);

            Image chartImage = new Image(com.itextpdf.io.image.ImageDataFactory.create(chartBytes));
            chartImage.setWidth(500);
            chartImage.setAutoScale(true);
            chartImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(chartImage);
        } else {
            Paragraph noData = new Paragraph("No hay datos disponibles para mostrar el gráfico")
                    .setFont(italicFont)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(noData);
        }
    }

    /**
     * Agrega tabla de información principal
     */
    private void addMainInfoTable(Document document, List<Ticket> tickets) {
        Paragraph title = new Paragraph("INFORMACION PRINCIPAL")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(title);

        // Crear tabla con 5 columnas
        float[] columnWidths = {15, 12, 28, 30, 15};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        addTableHeader(table, "Nº Ticket");
        addTableHeader(table, "Estado");
        addTableHeader(table, "Cliente");
        addTableHeader(table, "Asunto");
        addTableHeader(table, "Prioridad");

        // Filas de datos
        int rowCount = 0;
        for (Ticket ticket : tickets) {
            addTableCell(table, ticket.getNumeroTicket() != null ? ticket.getNumeroTicket() : "-");
            addTableCell(table, ticket.getEstado() != null ? ticket.getEstado() : "-");

            String empresa = ticket.getEmpresa() != null ? ticket.getEmpresa() : "-";
            if (empresa.length() > 35) empresa = empresa.substring(0, 32) + "...";
            addTableCell(table, empresa);

            String asunto = ticket.getAsunto() != null ? ticket.getAsunto() : "-";
            if (asunto.length() > 40) asunto = asunto.substring(0, 37) + "...";
            addTableCell(table, asunto);

            addTableCell(table, ticket.getPrioridad() != null ? ticket.getPrioridad() : "-");
            rowCount++;
        }

        document.add(table);

        // Añadir nota si hay muchos tickets
        if (rowCount > 15) {
            Paragraph note = new Paragraph("Nota: Se muestran todos los tickets (" + rowCount + " en total)")
                    .setFont(italicFont)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(5);
            document.add(note);
        }
    }

    /**
     * Agrega tabla de fechas y tiempos
     */
    private void addDatesTable(Document document, List<Ticket> tickets) {
        Paragraph title = new Paragraph("FECHAS Y TIEMPOS")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(title);

        // Crear tabla con 5 columnas
        float[] columnWidths = {15, 15, 15, 12, 13};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        addTableHeader(table, "Creacion");
        addTableHeader(table, "Notificacion Inicial");
        addTableHeader(table, "Ultima Actualizacion");
        addTableHeader(table, "Cierre");
        addTableHeader(table, "Tiempo Total");

        // Filas de datos
        for (Ticket ticket : tickets) {
            addTableCell(table, DateUtils.formatDateTime(ticket.getCreacion()));
            addTableCell(table, DateUtils.formatDateTime(ticket.getNotificacionInicial()));
            addTableCell(table, DateUtils.formatDateTime(ticket.getUltimaActualizacion()));

            String cierre = ticket.getCierre() != null ? DateUtils.formatDateTime(ticket.getCierre()) : "ABIERTO";
            addTableCell(table, cierre);

            addTableCell(table, ticket.getTiempoTotal() != null ? ticket.getTiempoTotal() : "-");
        }

        document.add(table);
    }

    /**
     * Agrega tabla de detalles y responsables
     */
    private void addDetailsTable(Document document, List<Ticket> tickets) {
        Paragraph title = new Paragraph("DETALLES Y RESPONSABLES")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(title);

        // Crear tabla con 5 columnas
        float[] columnWidths = {12, 10, 18, 15, 15};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Encabezados
        addTableHeader(table, "Cerrado por");
        addTableHeader(table, "Minutos Total");
        addTableHeader(table, "Propietario Actual");
        addTableHeader(table, "SLA");
        addTableHeader(table, "Cliente");

        // Filas de datos
        for (Ticket ticket : tickets) {
            addTableCell(table, ticket.getCerradoPor() != null ? ticket.getCerradoPor() : "-");
            addTableCell(table, ticket.getMinutosTotal() > 0 ? String.valueOf(ticket.getMinutosTotal()) : "-");
            addTableCell(table, ticket.getPropietarioActual() != null ? ticket.getPropietarioActual() : "-");
            addTableCell(table, ticket.getSla() != null ? ticket.getSla() : "-");
            addTableCell(table, ticket.getCliente() != null ? ticket.getCliente() : "-");
        }

        document.add(table);
    }

    /**
     * Agrega una celda de encabezado a la tabla
     */
    private void addTableHeader(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(boldFont).setFontSize(10))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addHeaderCell(cell);
    }

    /**
     * Agrega una celda normal a la tabla
     */
    private void addTableCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "-").setFont(normalFont).setFontSize(9))
                .setPadding(5);
        table.addCell(cell);
    }

    /**
     * Agrega espacio vertical
     */
    private void addSpacing(Document document, int lines) {
        for (int i = 0; i < lines; i++) {
            document.add(new Paragraph(" "));
        }
    }
}
