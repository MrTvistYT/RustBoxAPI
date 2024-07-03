package org.tvist.rustboxapi;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;


public final class RustBoxAPI extends JavaPlugin {
    public static NamespacedKey BOX_SIZE;
    public static NamespacedKey BOX_TITLE;
    public static NamespacedKey BOX_CONTENT;
    public static NamespacedKey BOX_OPENED;
    public static NamespacedKey BOX_TIME;
    public static NamespacedKey BOX_VERSION;

    public static int VERSION;

    @Override
    public void onEnable() {
        // Инициализация всех неймспейсов. Это ключи для получения информации из PersistentDataContainer
        BOX_SIZE = new NamespacedKey("box", "size");
        BOX_TITLE = new NamespacedKey("box", "title");
        BOX_CONTENT = new NamespacedKey("box", "content");
        BOX_OPENED = new NamespacedKey("box", "opened");
        BOX_TIME = new NamespacedKey("box", "time");
        BOX_VERSION = new NamespacedKey("box", "version");

        // Номер запуска сервера
        VERSION = getConfig().getInt("server");

        // Создаём конфиг
        saveDefaultConfig();

        // Инициализация Менеджера ящиков. В его конструкторе есть JavaPlugin, чтобы в дальнейшем можно было запускать Task.
        new BoxManager(this);

        // если вы знаете что это делает, то как вы тут оказались
        Bukkit.getPluginManager().registerEvents(new BoxEvent(this),this);
    }

    @Override
    public void onDisable() {
        getConfig().set("server", VERSION + 1);
        saveConfig();
    }
}