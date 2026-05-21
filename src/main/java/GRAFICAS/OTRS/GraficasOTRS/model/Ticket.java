package GRAFICAS.OTRS.GraficasOTRS.model;

import java.time.LocalDateTime;

public class Ticket {

    private String numeroTicket;
    private String estado;
    private String cliente;
    private String empresa;
    private String asunto;
    private String cola;
    private String prioridad;
    private LocalDateTime creacion;
    private LocalDateTime notificacionInicial;
    private LocalDateTime ultimaActualizacion;
    private LocalDateTime cierre;
    private String cerradoPor;
    private String tiempoTotal;
    private long minutosTotal;
    private String propietarioActual;
    private String sla;

    public Ticket(){}

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCola() {
        return cola;
    }

    public void setCola(String cola) {
        this.cola = cola;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDateTime getCreacion() {
        return creacion;
    }

    public void setCreacion(LocalDateTime creacion) {
        this.creacion = creacion;
    }

    public LocalDateTime getNotificacionInicial() {
        return notificacionInicial;
    }

    public void setNotificacionInicial(LocalDateTime notificacionInicial) {
        this.notificacionInicial = notificacionInicial;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public LocalDateTime getCierre() {
        return cierre;
    }

    public void setCierre(LocalDateTime cierre) {
        this.cierre = cierre;
    }

    public String getCerradoPor() {
        return cerradoPor;
    }

    public void setCerradoPor(String cerradoPor) {
        this.cerradoPor = cerradoPor;
    }

    public String getTiempoTotal() {
        return tiempoTotal;
    }

    public void setTiempoTotal(String tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }

    public long getMinutosTotal() {
        return minutosTotal;
    }

    public void setMinutosTotal(long minutosTotal) {
        this.minutosTotal = minutosTotal;
    }

    public String getPropietarioActual() {
        return propietarioActual;
    }

    public void setPropietarioActual(String propietarioActual) {
        this.propietarioActual = propietarioActual;
    }

    public String getSla() {
        return sla;
    }

    public void setSla(String sla) {
        this.sla = sla;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "numeroTicket='" + numeroTicket + '\'' +
                ", estado='" + estado + '\'' +
                ", empresa='" + empresa + '\'' +
                '}';
    }
}
