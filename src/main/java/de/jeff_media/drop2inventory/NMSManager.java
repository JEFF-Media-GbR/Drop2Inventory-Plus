package de.jeff_media.drop2inventory;

import de.jeff_media.jefflib.ReflUtil;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftHanging;

import java.lang.reflect.Method;

public class NMSManager {

    public static void applyBlockPhysics(Block block) {
        World world = block.getWorld();
        try {
            Object worldServer = world.getClass().getMethod("getHandle").invoke(world);
            Class blockPositionClass = ReflUtil.getNMSClass("BlockPosition");
            Method getTypeMethod = worldServer.getClass().getMethod("getType",blockPositionClass);
            Object blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class).newInstance(block.getX(), block.getY(), block.getZ());
            Object iBlockData = getTypeMethod.invoke(worldServer,blockPosition);
            Method getBlockMethod = iBlockData.getClass().getMethod("getBlock");
            Object nmsBlock = getBlockMethod.invoke(iBlockData);
            Class blockClass = ReflUtil.getNMSClass("Block");
            Method applyPhysicsMethod = worldServer.getClass().getMethod("applyPhysics", blockPositionClass, blockClass);
            applyPhysicsMethod.invoke(worldServer,blockPosition, nmsBlock);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
