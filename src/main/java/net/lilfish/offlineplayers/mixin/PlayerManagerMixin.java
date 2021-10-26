package net.lilfish.offlineplayers.mixin;

import com.mojang.authlib.GameProfile;
import net.lilfish.offlineplayers.NPC.NPCClass;
import net.lilfish.offlineplayers.OfflineNetHandlerPlayServer;
import net.lilfish.offlineplayers.OfflinePlayers;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(value=PlayerManager.class, priority = 1500)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private void fixOfflineStartingPos(ServerPlayerEntity serverPlayer, CallbackInfoReturnable<NbtCompound> cir) {
        if (serverPlayer instanceof NPCClass) {
            ((NPCClass) serverPlayer).fixStartingPosition.run();
        }
    }

    @Redirect(method = "onPlayerConnect", at = @At(value = "NEW", target = "net/minecraft/server/network/ServerPlayNetworkHandler"))
    private ServerPlayNetworkHandler replaceOfflineNetworkHandler(MinecraftServer server, ClientConnection clientConnection, ServerPlayerEntity playerIn) {
        boolean isNPC = playerIn instanceof NPCClass;
        if (isNPC) {
            return new OfflineNetHandlerPlayServer(this.server, clientConnection, playerIn);
        } else {
            return new ServerPlayNetworkHandler(this.server, clientConnection, playerIn);
        }
    }

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void initOfflinePlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        boolean isNPC = player instanceof NPCClass;
        if (!isNPC) {
            OfflinePlayers.playerJoined(player);
        }
    }

    @Redirect(method = "createPlayer", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private boolean cancelOfflineWhileLoop(Iterator iterator) {
        return false;
    }

    @Inject(method = "createPlayer", at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
            target = "Ljava/util/Iterator;hasNext()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void newOfflineWhileLoop(GameProfile gameProfile, CallbackInfoReturnable<ServerPlayerEntity> cir, UUID uUID,
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