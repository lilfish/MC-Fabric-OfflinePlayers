package net.lilfish.offlineplayers.mixin;

import net.lilfish.offlineplayers.OfflinePlayers;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerCoreMixin {
    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void serverLoaded(CallbackInfo ci)
    {
        OfflinePlayers.onServerLoaded((MinecraftServer) (Object) this);
    }
//    @Inject(method = "shutdown", at = @At("HEAD"))
//    private void serverClosed(CallbackInfo ci)
//    {
//        OfflinePlayers.onServerClosed((MinecraftServer) (Object) this);
//    }
//
//    @Inject(method = "shutdown", at = @At("TAIL"))
//    private void serverDoneClosed(CallbackInfo ci)
//    {
//        OfflinePlayers.onServerDoneClosing((MinecraftServer) (Object) this);
//    }

}
