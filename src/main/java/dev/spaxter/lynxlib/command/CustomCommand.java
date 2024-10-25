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

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getHelpText() {
        return this.helpText;
    }

    public void addArgument(String name, ArgumentType<?> argument) {
        this.arguments.put(name, argument);
        this.suggestions.put(name, Lists.newArrayList());
    }

    public void addArgument(String name, ArgumentType<?> argument, boolean optional) {
        if (optional) {
            this.optionalArguments.add(name);
        }
        this.addArgument(name, argument);
    }

    public List<String> getArguments() {
        return new ArrayList<>(this.arguments.keySet());
    }

    public List<String> getOptionalArguments() {
        return new ArrayList<>(this.optionalArguments);
    }

    public void addAutoCompleteSuggestions(String argument, String... suggestions) {
        this.suggestions.get(argument).addAll(Arrays.asList(suggestions));
    }

    public void addAutoCompleteSuggestion(String argument, String suggestion) {
        this.suggestions.get(argument).add(suggestion);
    }

    public SuggestionProvider<CommandSource> getSuggestionProvider(String argument) {
        return (context, builder) -> getSuggestionFuture(argument, builder);
    }

    public CompletableFuture<Suggestions> getSuggestionFuture(String argument, SuggestionsBuilder builder) {
        this.suggestions.getOrDefault(argument, Collections.emptyList()).forEach(builder::suggest);
        return builder.buildFuture();
    }

    public void addSubCommand(CustomCommand command) {
        this.subCommands.add(command);
        command.setParentCommand(this);
    }

    public void setParentCommand(CustomCommand command) {
        this.parentCommand = command;
    }

    public CustomCommand getParentCommand() {
        return this.parentCommand;
    }

    public void setPermission(String permission, String description) {
        this.permission = permission;
        PermissionAPI.registerNode(permission, DefaultPermissionLevel.ALL, description);
    }

    public String getPermission() {
        return this.permission;
    }

    public LiteralArgumentBuilder<CommandSource> build() {
        LiteralArgumentBuilder<CommandSource> command = Commands.literal(this.name);

        if (!this.arguments.isEmpty()) {
            // Get a list of argument entries
            List<Map.Entry<String, ArgumentType<?>>> argumentEntries = Lists.newArrayList(this.arguments.entrySet());

            // Create the argument chain
            ArgumentBuilder<CommandSource, ?> argumentChain = buildArgumentChain(argumentEntries, 0);

            // Add the argument chain to the main command
            if (argumentChain != null) {
                command.then(argumentChain);
            }
        }

        // Set the base command to execute if no arguments are provided.
        command = command.executes(this::executeCommand).requires(source -> this.checkPermission(source));

        // Add subcommands if present
        if (!this.subCommands.isEmpty()) {
            for (CustomCommand subCommand : this.subCommands) {
                command.then(subCommand.build().requires(source -> subCommand.checkPermission(source)));
            }
        }

        return command;
    }

    private ArgumentBuilder<CommandSource, ?> buildArgumentChain(List<Map.Entry<String, ArgumentType<?>>> arguments,
            int index) {
        if (index >= arguments.size()) {
            return null;
        }

        Map.Entry<String, ArgumentType<?>> entry = arguments.get(index);
        String argName = entry.getKey();
        ArgumentType<?> argType = entry.getValue();

        RequiredArgumentBuilder<CommandSource, ?> argument = Commands.argument(argName, argType)
                .executes(this::executeCommand);

        // Add suggestions if available
        if (!this.suggestions.getOrDefault(argName, Collections.emptyList()).isEmpty()) {
            argument.suggests(getSuggestionProvider(argName));
        }

        // Recursively add the next argument in the chain
        ArgumentBuilder<CommandSource, ?> nextArgument = buildArgumentChain(arguments, index + 1);
        if (nextArgument != null) {
            argument.then(nextArgument);
        }

        // If this argument is optional, ensure that the command can execute without it
        if (this.optionalArguments.contains(argName)) {
            argument.executes(this::executeCommand);
        }

        return argument;
    }

    private int executeCommand(CommandContext<CommandSource> context) {
        try {
            return this.execute(context);
        } catch (Exception e) {
            LynxLib.logger.error(e);
            Messenger.info(context.getSource(), "&c" + e.getLocalizedMessage());
            return 0;
        }
    }

    public boolean checkPermission(CommandSource source) {
        if (this.permission == null) {
            return true;
        }

        try {
            ServerPlayerEntity player = source.getPlayerOrException();
            boolean hasPermission = PermissionAPI.hasPermission(player, this.permission);
            return hasPermission;
        } catch (CommandSyntaxException e) {
            // Thrown if command was sent by console
            return true;
        }
    }

    public abstract int execute(CommandContext<CommandSource> context) throws CommandSyntaxException;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (this.parentCommand == null) {
            LiteralArgumentBuilder<CommandSource> command = this.build();
            event.getDispatcher().register(command);
        }
    }
}
