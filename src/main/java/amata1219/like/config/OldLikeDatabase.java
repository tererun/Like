package amata1219.like.config;

import amata1219.like.Main;
import amata1219.like.define.OldLike;
import amata1219.like.handler.HologramDataHandler;
import amata1219.like.tuplet.Tuple;
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
        HolographicConfig holographicConfig = Main.plugin().holographicConfig();
        HologramDataHandler hologramDataHandler = holographicConfig.getHologramDataHandler();
        for (String path : config.getKeys(false)) {
            long id = Long.parseLong(path);
            String[] data = config.getString(path).split(",");
            UUID owner = UUID.fromString(data[0]);
            OldLike like = new OldLike(hologramDataHandler.getHologramData(String.valueOf(id)), id, owner, Integer.parseInt(data[1]));
            likes.put(id, like);
            if (!playerLikes.containsKey(owner)) playerLikes.put(owner, new ArrayList<>());
            playerLikes.get(owner).add(like);
        }
        return Tuple.of(likes, playerLikes);
    }

}
