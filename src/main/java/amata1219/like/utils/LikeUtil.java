package amata1219.like.utils;

import amata1219.like.Main;
import amata1219.like.config.MainConfig;
import amata1219.like.consts.Like;
import amata1219.like.playerdata.PlayerData;
import amata1219.like.task.TaskRunner;
import me.filoghost.holographicdisplays.api.beta.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LikeUtil {

    public static LikeCreationResult createLike(Main plugin, MainConfig config, Player sender, UUID ownerUUID, boolean isOp) {
        if (!config.canLikesBeCreatedIn(sender.getWorld())) {
            return new LikeCreationResult(null, LikeCreationStatus.FAILED_DISABLED_IN_WORLD);
        }

        PlayerData playerdata = plugin.players.get(ownerUUID);

        if (!isOp) {
            if (plugin.cooldownMap.contains(ownerUUID)) {
                return new LikeCreationResult(null, LikeCreationStatus.FAILED_COOLDOWN);
            }
        }

        if (playerdata.likes.size() >= plugin.likeLimitDatabase().read(ownerUUID)) {
            return new LikeCreationResult(null, LikeCreationStatus.FAILED_LIMIT);
        }

        HolographicDisplaysAPI holographicDisplaysAPI = Main.getHolographicDisplaysAPI();
        Hologram hologram = holographicDisplaysAPI.createHologram(sender.getLocation().add(0, 2, 0));
        Like like = new Like(hologram, System.currentTimeMillis(), ownerUUID);
        like.save();

        plugin.likes.put(like.id, like);
        plugin.likeMap.put(like);
        playerdata.registerLike(like);

        if (!isOp) TaskRunner.runTaskLaterSynchronously(task -> plugin.cooldownMap.remove(ownerUUID), config.numberOfSecondsOfLikeCreationCooldown());
        return new LikeCreationResult(like, LikeCreationStatus.SUCCESS);
    }

    public static class LikeCreationResult {
        private Like like;
        private LikeCreationStatus status;

        public LikeCreationResult(Like like, LikeCreationStatus status) {
            this.like = like;
            this.status = status;
        }

        public Like getLike() {
            return like;
        }

        public LikeCreationStatus getStatus() {
            return status;
        }
    }

    public enum LikeCreationStatus {
        SUCCESS,
        FAILED_DISABLED_IN_WORLD,
        FAILED_COOLDOWN,
        FAILED_LIMIT
    }

}
