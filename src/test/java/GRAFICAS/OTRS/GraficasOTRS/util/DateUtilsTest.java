package GRAFICAS.OTRS.GraficasOTRS.util;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateUtilsTest {

    @Test
    void parseFecha_formatoCorrecto_devuelveLocalDateTime(){
        String date = "17/04/2026 18:36";
        LocalDateTime result = DateUtils.parseFecha(date);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(4, result.getMonthValue());
        assertEquals(17, result.getDayOfMonth());
        assertEquals(18, result.getHour());
        assertEquals(36, result.getMinute());
    }

    @Test
    void parseFecha_FormatoIncorrecto_devuelveLocalDateTime(){
        String dateInvalid = "16/04/2024";
        LocalDateTime r = DateUtils.parseFecha(dateInvalid);
        assertNull(r, "Deberia retornar null para formato invalido");
    }

    @Test
    void parseFecha_formatoInvalido_devuelveNull(){
        assertNull(DateUtils.parseFecha("fecha invalida"));
        assertNull(DateUtils.parseFecha(null));
        assertNull(DateUtils.parseFecha(""));
    }

    @Test
    void parseTiempoTotalToMinutos_formatoCorrecto_devuelveMinutos()
    {
        assertEquals(24648, DateUtils.parseTiempoTotalToMinutos("17d 2h 48m"));
        assertEquals(60, DateUtils.parseTiempoTotalToMinutos("1h"));
        assertEquals(30, DateUtils.parseTiempoTotalToMinutos("30m"));
        assertEquals(0, DateUtils.parseTiempoTotalToMinutos(null));

    }
}
