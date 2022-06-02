package amata1219.like.define;

import amata1219.like.Main;
import amata1219.like.config.MainConfig;
import org.bukkit.World;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class OldLike {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd (E) HH:mm:ss");

    private final Main plugin = Main.plugin();
    private final MainConfig config = plugin.config();

    public final long id;
    public final HologramData hologram;

    private UUID owner;
    private int favorites;

    public OldLike(HologramData hologram, long id, UUID owner, int favorites) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
        this.favorites = favorites;
    }

    public OldLike(HologramData hologram, long id, UUID owner) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
    }

    public World world() {
        return hologram.getLocation().getWorld();
    }

    public int x() {
        return (int) hologram.getLocation().getX();
    }

    public int y() {
        return (int) hologram.getLocation().getY();
    }

    public int z() {
        return (int) hologram.getLocation().getZ();
    }

    public UUID owner() {
        return owner;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public String ownerName() {
        return Main.nameFrom(owner);
    }

    public String description() {
        return hologram.getLines().get(1);
    }

    public int favorites() {
        return favorites;
    }

    public String creationTimestamp() {
        return DATE_FORMAT.format(id);
    }

    @Override
    public String toString() {
        return owner.toString() + "," + favorites;
    }

}
