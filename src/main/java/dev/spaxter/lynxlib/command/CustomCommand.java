package dev.spaxter.lynxlib.command;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.spaxter.lynxlib.chat.Messenger;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.PermissionAPI;

/**
 * Custom command implementation.
 */
public abstract class CustomCommand {
    private String name;
    private final List<CustomCommand> subCommands;
    private final HashMap<String, ArgumentType<?>> arguments;
    private @Nullable String permission;

    public CustomCommand(String name) {
        this.name = name;
        this.subCommands = Lists.newArrayList();
        this.arguments = Maps.newHashMap();
        this.permission = null;
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
