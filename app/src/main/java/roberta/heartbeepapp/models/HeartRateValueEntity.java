package roberta.heartbeepapp.models;

import org.threeten.bp.LocalDateTime;

import java.io.Serializable;

public class HeartRateValueEntity implements Serializable {
    private LocalDateTime date;
    private Long value;

    public HeartRateValueEntity(){

    }

    public HeartRateValueEntity(LocalDateTime date, Long value) {
        this.date = date;
        this.value = value;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
