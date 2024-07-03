package org.tvist.rustboxapi;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BoxManager {
    public static RustBoxAPI plugin;
    public static BoxManager instance;

    // Создаём коллекцию HashMap. В ней храняться инвентари и рюкзаки.
    // Когда игрок закрывает рюкзак, чтобы обновить информацию о предмете, нужно сделать связь между ними.
    private HashMap<Inventory, ArmorStand> boxInventory = new HashMap<>();



    // Инициализация всяких приколов
    public BoxManager(RustBoxAPI plugin){
        BoxManager.plugin = plugin;
        BoxManager.instance = this;
    }


    // Метод, для создания ящика.
    public static void createBox(Location location, String title, int size, int removeTime, List<ItemStack> items){
        // Создаём инвентарь с указанным размеров и тайтлом. И добавляем все предметы из items
        Inventory inventory = Bukkit.createInventory(null, size, title);
        items.stream().filter(Objects::nonNull).forEach(inventory::addItem);

        // Создаём энтити. Делаем так, чтобы он не исчезал
        ArmorStand as = createDisplay(location);
        as.setRemoveWhenFarAway(false);

        // Создаём информацию в энтити благодаря PDC и ключам.
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_TITLE, PersistentDataType.STRING, title);
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_SIZE, PersistentDataType.INTEGER, size);
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_CONTENT, DataType.ITEM_STACK_ARRAY, inventory.getContents());
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_OPENED, DataType.BOOLEAN, false);
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_TIME, PersistentDataType.INTEGER, removeTime);
        as.getPersistentDataContainer().set(RustBoxAPI.BOX_VERSION, PersistentDataType.INTEGER, RustBoxAPI.VERSION);

        Bukkit.broadcast(Component.text(as.getPersistentDataContainer().get(RustBoxAPI.BOX_TIME, PersistentDataType.INTEGER) + ""));

        // Запускаем таймер для удаления рюкзака
        Bukkit.getScheduler().runTaskLater(plugin, task -> as.remove(), removeTime);
    }

    // Тут всё понятно
    private static ArmorStand createDisplay(Location location){
        ArmorStand as = location.getWorld().spawn(location, ArmorStand.class);

        as.setInvisible(true);
        as.setInvulnerable(true);
        as.setSilent(true);
        as.setGravity(false);
        as.setSmall(true);

        as.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        as.setItem(EquipmentSlot.HEAD, head());
        return as;
    }

    // этот код спиздил. он создаёт голову с текстуркой, также можно засунуть туда модельку свою рюкзака через кфг
    private static ItemStack head(){
        String url = plugin.getConfig().getString("head.url");

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if(plugin.getConfig().getInt("head.customModelData") != 0){
            ItemMeta headMeta = head.getItemMeta();
            headMeta.setCustomModelData(plugin.getConfig().getInt("head.customModelData"));
            head.setItemMeta(headMeta);
        }

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        }
        catch (IllegalArgumentException|NoSuchFieldException|SecurityException | IllegalAccessException error) {
            error.printStackTrace();
        }

        head.setItemMeta(headMeta);
        return head;
    }





    public void addBoxInventory(Inventory inventory, ArmorStand armorStand){
        boxInventory.put(inventory, armorStand);
    }
    public void removeBoxInventory(Inventory inventory){
        boxInventory.remove(inventory);
    }
    public ArmorStand getBoxInventory(Inventory inventory){
        return boxInventory.get(inventory);
    }
}
