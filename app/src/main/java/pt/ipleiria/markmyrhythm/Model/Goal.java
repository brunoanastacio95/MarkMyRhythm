package pt.ipleiria.markmyrhythm.Model;

public class Goal {
    private float value;
    private int recurence;
    private String dataType;
    private float current;

    public Goal(float value, int recurence,String dataType,float current) {
        this.value = value;
        this.recurence = recurence;
        this.dataType = dataType;
        this.current = current;
    }

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getRecurence() {
        return recurence;
    }

    public void setRecurence(int recurence) {
        this.recurence = recurence;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
