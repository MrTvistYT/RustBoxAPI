package org.tvist.rustboxapi;

import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BoxEvent implements Listener {
    RustBoxAPI plugin;
    public BoxEvent(RustBoxAPI plugin){
        this.plugin = plugin;
    }



    @EventHandler
    public void openBox(PlayerInteractAtEntityEvent event){
        Player player = event.getPlayer();

        if(!(event.getRightClicked() instanceof ArmorStand)){
            return;
        }

        ArmorStand armorStand = (ArmorStand) event.getRightClicked();
        if(!armorStand.getPersistentDataContainer().has(RustBoxAPI.BOX_CONTENT, DataType.ITEM_STACK_ARRAY)){
            return;
        }

        if(armorStand.getPersistentDataContainer().get(RustBoxAPI.BOX_OPENED, DataType.BOOLEAN)){
            return;
        }

        String title = armorStand.getPersistentDataContainer().get(RustBoxAPI.BOX_TITLE, PersistentDataType.STRING);
        Integer size = armorStand.getPersistentDataContainer().get(RustBoxAPI.BOX_SIZE, PersistentDataType.INTEGER);
        ItemStack[] items = armorStand.getPersistentDataContainer().get(RustBoxAPI.BOX_CONTENT, DataType.ITEM_STACK_ARRAY);

        Inventory inv = Bukkit.createInventory(null, size, title);
        inv.setContents(items);

        armorStand.getPersistentDataContainer().set(RustBoxAPI.BOX_OPENED, DataType.BOOLEAN, true);
        BoxManager.instance.addBoxInventory(inv, armorStand);

        player.openInventory(inv);
    }


    @EventHandler
    public void close(InventoryCloseEvent event){
        ArmorStand as = BoxManager.instance.getBoxInventory(event.getInventory());
        if(as == null) return;

        if(event.getInventory().isEmpty()){
            BoxManager.instance.removeBoxInventory(event.getInventory());
            as.remove();
            return;
        }

        as.getPersistentDataContainer().set(RustBoxAPI.BOX_CONTENT, DataType.ITEM_STACK_ARRAY, event.getInventory().getContents());
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_OPENED, DataType.BOOLEAN, false);
    }

    @EventHandler
    public void load(EntitiesLoadEvent event){
        List<ArmorStand> armorStands = event.getEntities().stream().filter(entity -> entity.getType() == EntityType.ARMOR_STAND).map(entity -> ((ArmorStand)entity)).toList();

        for(ArmorStand as : armorStands){
            Bukkit.getLogger().info(as.getUniqueId().toString());
            if(!as.getPersistentDataContainer().has(RustBoxAPI.BOX_TIME, PersistentDataType.INTEGER)){
                continue;
            }
            int version = as.getPersistentDataContainer().get(RustBoxAPI.BOX_VERSION, PersistentDataType.INTEGER);
            if(version != RustBoxAPI.VERSION){
                as.getPersistentDataContainer().set(RustBoxAPI.BOX_VERSION, PersistentDataType.INTEGER, RustBoxAPI.VERSION);
                Bukkit.getScheduler().runTaskLater(plugin, as::remove, as.getPersistentDataContainer().get(RustBoxAPI.BOX_TIME, PersistentDataType.INTEGER));
            }
        }
    }
}
