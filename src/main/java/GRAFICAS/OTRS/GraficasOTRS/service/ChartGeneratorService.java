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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class ChartGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ChartGeneratorService.class);

    // Configuración de colores
    private static final Color COLOR_ABIERTO = new Color(255, 99, 71);   // Rojo tomate
    private static final Color COLOR_CERRADO = new Color(60, 179, 113);  // Verde mar
    private static final Color COLOR_BARRA = new Color(70, 130, 180);    // Azul acero

    /**
     * Genera gráfico circular de estados de tickets
     */
    public byte[] generateEstadoPieChart(Map<String, Long> ticketsPorEstado) throws Exception {
        log.info("Generando gráfico circular de estados: {}", ticketsPorEstado);

        if (ticketsPorEstado == null || ticketsPorEstado.isEmpty()) {
            log.warn("No hay datos para generar gráfico de estados");
            return createEmptyChartImage("No hay datos de tickets");
        }

        PieChart chart = new PieChartBuilder()
                .width(500)
                .height(400)
                .title("Estado de Tickets")
                .build();

        // Configurar estilo básico (compatible con versiones antiguas)
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setPlotContentSize(0.85);
        chart.getStyler().setStartAngleInDegrees(90);

        for (Map.Entry<String, Long> entry : ticketsPorEstado.entrySet()) {
            String estado = entry.getKey();
            Long cantidad = entry.getValue();

            chart.addSeries(estado, cantidad);

            // Configurar color según estado
            if ("ABIERTO".equalsIgnoreCase(estado)) {
                chart.getSeriesMap().get(estado).setFillColor(COLOR_ABIERTO);
            } else if ("CERRADO".equalsIgnoreCase(estado)) {
                chart.getSeriesMap().get(estado).setFillColor(COLOR_CERRADO);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        log.info("Gráfico circular generado correctamente ({} bytes)", baos.size());
        return baos.toByteArray();
    }

    /**
     * Genera gráfico de barras de tickets por mes
     */
    public byte[] generateTendenciaBarChart(Map<String, Long> ticketsPorMes) throws Exception {
        log.info("Generando gráfico de tendencia mensual: {} meses", ticketsPorMes.size());

        if (ticketsPorMes == null || ticketsPorMes.isEmpty()) {
            log.warn("No hay datos para generar gráfico de tendencia");
            return createEmptyChartImage("No hay tickets en el período");
        }

        // Convertir a listas para XChart
        List<String> meses = new ArrayList<>(ticketsPorMes.keySet());
        List<Number> cantidades = new ArrayList<>(ticketsPorMes.values());

        CategoryChart chart = new CategoryChartBuilder()
                .width(600)
                .height(400)
                .title("Tickets por Mes")
                .xAxisTitle("Mes")
                .yAxisTitle("Cantidad de Tickets")
                .build();

        // Configurar estilo básico
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

        // Agregar datos
        chart.addSeries("Tickets", meses, cantidades);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        log.info("Gráfico de barras generado correctamente ({} bytes)", baos.size());
        return baos.toByteArray();
    }

    /**
     * Genera gráfico de barras horizontal para prioridades o empresas top
     */
    public byte[] generateHorizontalBarChart(Map<String, Long> datos, String titulo, String xAxisTitle) throws Exception {
        log.info("Generando gráfico de barras horizontal: {}", titulo);

        if (datos == null || datos.isEmpty()) {
            log.warn("No hay datos para generar gráfico: {}", titulo);
            return createEmptyChartImage("No hay datos disponibles");
        }

        // Limitar a top 10
        Map<String, Long> datosLimitados = datos;
        if (datos.size() > 10) {
            datosLimitados = datos.entrySet().stream()
                    .limit(10)
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            java.util.LinkedHashMap::new
                    ));
        }

        List<String> categorias = new ArrayList<>(datosLimitados.keySet());
        List<Number> valores = new ArrayList<>(datosLimitados.values());

        CategoryChart chart = new CategoryChartBuilder()
                .width(600)
                .height(400)
                .title(titulo)
                .xAxisTitle(xAxisTitle)
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setPlotGridLinesVisible(true);

        chart.addSeries("Cantidad", categorias, valores);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        return baos.toByteArray();
    }

    /**
     * Genera gráfico de líneas para evolución temporal
     */
    public byte[] generateLineChart(Map<String, Long> ticketsPorMes) throws Exception {
        log.info("Generando gráfico de líneas para evolución mensual");

        if (ticketsPorMes == null || ticketsPorMes.isEmpty()) {
            return createEmptyChartImage("No hay datos suficientes");
        }

        List<String> meses = new ArrayList<>(ticketsPorMes.keySet());
        List<Number> cantidades = new ArrayList<>(ticketsPorMes.values());

        CategoryChart chart = new CategoryChartBuilder()
                .width(600)
                .height(400)
                .title("Evolución de Tickets por Mes")
                .xAxisTitle("Mes")
                .yAxisTitle("Cantidad")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(45);

        chart.addSeries("Tickets", meses, cantidades);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        return baos.toByteArray();
    }

    /**
     * Genera imagen vacía con mensaje
     */
    private byte[] createEmptyChartImage(String message) throws Exception {
        log.warn("Generando imagen vacía con mensaje: {}", message);

        List<String> emptyList = new ArrayList<>();
        emptyList.add("");
        List<Number> zeroList = new ArrayList<>();
        zeroList.add(0);

        CategoryChart emptyChart = new CategoryChartBuilder()
                .width(500)
                .height(300)
                .title(message)
                .build();

        emptyChart.getStyler().setLegendVisible(false);
        emptyChart.addSeries("Sin datos", emptyList, zeroList);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(emptyChart, baos, BitmapEncoder.BitmapFormat.PNG);

        return baos.toByteArray();
    }

    /**
     * Genera ambos gráficos principales de una sola vez
     */
    public Map<String, byte[]> generateAllCharts(Map<String, Long> ticketsPorEstado,
                                                 Map<String, Long> ticketsPorMes) throws Exception {
        java.util.Map<String, byte[]> charts = new java.util.HashMap<>();

        charts.put("estados", generateEstadoPieChart(ticketsPorEstado));
        charts.put("tendencia", generateTendenciaBarChart(ticketsPorMes));

        return charts;
    }

    /**
     * grafica para generar por prioridad
     */
    public byte[] generatePrioridadBarChart(Map<String, Long> ticketsPorPrioridad) throws Exception {
        log.info("Generando gráfico de prioridades. Datos recibidos: {}", ticketsPorPrioridad);

        if (ticketsPorPrioridad == null || ticketsPorPrioridad.isEmpty()) {
            log.warn("No hay datos de prioridades");
            return createEmptyChartImage("No hay datos de prioridades");
        }

        // Orden personalizado (ajústalo según tus valores reales)
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

        // Si no hay coincidencias con el orden, tomar todas las existentes
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
                .width(600)
                .height(400)
                .title("Tickets por Prioridad")
                .xAxisTitle("Prioridad")
                .yAxisTitle("Cantidad de Tickets")
                .build();

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(0);
        chart.getStyler().setPlotGridLinesVisible(true);

        // No personalices colores, los colores por defecto funcionan bien

        chart.addSeries("Cantidad", categorias, valores);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapEncoder.saveBitmap(chart, baos, BitmapEncoder.BitmapFormat.PNG);

        log.info("Gráfico de prioridades generado");
        return baos.toByteArray();
    }
}