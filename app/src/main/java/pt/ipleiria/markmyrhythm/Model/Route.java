package pt.ipleiria.markmyrhythm.Model;

public class Route {
    private String start;
    private String end;
    private String wayPoints;
    private String name;
    private int size;
    private int totalDistance = 0;

    public Route(String start, String end, String wayPoints, int size,String name) {
        this.start = start;
        this.end = end;
        this.wayPoints = wayPoints;
        this.size = size;
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
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
