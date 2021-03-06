package net.lilfish.offlineplayers.storage.models;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.ArrayList;
import java.util.UUID;

/**
 * NPC DB model
 *
 * @version 1.0 28-Sep-2016
 */
@Document(collection = "npcdata", schemaVersion = "1.0")
public class NPCModel {

    public static class NPCItem {
        public int count;
        public String itemname;
        public String nbttag;
    }

    @Id
    private UUID id;
    private UUID npc_id;
    private boolean dead;
    private ArrayList<NPCItem> inventory;
    private NPCItem offhand;
    private NPCItem armor_CHEST;
    private NPCItem armor_LEGS;
    private NPCItem armor_FEET;
    private NPCItem armor_HEAD;
    private int XPlevel;
    private int XPpoints;
    private double x;
    private double y;
    private double z;
    private String deathMessage;

    public ArrayList<NPCItem> getInventory() {
        return inventory;
    }

    public void setInventory(ArrayList<NPCItem> inventory) {
        this.inventory = inventory;
    }

    public NPCItem getOffhand() {
        return offhand;
    }

    public void setOffhand(NPCItem offhand) { this.offhand = offhand; }

    public NPCItem getArmor_CHEST() {
        return armor_CHEST;
    }

    public void setArmor_CHEST(NPCItem armor_CHEST) {
        this.armor_CHEST = armor_CHEST;
    }

    public NPCItem getArmor_LEGS() {
        return armor_LEGS;
    }

    public void setArmor_LEGS(NPCItem armor_LEGS) {
        this.armor_LEGS = armor_LEGS;
    }

    public NPCItem getArmor_FEET() {
        return armor_FEET;
    }

    public void setArmor_FEET(NPCItem armor_FEET) {
        this.armor_FEET = armor_FEET;
    }

    public NPCItem getArmor_HEAD() {
        return armor_HEAD;
    }

    public void setArmor_HEAD(NPCItem armor_HEAD) {
        this.armor_HEAD = armor_HEAD;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public void setDeathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNpc_id() {
        return npc_id;
    }

    public void setNpc_id(UUID npc_id) {
        this.npc_id = npc_id;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public int getXPlevel() {
        return XPlevel;
    }

    public void setXPlevel(int XPlevel) {
        this.XPlevel = XPlevel;
    }

    public int getXPpoints() {
        return XPpoints;
    }

    public void setXPpoints(int XPpoints) {
        this.XPpoints = XPpoints;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }


}