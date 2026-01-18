package org.example.plugin;

import au.ellie.hyui.builders.ButtonBuilder;
import au.ellie.hyui.builders.PageBuilder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class ExampleCommand extends AbstractAsyncCommand {

    private final String pluginName;
    private final String pluginVersion;
    private PageBuilder page;
    
    public ExampleCommand(String pluginName, String pluginVersion) {
        super("test", "Prints a test message from the " + pluginName + " plugin.");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        
        // Pre-build the UI as detatched page.
        // For more information on HYUIML and the builders
        // see: https://github.com/Elliesaur/HyUI/blob/main/docs/hyuiml.md
        // and: https://github.com/Elliesaur/HyUI/blob/main/docs/getting-started.md
        page = PageBuilder.detachedPage()
                .withLifetime(CustomPageLifetime.CanDismiss)
                .fromHtml("""
                        <div class="page-overlay">
                           <div class="container" data-hyui-title="Example HyUIML">
                               <button id="exampleBtn">Click Me</button>
                           </div>
                        </div>
                        """);
                // Alternatively, you can add a listener here, but there wouldn't be much useful things to pass to it.
                /*.addEventListener("exampleButton", CustomUIEventBindingType.Activating, 
                        (unused, context) -> {
                        // 
                        });*/
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        var sender = commandContext.sender();
        if (!(sender instanceof Player player)) {
            return CompletableFuture.completedFuture(null);
        }
        player.getWorldMapTracker().tick(0);
        Ref<EntityStore> ref = player.getReference();
        if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            return CompletableFuture.runAsync(() -> {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null) {
                    // Set up our button for this player specifically.
                    page.getById("exampleBtn", ButtonBuilder.class).ifPresent(button -> {
                        button.addEventListener(CustomUIEventBindingType.Activating, event -> {
                            commandContext.sendMessage(Message.raw("Button clicked!"));
                        });
                    });
                    page.open(playerRef, store);
                }
            }, world);
        } else {
            commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            return CompletableFuture.completedFuture(null);
        }
    }
}