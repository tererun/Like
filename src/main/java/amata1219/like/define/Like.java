package amata1219.like.define;

import amata1219.like.Main;
import amata1219.like.config.LikeSaveQueue;
import amata1219.like.config.MainConfig;
import amata1219.like.masquerade.task.AsyncTask;
import amata1219.like.playerdata.PlayerData;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.World;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.UUID;

public class Like {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd (E) HH:mm:ss");

    private final Main plugin = Main.plugin();
    private final MainConfig config = plugin.config();

    public final long id;
    public final Hologram hologram;

    private UUID owner;
    private int favorites;
    private String description;

    public Like(Hologram hologram, long id, UUID owner) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
        String defaultDescription = config.likeDescription().apply(owner);
        this.description = defaultDescription;
        appendTextLine(config.likeFavoritesText().apply(favorites));
        appendTextLine(defaultDescription);
        appendTextLine(config.likeUsage());
    }

    public Like(Hologram hologram, long id, UUID owner, int favorites, String description) {
        this.hologram = hologram;
        this.id = id;
        this.owner = owner;
        this.favorites = favorites;
        this.description = description;
        appendTextLine(config.likeFavoritesText().apply(favorites));
        appendTextLine(description);
        appendTextLine(config.likeUsage());
    }

    private void appendTextLine(String text) {
        DHAPI.addHologramLine(hologram, text);
        save();
    }

    public World world() {
        return hologram.getLocation().getWorld();
    }

    public int blockX() {
        return (int) hologram.getLocation().getX();
    }

    public int blockY() {
        return (int) hologram.getLocation().getY();
    }

    public int blockZ() {
        return (int) hologram.getLocation().getZ();
    }

    public double x() {
        return hologram.getLocation().getX();
    }

    public double y() {
        return hologram.getLocation().getY();
    }

    public double z() {
        return hologram.getLocation().getZ();
    }

    public UUID owner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        Objects.requireNonNull(uuid);
        PlayerData newOwner = plugin.players.get(uuid);
        if (newOwner.isFavoriteLike(this)) {
            decrementFavorites();
            newOwner.unfavoriteLike(this);
        }
        plugin.players.get(owner).unregisterLike(this);
        this.owner = uuid;
        newOwner.registerLike(this);
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public String ownerName() {
        return Main.nameFrom(owner);
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        rewriteHologramLine(1, description);
    }

    public int favorites() {
        return favorites;
    }

    public void incrementFavorites() {
        favorites++;
        rewriteHologramLine(0, config.likeFavoritesText().apply(favorites));
    }

    public void decrementFavorites() {
        favorites = Math.max(favorites - 1, 0);
        rewriteHologramLine(0, config.likeFavoritesText().apply(favorites));
    }

    private void rewriteHologramLine(int index, String text) {
        DHAPI.removeHologramLine(hologram, index);
        DHAPI.insertHologramLine(hologram, index, text);
        save();
    }

    public String creationTimestamp() {
        return DATE_FORMAT.format(id);
    }

    public void teleportTo(Location loc) {
        hologram.setLocation(loc.clone().add(0, 2, 0));
        save();
    }

    public void save() {
        LikeSaveQueue likeSaveQueue = new LikeSaveQueue(UUID.randomUUID());
        likeSaveQueue.addLike(this);
        likeSaveQueue.saveChanges();
    }

    public void delete(boolean alsoSave) {
        plugin.players.get(owner).unregisterLike(this);
        AsyncTask.define(() -> plugin.players.values().forEach(data -> data.unfavoriteLike(this))).execute();
        plugin.bookmarks.values().forEach(bookmark -> bookmark.likes.remove(this));
        plugin.likes.remove(id);
        plugin.likeDatabase().remove(this);
        hologram.delete();
        if (alsoSave) {
            LikeSaveQueue likeSaveQueue = new LikeSaveQueue(UUID.randomUUID());
            likeSaveQueue.addLike(this);
            likeSaveQueue.saveDelete();
        }
    }

    @Override
    public String toString() {
        return owner.toString() + "," + favorites + "," + world().getUID() + ":" + x() + ":" + y() + ":" + z() + "," + description;
    }

}
