package pt.ipleiria.markmyrhythm.Model;

public class Route {
    private String start;
    private String end;
    private String wayPoints;
    private int size;

    public Route(String start, String end, String wayPoints, int size) {
        this.start = start;
        this.end = end;
        this.wayPoints = wayPoints;
        this.size = size;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(String wayPoints) {
        this.wayPoints = wayPoints;
    }
}
