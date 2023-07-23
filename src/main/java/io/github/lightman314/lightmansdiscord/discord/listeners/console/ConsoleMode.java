package io.github.lightman314.lightmansdiscord.discord.listeners.console;

public enum ConsoleMode {

    LOGS_ONLY(true, false),
    COMMANDS_ONLY(true, false),
    LOGS_AND_COMMANDS(true,true);

    public final boolean showLogs;
    public final boolean acceptCommands;
    ConsoleMode(boolean showLogs, boolean acceptCommands) { this.showLogs = showLogs; this.acceptCommands = acceptCommands; }

}
