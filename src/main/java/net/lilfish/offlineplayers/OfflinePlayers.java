package net.lilfish.offlineplayers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.lilfish.offlineplayers.NPC.EntityPlayerActionPack;
import net.lilfish.offlineplayers.NPC.NPCClass;
import net.lilfish.offlineplayers.interfaces.ServerPlayerEntityInterface;
import net.lilfish.offlineplayers.storage.OfflineDatabase;
import net.lilfish.offlineplayers.storage.models.NPCModel;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

import static net.minecraft.command.CommandSource.suggestMatching;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OfflinePlayers implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "offlineplayer";
    public static final String MOD_NAME = "OfflinePlayer";
    public static OfflineDatabase STORAGE = new OfflineDatabase();

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
//        Init DB
        STORAGE.init();
//        Init command
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated) {
                dispatcher.register(literal("offline")
                        .executes(OfflinePlayers::spawn)
                        .then(argument("action", StringArgumentType.word())
                                .suggests((c, b) -> suggestMatching(new String[]{"place", "attack", "holdAttack", "jump", "dropItem"}, b))
                                .executes(OfflinePlayers::spawn)
                                .then(argument("interval", IntegerArgumentType.integer(0, 1000))
                                        .executes(OfflinePlayers::spawn)
                                        .then(argument("offset", IntegerArgumentType.integer(0, 1000))
                                                .executes(OfflinePlayers::spawn)))));
            }
        });
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    private static int spawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = null;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            OfflinePlayers.LOGGER.fatal("Couldn't get player");
            return 0;
        }

//      Arguments
        String thisAction = "none";
        int thisInterval = 15;
        int thisOffset = -1;

        EntityPlayerActionPack.Action action_action = EntityPlayerActionPack.Action.interval(15);
        EntityPlayerActionPack.ActionType action_type = EntityPlayerActionPack.ActionType.ATTACK;

//      Try and get arguments
        try {
            thisAction = StringArgumentType.getString(context, "action");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            thisInterval = IntegerArgumentType.getInteger(context, "interval");
            action_action = EntityPlayerActionPack.Action.interval(thisInterval);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            thisOffset = IntegerArgumentType.getInteger(context, "offset");
            if (thisOffset != -1)
                action_action = EntityPlayerActionPack.Action.interval(thisInterval, thisOffset);
        } catch (IllegalArgumentException ignored) {
        }

//      Create player
        NPCClass npc = NPCClass.createFake(
                player, GameMode.SURVIVAL, false);

//      Check if NPC should have action, if so, add it.
        if (thisAction != "none") {
            switch (thisAction) {
                case "attack":
                    action_type = EntityPlayerActionPack.ActionType.ATTACK;
                    break;
                case "holdAttack":
                    action_type = EntityPlayerActionPack.ActionType.ATTACK;
                    action_action = EntityPlayerActionPack.Action.continuous();
                    break;
                case "place":
                    action_type = EntityPlayerActionPack.ActionType.USE;
                    break;
                case "jump":
                    action_type = EntityPlayerActionPack.ActionType.JUMP;
                    break;
                case "dropItem":
                    action_type = EntityPlayerActionPack.ActionType.DROP_ITEM;
                    break;
            }
            EntityPlayerActionPack.ActionType finalType = action_type;
            EntityPlayerActionPack.Action finalAction = action_action;

            manipulate(npc, ap -> ap.start(finalType, finalAction));
        }

//      Check if NPC was created
        if (npc == null) {
            OfflinePlayers.LOGGER.fatal("Offline player not generated");
            return 0;
        }
        player.networkHandler.disconnect(Text.of("Offline player generated"));

        return 1;
    }

    private static int manipulate(ServerPlayerEntity player, Consumer<EntityPlayerActionPack> action) {
        action.accept(((ServerPlayerEntityInterface) player).getActionPack());
        return 1;
    }

    public static void playerJoined(ServerPlayerEntity player) {
        NPCModel npc = STORAGE.findNPCByPlayer(player.getUuid());
        if(npc != null){
            boolean correct = false;
            if(npc.isDead()){
                correct = handleDeadNPC(player, npc);
            } else if(!npc.isDead()){
                correct = handleAliveNPC(player, npc);
            }
            // Remove NPC from DataBase
            if(correct)
                STORAGE.removeNPC(player.getUuid());
        }

    }

    private static boolean handleAliveNPC(ServerPlayerEntity player, NPCModel npc){
        ServerPlayerEntity npcPlayer = player.server.getPlayerManager().getPlayer(npc.getNpc_id());
        if(npcPlayer != null) {
//          Set pos
            player.refreshPositionAfterTeleport(npcPlayer.getX(), npcPlayer.getY(), npcPlayer.getZ());
//          Copy inv.
            PlayerInventory npcInv = npcPlayer.getInventory();
            setInventory(player, npcInv);
//          Copy XP
            int points = Math.round(npcPlayer.getNextLevelExperience()/1*npcPlayer.experienceProgress);
            player.setExperienceLevel(npcPlayer.experienceLevel);
            player.setExperiencePoints(points);
//          Set status effects
            for (StatusEffectInstance statusEffect : npcPlayer.getStatusEffects())
            {
                player.addStatusEffect(statusEffect);
            }
            npcPlayer.kill();
        }
        return true;
    }

    private static boolean handleDeadNPC(ServerPlayerEntity player, NPCModel npc){
//      Set pos
//        player.requestTeleport(npc.getX(), npc.getY(), npc.getZ());
        player.resetPosition();

        player.refreshPositionAfterTeleport(npc.getX(), npc.getY(), npc.getZ());
//      Copy inv.
        PlayerInventory npcInv = STORAGE.getNPCInventory(npc);

        setInventory(player, npcInv);
////      Copy XP
        player.setExperiencePoints(npc.getXPpoints());
        player.setExperienceLevel(npc.getXPlevel());
////      Kill player
        player.setHealth(0);
        player.server.getPlayerManager().broadcastChatMessage(Text.of(player.getDisplayName().asString() + " died: " + npc.getDeathMessage()), MessageType.CHAT, player.getUuid());

        return true;
    }

    private static void setInventory(ServerPlayerEntity player, PlayerInventory npcInv){
        player.getInventory().main.clear();
        for (int i = 0; i < npcInv.main.size(); i++) {
            player.getInventory().main.set(i, npcInv.main.get(i));
        }
        player.getInventory().armor.clear();
        for (int i = 0; i < npcInv.armor.size(); i++) {
            player.getInventory().armor.set(i, npcInv.armor.get(i));
        }
        player.getInventory().offHand.clear();
        for (int i = 0; i < npcInv.offHand.size(); i++) {
            player.getInventory().offHand.set(i, npcInv.offHand.get(i));
        }
    }
}