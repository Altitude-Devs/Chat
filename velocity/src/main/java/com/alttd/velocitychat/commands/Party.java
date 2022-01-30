package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.velocitychat.commands.partysubcommands.Create;
import com.alttd.velocitychat.commands.partysubcommands.Invite;
import com.alttd.velocitychat.commands.partysubcommands.Join;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Party implements SimpleCommand {
    private final List<SubCommand> subCommands;
    private final MiniMessage miniMessage;

    public Party() {
        subCommands = Arrays.asList(new Create(),
                new Invite(),
                new Join());
        miniMessage = MiniMessage.get();
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (args.length < 1) {
            if (!source.hasPermission("party.use"))
                source.sendMessage(miniMessage.parse(Config.NO_PERMISSION));
            else if (source instanceof Player)
                source.sendMessage(miniMessage.parse(Config.PARTY_HELP));
            else
                source.sendMessage(miniMessage.parse(Config.NO_CONSOLE));
            return;
        }

        subCommands.stream()
                .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                .findFirst()
                .ifPresentOrElse(subCommand -> subCommand.execute(args, source),
                        () -> source.sendMessage(getHelpMessage(source)));
    }

    @Override
    public List<String> suggest(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggest = new ArrayList<>();

        if (args.length == 0) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else if (args.length <= 1) {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().startsWith(args[0].toLowerCase()))
                    .forEach(subCommand -> suggest.add(subCommand.getName()));
        } else {
            subCommands.stream()
                    .filter(subCommand -> invocation.source().hasPermission(subCommand.getPermission()))
                    .filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .ifPresent(subCommand -> suggest.addAll(subCommand.suggest(args)));
        }

        if (args.length == 0)
            return suggest;
        else
            return finalizeSuggest(suggest, args[args.length - 1]);
    }

    public List<String> finalizeSuggest(List<String> possibleValues, String remaining) {
        List<String> finalValues = new ArrayList<>();

        for (String str : possibleValues) {
            if (str.toLowerCase().startsWith(remaining)) {
                finalValues.add(StringArgumentType.escapeIfRequired(str));
            }
        }

        return finalValues;
    }

    private Component getHelpMessage(CommandSource source) {
        StringBuilder stringBuilder = new StringBuilder();

        subCommands.stream()
                .filter(subCommand -> source.hasPermission(subCommand.getPermission()))
                .forEach(subCommand -> stringBuilder.append(subCommand.getHelpMessage()).append("\n"));
        if (stringBuilder.length() != 0)
            stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "");

        return miniMessage.parse(Config.PARTY_HELP, Template.of("commands", stringBuilder.toString()));
    }
}