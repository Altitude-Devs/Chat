package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.velocitychat.commands.PartyCommand;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class Help implements SubCommand {

    private final PartyCommand partyCommand;

    public Help(PartyCommand partyCommand)
    {
        this.partyCommand = partyCommand;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        source.sendMessage(partyCommand.getHelpMessage(source));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_HELP;
    }
}
