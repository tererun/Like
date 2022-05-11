package amata1219.like.command;

import amata1219.like.Main;
import amata1219.like.bryionake.constant.CommandSenderCasters;
import amata1219.like.bryionake.dsl.BukkitCommandExecutor;
import amata1219.like.bryionake.dsl.context.CommandContext;
import amata1219.like.config.MainConfig;
import amata1219.like.consts.Like;
import amata1219.like.utils.LikeUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class LikeCreationCommand implements BukkitCommandExecutor {

	public static final LikeCreationCommand INSTANCE = new LikeCreationCommand();

	private final CommandContext<CommandSender> executor = define(CommandSenderCasters.casterToPlayer, (sender, unparsedArguments, parsedArguments) -> {
		Main plugin = Main.plugin();
		MainConfig config = plugin.config();
		UUID ownerUUID = sender.getUniqueId();

		LikeUtil.LikeCreationResult likeCreationResult = LikeUtil.createLike(plugin, config, sender, ownerUUID, false);
		LikeUtil.LikeCreationStatus likeCreationStatus = likeCreationResult.getStatus();

		if (likeCreationStatus == LikeUtil.LikeCreationStatus.FAILED_DISABLED_IN_WORLD) {
			sender.sendMessage(ChatColor.RED + "このワールドではLikeを作成できません。");
			return;
		}

		if (likeCreationStatus == LikeUtil.LikeCreationStatus.FAILED_COOLDOWN) {
			sender.sendMessage(ChatColor.RED + "クールダウン中であるためLikeを作成できません。");
			return;
		}

		if (likeCreationStatus == LikeUtil.LikeCreationStatus.FAILED_LIMIT) {
			sender.sendMessage(ChatColor.RED + "Likeの作成上限に達しているため、これ以上Likeを作成できません。");
			return;
		}

		Like like = likeCreationResult.getLike();
		sender.sendMessage(ChatColor.GREEN + "Like(ID: " + like.id + ")を作成しました。");
	});

	@Override
	public CommandContext<CommandSender> executor() {
		return executor;
	}

}
