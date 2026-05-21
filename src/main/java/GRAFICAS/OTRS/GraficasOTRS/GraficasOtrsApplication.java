package GRAFICAS.OTRS.GraficasOTRS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GraficasOtrsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraficasOtrsApplication.class, args);
        System.out.println("====================================================");
        System.out.println("   TICKET REPORT GENERATOR - STARTING               ");
        System.out.println("   http://localhost:8080                            ");
        System.out.println("====================================================");
	}
}
