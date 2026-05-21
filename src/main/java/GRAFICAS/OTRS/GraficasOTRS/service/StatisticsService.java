package GRAFICAS.OTRS.GraficasOTRS.service;

import GRAFICAS.OTRS.GraficasOTRS.dto.StatisticsDto;
import GRAFICAS.OTRS.GraficasOTRS.model.Ticket;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static GRAFICAS.OTRS.GraficasOTRS.util.DateUtils.formatDate;

@Service
public class StatisticsService {

   private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

   /**
    * TODO: CALCULA TODAS LAS ESTADISTICAS A PARTIR DE LA LISTA DE TICKETS
    * */
   public StatisticsDto calculateStatistics(List<Ticket> tickets) {
       log.info("CALCULANDO ESTADISTICAS PARA {} TICKETS", tickets.size());

       if (tickets == null || tickets.isEmpty()) {
           return createEmptyStatistics();
       }

       StatisticsDto dto = new StatisticsDto();

       // 1. Totales básicos
       long totalTickets = tickets.size();
       long ticketsAbiertos = tickets.stream()
               .filter(t -> t.getEstado() != null && "ABIERTO".equalsIgnoreCase(t.getEstado()))
               .count();
       long ticketsCerrados = tickets.stream()
               .filter(t -> t.getEstado() != null && "CERRADO".equalsIgnoreCase(t.getEstado()))
               .count();

       dto.setTotalTickets(totalTickets);
       dto.setTicketsAbiertos(ticketsAbiertos);
       dto.setTicketsCerrados(ticketsCerrados);

       // 2. Tasa de cierre
       double tasaCierre = (totalTickets > 0) ? (ticketsCerrados * 100.0 / totalTickets) : 0.0;
       dto.setTasaCierre(Math.round(tasaCierre * 10.0) / 10.0);

       // 3. Tickets por Estado - IMPORTANTE: Siempre inicializar
       Map<String, Long> ticketsPorEstado = new LinkedHashMap<>();
       ticketsPorEstado.put("ABIERTO", ticketsAbiertos);
       ticketsPorEstado.put("CERRADO", ticketsCerrados);
       dto.setTicketsPorEstado(ticketsPorEstado);  // ← NUNCA debe ser null

       // 4. Tickets por Mes
       Map<String, Long> ticketsPorMes = calculateTicketsByMonth(tickets);
       dto.setTicketsPorMes(ticketsPorMes != null ? ticketsPorMes : new LinkedHashMap<>());

       // 5. Tickets por Prioridad
       Map<String, Long> ticketsPorPrioridad = tickets.stream()
               .filter(t -> t.getPrioridad() != null && !t.getPrioridad().isEmpty())
               .collect(Collectors.groupingBy(
                       Ticket::getPrioridad,
                       LinkedHashMap::new,
                       Collectors.counting()
               ));
       dto.setTicketsPorPrioridad(ticketsPorPrioridad != null ? ticketsPorPrioridad : new LinkedHashMap<>());

       // 6. Tickets por Empresa
       Map<String, Long> ticketsPorEmpresa = tickets.stream()
               .filter(t -> t.getEmpresa() != null && !t.getEmpresa().isEmpty())
               .collect(Collectors.groupingBy(
                       Ticket::getEmpresa,
                       Collectors.counting()
               ))
               .entrySet().stream()
               .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
               .limit(10)
               .collect(Collectors.toMap(
                       Map.Entry::getKey,
                       Map.Entry::getValue,
                       (e1, e2) -> e1,
                       LinkedHashMap::new
               ));
       dto.setTicketsPorEmpresa(ticketsPorEmpresa != null ? ticketsPorEmpresa : new LinkedHashMap<>());

       // 7. Fechas
       Optional<LocalDateTime> fechaMinima = tickets.stream()
               .map(Ticket::getCreacion)
               .filter(Objects::nonNull)
               .min(LocalDateTime::compareTo);
       Optional<LocalDateTime> fechaMaxima = tickets.stream()
               .map(Ticket::getCreacion)
               .filter(Objects::nonNull)
               .max(LocalDateTime::compareTo);

       fechaMinima.ifPresent(dto::setFechaMinima);
       fechaMaxima.ifPresent(dto::setFechaMaxima);

       // 8. Tiempo promedio
       double tiempoPromedioMinutos = tickets.stream()
               .filter(t -> "CERRADO".equalsIgnoreCase(t.getEstado()))
               .mapToLong(Ticket::getMinutosTotal)
               .average()
               .orElse(0.0);

       double tiempoPromedioDias = tiempoPromedioMinutos / (24.0 * 60.0);
       dto.setTiempoPromedioDias(Math.round(tiempoPromedioDias * 100.0) / 100.0);
       dto.setTiempoPromedioMinutos((long) tiempoPromedioMinutos);

       log.info("Estadisticas calculadas: TOTAL={}, ABIERTOS={}, CERRADOS={}, TASA={}, PERIODO={} - {}",
               totalTickets, ticketsAbiertos, ticketsCerrados, tasaCierre,
               fechaMinima.isPresent() ? fechaMinima.get().toLocalDate() : "N/A",
               fechaMaxima.isPresent() ? fechaMaxima.get().toLocalDate() : "N/A");

       return dto;
   }

