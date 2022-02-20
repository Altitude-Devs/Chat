package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.partysubcommands.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PartyCommand implements SimpleCommand {
    private final List<SubCommand> subCommands;

    public PartyCommand() {
        subCommands = Arrays.asList(
                new Help(this),
                new Create(),
                new Disband(),
                new Info(),
                new Invite(),
                new Join(),
                new Leave(),
                new Name(),
                new Owner(),
                new Password(),
                new Remove());

    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (args.length < 1) {
            if (!source.hasPermission("party.use"))
                source.sendMessage(Utility.parseMiniMessage(Config.NO_PERMISSION));
            else if (source instanceof Player)
                source.sendMessage(getHelpMessage(source));
            else
                source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }

        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .ifPresentOrElse(subCommand -> {
                    if (source.hasPermission(subCommand.getPermission()))
                        subCommand.execute(args, source);
                    else
                        source.sendMessage(Utility.parseMiniMessage(Config.NO_PERMISSION));
                    }, () -> source.sendMessage(getHelpMessage(source)));
    }

    @Override
    public List<String> suggest(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggest = new ArrayList<>();

        if (!invocation.source().hasPermission("party.use"))
            return suggest;
        else if (args.length == 0) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else if (args.length == 1) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().startsWith(args[0].toLowerCase()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .ifPresent(subCommand -> suggest.addAll(subCommand.suggest(args, invocation.source())));
        }

        if (args.length == 0)
            return suggest;
        else
            return finalizeSuggest(suggest, args[args.length - 1]);
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining.toLowerCase())) {
                finalValues.add(StringArgumentType.escapeIfRequired(str));
            }
        }

        return finalValues;
    }

    public Component getHelpMessage(CommandSource source) {
        StringBuilder stringBuilder = new StringBuilder();

        subCommands.stream()
                .filter(subCommand -> source.hasPermission(subCommand.getPermission()))
                .forEach(subCommand -> stringBuilder.append(subCommand.getHelpMessage()).append("\n"));
        if (source.hasPermission("command.chat.p"))
            stringBuilder.append(Config.PARTY_HELP_CHAT).append("\n");
        if (stringBuilder.length() != 0)
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");

        return Utility.parseMiniMessage(Config.PARTY_HELP_WRAPPER,
                Placeholder.component("commands", Utility.parseMiniMessage(stringBuilder.toString()))
        );
    }
}