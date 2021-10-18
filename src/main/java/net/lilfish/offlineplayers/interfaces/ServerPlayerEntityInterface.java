package net.lilfish.offlineplayers.interfaces;

import net.lilfish.offlineplayers.NPC.EntityPlayerActionPack;

public interface ServerPlayerEntityInterface
{
    EntityPlayerActionPack getActionPack();
    void invalidateEntityObjectReference();
    boolean isInvalidEntityObject();
    String getLanguage();
}