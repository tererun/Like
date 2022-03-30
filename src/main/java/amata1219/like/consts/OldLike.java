package amata1219.like.consts;

import amata1219.like.Main;
import amata1219.like.config.MainConfig;
import me.filoghost.holographicdisplays.api.beta.hologram.line.TextHologramLine;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologram;
import org.bukkit.World;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class OldLike {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd (E) HH:mm:ss");

    private final Main plugin = Main.plugin();
    private final MainConfig config = plugin.config();

    public final long id;
    public final InternalHologram hologram;

    private UUID owner;
    private int favorites;

    public OldLike(InternalHologram hologram, long id, UUID owner, int favorites) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
        this.favorites = favorites;
    }

    public OldLike(InternalHologram hologram, long id, UUID owner) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
    }

    public World world() {
        return hologram.getPosition().getWorldIfLoaded();
    }

    public int x() {
        return (int) hologram.getPosition().getX();
    }

    public int y() {
        return (int) hologram.getPosition().getY();
    }

    public int z() {
        return (int) hologram.getPosition().getZ();
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
        return ((TextHologramLine) hologram.getLines().get(1)).getText();
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
