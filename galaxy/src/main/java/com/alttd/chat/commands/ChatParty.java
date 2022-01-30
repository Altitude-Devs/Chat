package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.objects.PartyUser;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChatParty implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if (args.length == 0) {
            helpMessage(sender);
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        if (args.length < 3 || !args[1].matches("[\\w]{3,16}") || !args[2].matches("[\\w]{3,16}")) {
                            invalidMessage(sender, CommandUsage.CREATE);
                            break;
                        }
                        if (PartyManager.getParty(args[1]) != null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>A chat party with this name already exists.</red>"));
                            break;
                        }
                        Party party = Queries.addParty(player.getUniqueId(), args[1], args[2]);
//                        party.addUser(ChatUserManager.getChatUser(player.getUniqueId())); //Removed until we can get nicknames to translate to colors correctly
                        party.addUser(ChatUserManager.getChatUser(player.getUniqueId()), player.getName());
                        PartyManager.addParty(party);
                        sender.sendMessage(Utility.parseMiniMessage("<green>You created a chat party called: '<gold>" +
                                party.getPartyName() + "</gold>' with the password: '<gold>" +
                                party.getPartyPassword() + "</gold>'</green>"));
                        update(player, party.getPartyId());
                    }
                    case "invite" -> {
                        if (args.length < 2) {
                            invalidMessage(sender, CommandUsage.INVITE);
                            break;
                        }
                        Party party = PartyManager.getParty(player.getUniqueId());
                        if (party == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You're not in a chat party.</red>"));
                            break;
                        }
                        if (!party.getOwnerUuid().equals(player.getUniqueId())) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You don't own this chat party.</red>"));
                            break;
                        }
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null || !target.isOnline()) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>The player must be on the same server to receive an invite.</red>"));
                            break;
                        }

                        target.sendMessage(Utility.parseMiniMessage("<click:run_command:'/chatparty join " + party.getPartyName() + " " + party.getPartyPassword() +
                                "'><dark_aqua>You received an invite to join " + party.getPartyName() + " click this message to accept.</dark_aqua></click>"));
                        sender.sendMessage(Utility.parseMiniMessage("<green>You send a chat party invite to " + target.getName() + "!</green>"));
                    }
                    case "join" -> {
                        if (args.length < 3 || !args[1].matches("[\\w]{3,16}") || !args[2].matches("[\\w]{3,16}")) {
                            invalidMessage(sender, CommandUsage.JOIN);
                            break;
                        }

                        Party party = PartyManager.getParty(args[1]);
                        if (party == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>This chat party does not exist.</red>"));
                            break;
                        }
                        if (!party.getPartyPassword().equals(args[2])) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>Invalid password.</red>"));
                            break;
                        }

