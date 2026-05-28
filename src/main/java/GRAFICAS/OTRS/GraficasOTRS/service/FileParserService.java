package GRAFICAS.OTRS.GraficasOTRS.service;

import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import GRAFICAS.OTRS.GraficasOTRS.util.DateUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;

@Service
public class FileParserService {
    private static final Logger log = LoggerFactory.getLogger(FileParserService.class);

    private static final Map<String, String> COLUMN_MAPPING = new HashMap<>();

    private static final String FIELD_NUMBER_TICKET = "numeroTicket";
    private static final String FIELD_STATUS= "estado";
    private static final String FIELD_COSTUMER="cliente";
    private static final String FIELD_COMPANY="empresa";
    private static final String FIELD_SUBJECT="asunto";
    private static final String FIELD_QUEUE="cola";
    private static final String FIELD_PRIORITY="prioridad";
    private static final String FIELD_CREATION="creacion";

    static {
        // Nº Ticket - AGREGAR VARIANTES CON °
        COLUMN_MAPPING.put("n° ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("nº ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("numero ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("num ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("ticket", FIELD_NUMBER_TICKET);
        COLUMN_MAPPING.put("id", FIELD_NUMBER_TICKET);

        // Estado
        COLUMN_MAPPING.put(FIELD_STATUS, FIELD_STATUS);
        COLUMN_MAPPING.put("status", FIELD_STATUS);
        COLUMN_MAPPING.put("state", FIELD_STATUS);

        // Cliente
        COLUMN_MAPPING.put(FIELD_COSTUMER, FIELD_COSTUMER);
        COLUMN_MAPPING.put("client", FIELD_COSTUMER);

        // Empresa
        COLUMN_MAPPING.put(FIELD_COMPANY, FIELD_COMPANY);
        COLUMN_MAPPING.put("company", FIELD_COMPANY);

        // Asunto
        COLUMN_MAPPING.put(FIELD_SUBJECT, FIELD_SUBJECT);
        COLUMN_MAPPING.put("subject", FIELD_SUBJECT);

        // Cola
        COLUMN_MAPPING.put(FIELD_QUEUE, FIELD_QUEUE);
        COLUMN_MAPPING.put("queue", FIELD_QUEUE);

        // Prioridad
        COLUMN_MAPPING.put(FIELD_PRIORITY, FIELD_PRIORITY);
        COLUMN_MAPPING.put("priority", FIELD_PRIORITY);

        // Creación
        COLUMN_MAPPING.put(FIELD_CREATION, FIELD_CREATION);
        COLUMN_MAPPING.put(FIELD_CREATION, FIELD_CREATION);

        COLUMN_MAPPING.put("fecha creación", FIELD_CREATION);
        COLUMN_MAPPING.put("fecha de creación", FIELD_CREATION);
        COLUMN_MAPPING.put("created at", FIELD_CREATION);
        COLUMN_MAPPING.put("fecha creacion", FIELD_CREATION);

        // Notificación Inicial
        COLUMN_MAPPING.put("notificación inicial", "notificacionInicial");
        COLUMN_MAPPING.put("notificacion inicial", "notificacionInicial");
        COLUMN_MAPPING.put("notificación", "notificacionInicial");

        // Última Actualización
        COLUMN_MAPPING.put("última actualización", "ultimaActualizacion");
        COLUMN_MAPPING.put("ultima actualización", "ultimaActualizacion");
        COLUMN_MAPPING.put("last update", "ultimaActualizacion");

        // Cierre
        COLUMN_MAPPING.put("cierre", "cierre");
        COLUMN_MAPPING.put("fecha cierre", "cierre");

        // Cerrado por
        COLUMN_MAPPING.put("cerrado por", "cerradoPor");
        COLUMN_MAPPING.put("closed by", "cerradoPor");

        // Tiempo Total
        COLUMN_MAPPING.put("tiempo total", "tiempoTotal");
        COLUMN_MAPPING.put("tiempo", "tiempoTotal");
        COLUMN_MAPPING.put("duration", "tiempoTotal");

        // Minutos Total
        COLUMN_MAPPING.put("minutos total", "minutosTotal");
        COLUMN_MAPPING.put("minutos", "minutosTotal");
        COLUMN_MAPPING.put("duration minutes", "minutosTotal");

        // Propietario Actual
        COLUMN_MAPPING.put("propietario actual", "propietarioActual");
        COLUMN_MAPPING.put("propietario", "propietarioActual");
        COLUMN_MAPPING.put("owner", "propietarioActual");

        // SLA
        COLUMN_MAPPING.put("sla", "sla");
    }


    /**
     * Parsea el archivo subido (CSV o Excel) y devuelve una lista de Tickets
     */
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

    /**
     * Parsea archivo CSV
     */
    private List<Ticket> parseCSV(MultipartFile file) throws Exception {
        List<Ticket> tickets = new ArrayList<>();
        int rowNumber = 0;
        int errorsCount = 0;

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            String[] headers = csvReader.readNext();
            log.info("=== COLUMNAS CSV ===");
            for (int i = 0; i < headers.length; i++){
                log.info("Columna {}", i, headers[i]);
            }
            log.info("=======================");

            if (headers == null || headers.length == 0) {
                throw new IllegalArgumentException("El archivo CSV está vacío");
            }

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

    /**
     * Parsea archivo Excel
     */
    private List<Ticket> parseExcel(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("El archivo no puede ser nulo o vacío");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || file.isEmpty()){
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacio");
        }

        List<Ticket> tickets = new ArrayList<>();
        int rowNumber = 0;
        int errorsCount = 0;

        try(InputStream inputStream = file.getInputStream()){
            Workbook workbook = createWorkbook(inputStream, filename);
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null){
                throw new IllegalArgumentException("El archivo Excel está vacio");
            }

            String[] headers = getHeadersFromRow(headerRow);
            Map<String, Integer> columnIndexMap = mapColumnIndices(headers);

            for (int i = 1; i <= sheet.getLastRowNum(); i++ ){
                rowNumber++;
                Row row = sheet.getRow(i);

                if(row == null){
                    continue;
                }

                try{
                    Ticket ticket = mapRowToTicketExcel(row, columnIndexMap);
                    if (ticket.getNumeroTicket() != null && !ticket.getNumeroTicket().isEmpty()){
                        tickets.add(ticket);
                    }else {
                        log.warn("fila {}: Ticket sin número, ignorado", rowNumber);
                        errorsCount++;
                    }
                }catch (Exception e){
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
        throw new IOException("Formato");
    }

    private String[] getHeadersFromRow(Row headerRow) {
        int cellCount = headerRow.getLastCellNum();
        String[] headers = new String[cellCount];

        for (int i = 0; i < cellCount; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                headers[i] = Optional.ofNullable(getCellValueAsString(cell))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .orElse("");
            }
        }
        return headers;
    }

    private Map<String, Integer> mapColumnIndices(String[] headers) {
        Map<String, Integer> indexMap = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];
            if (header != null && !header.isEmpty()) {
                String headerLower = header.toLowerCase().trim();
                String mappedField = COLUMN_MAPPING.get(headerLower);
                log.info("Buscando columna a: '{}' -> mapeada a: {}", headerLower, mappedField);
                if (mappedField != null) {
                    indexMap.put(mappedField, i);
                    log.debug("Columna '{}' mapeada a '{}' (índice {})", header, mappedField, headerLower, mappedField ,i);
                } else {
                    log.debug("Columna '{}' ignorada (no reconocida)", header);
                }
            }
        }

        return indexMap;
    }

    private Ticket mapRowToTicket(String[] row, Map<String, Integer> columnIndexMap) {
        Ticket ticket = new Ticket();

        Integer index = columnIndexMap.get(FIELD_NUMBER_TICKET);
        if (index != null && index < row.length) {
            ticket.setNumeroTicket(getValue(row[index]));
        }

        index = columnIndexMap.get(FIELD_STATUS);
        if (index != null && index < row.length) {
            ticket.setEstado(getValue(row[index]));
        }

        index = columnIndexMap.get(FIELD_COSTUMER);
        if (index != null && index < row.length) {
            ticket.setCliente(getValue(row[index]));
        }

        index = columnIndexMap.get("empresa");
        if (index != null && index < row.length) {
            ticket.setEmpresa(getValue(row[index]));
        }

        index = columnIndexMap.get("asunto");
        if (index != null && index < row.length) {
            ticket.setAsunto(getValue(row[index]));
        }

        index = columnIndexMap.get("cola");
        if (index != null && index < row.length) {
            ticket.setCola(getValue(row[index]));
        }

        index = columnIndexMap.get("prioridad");
        if (index != null && index < row.length) {
            ticket.setPrioridad(getValue(row[index]));
        }

        index = columnIndexMap.get("creacion");
        if (index != null && index < row.length) {
            String fechaStr = getValue(row[index]);
            ticket.setCreacion(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("notificacionInicial");
        if (index != null && index < row.length) {
            String fechaStr = getValue(row[index]);
            ticket.setNotificacionInicial(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("ultimaActualizacion");
        if (index != null && index < row.length) {
            String fechaStr = getValue(row[index]);
            ticket.setUltimaActualizacion(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("cierre");
        if (index != null && index < row.length) {
            String fechaStr = getValue(row[index]);
            if (fechaStr != null && !"ABIERTO".equalsIgnoreCase(fechaStr)) {
                ticket.setCierre(DateUtils.parseFecha(fechaStr));
            }
        }

        index = columnIndexMap.get("cerradoPor");
        if (index != null && index < row.length) {
            ticket.setCerradoPor(getValue(row[index]));
        }

        index = columnIndexMap.get("tiempoTotal");
        if (index != null && index < row.length) {
            String tiempoTotal = getValue(row[index]);
            ticket.setTiempoTotal(tiempoTotal);
            if (tiempoTotal != null) {
                ticket.setMinutosTotal(DateUtils.parseTiempoTotalToMinutos(tiempoTotal));
            }
        }

        index = columnIndexMap.get("minutosTotal");
        if (index != null && index < row.length) {
            String minutosStr = getValue(row[index]);
            if (minutosStr != null && !minutosStr.isEmpty()) {
                try {
                    ticket.setMinutosTotal(Long.parseLong(minutosStr));
                } catch (NumberFormatException e) {
                    log.warn("Minutos Total no es un número válido: {}", minutosStr);
                }
            }
        }

        index = columnIndexMap.get("propietarioActual");
        if (index != null && index < row.length) {
            ticket.setPropietarioActual(getValue(row[index]));
        }

        index = columnIndexMap.get("sla");
        if (index != null && index < row.length) {
            ticket.setSla(getValue(row[index]));
        }

        return ticket;
    }

    private Ticket mapRowToTicketExcel(Row row, Map<String, Integer> columnIndexMap) {
        Ticket ticket = new Ticket();

        Integer index = columnIndexMap.get(FIELD_NUMBER_TICKET);
        if (index != null) {
            ticket.setNumeroTicket(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get(FIELD_STATUS);
        if (index != null) {
            ticket.setEstado(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get(FIELD_COSTUMER);
        if (index != null) {
            ticket.setCliente(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("empresa");
        if (index != null) {
            ticket.setEmpresa(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("asunto");
        if (index != null) {
            ticket.setAsunto(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("cola");
        if (index != null) {
            ticket.setCola(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("prioridad");
        if (index != null) {
            ticket.setPrioridad(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("creacion");
        if (index != null) {
            String fechaStr = getCellValueAsString(row.getCell(index));
            ticket.setCreacion(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("notificacionInicial");
        if (index != null) {
            String fechaStr = getCellValueAsString(row.getCell(index));
            ticket.setNotificacionInicial(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("ultimaActualizacion");
        if (index != null) {
            String fechaStr = getCellValueAsString(row.getCell(index));
            ticket.setUltimaActualizacion(DateUtils.parseFecha(fechaStr));
        }

        index = columnIndexMap.get("cierre");
        if (index != null) {
            String fechaStr = getCellValueAsString(row.getCell(index));
            if (fechaStr != null && !"ABIERTO".equalsIgnoreCase(fechaStr)) {
                ticket.setCierre(DateUtils.parseFecha(fechaStr));
            }
        }

        index = columnIndexMap.get("cerradoPor");
        if (index != null) {
            ticket.setCerradoPor(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("tiempoTotal");
        if (index != null) {
            String tiempoTotal = getCellValueAsString(row.getCell(index));
            ticket.setTiempoTotal(tiempoTotal);
            if (tiempoTotal != null) {
                ticket.setMinutosTotal(DateUtils.parseTiempoTotalToMinutos(tiempoTotal));
            }
        }

        index = columnIndexMap.get("minutosTotal");
        if (index != null) {
            String minutosStr = getCellValueAsString(row.getCell(index));
            if (minutosStr != null && !minutosStr.isEmpty()) {
                try {
                    ticket.setMinutosTotal(Long.parseLong(minutosStr));
                } catch (NumberFormatException e) {
                    log.warn("Minutos Total no es un número válido: {}", minutosStr);
                }
            }
        }

        index = columnIndexMap.get("propietarioActual");
        if (index != null) {
            ticket.setPropietarioActual(getCellValueAsString(row.getCell(index)));
        }

        index = columnIndexMap.get("sla");
        if (index != null) {
            ticket.setSla(getCellValueAsString(row.getCell(index)));
        }

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
                return String.valueOf((long) cell.getNumericCellValue());
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
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) return "";
        return fileName.substring(lastDot + 1);
    }
}
