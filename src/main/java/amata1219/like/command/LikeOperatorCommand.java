package amata1219.like.command;

import amata1219.like.Main;
import amata1219.like.bookmark.Bookmark;
import amata1219.like.bookmark.Order;
import amata1219.like.bryionake.constant.CommandSenderCasters;
import amata1219.like.bryionake.constant.Parsers;
import amata1219.like.bryionake.dsl.BukkitCommandExecutor;
import amata1219.like.bryionake.dsl.context.BranchContext;
import amata1219.like.bryionake.dsl.context.CommandContext;
import amata1219.like.bryionake.dsl.parser.FailableParser;
import amata1219.like.bryionake.interval.Endpoint;
import amata1219.like.bryionake.interval.Interval;
import amata1219.like.chunk.LikeMap;
import amata1219.like.config.*;
import amata1219.like.define.Like;
import amata1219.like.define.OldLike;
import amata1219.like.sound.SoundEffects;
import amata1219.like.task.TourRegularNotificationTask;
import amata1219.like.tuplet.Tuple;
import amata1219.like.utils.HologramUtil;
import amata1219.like.utils.LikeUtil;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class LikeOperatorCommand implements BukkitCommandExecutor {

	private final CommandContext<CommandSender> executor;

	private static String join(String... strs) {
		return Joiner.on('\n').join(strs);
	}

	{
		CommandContext<CommandSender> move = define(CommandSenderCasters.casterToPlayer, define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "Likeを現在地に移動する: /likeop move <LikeのID>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Like like = parsedArguments.poll();
					LikeMap likeMap = Main.plugin().likeMap;
					likeMap.remove(like);
					like.teleportTo(sender.getLocation());
					likeMap.put(like);
					sender.sendMessage(ChatColor.GREEN + "Like(ID: " + like.id + ")を現在の位置に移動しました。");
				},
				ParserTemplates.like
		));

		CommandContext<CommandSender> createWithDescription = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "指定したユーザー名でLikeホログラムを作る：/likeop create <プレイヤー名> [表示text]"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					if (!(sender instanceof Player player)) {
						sender.sendMessage(CommandSenderCasters.senderCastErrorMessage);
						return;
					}

					Main plugin = Main.plugin();
					MainConfig config = plugin.config();
					OfflinePlayer ownerOfflinePlayer = parsedArguments.poll();
					StringBuilder description = null;
					int unparsedArgumentsSize = unparsedArguments.size();
					if (unparsedArgumentsSize != 0) {
						description = new StringBuilder();
						int index = 0;
						for (String lore : unparsedArguments) {
							description.append(lore);
							if (index <= unparsedArgumentsSize - 2) {
								description.append(" ");
							}
							index++;
						}
					}
					LikeUtil.LikeCreationResult likeCreationResult = LikeUtil.createLike(plugin, config, player, ownerOfflinePlayer.getUniqueId(), true);
					LikeUtil.LikeCreationStatus likeCreationStatus = likeCreationResult.getStatus();

					if (likeCreationStatus == LikeUtil.LikeCreationStatus.FAILED_DISABLED_IN_WORLD) {
						sender.sendMessage(ChatColor.RED + "このワールドではLikeを作成できません。");
						return;
					}

					if (likeCreationStatus == LikeUtil.LikeCreationStatus.FAILED_LIMIT) {
						sender.sendMessage(ChatColor.RED + "Likeの作成上限に達しているため、これ以上Likeを作成できません。");
						return;
					}

					Like like = likeCreationResult.getLike();
					if (description != null) like.setDescription(description.toString());
					sender.sendMessage(ChatColor.GREEN + "Like(ID: " + like.id + ")を作成しました。");
				},
				ParserTemplates.player
		);

		CommandContext<CommandSender> delete = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "Likeを削除する：/likeop delete <LikeのID>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Like like = parsedArguments.poll();
					Main.plugin().likeMap.remove(like);
					like.delete(true);
					sender.sendMessage(ChatColor.RED + "Like(ID: " + like.id + ")を削除しました。");
				},
				ParserTemplates.like
		);

		CommandContext<CommandSender> deletePlayer = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.DARK_RED + "プレイヤーが作成したLikeを全削除する：/likeop deleteplayer <プレイヤー名>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					OfflinePlayer player = parsedArguments.poll();

					Main plugin = Main.plugin();
					HashMap<Long, Like> playerLikes = plugin.players.get(player.getUniqueId()).likes;
					if (playerLikes.isEmpty()) {
						sender.sendMessage(ChatColor.RED + player.getName() + "はLikeを作成していません。");
						return;
					}

					int deletedLikesCount = playerLikes.size();
					LikeSaveQueue likeSaveQueue = new LikeSaveQueue(UUID.randomUUID());
					for (Like like : playerLikes.values()) {
						plugin.likeMap.remove(like);
						like.delete(false);
						likeSaveQueue.addLike(like);
					}
					likeSaveQueue.saveDelete();

					sender.sendMessage(ChatColor.DARK_RED + player.getName() + "が作成したLike(" + deletedLikesCount + ")を全て削除しました。");
				},
				ParserTemplates.player
		);

		CommandContext<CommandSender> deleteWorld = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.DARK_RED + "ワールド内の全Likeを削除する：/likeop deleteworld <ワールド名>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					World world = parsedArguments.poll();
					AtomicInteger count = new AtomicInteger();
					Main plugin = Main.plugin();
					LikeSaveQueue likeSaveQueue = new LikeSaveQueue(UUID.randomUUID());
					for (Like like : new HashMap<>(plugin.likes).values()) {
						if (like.world().equals(world)) {
							plugin.likeMap.remove(like);
							like.delete(false);
							count.incrementAndGet();
							likeSaveQueue.addLike(like);
						}
					}
					likeSaveQueue.saveDelete();
					sender.sendMessage(ChatColor.DARK_RED + world.getName() + "ワールドに存在する全Like(" + count.get() + ")を削除しました。");
				},
				Parsers.world
		);

		CommandContext<CommandSender> changeOwner = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "Likeの所有者を変更する：/likeop changeowner <LikeのID> <新しい所有者のプレイヤー名>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Like like = parsedArguments.poll();
					OfflinePlayer player = parsedArguments.poll();
					if (like.isOwner(player.getUniqueId())) {
						sender.sendMessage(ChatColor.RED + player.getName() + "は指定されたLike(ID: " + like.id + ")の所有者です。");
						return;
					}
					like.setOwner(player.getUniqueId());
					sender.sendMessage(ChatColor.GREEN + "Like(ID: " + like.id + ")の所有者を" + player.getName() + "に変更しました。");
				},
				ParserTemplates.like,
				ParserTemplates.player
		);

		CommandContext<CommandSender> changePlayerData = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "指定したプレイヤーの全Likeの所有権を新しいプレイヤーに譲渡する：/likeop changedata <元のプレイヤーの名前> <引き継ぐプレイヤーの名前>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					OfflinePlayer oldOwner = parsedArguments.poll();
					OfflinePlayer newOwner = parsedArguments.poll();
					AtomicInteger count = new AtomicInteger();
					for (Like like : new HashMap<>(Main.plugin().players.get(oldOwner.getUniqueId()).likes).values()) {
						like.setOwner(newOwner.getUniqueId());
						count.incrementAndGet();
					}
					sender.sendMessage(ChatColor.GREEN + oldOwner.getName() + "のLike(" + count.get() + "個)を" + newOwner.getName() + "に引き継ぎました。");
				},
				ParserTemplates.player,
				ParserTemplates.player
		);

		CommandContext<CommandSender> reload = (sender, unparsedArguments, parsedArguments) -> {
			MainConfig config = Main.plugin().config();
			config.reload();
			config.load();
			sender.sendMessage(ChatColor.GREEN + "コンフィグを再読み込みしました。");
		};

		Supplier<String> limitDescription = () -> join(
				ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
				ChatColor.GRAY + "プレイヤーのLike作成上限数を指定値に書き換える：/likeop limit <player> set <limit>",
				ChatColor.GRAY + "プレイヤーのLike作成上限数を指定値だけ引き上げる：/likeop limit <player> add <amount_to_add>",
				ChatColor.GRAY + "プレイヤーのLike作成上限数を指定値だけ引き下げる：/likeop limit <player> sub <amount_to_sub>"
		);

		CommandContext<CommandSender> limit = define(
				limitDescription,
				(sender, unparsedArguments, parsedArguments) -> {
					OfflinePlayer player = parsedArguments.poll();
					String operation = parsedArguments.poll();
					int operand = parsedArguments.poll();
					LikeLimitDatabase database = Main.plugin().likeLimitDatabase();
					int limitation = database.read(player.getUniqueId());
					switch (operation) {
						case "set":
							limitation = operand;
							break;
						case "add":
							limitation += operand;
							break;
						case "sub":
							limitation = Math.max(limitation - operand, 0);
							break;
						default:
							sender.sendMessage(limitDescription.get());
							return;
					}
					database.write(player.getUniqueId(), limitation);
					database.update();
					sender.sendMessage(ChatColor.GREEN + player.getName() + "のLike作成上限数を" + limitation + "に変更しました。");
				},
				ParserTemplates.player,
				Parsers.str,
				Parsers.u32
		);

		CommandContext<CommandSender> createBookmark = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークを作成する：/likeop book create <ブックマーク名>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					String bookmarkName = parsedArguments.poll();
					HashMap<String, Bookmark> bookmarks = Main.plugin().bookmarks;
					if (bookmarks.containsKey(bookmarkName)) {
						sender.sendMessage(ChatColor.RED + "指定された名前のブックマークが既に存在しています。");
						return;
					}
					bookmarks.put(bookmarkName, new Bookmark(bookmarkName));
					sender.sendMessage(ChatColor.GREEN + "ブックマーク(" + bookmarkName + ")を作成しました。");
				},
				Parsers.str
		);

		CommandContext<CommandSender> deleteBookmark = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークを削除する：/likeop book delete <ブックマーク名>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Bookmark bookmark = parsedArguments.poll();
					Main.plugin().bookmarkDatabase().remove(bookmark);
					sender.sendMessage(ChatColor.RED + "ブックマーク(" + bookmark.name + ")を削除しました。");
				},
				ParserTemplates.bookmark
		);

		CommandContext<CommandSender> addLikeToBookmark = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークにLikeを追加する: /likeop book add <ブックマーク名> <LikeのID>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Bookmark bookmark = parsedArguments.poll();
					Like like = parsedArguments.poll();
					if (bookmark.likes.contains(like)) {
						sender.sendMessage(ChatColor.RED + "このLikeは既に追加されています。");
						return;
					}
					bookmark.likes.add(like);
					sender.sendMessage(ChatColor.GREEN + "ブックマーク(" + bookmark.name + ")にLike(ID: " + like.id + ")を追加しました。");
				},
				ParserTemplates.bookmark,
				ParserTemplates.like
		);

		CommandContext<CommandSender> removeLikeFromBookmark = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークからLikeを削除する: /likeop book remove <ブックマーク名> <LikeのID>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Bookmark bookmark = parsedArguments.poll();
					Like like = parsedArguments.poll();
					if (!bookmark.likes.contains(like)) {
						sender.sendMessage(ChatColor.RED + "このLikeは追加されていません。");
						return;
					}
					bookmark.likes.remove(like);
					sender.sendMessage(ChatColor.RED + "ブックマーク(" + bookmark.name + ")からLike(ID: " + like.id + ")を削除しました。");
				},
				ParserTemplates.bookmark,
				ParserTemplates.like
		);

		CommandContext<CommandSender> sortBookmarkLikes = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークにLikeを追加する：/likeop book sort <ブックマーク名> <newest/oldest>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					Bookmark bookmark = parsedArguments.poll();
					Order order = parsedArguments.poll();
					bookmark.setOrder(order);
					sender.sendMessage(ChatColor.GREEN + "ブックマーク(" + bookmark.name + ")のLike表示順を" + order.toString() + "にしました。");
				},
				ParserTemplates.bookmark,
				ParserTemplates.order
		);

		BranchContext<CommandSender> bookmarkCommandsBranches = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ブックマークを作成・削除する: /likeop book <create/delete> <ブックマーク名>",
						ChatColor.GRAY + "Likeを追加・削除する: /likeop book <add/remove> <ブックマーク名> <LikeのID>",
						ChatColor.GRAY + "ソートする: /likeop book sort <ブックマーク名> <newest/oldest>"
				),
				bind("create", createBookmark),
				bind("delete", deleteBookmark),
				bind("add", addLikeToBookmark),
				bind("remove", removeLikeFromBookmark),
				bind("sort", sortBookmarkLikes)
		);

		CommandContext<CommandSender> reloadTourConfig = (sender, unparsedArguments, parsedArguments) -> {
			Main plugin = Main.plugin();
			if (plugin.tourRegularNotificationTask != null) plugin.tourRegularNotificationTask.cancel();

			TourConfig config = plugin.tourConfig();
			config.reload();
			config.load();

			plugin.tourRegularNotificationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
					plugin,
					new TourRegularNotificationTask(config),
					0,
					config.notificationIntervalTicks()
			);
		};

		FailableParser<Integer> tourLikesCount = Parsers.define(
				Parsers.u32,
				new Interval<>(Endpoint.openEndpoint(0), Endpoint.closedEndpoint((Supplier<Integer>) Main.plugin().likes::size)),
				() -> ChatColor.RED + "ピックアップするLikeの数Lは、0 < L ≦ " + Main.plugin().likes.size() + "(現在のLikeの合計数) の間で指定して下さい。"
		);

		CommandContext<CommandSender> shuffle = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ツアー用のLikeを指定数分ランダムにピックアップする：/likeop tour shuffle <ピックアップする個数>"
				),
				(sender, unparsedArguments, parsedArguments) -> {
					List<Like> likes = new ArrayList<>(Main.plugin().likes.values());
					int count = Math.min(parsedArguments.poll(), likes.size());
					Collections.shuffle(likes);
					likes = likes.subList(0, count);
					Main.plugin().tourConfig().setLikes(likes);
					sender.sendMessage(ChatColor.GREEN + "ツアー用のLikeを" + count + "個ランダムにピックアップしました！");
					for (Like like : likes) {
						sender.sendMessage(ChatColor.GRAY + "・" + like.id);
						sender.sendMessage("- " + like.description());
					}
					if (sender instanceof Player) SoundEffects.SUCCEEDED.play((Player) sender);
				},
				tourLikesCount
		);

		CommandContext<CommandSender> startNoticing = (sender, unparsedArguments, parsedArguments) -> {
			Main plugin = Main.plugin();
			TourConfig config = plugin.tourConfig();
			if (config.notificationIsEnabled()) {
				sender.sendMessage(ChatColor.RED + "定期告知は既に有効化されています。");
				return;
			}

			config.setNotificationIsEnabled(true);

			plugin.tourRegularNotificationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
					plugin,
					new TourRegularNotificationTask(config),
					0,
					config.notificationIntervalTicks()
			);

			sender.sendMessage(ChatColor.GREEN + "ツアーの定期告知を有効化しました。");
		};

		CommandContext<CommandSender> stopNoticing = (sender, unparsedArguments, parsedArguments) -> {
			Main plugin = Main.plugin();
			TourConfig config = plugin.tourConfig();
			if (!config.notificationIsEnabled()) {
				sender.sendMessage(ChatColor.RED + "定期告知は既に無効化されています。");
				return;
			}

			config.setNotificationIsEnabled(false);
			plugin.tourRegularNotificationTask.cancel();
			plugin.tourRegularNotificationTask = null;

			sender.sendMessage(ChatColor.GREEN + "ツアーの定期告知を無効化しました。");
		};

		BranchContext<CommandSender> noticeBranches = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "ツアーの定期告知を開始する：/likeop tour notice start",
						ChatColor.GRAY + "ツアーの定期告知を停止する：/likeop tour notice stop"
				),
				bind("start", startNoticing),
				bind("stop", stopNoticing)
		);

		BranchContext<CommandSender> tourBranches = define(
				() -> join(
						ChatColor.RED + "正しいコマンドが入力されなかったため実行できませんでした。",
						ChatColor.GRAY + "tour.ymlを再読み込みする：/likeop tour reload",
						ChatColor.GRAY + "ツアー用のLikeを指定数分ランダムにピックアップする：/likeop tour shuffle <ピックアップする個数>",
						ChatColor.GRAY + "ツアーの定期告知を停止する：/likeop tour notice stop"
				),
				bind("reload", reloadTourConfig),
				bind("shuffle", shuffle),
				bind("notice", noticeBranches)
		);

		CommandContext<CommandSender> convertToV4 = (sender, unparsedArguments, parsedArguments) -> {
			sender.sendMessage(ChatColor.GREEN + "v3からv4にデータを移行します");
			Main plugin = Main.plugin();
			OldLikeDatabase oldLikeDatabase = new OldLikeDatabase();
			Tuple<HashMap<Long, OldLike>, HashMap<UUID, List<OldLike>>> maps = oldLikeDatabase.readAll();
			LikeSaveQueue likeSaveQueue = new LikeSaveQueue(UUID.randomUUID());
			HolographicConfig holographicConfig = Main.plugin().holographicConfig();
			for (OldLike oldLike : new HashMap<>(maps.first).values()) {
				Hologram hologram = HologramUtil.createHologram(oldLike.hologram.getLocation());
				Like like = new Like(hologram, oldLike.id, oldLike.owner(), oldLike.favorites(), oldLike.description());
				plugin.likes.put(like.id, like);
				plugin.likeMap.put(like);
				likeSaveQueue.addLike(like);
				Main.plugin().getLogger().info("deleted: " + oldLike.id);
				holographicConfig.getConfig().set(String.valueOf(oldLike.id), null);
			}
			holographicConfig.saveConfig();
			HashMap<UUID, List<OldLike>> readSecond = new HashMap<>(maps.second);
			HashMap<UUID, List<Like>> convertedSecond = new HashMap<>();
			for (UUID uuid : readSecond.keySet()) {
				List<Like> likes = new ArrayList<>();
				for (OldLike oldLike : readSecond.get(uuid)) {
					likes.add(plugin.likes.get(oldLike.id));
				}
				convertedSecond.put(uuid, likes);
			}
			plugin.players.putAll(plugin.playerDatabase().readAll(convertedSecond));
			likeSaveQueue.saveChanges();
			sender.sendMessage(ChatColor.GREEN + "v3からv4へのデータ移行が完了しました!");
			sender.sendMessage(ChatColor.GREEN + "必ず再起動を行ってください。");
			sender.sendMessage(ChatColor.GREEN + "ホログラムの消去を行いました。行った一覧はコンソールに表示されています。");
		};

		executor = define(
				() -> join(
						ChatColor.GRAY + "Likeを現在地に移動する: /likeop move <like_id>",
						ChatColor.GRAY + "Likeを削除する: /likeop delete <like_id>",
						ChatColor.GRAY + "指定したユーザー名でLikeホログラムを作る: /likeop create <player_name> [表示text]",
						ChatColor.GRAY + "プレイヤーが作成したLikeを全削除する: /likeop deleteplayer <player_name>",
						ChatColor.GRAY + "ワールド内のLikeを全削除する: /likeop deleteworld <world_name>",
						ChatColor.GRAY + "Likeの所有者を変更する: /likeop changeowner <like_id> <new_owner_name>",
						ChatColor.GRAY + "プレイヤーの作成したLikeを新しいプレイヤーに引き継ぐ: /likeop changedata <old_owner_name> <new_owner_name>",
						ChatColor.GRAY + "プレイヤーのLike作成上限数を書き換える: /likeop limit <player> set <limit>",
						ChatColor.GRAY + "プレイヤーのLike作成上限数を引き上げる: /likeop limit <player> add <amount_to_add>",
						ChatColor.GRAY + "プレイヤーのLike作成上限数を引き下げる: /likeop limit <player> sub <amount_to_sub>",
						ChatColor.GRAY + "コンフィグをリロードする: /likeop reload",
						ChatColor.GRAY + "ブックマークを作成・削除する: /likeop book <create/delete> <book_name>",
						ChatColor.GRAY + "ブックマークに対してLikeを追加・削除する: /likeop book <add/remove> <book_name> <like_id>",
						ChatColor.GRAY + "ブックマークをソートする: /likeop book sort <book_name> <newest/oldest>",
						ChatColor.GRAY + "tour.ymlを再読み込みする：/likeop tour reload",
						ChatColor.GRAY + "ツアー用のLikeを指定数分ランダムにピックアップする：/likeop tour shuffle <ピックアップする個数>",
						ChatColor.GRAY + "ツアーの定期告知を停止する：/likeop tour notice stop",
						ChatColor.GRAY + "v3からv4にデータを移行する：/likeop converttov4"
				),
				bind("move", move),
				bind("create", createWithDescription),
				bind("delete", delete),
				bind("deleteplayer", deletePlayer),
				bind("deleteworld", deleteWorld),
				bind("changeowner", changeOwner),
				bind("changedata", changePlayerData),
				bind("reload", reload),
				bind("limit", limit),
				bind("book", bookmarkCommandsBranches),
				bind("tour", tourBranches),
				bind("converttov4", convertToV4)
		);
	}

	@Override
	public CommandContext<CommandSender> executor() {
		return executor;
	}
}