//                        party.addUser(ChatUserManager.getChatUser(player.getUniqueId())); //Removed until we can get nicknames to translate to colors correctly
                        party.addUser(ChatUserManager.getChatUser(player.getUniqueId()), player.getName());
                        sender.sendMessage(Utility.parseMiniMessage("<green>You joined " + party.getPartyName() + "!</green>"));
                        update(player, party.getPartyId());
                    }
                    case "leave" -> {
                        Party party = PartyManager.getParty(player.getUniqueId());
                        if (party == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You're not in a chat party.</red>"));
                            break;
                        }

                        party.removeUser(player.getUniqueId());
                        if (party.getOwnerUuid().equals(player.getUniqueId())) {
                            if (party.getPartyUsers().size() > 0) {
                                UUID uuid = party.setNewOwner();
                                sender.sendMessage(Utility.parseMiniMessage("<dark_aqua>Since you own this chat party a new party owner will be chosen.<dark_aqua>"));
//                                ChatPlugin.getInstance().getChatHandler().partyMessage(party, player, "<dark_aqua>" +
//                                        player.getName() +
//                                        " left the chat party, the new party owner is " + party.getPartyUser(uuid).getPlayerName());
                            } else {
                                party.delete();
                            }
                        } else {
                            sender.sendMessage(Utility.parseMiniMessage("<green>You have left the chat party!</green>"));
                        }
                        update(player, party.getPartyId());
                    }
                    case "remove" -> {
                        if (args.length < 2) {
                            invalidMessage(sender, CommandUsage.REMOVE);
                            break;
                        }
                        Party party = PartyManager.getParty(player.getUniqueId());
                        if (party == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You're not in a chat party.</red>"));
                            break;
                        }
                        if (!party.getOwnerUuid().equals(player.getUniqueId())) {
                            sender.sendMessage("<red>You don't own this chat party.</red>");
                            break;
                        }
                        OfflinePlayer offlinePlayerIfCached = Bukkit.getOfflinePlayerIfCached((args[1]));
                        if (offlinePlayerIfCached == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>Unable to find this player.</red>"));
                            return;
                        }
                        party.removeUser(ChatUserManager.getChatUser(offlinePlayerIfCached.getUniqueId()));

                        if (offlinePlayerIfCached.getUniqueId().equals(party.getOwnerUuid())) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You can't remove yourself, please leave instead.</red>"));
                            return;
                        }

                        if (offlinePlayerIfCached.isOnline()) {
                            Objects.requireNonNull(offlinePlayerIfCached.getPlayer())
                                    .sendMessage(Utility.parseMiniMessage("<red>You were removed from the '" + party.getPartyName() + "' chat party."));
                        }

                        sender.sendMessage(Utility.parseMiniMessage("<green>You removed " + offlinePlayerIfCached.getName() + " from the chat party!</green>"));
                        update(player, party.getPartyId());
                    }
                    case "info" -> {
                        Party party = PartyManager.getParty(player.getUniqueId());
                        if (party == null) {
                            sender.sendMessage(Utility.parseMiniMessage("<red>You're not in a chat party.</red>"));
                            break;
                        }

                        String message = "<gold><bold>Chat party info</bold>:\n</gold>"
                                + "<green>Name: <dark_aqua><partyname></dark_aqua>\n"
                                + (party.getOwnerUuid().equals(player.getUniqueId()) ? "Password: <dark_aqua><password></dark_aqua>\n" : "")
                                + "Owner: <ownername>\n"
                                + "Members: <members>";
                        List<Component> displayNames = new ArrayList<>();
                        for (PartyUser partyUser : party.getPartyUsers()) {
                            displayNames.add(partyUser.getDisplayName());
                        }

                        List<Template> templates = new ArrayList<>(List.of(
                                Template.template("partyname", party.getPartyName()),
                                Template.template("password", party.getPartyPassword()),
                                Template.template("ownername", party.getPartyUser(party.getOwnerUuid()).getDisplayName()),
                                Template.template("members", Component.join(Component.text(", "), displayNames)),
                                Template.template("message", message)));

                        sender.sendMessage(Utility.parseMiniMessage("<message>", templates));
                    }
                    // TODO: 08/08/2021 add a way to change the password and owner (and name?)
                    default -> {
                        helpMessage(sender);
                    }
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return false;
    }

    private void invalidMessage(CommandSender sender, CommandUsage commandUsage) {
        sender.sendMessage(Utility.parseMiniMessage("<red>Invalid command, proper usage: %command%.</red>".replaceAll("%command%", commandUsage.message)));
    }

    private void helpMessage(CommandSender sender) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<dark_aqua>Chat party commands:</dark_aqua><green>");
        for (CommandUsage commandUsage : CommandUsage.values()) {
            stringBuilder.append("\n- ").append(commandUsage.message);
        }
        stringBuilder.append("</green>");
        sender.sendMessage(Utility.parseMiniMessage(stringBuilder.toString()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> possibleValues = new ArrayList<>();
        String current = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length <= 1) {
            possibleValues.add("create");
            possibleValues.add("invite");
            possibleValues.add("join");
            possibleValues.add("leave");
            possibleValues.add("remove");
            possibleValues.add("info");
            return finalizeSuggest(possibleValues, current);
        }

        switch (args[0].toLowerCase()) {
            case "invite","remove" -> {
                Bukkit.getOnlinePlayers().stream().filter(p -> !p.getName().equals(sender.getName())).forEach(p -> possibleValues.add(p.getName()));
            }
        }

        return finalizeSuggest(possibleValues, current.toLowerCase());
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining)) {
                finalValues.add(str);
            }
        }

        return finalValues;
    }

    private void update(Player player, int partyId) { // FIXME: 08/08/2021 This only updates members btw and should be replaced with per change messages
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("tmppartyupdate");
        out.writeUTF(String.valueOf(partyId));
        player.sendPluginMessage(ChatPlugin.getInstance(), Config.MESSAGECHANNEL, out.toByteArray());
    }

    private enum CommandUsage {
        CREATE("<gold>/chatparty create <#FFE800><hover:show_text:'<gold>A chat party name must be 3-16 characters</gold>'><name></hover> " +
                "<hover:show_text:'<gold>A chat party password must be 3-16 characters\n</gold>" +
                "<red>When choosing a password keep in mind staff can see it and you might need to share it with other players!</red>'><password></#FFE800></hover></gold>"),
        INVITE("<gold>/chatparty invite <username></gold>"),
        JOIN("<gold>/chatparty join <chat party name> <password></gold>"),
        LEAVE("<gold><hover:show_text:'<red>If the chat party owner leaves the server will choose a new chat party owner</red>'>/chatparty leave</hover></gold>"),
        REMOVE("<gold>/chatparty remove <username></gold>"),
        INFO("<gold>/chatparty info</gold>");

        private final String message;

        CommandUsage(String message) {
            this.message = message;
        }
    }
}