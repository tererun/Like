package amata1219.like.config;

import amata1219.like.consts.Like;
import amata1219.like.Main;
import amata1219.like.tuplet.Tuple;
import me.filoghost.holographicdisplays.api.beta.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LikeDatabase extends Config {

    public LikeDatabase() {
        super("like_main_data.yml");
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException();
    }

    public Tuple<HashMap<Long, Like>, HashMap<UUID, List<Like>>> readAll() {
        HolographicDisplaysAPI holographicDisplaysAPI = Main.getHolographicDisplaysAPI();
        FileConfiguration config = config();
        HashMap<Long, Like> likes = new HashMap<>();
        HashMap<UUID, List<Like>> playerLikes = new HashMap<>();
        for (String path : config.getKeys(false)) {
            long id = Long.parseLong(path);
            String[] data = config.getString(path).split(",", 4);
            UUID owner = UUID.fromString(data[0]);
            int favorites = Integer.parseInt(data[1]);
            String[] locations = data[2].split(":");
            Location location = new Location(Bukkit.getWorld(UUID.fromString(locations[0])), Double.parseDouble(locations[1]), Double.parseDouble(locations[2]), Double.parseDouble(locations[3]));
            String description = data[3];
            Hologram hologram = holographicDisplaysAPI.createHologram(location);
            Like like = new Like(hologram, id, owner, favorites, description, false);
            likes.put(id, like);
            if (!playerLikes.containsKey(owner)) playerLikes.put(owner, new ArrayList<>());
            playerLikes.get(owner).add(like);
        }
        return Tuple.of(likes, playerLikes);
    }

    public void remove(Like like) {
        FileConfiguration config = config();
        config.set(String.valueOf(like.id), null);
        update();
    }

    public void writeAll() {
        FileConfiguration config = config();
        plugin.likes.forEach((id, like) -> config.set(String.valueOf(id), like.toString()));
        update();
    }

}
