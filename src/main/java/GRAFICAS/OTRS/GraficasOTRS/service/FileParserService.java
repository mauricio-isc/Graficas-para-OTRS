package GRAFICAS.OTRS.GraficasOTRS.service;

import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import GRAFICAS.OTRS.GraficasOTRS.util.DateUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class FileParserService {

    private static final Logger log = LoggerFactory.getLogger(FileParserService.class);
    private static final Map<String, String> COLUMN_MAPPING = new HashMap<>();

    // Constantes para los campos
    private static final String FIELD_NUMBER_TICKET = "numeroTicket";
    private static final String FIELD_STATUS = "estado";
    private static final String FIELD_CUSTOMER = "cliente";
    private static final String FIELD_COMPANY = "empresa";
    private static final String FIELD_SUBJECT = "asunto";
    private static final String FIELD_QUEUE = "cola";
    private static final String FIELD_PRIORITY = "prioridad";
    private static final String FIELD_CREATION = "creacion";
    private static final String FIELD_NOTIFICATION = "notificacionInicial";
    private static final String FIELD_LAST_UPDATE = "ultimaActualizacion";
    private static final String FIELD_CLOSE = "cierre";
    private static final String FIELD_CLOSED_BY = "cerradoPor";
    private static final String FIELD_TOTAL_TIME = "tiempoTotal";
    private static final String FIELD_TOTAL_MINUTES = "minutosTotal";
    private static final String FIELD_OWNER = "propietarioActual";
    private static final String FIELD_SLA = "sla";

    static {
        // ----- Nº Ticket (varias variantes) -----
        COLUMN_MAPPING.put("n° ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("nº ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("numero ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("num ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("id", FIELD_NUMBER_TICKET);

        // ----- Estado -----
        COLUMN_MAPPING.put("estado", FIELD_STATUS);
        COLUMN_MAPPING.put("status", FIELD_STATUS);
        COLUMN_MAPPING.put("state", FIELD_STATUS);

        // ----- Cliente -----
        COLUMN_MAPPING.put("cliente", FIELD_CUSTOMER);
        COLUMN_MAPPING.put("client", FIELD_CUSTOMER);

        // ----- Empresa -----
        COLUMN_MAPPING.put("empresa", FIELD_COMPANY);
        COLUMN_MAPPING.put("company", FIELD_COMPANY);

        // ----- Asunto -----
        COLUMN_MAPPING.put("asunto", FIELD_SUBJECT);
        COLUMN_MAPPING.put("subject", FIELD_SUBJECT);

        // ----- Cola -----
        COLUMN_MAPPING.put("cola", FIELD_QUEUE);
        COLUMN_MAPPING.put("queue", FIELD_QUEUE);

        // ----- Prioridad -----
        COLUMN_MAPPING.put("prioridad", FIELD_PRIORITY);
        COLUMN_MAPPING.put("priority", FIELD_PRIORITY);

        // ----- Creación (con y sin acento) -----
        COLUMN_MAPPING.put("creación", FIELD_CREATION);
        COLUMN_MAPPING.put("creacion", FIELD_CREATION);
        COLUMN_MAPPING.put("fecha creación", FIELD_CREATION);
        COLUMN_MAPPING.put("fecha de creación", FIELD_CREATION);
        COLUMN_MAPPING.put("created at", FIELD_CREATION);
        COLUMN_MAPPING.put("fecha creacion", FIELD_CREATION);

        // ----- Notificación Inicial -----
        COLUMN_MAPPING.put("notificación inicial", FIELD_NOTIFICATION);
        COLUMN_MAPPING.put("notificacion inicial", FIELD_NOTIFICATION);
        COLUMN_MAPPING.put("notificación", FIELD_NOTIFICATION);
        COLUMN_MAPPING.put("notificacion", FIELD_NOTIFICATION);

        // ----- Última Actualización -----
        COLUMN_MAPPING.put("última actualización", FIELD_LAST_UPDATE);
        COLUMN_MAPPING.put("ultima actualización", FIELD_LAST_UPDATE);
        COLUMN_MAPPING.put("ultima actualizacion", FIELD_LAST_UPDATE);
        COLUMN_MAPPING.put("last update", FIELD_LAST_UPDATE);

        // ----- Cierre -----
        COLUMN_MAPPING.put("cierre", FIELD_CLOSE);
        COLUMN_MAPPING.put("fecha cierre", FIELD_CLOSE);
        COLUMN_MAPPING.put("fecha de cierre", FIELD_CLOSE);

        // ----- Cerrado por -----
        COLUMN_MAPPING.put("cerrado por", FIELD_CLOSED_BY);
        COLUMN_MAPPING.put("closed by", FIELD_CLOSED_BY);

        // ----- Tiempo Total (horas, etc.) -----
        COLUMN_MAPPING.put("horas total", FIELD_TOTAL_TIME);
        COLUMN_MAPPING.put("horas", FIELD_TOTAL_TIME);
        COLUMN_MAPPING.put("tiempo total", FIELD_TOTAL_TIME);
        COLUMN_MAPPING.put("tiempo", FIELD_TOTAL_TIME);
        COLUMN_MAPPING.put("duration", FIELD_TOTAL_TIME);

        // ----- Minutos Total -----
        COLUMN_MAPPING.put("minutos total", FIELD_TOTAL_MINUTES);
        COLUMN_MAPPING.put("minutos", FIELD_TOTAL_MINUTES);
        COLUMN_MAPPING.put("duration minutes", FIELD_TOTAL_MINUTES);

        // ----- Propietario Actual -----
        COLUMN_MAPPING.put("propietario actual", FIELD_OWNER);
        COLUMN_MAPPING.put("propietario", FIELD_OWNER);
        COLUMN_MAPPING.put("owner", FIELD_OWNER);

        // ----- SLA -----
        COLUMN_MAPPING.put("sla", FIELD_SLA);
    }

    public List<Ticket> parseFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        log.info("Procesando archivo: {}", fileName);
        if (fileName == null) {
            throw new IllegalArgumentException("El nombre del archivo es nulo");
        }
        String extension = getFileExtension(fileName);
        if (extension.equalsIgnoreCase("csv")) {
            return parseCSV(file);
        } else if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls")) {
            return parseExcel(file);
        } else {
            throw new IllegalArgumentException("Formato no soportado. Use CSV o Excel (.xlsx, .xls)");
        }
    }

    private List<Ticket> parseCSV(MultipartFile file) throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        int rowNumber = 0;
        int errorsCount = 0;

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            String[] headers = csvReader.readNext();
            if (headers == null || headers.length == 0) {
                throw new IllegalArgumentException("El archivo CSV está vacío");
            }
            log.info("=== COLUMNAS CSV ===");
            for (int i = 0; i < headers.length; i++) {
                log.info("Columna {}: {}", i, headers[i]);
            }
            log.info("=======================");
            log.info("Cabeceras CSV encontradas: {}", String.join(", ", headers));

            Map<String, Integer> columnIndexMap = mapColumnIndices(headers);

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowNumber++;
                try {
                    Ticket ticket = mapRowToTicket(row, columnIndexMap);
                    if (ticket.getNumeroTicket() != null && !ticket.getNumeroTicket().isEmpty()) {
                        tickets.add(ticket);
                    } else {
                        log.warn("Fila {}: Ticket sin número, ignorado", rowNumber);
                        errorsCount++;
                    }
                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowNumber, e.getMessage());
                    errorsCount++;
                }
            }
        }
        log.info("CSV procesado: {} tickets válidos, {} errores", tickets.size(), errorsCount);
        return tickets;
    }

    private List<Ticket> parseExcel(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede ser nulo o vacío");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        List<Ticket> tickets = new ArrayList<>();
        int rowNumber = 0;
        int errorsCount = 0;

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, filename);
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("El archivo Excel está vacío");
            }

            String[] headers = getHeadersFromRow(headerRow);
            Map<String, Integer> columnIndexMap = mapColumnIndices(headers);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                rowNumber++;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Ticket ticket = mapRowToTicketExcel(row, columnIndexMap);
                    if (ticket.getNumeroTicket() != null && !ticket.getNumeroTicket().isEmpty()) {
                        tickets.add(ticket);
                    } else {
                        log.warn("Fila {}: Ticket sin número, ignorado", rowNumber);
                        errorsCount++;
                    }
                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowNumber, e.getMessage());
                    errorsCount++;
                }
            }
        }
        log.info("Excel procesado: {} tickets válidos, {} errores", tickets.size(), errorsCount);
        return tickets;
    }

    private Workbook createWorkbook(InputStream inputStream, String fileName) throws Exception {
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        }
        throw new IllegalArgumentException("Formato de archivo no soportado para Excel");
    }

    private String[] getHeadersFromRow(Row headerRow) {
        int cellCount = headerRow.getLastCellNum();
        String[] headers = new String[cellCount];
        for (int i = 0; i < cellCount; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String value = getCellValueAsString(cell);
                headers[i] = (value == null) ? "" : value.trim().toLowerCase();
            } else {
                headers[i] = "";
            }
        }
        return headers;
    }

    /**
     * Mapea los índices de las columnas convirtiendo los encabezados a minúsculas.
     * Esto permite que "Ticket" coincida con "ticket", etc.
     */
    private Map<String, Integer> mapColumnIndices(String[] headers) {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (header != null && !header.isEmpty()) {
                String headerLower = header.toLowerCase().trim();
                String mappedField = COLUMN_MAPPING.get(headerLower);
                log.info("Buscando columna: '{}' (lower: '{}') -> mapeada a: {}", header, headerLower, mappedField);
                if (mappedField != null) {
                    indexMap.put(mappedField, i);
                    log.debug("Columna '{}' mapeada a '{}' (índice {})", header, mappedField, i);
                } else {
                    log.debug("Columna '{}' ignorada (no reconocida)", header);
                }
            }
        }
        return indexMap;
    }

    private Ticket mapRowToTicket(String[] row, Map<String, Integer> columnIndexMap) {
        Ticket ticket = new Ticket();

        Integer idx = columnIndexMap.get(FIELD_NUMBER_TICKET);
        if (idx != null && idx < row.length) ticket.setNumeroTicket(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_STATUS);
        if (idx != null && idx < row.length) ticket.setEstado(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_CUSTOMER);
        if (idx != null && idx < row.length) ticket.setCliente(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_COMPANY);
        if (idx != null && idx < row.length) ticket.setEmpresa(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_SUBJECT);
        if (idx != null && idx < row.length) ticket.setAsunto(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_QUEUE);
        if (idx != null && idx < row.length) ticket.setCola(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_PRIORITY);
        if (idx != null && idx < row.length) ticket.setPrioridad(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_CREATION);
        if (idx != null && idx < row.length) ticket.setCreacion(DateUtils.parseFecha(getValue(row[idx])));

        idx = columnIndexMap.get(FIELD_NOTIFICATION);
        if (idx != null && idx < row.length) ticket.setNotificacionInicial(DateUtils.parseFecha(getValue(row[idx])));

        idx = columnIndexMap.get(FIELD_LAST_UPDATE);
        if (idx != null && idx < row.length) ticket.setUltimaActualizacion(DateUtils.parseFecha(getValue(row[idx])));

        idx = columnIndexMap.get(FIELD_CLOSE);
        if (idx != null && idx < row.length) {
            String closeStr = getValue(row[idx]);
            if (closeStr != null && !"ABIERTO".equalsIgnoreCase(closeStr))
                ticket.setCierre(DateUtils.parseFecha(closeStr));
        }

        idx = columnIndexMap.get(FIELD_CLOSED_BY);
        if (idx != null && idx < row.length) ticket.setCerradoPor(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_TOTAL_TIME);
        if (idx != null && idx < row.length) {
            String timeStr = getValue(row[idx]);
            ticket.setTiempoTotal(timeStr);
            if (timeStr != null) {
                ticket.setMinutosTotal(DateUtils.parseTiempoTotalToMinutos(timeStr));
            }
        }

        idx = columnIndexMap.get(FIELD_TOTAL_MINUTES);
        if (idx != null && idx < row.length) {
            String minsStr = getValue(row[idx]);
            if (minsStr != null && !minsStr.isEmpty()) {
                try {
                    ticket.setMinutosTotal(Long.parseLong(minsStr));
                } catch (NumberFormatException e) {
                    log.warn("Minutos Total no es un número válido: {}", minsStr);
                }
            }
        }

        idx = columnIndexMap.get(FIELD_OWNER);
        if (idx != null && idx < row.length) ticket.setPropietarioActual(getValue(row[idx]));

        idx = columnIndexMap.get(FIELD_SLA);
        if (idx != null && idx < row.length) ticket.setSla(getValue(row[idx]));

        return ticket;
    }

    private Ticket mapRowToTicketExcel(Row row, Map<String, Integer> columnIndexMap) {
        Ticket ticket = new Ticket();

        Integer idx = columnIndexMap.get(FIELD_NUMBER_TICKET);
        if (idx != null) ticket.setNumeroTicket(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_STATUS);
        if (idx != null) ticket.setEstado(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_CUSTOMER);
        if (idx != null) ticket.setCliente(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_COMPANY);
        if (idx != null) ticket.setEmpresa(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_SUBJECT);
        if (idx != null) ticket.setAsunto(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_QUEUE);
        if (idx != null) ticket.setCola(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_PRIORITY);
        if (idx != null) ticket.setPrioridad(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_CREATION);
        if (idx != null) ticket.setCreacion(DateUtils.parseFecha(getCellValueAsString(row.getCell(idx))));

        idx = columnIndexMap.get(FIELD_NOTIFICATION);
        if (idx != null) ticket.setNotificacionInicial(DateUtils.parseFecha(getCellValueAsString(row.getCell(idx))));

        idx = columnIndexMap.get(FIELD_LAST_UPDATE);
        if (idx != null) ticket.setUltimaActualizacion(DateUtils.parseFecha(getCellValueAsString(row.getCell(idx))));

        idx = columnIndexMap.get(FIELD_CLOSE);
        if (idx != null) {
            String closeStr = getCellValueAsString(row.getCell(idx));
            if (closeStr != null && !"ABIERTO".equalsIgnoreCase(closeStr))
                ticket.setCierre(DateUtils.parseFecha(closeStr));
        }

        idx = columnIndexMap.get(FIELD_CLOSED_BY);
        if (idx != null) ticket.setCerradoPor(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_TOTAL_TIME);
        if (idx != null) {
            String timeStr = getCellValueAsString(row.getCell(idx));
            ticket.setTiempoTotal(timeStr);
            if (timeStr != null) ticket.setMinutosTotal(DateUtils.parseTiempoTotalToMinutos(timeStr));
        }

        idx = columnIndexMap.get(FIELD_TOTAL_MINUTES);
        if (idx != null) {
            String minsStr = getCellValueAsString(row.getCell(idx));
            if (minsStr != null && !minsStr.isEmpty()) {
                try {
                    ticket.setMinutosTotal(Long.parseLong(minsStr));
                } catch (NumberFormatException e) {
                    log.warn("Minutos Total no es un número válido: {}", minsStr);
                }
            }
        }

        idx = columnIndexMap.get(FIELD_OWNER);
        if (idx != null) ticket.setPropietarioActual(getCellValueAsString(row.getCell(idx)));

        idx = columnIndexMap.get(FIELD_SLA);
        if (idx != null) ticket.setSla(getCellValueAsString(row.getCell(idx)));

        return ticket;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private String getValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    }
}