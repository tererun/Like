package amata1219.like.define;

import org.bukkit.Location;

import java.util.List;

public class HologramData {

    private String name;
    private List<String> lines;
    private Location location;

    public HologramData(String name, List<String> lines, Location location) {
        this.name = name;
        this.lines = lines;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public List<String> getLines() {
        return lines;
    }

    public Location getLocation() {
        return location;
    }

}
