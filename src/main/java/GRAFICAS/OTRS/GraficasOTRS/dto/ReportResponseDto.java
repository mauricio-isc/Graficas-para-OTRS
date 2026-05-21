package GRAFICAS.OTRS.GraficasOTRS.dto;

public class ReportResponseDto {

    private boolean success;
    private String message;
    private String filename;
    private String downloadUrl;
    private long totalTickets;

    public ReportResponseDto(){}

    public ReportResponseDto(boolean success, String message, String filename, String downloadUrl, long totalTickets) {
        this.success = success;
        this.message = message;
        this.filename = filename;
        this.downloadUrl = downloadUrl;
        this.totalTickets = totalTickets;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }
}
