package net.fish.offlineplayers.mixin;

import com.mojang.authlib.GameProfile;
import net.fish.offlineplayers.NPC.NPCClass;
import net.fish.offlineplayers.OfflineNetHandlerPlayServer;
import net.fish.offlineplayers.OfflinePlayers;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixStartingPos(ServerPlayerEntity serverPlayer, CallbackInfoReturnable<NbtCompound> cir) {
        if (serverPlayer instanceof NPCClass) {
            ((NPCClass) serverPlayer).fixStartingPosition.run();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/server/network/ServerPlayNetworkHandler"))
    private ServerPlayNetworkHandler replaceNetworkHandler(MinecraftServer server, ClientConnection clientConnection, ServerPlayerEntity playerIn) {
        boolean isServerPlayerEntity = playerIn instanceof NPCClass;
        if (isServerPlayerEntity) {
            return new OfflineNetHandlerPlayServer(this.server, clientConnection, playerIn);
        } else {
            System.out.println("On player connect");
            return new ServerPlayNetworkHandler(this.server, clientConnection, playerIn);
        }
    }

    @Redirect(method = "createPlayer", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private boolean cancelWhileLoop(Iterator iterator) {
        return false;
    }

    @Inject(method = "createPlayer", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Ljava/util/Iterator;hasNext()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newWhileLoop(GameProfile gameProfile, CallbackInfoReturnable<ServerPlayerEntity> cir, UUID uUID,
                              List list, Iterator ServerPlayers) {
        while (ServerPlayers.hasNext()) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) ServerPlayers.next();
            if (serverPlayer instanceof NPCClass) {
                ((NPCClass) serverPlayer).kill(new TranslatableText("multiplayer.disconnect.duplicate_login"));
                continue;
            }
            serverPlayer.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.duplicate_login"));
        }
    }

}