package amata1219.like.utils;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;

public class HologramUtil {

    public static Hologram createHologram(long id, Location location) {
        return DHAPI.createHologram(String.valueOf(id), location);
    }

    public static Hologram getHologram(long id) {
        return DHAPI.getHologram(String.valueOf(id));
    }

}
