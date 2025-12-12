package com.linett.strong_love.common.items;

import com.linett.strong_love.render.ItemWith2DBeamRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class BeamItemRegistry {
    public static final Map<Item, ItemWith2DBeamRenderer.BeamProperties> ITEM_PROPERTIES = new HashMap<>();

    static {

        registerItemWithBeam(ModItems.LOVE_ARTIFACT.get(), new int[]{0xffd81d,0xe40099},1.5F, 5);
        registerItemWithBeam(Items.DIAMOND, new int[]{0x1ffffb},0.8F, 3);
        registerItemWithBeam(Items.NETHER_STAR, new int[]{0xff9494,0xc4ff94,0x94c5ff,0xffffff},1.0F, 4);

    }

    public static void registerItemWithBeam(Item item, int[] colors, float beamLength, int beamCount) {
        ITEM_PROPERTIES.put(item, new ItemWith2DBeamRenderer.BeamProperties(colors, beamLength, beamCount));
    }

}

