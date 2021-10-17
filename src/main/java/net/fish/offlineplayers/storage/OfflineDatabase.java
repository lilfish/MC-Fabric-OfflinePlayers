package net.fish.offlineplayers.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;
import io.jsondb.query.Update;
import net.fish.offlineplayers.NBTUtils.JsonToNbt;
import net.fish.offlineplayers.NPC.NPCClass;
import net.fish.offlineplayers.storage.models.NPCModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class OfflineDatabase {
    //Actual location on disk for database files, process should have read-write permissions to this folder
    String dbFilesLocation = "./offlineplayers/";

    //Java package name where POJO's are present
    String baseScanPackage = "net.fish.offlineplayers.storage.models";

    public Items items = new Items();

    //Optionally a Cipher object if you need Encryption

    JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage);

    public void init() {
        try {
            jsonDBTemplate.createCollection(NPCModel.class);
        } catch (InvalidJsonDbApiUsageException ignored) {
        }
    }

    public List<NPCModel> getAllNPC() {
        return jsonDBTemplate.findAll(NPCModel.class);
    }

    public void addNPC(UUID player_id, UUID npc_id) {
        if (jsonDBTemplate.findById(player_id, NPCModel.class) != null) {
            this.removeNPC(player_id);
        }
        NPCModel instance = new NPCModel();
        instance.setId(player_id);
        instance.setNpc_id(npc_id);
        instance.setDead(false);
        jsonDBTemplate.insert(instance);
    }

    public void removeNPC(UUID uuid) {
        NPCModel instance = new NPCModel();
        instance.setId(uuid);
        jsonDBTemplate.remove(instance, NPCModel.class);
    }

    public void saveDeathNPC(NPCClass npc, Text deathMessage) {
//      Seat Dead
        Update update = Update.update("dead", true);
//      Set inventory
        Collection<NPCModel.NPCItem> inventoryItemCollection = new ArrayList<NPCModel.NPCItem>();
        for (ItemStack npcItem : npc.getInventory().main) {
            NPCModel.NPCItem newItem = new NPCModel.NPCItem();
            newItem.itemname = npcItem.getItem().toString().toUpperCase();
            newItem.count = npcItem.getCount();
            if (npcItem.hasNbt())
                newItem.nbttag = npcItem.getNbt().asString();
            inventoryItemCollection.add(newItem);
        }

        update.set("inventory", inventoryItemCollection);
//      Set CHEST
        ItemStack npcChest = npc.getEquippedStack(EquipmentSlot.CHEST);
        NPCModel.NPCItem chestItem = new NPCModel.NPCItem();
        chestItem.itemname = npcChest.getName().getString();
        chestItem.count = npcChest.getCount();
        if (npcChest.hasNbt())
            chestItem.nbttag = npcChest.getNbt().asString();
        update.set("armor_CHEST", chestItem);

//      Set LEGS
        ItemStack npcLegs = npc.getEquippedStack(EquipmentSlot.LEGS);
        NPCModel.NPCItem legItem = new NPCModel.NPCItem();
        legItem.itemname = npcLegs.getName().getString();
        legItem.count = npcLegs.getCount();
        if (npcLegs.hasNbt())
            legItem.nbttag = npcLegs.getNbt().asString();
        update.set("armor_LEGS", legItem);

//      Set HEAD
        ItemStack npcHead = npc.getEquippedStack(EquipmentSlot.HEAD);
        NPCModel.NPCItem headItem = new NPCModel.NPCItem();
        headItem.itemname = npcHead.getName().getString();
        headItem.count = npcHead.getCount();
        if (npcHead.hasNbt())
            headItem.nbttag = npcHead.getNbt().asString();
        update.set("armor_HEAD", headItem);

//      Set FEET
        ItemStack npcFeet = npc.getEquippedStack(EquipmentSlot.FEET);
        NPCModel.NPCItem feetItem = new NPCModel.NPCItem();
        feetItem.itemname = npcFeet.getName().getString();
        feetItem.count = npcFeet.getCount();
        if (npcFeet.hasNbt())
            feetItem.nbttag = npcFeet.getNbt().asString();
        update.set("armor_FEET", feetItem);

//      Set offhand
        ItemStack npcOffhand = npc.getEquippedStack(EquipmentSlot.OFFHAND);
        NPCModel.NPCItem offhandItem = new NPCModel.NPCItem();
        offhandItem.itemname = npcOffhand.getName().getString();
        offhandItem.count = npcOffhand.getCount();
        if (npcOffhand.hasNbt())
            offhandItem.nbttag = npcOffhand.getNbt().asString();
        update.set("offhand", offhandItem);

//      Set XP
        update.set("XPlevel", npc.experienceLevel);
        update.set("XPpoints", npc.getNextLevelExperience() / 1 * npc.experienceProgress);
//      Set XYZ
        update.set("x", npc.getX());
        update.set("y", npc.getY());
        update.set("z", npc.getZ());
        update.set("deathMessage", deathMessage.getString());

        String jxQuery = String.format("/.[npc_id='%s']", npc.getUuid());
        jsonDBTemplate.findAndModify(jxQuery, update, "npcdata");
    }

    public Inventory getNPCInventoryNPC(UUID npc_id)
    {
        String jxQuery = String.format("/.[npc_id='%s']", npc_id);
        NPCModel npc = jsonDBTemplate.findOne(jxQuery, NPCModel.class);
        return getNPCInventory(npc);
    }
    public Inventory getNPCInventoryPlayer(UUID player_id)
    {
        NPCModel npc = jsonDBTemplate.findById(player_id, NPCModel.class);
        return getNPCInventory(npc);
    }
    public Inventory getNPCInventory(NPCModel npc)
    {

        PlayerInventory inv = new PlayerInventory(null);
        ArrayList<NPCModel.NPCItem> inventory = npc.getInventory();
        for (int i = 0; i < inv.main.size(); i++) {
            NPCModel.NPCItem npcItem = inventory.get(i);
            inv.main.set(i, this.getItemStack(npcItem));
        }
        //set offhand
        NPCModel.NPCItem offHand = npc.getOffhand();
        inv.offHand.set(0, this.getItemStack(offHand));
        //set armor
        NPCModel.NPCItem feet_item = npc.getArmor_FEET();
        NPCModel.NPCItem legs_item = npc.getArmor_LEGS();
        NPCModel.NPCItem chest_item = npc.getArmor_CHEST();
        NPCModel.NPCItem head_item = npc.getArmor_HEAD();
        inv.armor.set(0, this.getItemStack(feet_item));
        inv.armor.set(1, this.getItemStack(legs_item));
        inv.armor.set(2, this.getItemStack(chest_item));
        inv.armor.set(3, this.getItemStack(head_item));

        return inv;
    }

    public ItemStack getItemStack(NPCModel.NPCItem npcItem){
        try {
            String itemName = npcItem.itemname.toUpperCase();
            Field itemField = items.getClass().getDeclaredField(itemName);
            Item realItem = (Item) itemField.get(this);
            ItemStack itemStack = new ItemStack(realItem, npcItem.count);
            if(npcItem.nbttag != null){
                JsonElement jsonElement = JsonParser.parseString(npcItem.nbttag);
                NbtElement tags = JsonToNbt.toNbt(jsonElement);
                System.out.println(tags.toString());
                itemStack.setNbt((NbtCompound) tags);
            }
            return itemStack;
        } catch (Exception ignore) {
            System.out.println("I: "+ignore);
        }
        return new ItemStack(Items.AIR, 1);
    }
}
