package GRAFICAS.OTRS.GraficasOTRS.util;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm");

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm");

    public static LocalDateTime parseFecha(String fechaStr){
        try{
            if (fechaStr == null || fechaStr.trim().isEmpty() || "ABIERTO".equalsIgnoreCase(fechaStr)){
                return null;
            }
            return LocalDateTime.parse(fechaStr, DATE_FORMATTER);
        }catch (Exception e ){
            return null;
        }
    }

    public static String formatDateTime(LocalDateTime dateTime){
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime){
        if (dateTime == null) return "";
        return dateTime.format(DATE_ONLY_FORMATTER);
    }

    public static long parseTiempoTotalToMinutos(String tiempoTotal){
        if (tiempoTotal == null || tiempoTotal.trim().isEmpty()) return 0;

        long minutos = 0;
        String[] partes = tiempoTotal.split(" ");

        for (String parte : partes){
            if (parte.endsWith("d") || parte.endsWith("días") || parte.endsWith("dias")){
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

    public static double minutosToDias(long minutos){
        return minutos / (24.0 * 60.0);
    }

    public static String calcularDiferiencia(LocalDateTime inicio, LocalDateTime fin){
        if(inicio == null) return "-";
        LocalDateTime end = (fin == null) ? LocalDateTime.now() : fin;

        long dias = ChronoUnit.DAYS.between(inicio, end);
        long horas = ChronoUnit.HOURS.between(inicio, end) % 24;
        long minutos = ChronoUnit.MINUTES.between(inicio, end) % 60;

        if (dias > 0 ) return dias + "d " + horas + "h " + minutos + "m";
        if (horas > 0 ) return horas + "h " + minutos + "m";
        return minutos + "m";
    }

}
