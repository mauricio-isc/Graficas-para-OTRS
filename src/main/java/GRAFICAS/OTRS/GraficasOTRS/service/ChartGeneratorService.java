package GRAFICAS.OTRS.GraficasOTRS.service;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.Styler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.Map;

@Service
public class ChartGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ChartGeneratorService.class);

    private static final Color COLOR_ABIERTO = new Color(255, 99, 71);
    private static final Color COLOR_CERRADO = new Color(60, 179, 113);

    public byte[] generateEstadoPieChart(Map<String, Long> ticketsPorEstado) throws Exception {
        log.info("Generando gráfico circular de estados: {}", ticketsPorEstado);
        if (ticketsPorEstado == null || ticketsPorEstado.isEmpty()) {
            return createEmptyChartImage("No hay datos de tickets");
        }
        PieChart chart = new PieChartBuilder()
                .width(500).height(400).title("Estado de Tickets").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setPlotContentSize(0.85);
        chart.getStyler().setStartAngleInDegrees(90);
        for (Map.Entry<String, Long> entry : ticketsPorEstado.entrySet()) {
            String estado = entry.getKey();
            Long cantidad = entry.getValue();
            chart.addSeries(estado, cantidad);
            if ("ABIERTO".equalsIgnoreCase(estado))
                chart.getSeriesMap().get(estado).setFillColor(COLOR_ABIERTO);
            else if ("CERRADO".equalsIgnoreCase(estado))
                chart.getSeriesMap().get(estado).setFillColor(COLOR_CERRADO);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    public byte[] generateTendenciaBarChart(Map<String, Long> ticketsPorMes) throws Exception {
        log.info("Generando gráfico de tendencia mensual: {} meses", ticketsPorMes.size());
        if (ticketsPorMes == null || ticketsPorMes.isEmpty()) {
            return createEmptyChartImage("No hay tickets en el período");
        }
        List<String> meses = new ArrayList<>(ticketsPorMes.keySet());
        List<Number> cantidades = new ArrayList<>(ticketsPorMes.values());
        CategoryChart chart = new CategoryChartBuilder()
                .width(600).height(400).title("Tickets por Mes")
                .xAxisTitle("Mes").yAxisTitle("Cantidad de Tickets").build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);
        chart.addSeries("Tickets", meses, cantidades);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    public byte[] generatePrioridadBarChart(Map<String, Long> ticketsPorPrioridad) throws Exception {
        log.info("Generando gráfico de prioridades: {}", ticketsPorPrioridad);
        if (ticketsPorPrioridad == null || ticketsPorPrioridad.isEmpty()) {
            return createEmptyChartImage("No hay datos de prioridades");
        }
        List<String> ordenPrioridades = Arrays.asList("Crítica", "Alta", "Media", "Baja", "Urgente");
        List<String> categorias = new ArrayList<>();
        List<Number> valores = new ArrayList<>();
        for (String prioridad : ordenPrioridades) {
            Long cantidad = ticketsPorPrioridad.get(prioridad);
            if (cantidad != null && cantidad > 0) {
                categorias.add(prioridad);
                valores.add(cantidad);
            }
        }
        if (categorias.isEmpty()) {
            for (Map.Entry<String, Long> entry : ticketsPorPrioridad.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    categorias.add(entry.getKey());
                    valores.add(entry.getValue());
                }
            }
        }
        if (categorias.isEmpty()) {
            return createEmptyChartImage("No hay valores positivos de prioridad");
        }
        CategoryChart chart = new CategoryChartBuilder()
                .width(600).height(400).title("Tickets por Prioridad")
                .xAxisTitle("Prioridad").yAxisTitle("Cantidad de Tickets").build();
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(0);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.addSeries("Cantidad", categorias, valores);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    /**
     * Método auxiliar que genera una imagen con un mensaje cuando no hay datos.
     * IMPORTANTE: evita el error "Zero length string" usando categorías no vacías.
     */
    private byte[] createEmptyChartImage(String message) throws Exception {
        String title = (message == null || message.trim().isEmpty()) ? "No hay datos disponibles" : message;
        List<String> categories = Arrays.asList("Sin datos");
        List<Number> values = Arrays.asList(0);
        CategoryChart emptyChart = new CategoryChartBuilder()
                .width(500).height(300).title(title).xAxisTitle("").yAxisTitle("Cantidad").build();
        emptyChart.getStyler().setLegendVisible(false);
        emptyChart.addSeries("", categories, values);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(emptyChart, baos, BitmapEncoder.BitmapFormat.PNG);
        return baos.toByteArray();
    }

    public Map<String, byte[]> generateAllCharts(Map<String, Long> ticketsPorEstado,
                                                 Map<String, Long> ticketsPorMes) throws Exception {
        Map<String, byte[]> charts = new HashMap<>();
        charts.put("estados", generateEstadoPieChart(ticketsPorEstado));
        charts.put("tendencia", generateTendenciaBarChart(ticketsPorMes));
        return charts;
    }
}