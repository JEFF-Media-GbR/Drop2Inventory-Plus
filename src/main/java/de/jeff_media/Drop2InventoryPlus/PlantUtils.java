package de.jeff_media.Drop2InventoryPlus;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.LinkedList;

public class PlantUtils {

    final static String[] plantNames = {
            "CACTUS",
            "SUGAR_CANE",
            "KELP_PLANT",
            "BAMBOO"
    };

    final LinkedList<Material> plants;

    final static BlockFace[] chorusBlockFaces = {
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
    };

    public PlantUtils() {
        plants = new LinkedList<>();
        for(String s : plantNames) {
            if(Material.getMaterial(s) != null) {
                plants.add(Material.getMaterial(s));
            }
        }
    }

    public static boolean isChorusTree(Block block) {
        return block.getType() == Material.CHORUS_PLANT;
    }

    static boolean isPartOfChorusTree(Block block) {
        Material mat = block.getType();
        return mat == Material.CHORUS_PLANT || mat == Material.CHORUS_FLOWER;
    }

    public boolean isPlant(Block block) {
        Material mat = block.getType();
        for(Material p : plants) {
            if(mat == p) {
                return true;
            }
        }
        return false;
    }

    static boolean matchesPlant(Material origin, Material current) {
        if(origin==current) return true;
        return origin == Material.KELP_PLANT && current == Material.KELP;
    }

    public static ArrayList<Block> getPlant(Block block) {
        Material mat = block.getType();
        ArrayList<Block> blocks = new ArrayList<>();
        blocks.add(block);
        Block next = block.getRelative(BlockFace.UP);
        while(matchesPlant(mat,next.getType())) {
            blocks.add(next);
            next = next.getRelative(BlockFace.UP);
        }
        return blocks;
    }

    public static void getChorusTree(Block block, ArrayList<Block> list) {

        Block currentBlock = block;

        if(isPartOfChorusTree(currentBlock) /*&& list.size()<maxTreeSize */) {
            if(!list.contains(currentBlock)) {
                list.add(currentBlock);

                for(BlockFace face:chorusBlockFaces) {
                    if(isPartOfChorusTree(currentBlock.getRelative(face))) {
                        getChorusTree(currentBlock.getRelative(face),list);
                    }
                }

            }

        }
    }

    public static void destroyPlant(ArrayList<Block> blocks) {
        blocks.forEach((b) -> b.setType(Material.AIR,true));
    }

    public static Material getPlantDrop(Material mat) {
        //noinspection SwitchStatementWithTooFewBranches
        switch(mat) {
            case KELP_PLANT:
                return Material.KELP;
            default:
                return mat;
        }
    }

    public static int getAmountInList(ArrayList<Block> blocks, Material search) {
        int i = 0;
        for(Block block : blocks) {
            if(block.getType()==search) i++;
        }
        return i;
    }
}
