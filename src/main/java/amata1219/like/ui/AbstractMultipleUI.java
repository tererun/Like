package amata1219.like.ui;

import amata1219.like.Main;
import amata1219.like.config.MainConfig;
import amata1219.like.consts.Like;
import amata1219.like.masquerade.dsl.InventoryUI;
import amata1219.like.masquerade.dsl.component.Layout;
import amata1219.like.masquerade.option.Lines;
import amata1219.like.masquerade.text.Text;
import amata1219.like.storages.ItemStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractMultipleUI implements InventoryUI {

	protected final Main plugin = Main.plugin();
	protected final MainConfig config = plugin.config();
	protected int index;
	
	@Override
	public Function<Player, Layout> layout() {
		List<Like> likes = likes();
		ItemStorage itemStorage = Main.plugin().itemStorage();
		return build(Lines.x6, (p, l) -> {
			l.defaultSlot(s -> {
				s.icon(i -> {
					i.material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
					i.displayName = " ";
				});
			});
			
			final Type type = Type.type(index, likes);
			if(type == Type.SINGLE || type == Type.FIRST){
				l.put(s -> {
					s.icon(i -> {
						i.basedItemStack = itemStorage.getDisabledPrevButtonSkull();
						i.displayName = Text.color("&c-これ以上前には戻れません");
					});
				}, 45);
			}else{
				l.put(s -> {
					s.icon(i -> {
						i.basedItemStack = itemStorage.getPrevButtonSkull();
						i.displayName = Text.color("&a-前のページに戻る");
					});
					
					s.onClick(e -> {
						index--;
						open(p);
					});
				}, 45);
			}
			
			if(type == Type.SINGLE || type == Type.LAST){
				l.put(s -> {
					s.icon(i -> {
						i.basedItemStack = itemStorage.getDisabledNextButtonSkull();
						i.displayName = Text.color("&c-これ以上次には進めません");
					});
				}, 53);
			}else{
				l.put(s -> {
					s.icon(i -> {
						i.basedItemStack = itemStorage.getNextButtonSkull();
						i.displayName = Text.color("&a-次のページに進む");
					});
					
					s.onClick(e -> {
						index++;
						open(p);
					});
				}, 53);
			}
			
			layout(p, l, likes);
		});
	}
	
	protected abstract List<Like> likes();
	
	protected abstract void layout(Player player, Layout l, List<Like> likes);
	
	protected enum Type {
		
		SINGLE,
		FIRST,
		MIDDLE,
		LAST;
		
		protected static Type type(int index, List<Like> likes){
			boolean middle = index < likes.size() / 45 + (likes.size() % 45 == 0 ? -1 : 0);
			if(index == 0 || likes.isEmpty()) return middle ? FIRST : SINGLE;
			else return middle ? MIDDLE : LAST;
		}
		
	}

}
