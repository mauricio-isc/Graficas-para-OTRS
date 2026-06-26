package GRAFICAS.OTRS.GraficasOTRS.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static LocalDateTime parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(fechaStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return (dateTime == null) ? "-" : dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return (dateTime == null) ? "-" : dateTime.format(DATE_ONLY_FORMATTER);
    }

    /**
     * Parsea un string de tiempo a minutos.
     * Soporta:
     *  - Formato "Xd Xh Xm" (ej. "17d 2h 48m")
     *  - Número decimal de horas (ej. "629.30" -> 629.30 * 60)
     */
    public static long parseTiempoTotalToMinutos(String tiempoTotal) {
        if (tiempoTotal == null || tiempoTotal.trim().isEmpty()) return 0;
        String trimmed = tiempoTotal.trim();

        // Intentar como número decimal (horas)
        try {
            double horas = Double.parseDouble(trimmed);
            return (long) (horas * 60);
        } catch (NumberFormatException e) {
            // No es número, continuar con formato "Xd Xh Xm"
        }

        long minutos = 0;
        String[] partes = trimmed.split(" ");
        for (String parte : partes) {
            if (parte.endsWith("d") || parte.endsWith("días") || parte.endsWith("dias")) {
                int valor = Integer.parseInt(parte.replaceAll("[^0-9]", ""));
                minutos += valor * 24 * 60;
            } else if (parte.endsWith("h") || parte.endsWith("horas")) {
                int valor = Integer.parseInt(parte.replaceAll("[^0-9]", ""));
                minutos += valor * 60;
            } else if (parte.endsWith("m") || parte.endsWith("minutos")) {
                int valor = Integer.parseInt(parte.replaceAll("[^0-9]", ""));
                minutos += valor;
            }
        }
        return minutos;
    }

    public static double minutosToDias(long minutos) {
        return minutos / (24.0 * 60.0);
    }

    public static String calcularDiferencia(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null) return "-";
        LocalDateTime end = (fin == null) ? LocalDateTime.now() : fin;
        long dias = ChronoUnit.DAYS.between(inicio, end);
        long horas = ChronoUnit.HOURS.between(inicio, end) % 24;
        long minutos = ChronoUnit.MINUTES.between(inicio, end) % 60;
        if (dias > 0) return dias + "d " + horas + "h " + minutos + "m";
        if (horas > 0) return horas + "h " + minutos + "m";
        return minutos + "m";
    }
}