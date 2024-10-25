package dev.spaxter.lynxlib.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.spaxter.lynxlib.LynxLib;
import dev.spaxter.lynxlib.chat.Messenger;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * Custom command implementation.
 */
public abstract class CustomCommand {
    public static final List<CustomCommand> registeredCommands = new ArrayList<>();

    private String name;
    private String helpText;
    private final List<CustomCommand> subCommands;
    private final LinkedHashMap<String, ArgumentType<?>> arguments;
    private final Set<String> optionalArguments;
    private @Nullable String permission;
    private @Nullable CustomCommand parentCommand;
    private final HashMap<String, List<String>> suggestions;

    public CustomCommand(String name) {
        this.name = name;
        this.helpText = "No description available";
        this.subCommands = new ArrayList<>();
        this.arguments = new LinkedHashMap<>();
        this.suggestions = new HashMap<>();
        this.optionalArguments = new HashSet<>();
        this.permission = null;

        CustomCommand.registeredCommands.add(this);
    }

    public static String generateHelpText(CommandSource source) {
        StringBuilder builder = new StringBuilder();

        for (CustomCommand command : CustomCommand.registeredCommands) {
            if (command.checkPermission(source)) {
                builder.append("&f - &e/");
                if (command.getParentCommand() != null) {
                    builder.append(command.getParentCommand().getName() + " ");
                }
                builder.append(command.getName());
                List<String> optionalArguments = command.getOptionalArguments();
                for (String argument : command.getArguments()) {
                    if (optionalArguments.contains(argument)) {
                        builder.append(" &b[<" + argument + "&b>]");
                    } else {
                        builder.append(" &b<" + argument + "&b>");
                    }
                }
                builder.append("\n&f").append(command.getHelpText()).append("\n\n");
            }
        }

        return builder.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addArgument(String name, ArgumentType<?> argument) {
        this.arguments.put(name, argument);
    }

    public void addSubCommand(CustomCommand command) {
        this.subCommands.add(command);
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public LiteralArgumentBuilder<CommandSource> build() {
        LiteralArgumentBuilder<CommandSource> command = Commands.literal(this.name);
        command.executes(this::executeCommand);

        if (!this.arguments.isEmpty()) {
            this.arguments.forEach((name, type) -> {
                command.then(Commands.argument(name, type).executes(this::executeCommand));
            });
        }

        if (!this.subCommands.isEmpty()) {
            this.subCommands.forEach(subCommand -> command.then(subCommand.build()));
        }

        return command;
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        try {
            if (this.checkPermission(context.getSource())) {
                return this.execute(context);
            } else {
                Messenger.info(context.getSource(), "&cYou do not have permission to use this command.");
                return 1;
            }
        } catch (CommandSyntaxException e) {
            return 0;
        }
    }

    private boolean checkPermission(CommandSource source) {
        if (this.permission == null) {
            return true;
        }

        try {
            ServerPlayerEntity player = source.getPlayerOrException();
            return PermissionAPI.hasPermission(player, this.permission);
        } catch (CommandSyntaxException ignored) {
            // Thrown if command is ran by console, no need to check permissions.
            return true;
        }
    }

    public abstract int execute(CommandContext<CommandSource> context) throws CommandSyntaxException;
}
