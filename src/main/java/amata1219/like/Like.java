package amata1219.like;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import amata1219.like.masquerade.dsl.InventoryUI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.filoghost.fcommons.Strings;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import me.filoghost.holographicdisplays.api.beta.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.plugin.HolographicDisplays;
import me.filoghost.holographicdisplays.plugin.commands.InternalHologramEditor;
import me.filoghost.holographicdisplays.plugin.event.InternalHologramChangeEvent;
import me.filoghost.holographicdisplays.plugin.hologram.base.BaseHologramLines;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologram;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalHologramLine;
import me.filoghost.holographicdisplays.plugin.internal.hologram.InternalTextHologramLine;
import me.filoghost.holographicdisplays.plugin.lib.fcommons.command.validation.CommandException;
import org.bukkit.Location;
import org.bukkit.World;

import amata1219.like.config.MainConfig;
import amata1219.like.masquerade.task.AsyncTask;
import amata1219.like.masquerade.text.Text;
import amata1219.like.playerdata.PlayerData;
import amata1219.like.ui.AdministratorUI;
import amata1219.like.ui.LikeEditingUI;
import amata1219.like.ui.LikeInformationUI;
import org.bukkit.entity.Player;

public class Like {
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd (E) HH:mm:ss");

	private final Main plugin = Main.plugin();
	private final MainConfig config = plugin.config();
	
	public final long id;
	public final InternalHologram hologram;
	
	private UUID owner;
	private int favorites;
	
	public Like(InternalHologram hologram, UUID owner, int favorites) {

		this.id = Long.parseLong(hologram.getName());
		this.hologram = hologram;
		this.owner = owner;
		this.favorites = favorites;
		enableTouchHandler();
	}
	
	public Like(InternalHologram hologram, UUID owner) {
		this.id = Long.parseLong(hologram.getName());
		this.hologram = hologram;
		this.owner = owner;

		try {
			appendTextLine(config.likeFavoritesText().apply(favorites));
			appendTextLine(config.likeDescription().apply(owner));
			appendTextLine(config.likeUsage());
		} catch (CommandException e) {
			e.printStackTrace();
		}

		enableTouchHandler();
	}

	private void appendTextLine(String text) throws CommandException {
		InternalHologramEditor hologramEditor = HolographicDisplays.getInstance().getInternalHologramEditor();
		InternalHologramLine line = hologramEditor.parseHologramLine(hologram, text);
		hologram.getLines().add(line);
		save(InternalHologramChangeEvent.ChangeType.EDIT_LINES);
	}
	
	public World world(){
		return hologram.getPosition().getWorldIfLoaded();
	}
	
	public int x(){
		return (int) hologram.getPosition().getX();
	}
	
	public int y(){
		return (int) hologram.getPosition().getY();
	}
	
	public int z(){
		return (int) hologram.getPosition().getZ();
	}
	
	public UUID owner(){
		return owner;
	}
	
	public void setOwner(UUID uuid){
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
	
	public boolean isOwner(UUID uuid){
		return owner.equals(uuid);
	}
	
	public String ownerName(){
		return Main.nameFrom(owner);
	}
	
	public String description(){
		return hologram.getLines().get(1).getSerializedConfigValue();
	}
	
	public void setDescription(String description) throws CommandException {
		rewriteHologramLine(1, description);
	}
	
	public int favorites(){
		return favorites;
	}
	
	public void incrementFavorites() throws CommandException {
		favorites++;
		rewriteHologramLine(0, config.likeFavoritesText().apply(favorites));
	}
	
	public void decrementFavorites() throws CommandException {
		favorites = Math.max(favorites - 1, 0);
		rewriteHologramLine(0, config.likeFavoritesText().apply(favorites));
	}
	
	private void rewriteHologramLine(int index, String text) throws CommandException {
		InternalHologramEditor hologramEditor = HolographicDisplays.getInstance().getInternalHologramEditor();
		BaseHologramLines<InternalHologramLine> lines = hologram.getLines();
		InternalHologramLine line = hologramEditor.parseHologramLine(hologram, text);
		lines.set(index, line);
		if (index == 0) {
			disableTouchHandler();
			enableTouchHandler();
		}
		save(InternalHologramChangeEvent.ChangeType.EDIT_LINES);
	}
	
	public String creationTimestamp(){
		return DATE_FORMAT.format(id);
	}
	
	public void teleportTo(Location loc){
		hologram.setPosition(loc.add(0, 2, 0));
		disableTouchHandler();
		enableTouchHandler();
		save(InternalHologramChangeEvent.ChangeType.EDIT_POSITION);
	}
	
	public void save(InternalHologramChangeEvent.ChangeType changeType){
		HolographicDisplays.getInstance().getInternalHologramEditor().saveChanges(hologram, changeType);
	}
	
	public void delete(){
		InternalHologramEditor hologramEditor = HolographicDisplays.getInstance().getInternalHologramEditor();
		plugin.players.get(owner).unregisterLike(this);
		AsyncTask.define(() -> plugin.players.values().forEach(data -> data.unfavoriteLike(this))).execute();
		plugin.bookmarks.values().forEach(bookmark -> bookmark.likes.remove(this));
		plugin.likes.remove(id);
		plugin.likeDatabase().remove(this);
		hologramEditor.delete(hologram);
		save(InternalHologramChangeEvent.ChangeType.DELETE);
	}

	private void enableTouchHandler() {
		setTouchHandler(this::touchHandler);
	}

	private void touchHandler(Player player) {
		if (player.isSneaking()) {
			InventoryUI ui;
			if (isOwner(player.getUniqueId())) ui = new LikeEditingUI(this);
			else if (player.hasPermission(Main.OPERATOR_PERMISSION)) ui = new AdministratorUI(this);
			else ui = new LikeInformationUI(this);
			ui.open(player);
		}else{
			if (isOwner(player.getUniqueId())) {
				Text.of("&c-自分のLikeはお気に入りに登録できません。").sendTo(player);
				return;
			}

			PlayerData data = plugin.players.get(player.getUniqueId());
			if(data.isFavoriteLike(this)){
				Text.of("&c-このLikeは既にお気に入りに登録しています。").sendTo(player);
				return;
			}

			data.favoriteLike(this);
			incrementFavorites();
			Text.of("&a-このLikeをお気に入りに登録しました！", config.tip()).sendTo(player);
		}
	}

	private void disableTouchHandler() {
		setTouchHandler(null);
	}

	private void setTouchHandler(TouchHandler handler){
		Location loc = hologram.getLocation();
		loc.setPitch(90.0F);
		CraftTouchableLine line = (CraftTouchableLine) hologram.getLine(0);
		setTouchHandler(line, handler, loc.getWorld(), loc.getX(), loc.getY() - line.getHeight() * 3, loc.getZ());
		line = (CraftTouchableLine) hologram.getLine(2);
		setTouchHandler(line, handler, loc.getWorld(), loc.getX(), loc.getY() - line.getHeight() * 1, loc.getZ());
	}

	private static void setTouchHandler(CraftTouchableLine line, TouchHandler handler, World world, double x, double y, double z) {
		try {
			setTouchHandler.invoke(line, handler, world, x, y, z);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString(){
		return owner.toString() + "," + favorites;
	}
	
}
