package GRAFICAS.OTRS.GraficasOTRS.dto;

import org.springframework.web.multipart.MultipartFile;

public class UploadRequestDto {

    private String cliente;
    private MultipartFile file;

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
