package amata1219.like.utils;

import amata1219.like.Main;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;

import java.util.Collection;

public class HologramUtil {

    public static Hologram createHologram(Location location) {
        return HologramsAPI.createHologram(Main.plugin(), location);
    }

    public static Collection<Hologram> getHolograms() {
        return HologramsAPI.getHolograms(Main.plugin());
    }

}
