package pl.loseq.nemos;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import pl.loseq.nemos.mixin.MinecraftClientAccessor;

public class LoseqnemosClient implements ClientModInitializer {
    private static final int SLOW_DELAY = 2; // ticki miedzy blokami (vanilla = 4)
    private static final int FAST_DELAY = 0; // max szybkosc

    private static boolean enabled = true;
    private static boolean fast = true;

    private static KeyBinding modeKey;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        modeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.loseqnemos.mode", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.loseqnemos"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.loseqnemos.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.loseqnemos"));

        ClientTickEvents.END_CLIENT_TICK.register(LoseqnemosClient::onTick);
    }

    private static void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            client.player.sendMessage(Text.literal("loseqnemos: " + (enabled ? "ON" : "OFF")), true);
        }
        while (modeKey.wasPressed()) {
            fast = !fast;
            client.player.sendMessage(Text.literal("loseqnemos: " + (fast ? "MEGA SZYBKO" : "WOLNO")), true);
        }

        if (!enabled || client.currentScreen != null) return;
        if (!client.options.useKey.isPressed()) return;

        // tylko bloki, bez pajeczyn i dzwigni
        if (!(client.player.getMainHandStack().getItem() instanceof BlockItem blockItem)) return;
        Block held = blockItem.getBlock();
        if (held == Blocks.COBWEB || held == Blocks.LEVER) return;

        // nie przyspieszaj, jesli celujesz w dzwignie
        if (client.crosshairTarget instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK
                && client.world.getBlockState(hit.getBlockPos()).isOf(Blocks.LEVER)) return;

        MinecraftClientAccessor acc = (MinecraftClientAccessor) client;
        int target = fast ? FAST_DELAY : SLOW_DELAY;
        if (acc.getItemUseCooldown() > target) {
            acc.setItemUseCooldown(target);
        }
    }
}
