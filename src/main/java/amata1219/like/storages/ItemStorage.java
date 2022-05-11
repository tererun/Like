package amata1219.like.storages;

import amata1219.like.masquerade.item.Skull;
import org.bukkit.inventory.ItemStack;

public class ItemStorage {

    private ItemStack prevButtonSkull;
    private ItemStack disabledPrevButtonSkull;
    private ItemStack nextButtonSkull;
    private ItemStack disabledNextButtonSkull;

    public ItemStorage() {
        this.init();
    }

    public void init() {
        prevButtonSkull = Skull.createFrom("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
        disabledPrevButtonSkull = Skull.createFrom("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVkNzg4MjI1NzYzMTdiMDQ4ZWVhOTIyMjdjZDg1ZjdhZmNjNDQxNDhkY2I4MzI3MzNiYWNjYjhlYjU2ZmExIn19fQ==");
        nextButtonSkull = Skull.createFrom("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
        disabledNextButtonSkull = Skull.createFrom("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE1NDQ1ZGExNmZhYjY3ZmNkODI3ZjcxYmFlOWMxZDJmOTBjNzNlYjJjMWJkMWVmOGQ4Mzk2Y2Q4ZTgifX19");
    }

    public ItemStack getPrevButtonSkull() {
        return prevButtonSkull;
    }

    public ItemStack getDisabledPrevButtonSkull() {
        return disabledPrevButtonSkull;
    }

    public ItemStack getNextButtonSkull() {
        return nextButtonSkull;
    }

    public ItemStack getDisabledNextButtonSkull() {
        return disabledNextButtonSkull;
    }

}
