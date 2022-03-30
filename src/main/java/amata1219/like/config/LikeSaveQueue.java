package amata1219.like.config;

import amata1219.like.Main;
import amata1219.like.consts.Like;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LikeSaveQueue {

    private UUID taskUUID;
    private List<Like> saveLikeList;

    public LikeSaveQueue(UUID taskUUID) {
        this.taskUUID = taskUUID;
        this.saveLikeList = new ArrayList<>();
    }

    public void saveDelete() {
        LikeDatabase likeDatabase = Main.plugin().likeDatabase();
        FileConfiguration config = likeDatabase.config();
        for (Like like : saveLikeList) {
            config.set(String.valueOf(like.id), null);
        }
        likeDatabase.update();
    }

    public void saveChanges() {
        LikeDatabase likeDatabase = Main.plugin().likeDatabase();
        FileConfiguration config = likeDatabase.config();
        for (Like like : saveLikeList) {
            config.set(String.valueOf(like.id), like.toString());
        }
        likeDatabase.update();
    }

    public void addAllLikes(Like...likes) {
        saveLikeList.addAll(List.of(likes));
    }

    public void addLike(Like like) {
        saveLikeList.add(like);
    }

    public UUID getTaskUUID() {
        return taskUUID;
    }

}
