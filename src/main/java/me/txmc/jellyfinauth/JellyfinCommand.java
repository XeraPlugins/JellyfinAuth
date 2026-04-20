package me.txmc.jellyfinauth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JellyfinCommand implements CommandExecutor {
    private final JellyfinAuth plugin;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

    public JellyfinCommand(JellyfinAuth plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("jellyfinauth.use")) {
            sender.sendMessage(serializer.deserialize("§cYou don't have permission to use this command."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(serializer.deserialize("§cThis command can only be used by players."));
            return true;
        }

        handleJellyfinAccount(sender, player);
        return true;
    }

    private void handleJellyfinAccount(CommandSender sender, Player player) {
        String username = player.getName();
        JellyfinAPI api = plugin.getApi();

        sender.sendMessage(serializer.deserialize("§aCreating your Jellyfin account..."));

        try {
            if (api.userExists(username)) {
                sender.sendMessage(serializer.deserialize("§eYou already have a Jellyfin account!"));

                String url = plugin.getConfig().getString("jellyfin.url");
                TextComponent urlComponent = Component.text()
                        .append(Component.text("§6Visit "))
                        .append(Component.text(url)
                                .color(TextColor.fromHexString("#55FFFF"))
                                .decorate(TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl(url))
                                .hoverEvent(HoverEvent.showText(Component.text("Click to open §e" + url))))
                        .append(Component.text(" §6to login"))
                        .build();
                sender.sendMessage(urlComponent);
                return;
            }

            String userId = api.createUser(username);
            String password = api.generatePassword();
            api.setPassword(userId, password);

            String url = plugin.getConfig().getString("jellyfin.url");

            sender.sendMessage(serializer.deserialize("§aAccount created successfully!"));
            sender.sendMessage(serializer.deserialize("§6================================"));
            sender.sendMessage(serializer.deserialize("§3Username: §e" + username));

            TextComponent passwordComponent = Component.text()
                    .append(Component.text("§3Password: "))
                    .append(Component.text("[Click to Copy] ")
                            .color(TextColor.fromHexString("#55FFFF"))
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.copyToClipboard(password))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to copy password: §e" + password))))
                    .append(Component.text("§e(" + password + ")")
                            .color(TextColor.fromHexString("e")))
                    .build();
            sender.sendMessage(passwordComponent);

            sender.sendMessage(serializer.deserialize("§6================================"));

            TextComponent loginComponent = Component.text()
                    .append(Component.text("§aLogin at: "))
                    .append(Component.text(url)
                            .color(TextColor.fromHexString("#55FFFF"))
                            .decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.openUrl(url))
                            .hoverEvent(HoverEvent.showText(Component.text("Click to open §e" + url))))
                    .build();
            sender.sendMessage(loginComponent);

            sender.sendMessage(serializer.deserialize("§eYou can change your password after logging in!"));
        } catch (IOException e) {
            sender.sendMessage(serializer.deserialize("§cError creating account: " + e.getMessage()));
        }
    }
}