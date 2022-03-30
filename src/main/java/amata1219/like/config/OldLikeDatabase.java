package amata1219.like.config;

import amata1219.like.consts.OldLike;
import amata1219.like.tuplet.Tuple;
import me.filoghost.holographicdisplays.plugin.HolographicDisplays;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologram;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologramManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OldLikeDatabase extends Config {

    public OldLikeDatabase() {
        super("like_data.yml");
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException();
    }

    public Tuple<HashMap<Long, OldLike>, HashMap<UUID, List<OldLike>>> readAll() {
        FileConfiguration config = config();
        HashMap<Long, OldLike> likes = new HashMap<>();
        HashMap<UUID, List<OldLike>> playerLikes = new HashMap<>();
        InternalHologramManager internalHologramManager = HolographicDisplays.getInstance().getInternalHologramManager();
        for (String path : config.getKeys(false)) {
            InternalHologram internalHologram = internalHologramManager.getHologramByName(path);
            long id = Long.parseLong(path);
            String[] data = config.getString(path).split(",");
            UUID owner = UUID.fromString(data[0]);
            OldLike like = new OldLike(internalHologram, id, owner, Integer.parseInt(data[1]));
            likes.put(id, like);
            if (!playerLikes.containsKey(owner)) playerLikes.put(owner, new ArrayList<>());
            playerLikes.get(owner).add(like);
        }
        return Tuple.of(likes, playerLikes);
    }

}
