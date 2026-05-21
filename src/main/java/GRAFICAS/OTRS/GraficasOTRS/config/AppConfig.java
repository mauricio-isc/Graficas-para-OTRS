package GRAFICAS.OTRS.GraficasOTRS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

public class AppConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.pdf.dir}")
    private String pdfDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        //exponer directorios parra descarga de PDFS
        registry.addResourceHandler("/pdfs/**")
                .addResourceLocations("file:"+pdfDir);

        registry.addResourceHandler("/uploads/¨**")
                .addResourceLocations("file:" +uploadDir);
    }

    //metodo para ejecutar cuando se inicie la app al crear los directorios
    @jakarta.annotation.PostConstruct
    public void init(){
        new File(uploadDir).mkdirs();
        new File(pdfDir).mkdirs();
    }
}
