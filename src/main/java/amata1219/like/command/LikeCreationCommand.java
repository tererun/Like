package amata1219.like.command;

import amata1219.like.Like;
import amata1219.like.Main;
import amata1219.like.bryionake.constant.CommandSenderCasters;
import amata1219.like.bryionake.dsl.BukkitCommandExecutor;
import amata1219.like.bryionake.dsl.context.CommandContext;
import amata1219.like.config.MainConfig;
import amata1219.like.playerdata.PlayerData;
import amata1219.like.task.TaskRunner;
import me.filoghost.holographicdisplays.plugin.HolographicDisplays;
import me.filoghost.holographicdisplays.plugin.hologram.base.ImmutablePosition;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologram;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologramManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class LikeCreationCommand implements BukkitCommandExecutor {

	public static final LikeCreationCommand INSTANCE = new LikeCreationCommand();

	private final CommandContext<CommandSender> executor = define(CommandSenderCasters.casterToPlayer, (sender, unparsedArguments, parsedArguments) -> {
		Main plugin = Main.plugin();
		MainConfig config = plugin.config();

		if (!config.canLikesBeCreatedIn(sender.getWorld())) {
			sender.sendMessage(ChatColor.RED + "このワールドではLikeを作成できません。");
			return;
		}

		if (plugin.cooldownMap.contains(sender.getUniqueId())) {
			sender.sendMessage(ChatColor.RED + "クールダウン中であるためLikeを作成できません。");
			return;
		}

		UUID uniqueId = sender.getUniqueId();
		PlayerData playerdata = plugin.players.get(uniqueId);
		if (playerdata.likes.size() >= plugin.likeLimitDatabase().read(uniqueId)) {
			sender.sendMessage(ChatColor.RED + "Likeの作成上限に達しているため、これ以上Likeを作成できません。");
			return;
		}

		InternalHologramManager internalHologramManager = HolographicDisplays.getInstance().getInternalHologramManager();
		InternalHologram internalHologram = internalHologramManager.createHologram(String.valueOf(System.currentTimeMillis()), ImmutablePosition.of(sender.getLocation().add(0, 2, 0)));
		Like like = new Like(internalHologram, uniqueId);
		like.save();

		plugin.likes.put(like.id, like);
		plugin.likeMap.put(like);
		playerdata.registerLike(like);

		sender.sendMessage(ChatColor.GREEN + "Like(ID: " + like.id + ")を作成しました。");

		TaskRunner.runTaskLaterSynchronously(task -> plugin.cooldownMap.remove(uniqueId), config.numberOfSecondsOfLikeCreationCooldown());
	});

	@Override
	public CommandContext<CommandSender> executor() {
		return executor;
	}

}