    /**
     *  calcula la cantidad de tickets por mes consideranto el anio
     */

    private Map<String, Long> calculateTicketsByMonth(List<Ticket> tickets){
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
        Map<String, Long> ticketsPorMes = tickets.stream()
                .filter(t -> t.getCreacion() != null)
                .collect(Collectors.groupingBy(
                        t->t.getCreacion().format(monthFormatter),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
        //ordenar por fecha
        return sortByChronologicalDate(ticketsPorMes, tickets);
    }

    /**
  * ordena el mapa de meses cronologicamente
  */
 private Map<String, Long> sortByChronologicalDate(Map<String, Long> ticketsPorMes, List<Ticket> tickets){
     //Obtener todas las fechas unicas ordenadas
     List<LocalDateTime> fechasOrdenadas = tickets.stream()
             .map(Ticket::getCreacion)
             .filter(Objects::nonNull)
             .distinct()
             .sorted()
             .toList();

     //crear mapa ordenado
     Map<String, Long> sortedMap = new LinkedHashMap<>();
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

     for (LocalDateTime fecha : fechasOrdenadas){
         String mesKey = fecha.format(formatter);
         if (ticketsPorMes.containsKey(mesKey) && !sortedMap.containsKey(mesKey)){
             sortedMap.put(mesKey, ticketsPorMes.get(mesKey));
         }
     }

     //alternativa por si el metodo anterior no funciona, entonces ordenarlo por nombre de mes
     if (sortedMap.isEmpty()){
         sortedMap = ticketsPorMes.entrySet().stream()
                 .sorted((e1,e2) -> {
                     try {
                         //ordenar anio y mes
                         String[] parts1 = e1.getKey().split(" ");
                         String[] parts2 = e2.getKey().split(" ");
                         if (parts1.length == 2 && parts2.length == 2){
                             int yearCompare = parts1[1].compareTo(parts2[1]);
                             if (yearCompare != 0) return yearCompare;
                             return compareMonths(parts1[0], parts2[0]);
                         }
                     }catch (Exception ex){
                         log.warn("Error ordenado en meses: {}", ex.getMessage());
                     }
                     return e1.getKey().compareTo(e2.getKey());
                 })
                 .collect(Collectors.toMap(
                         Map.Entry::getKey,
                         Map.Entry::getValue,
                         (e1,e2) -> e1,
                         LinkedHashMap::new
                 ));
     }
     return sortedMap;
 }

 /**
  * Compara dos meses en espanol
  */
 private int compareMonths(String month1, String month2){
    List<String> months = Arrays.asList(
            "enero", "febrero", "marzo", "abril", "mayo",
            "junio", "julio", "agosto", "septiembre",
            "octubre", "noviembre", "diciembre"
    );

    int index1 = months.indexOf(month1.toLowerCase());
    int index2 = months.indexOf(month2.toLowerCase());

    if (index1 == -1 && index2==-1) return 0;
    if (index1 == -1) return 1;
    if (index2 == -1) return 1;
    return Integer.compare(index1, index2);
 }

    /**
     * Formatea una fecha para logging
     * @param date
     * @return
     */
 private String formatDate(LocalDateTime date){
    if (date == null) return "N/A";
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
 }

    /**
     * Crea un StaticsDto vacio
     * @return
     */
 private StatisticsDto createEmptyStatistics(){
    StatisticsDto dto = new StatisticsDto();
    dto.setTotalTickets(0);
    dto.setTicketsAbiertos(0);
    dto.setTicketsCerrados(0);
    dto.setTasaCierre(0.0);
    dto.setTiempoPromedioDias(0.0);
    dto.setTiempoPromedioMinutos(0);
    dto.setTicketsPorEstado(new LinkedHashMap<>());
    dto.setTicketsPorMes(new LinkedHashMap<>());
    dto.setTicketsPorPrioridad(new LinkedHashMap<>());
    dto.setTicketsPorEmpresa(new LinkedHashMap<>());
    return dto;
 }

}
