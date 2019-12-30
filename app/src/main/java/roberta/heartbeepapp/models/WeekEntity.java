package roberta.heartbeepapp.models;

public class WeekEntity {
    private String date;
    private Object value;

    public WeekEntity(String date, Object value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public Object getValue() {
        return value;
    }
}
