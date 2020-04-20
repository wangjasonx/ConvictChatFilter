package me.tsyrac.convictchatfilter;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.chat.Chat;

import java.util.Set;
import java.util.regex.*;

public final class ConvictChatFilter extends JavaPlugin implements Listener {

    private Set<String> words;
    private Set<String> players;
    private static Chat chat = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        setupChat();

        System.out.println("ConvictChatFilter is now Enabled!");
        this.saveDefaultConfig();

        //Setting up the playerlist config, adding Tsyrac'd UUID as the default first player.
        Playerlist.setup();
        Playerlist.getPlayerFile().addDefault("b5ef60e6-2450-4b18-8c09-ad00da529e2f", true);
        Playerlist.getPlayerFile().options().copyDefaults(true);
        Playerlist.save();
        players = Playerlist.getPlayerFile().getKeys(false);

        // Grab the list of words to check for from the config.yml file
        words = getConfig().getConfigurationSection("Words").getKeys(false);

        // Set up the commands
        getCommand("toggleChatFilter").setExecutor(this::onCommand);
        getCommand("reloadChatFilter").setExecutor(this::reloadCommand);

        getServer().getPluginManager().registerEvents(this, this);




    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("ConvictChatFilter is now Disabled!");
        saveConfig();
        Playerlist.save();
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player;

        if (command.getName().equalsIgnoreCase("toggleChatFilter") && sender.hasPermission("toggleChatFilter.use")) {

            if (sender instanceof Player) {
                player = (Player) sender;
                String playerUUID  = player.getUniqueId().toString();


                if(Playerlist.getPlayerFile().getBoolean(playerUUID)){
                    Playerlist.getPlayerFile().set(playerUUID, false);
                    player.sendMessage(ChatColor.RED + "No longer filtering chat");
                } else {
                    Playerlist.getPlayerFile().set(playerUUID, true);
                    player.sendMessage(ChatColor.GREEN + "Chat filter enabled");
                }

                Playerlist.save();
                Playerlist.reload();
            }


        }

        return true;
    }

    public boolean reloadCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("reloadChatFilter") && sender.hasPermission("toggleChatFilter.reload")) {
            Player player;

            if (sender instanceof Player) {
                player = (Player) sender;
                player.sendMessage(ChatColor.GREEN + "toggleChatFilter Plugin Reloaded!");
            }

            reloadConfig();
            words = getConfig().getConfigurationSection("Words").getKeys(false);
            Playerlist.reload();
            players = Playerlist.getPlayerFile().getKeys(false);

        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        if (!players.contains(playerUUID)) {
            players.add(playerUUID);
            Playerlist.getPlayerFile().set(playerUUID, true);
            Playerlist.save();
            Playerlist.reload();
        }

    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        String factionName = fPlayer.getChatTag();

        if(!fPlayer.hasFaction()){
            factionName = "";
        }

        String prefix = ChatColor.translateAlternateColorCodes('&', chat.getPlayerPrefix(player));
        String suffix = ChatColor.translateAlternateColorCodes('&', chat.getPlayerSuffix(player));
        String indicator = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Chat-Indicator"));

        String playerName = factionName + " " + prefix + player.getName() + suffix + indicator;

        String message = event.getMessage().toLowerCase();
        Set<Player> availablePlayers = event.getRecipients();

        for (String s : words) {
            if (message.contains(s.toLowerCase())) {

                Pattern pattern = Pattern.compile("(?i)" + s);

                for(Player p : availablePlayers){

                    FPlayer toCheck = FPlayers.getInstance().getByPlayer(p);
                    Relation toCompare = fPlayer.getRelationTo(toCheck);

                    ChatColor changeColor;
                    if(toCompare.equals(Relation.NEUTRAL)){
                        changeColor = ChatColor.WHITE;
                    } else if( toCompare.equals(Relation.MEMBER)){
                        changeColor = ChatColor.GREEN;
                    } else if(toCompare.equals(Relation.ENEMY)){
                        changeColor = ChatColor.RED;
                    } else if(toCompare.equals(Relation.TRUCE)){
                        changeColor = ChatColor.DARK_PURPLE;
                    } else {
                        changeColor = ChatColor.LIGHT_PURPLE;
                    }

                    String playerUUID = p.getUniqueId().toString();
                    if(Playerlist.getPlayerFile().getBoolean(playerUUID)){
                        p.sendMessage(changeColor + playerName + event.getMessage().replaceAll(pattern.toString(), getConfig().getString("Words." + s)));
                    } else {
                        p.sendMessage(changeColor + playerName + event.getMessage());
                    }
                }

                event.setCancelled(true);
            }
        }



    }
}
