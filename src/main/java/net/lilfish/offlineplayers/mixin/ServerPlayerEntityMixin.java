package net.lilfish.offlineplayers.mixin;


import com.mojang.authlib.GameProfile;
import net.lilfish.offlineplayers.NPC.EntityPlayerActionPack;
import net.lilfish.offlineplayers.OfflinePlayers;
import net.lilfish.offlineplayers.interfaces.ServerPlayerEntityInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements ServerPlayerEntityInterface {
    @Unique
    public EntityPlayerActionPack actionPack;

    @Override
    public EntityPlayerActionPack getActionPack() {
        return actionPack;
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onServerPlayerEntityConstructor(
            MinecraftServer minecraftServer_1,
            ServerWorld serverWorld_1,
            GameProfile gameProfile_1,
            CallbackInfo ci) {
        this.actionPack = new EntityPlayerActionPack((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onTick(CallbackInfo ci) {
        try {
            actionPack.onUpdate();
        } catch (StackOverflowError soe) {
            OfflinePlayers.log(Level.FATAL, "Caused stack overflow when performing player action");
        } catch (Throwable exc) {
            OfflinePlayers.log(Level.FATAL, "Error executing player tasks");
        }
    }
}
