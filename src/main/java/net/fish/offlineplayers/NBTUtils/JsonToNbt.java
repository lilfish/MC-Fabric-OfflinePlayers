package net.fish.offlineplayers.NBTUtils;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import net.minecraft.nbt.*;

import java.util.Map;

/**
 * Converter utility for converting JSON (GSON) to NBT.
 * COPIED/EDITED FROM: https://github.com/KSashaDF/JSON-to-NBT-Converter/blob/master/converter/NbtToJson.java
 */
public final class JsonToNbt {

    /**
     * Converts a JSON element to an NBT tag.
     *
     * @param jsonElement Element to convert.
     * @return The NBT tag equivalent. (imperfect in certain cases)
     */

    public static NbtElement toNbt(JsonElement jsonElement) {

        // JSON Primitive
        if (jsonElement instanceof JsonPrimitive) {
            JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonElement;

            if (jsonPrimitive.isBoolean()) {
                boolean value = jsonPrimitive.getAsBoolean();

                if (value) {
                    return NbtByte.of(true);
                } else {
                    return NbtByte.of(false);
                }

            } else if (jsonPrimitive.isNumber()) {
                Number number = jsonPrimitive.getAsNumber();

                if (number instanceof Byte) {
                    return NbtByte.of(number.byteValue());
                } else if (number instanceof Short) {
                    return NbtShort.of(number.shortValue());
                } else if (number instanceof Integer || number instanceof LazilyParsedNumber) {
                    return NbtInt.of(number.intValue());
                } else if (number instanceof Long) {
                    return NbtLong.of(number.longValue());
                } else if (number instanceof Float) {
                    return NbtFloat.of(number.floatValue());
                } else if (number instanceof Double) {
                    return NbtDouble.of(number.doubleValue());
                }

            } else if (jsonPrimitive.isString()) {
                return NbtString.of(jsonPrimitive.getAsString());
            }

            // JSON Array
        } else if (jsonElement instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) jsonElement;
            NbtList nbtList = new NbtList();

            for (JsonElement element : jsonArray) {
                nbtList.add(toNbt(element));
            }

            return nbtList;

            // JSON Object
        } else if (jsonElement instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) jsonElement;
            NbtCompound nbtCompound = new NbtCompound();

            for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
                nbtCompound.put(jsonEntry.getKey(), toNbt(jsonEntry.getValue()));
            }

            return nbtCompound;

            // Null - Not fully supported
        } else if (jsonElement instanceof JsonNull) {
            return new NbtCompound();
        }
        System.out.println("JsonToNBT: Something went wrong while parsing!         returning new NbtCompound()");
        return new NbtCompound();
//        throw new AssertionError();
    }
}