package amata1219.like.define;

import amata1219.like.Main;
import amata1219.like.config.LikeSaveQueue;
import amata1219.like.config.MainConfig;
import amata1219.like.masquerade.dsl.InventoryUI;
import amata1219.like.masquerade.task.AsyncTask;
import amata1219.like.masquerade.text.Text;
import amata1219.like.playerdata.PlayerData;
import amata1219.like.ui.AdministratorUI;
import amata1219.like.ui.LikeEditingUI;
import amata1219.like.ui.LikeInformationUI;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TouchableLine;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        enableHologramLineClickListener();
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

        enableHologramLineClickListener();
    }

    private void appendTextLine(String text) {
        hologram.appendTextLine(text);
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
        hologram.removeLine(index);
        hologram.insertTextLine(index, text);
        if (index == 0) {
            disableHologramLineClickListener();
            enableHologramLineClickListener();
        }
        save();
    }

    public String creationTimestamp() {
        return DATE_FORMAT.format(id);
    }

    public void teleportTo(Location loc) {
        hologram.teleport(loc.add(0, 2, 0));
        disableHologramLineClickListener();
        enableHologramLineClickListener();
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

    private void enableHologramLineClickListener() {
        setHologramLineClickListener(this::touchHandler);
    }

    private void disableHologramLineClickListener() {
        setHologramLineClickListener(null);
    }

    private void touchHandler(@NotNull Player player) {
        if (player.isSneaking()) {
            InventoryUI ui;
            if (isOwner(player.getUniqueId())) ui = new LikeEditingUI(this);
            else if (player.hasPermission(Main.OPERATOR_PERMISSION)) ui = new AdministratorUI(this);
            else ui = new LikeInformationUI(this);
            ui.open(player);
        } else {
            if (isOwner(player.getUniqueId())) {
                Text.of("&c-自分のLikeはお気に入りに登録できません。").sendTo(player);
                return;
            }

            PlayerData data = plugin.players.get(player.getUniqueId());
            if (data.isFavoriteLike(this)) {
                Text.of("&c-このLikeは既にお気に入りに登録しています。").sendTo(player);
                return;
            }

            data.favoriteLike(this);
            incrementFavorites();
            Text.of("&a-このLikeをお気に入りに登録しました！", config.tip()).sendTo(player);
        }
    }

    private void setHologramLineClickListener(TouchHandler listener) {
        Location loc = hologram.getLocation();
        loc.setPitch(90.0F);

        ((TouchableLine) hologram.getLine(0)).setTouchHandler(listener);
        ((TouchableLine) hologram.getLine(2)).setTouchHandler(listener);
    }

    @Override
    public String toString() {
        return owner.toString() + "," + favorites + "," + world().getUID() + ":" + x() + ":" + y() + ":" + z() + "," + description;
    }

}
