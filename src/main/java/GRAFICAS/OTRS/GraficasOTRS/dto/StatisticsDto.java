package GRAFICAS.OTRS.GraficasOTRS.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StatisticsDto {
    private long totalTickets;
    private long ticketsAbiertos;
    private long ticketsCerrados;
    private double tasaCierre;
    private LocalDateTime fechaMinima;
    private LocalDateTime fechaMaxima;
    private double tiempoPromedioDias;
    private long tiempoPromedioMinutos;

    private Map<String, Long> ticketsPorEstado;
    private Map<String, Long> ticketsPorMes;
    private Map<String, Long> ticketsPorPrioridad;
    private Map<String, Long> ticketsPorEmpresa;

    public StatisticsDto(){}

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getTicketsAbiertos() {
        return ticketsAbiertos;
    }

    public void setTicketsAbiertos(long ticketsAbiertos) {
        this.ticketsAbiertos = ticketsAbiertos;
    }

    public long getTicketsCerrados() {
        return ticketsCerrados;
    }

    public void setTicketsCerrados(long ticketsCerrados) {
        this.ticketsCerrados = ticketsCerrados;
    }

    public double getTasaCierre() {
        return tasaCierre;
    }

    public void setTasaCierre(double tasaCierre) {
        this.tasaCierre = tasaCierre;
    }

    public LocalDateTime getFechaMinima() {
        return fechaMinima;
    }

    public void setFechaMinima(LocalDateTime fechaMinima) {
        this.fechaMinima = fechaMinima;
    }

    public LocalDateTime getFechaMaxima() {
        return fechaMaxima;
    }

    public void setFechaMaxima(LocalDateTime fechaMaxima) {
        this.fechaMaxima = fechaMaxima;
    }

    public double getTiempoPromedioDias() {
        return tiempoPromedioDias;
    }

    public void setTiempoPromedioDias(double tiempoPromedioDias) {
        this.tiempoPromedioDias = tiempoPromedioDias;
    }

    public long getTiempoPromedioMinutos() {
        return tiempoPromedioMinutos;
    }

    public void setTiempoPromedioMinutos(long tiempoPromedioMinutos) {
        this.tiempoPromedioMinutos = tiempoPromedioMinutos;
    }

    public Map<String, Long> getTicketsPorEstado() {
        return ticketsPorEstado;
    }

    public void setTicketsPorEstado(Map<String, Long> ticketsPorEstado) {
        this.ticketsPorEstado = ticketsPorEstado;
    }

    public Map<String, Long> getTicketsPorMes() {
        return ticketsPorMes;
    }

    public void setTicketsPorMes(Map<String, Long> ticketsPorMes) {
        this.ticketsPorMes = ticketsPorMes;
    }

    public Map<String, Long> getTicketsPorPrioridad() {
        return ticketsPorPrioridad;
    }

    public void setTicketsPorPrioridad(Map<String, Long> ticketsPorPrioridad) {
        this.ticketsPorPrioridad = ticketsPorPrioridad;
    }

    public Map<String, Long> getTicketsPorEmpresa() {
        return ticketsPorEmpresa;
    }

    public void setTicketsPorEmpresa(Map<String, Long> ticketsPorEmpresa) {
        this.ticketsPorEmpresa = ticketsPorEmpresa;
    }

    public String getPeriodoFormatted(){
        if (fechaMinima == null || fechaMaxima == null){
            return "sin datos";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return fechaMinima.format(formatter) + " - " + fechaMaxima.format(formatter);
    }

    @Override
    public String toString() {
        return "StatisticsDto{" +
                "totalTickets=" + totalTickets +
                ", ticketsAbiertos=" + ticketsAbiertos +
                ", ticketsCerrados=" + ticketsCerrados +
                ", tasaCierre=" + tasaCierre +
                ", periodo=" + getPeriodoFormatted() +
                ", tiempoPromedioDias=" + tiempoPromedioDias +
                '}';
    }
}
