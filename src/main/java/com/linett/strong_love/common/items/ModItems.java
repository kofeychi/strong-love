package com.linett.strong_love.common.items;

import com.linett.strong_love.Strong_love;
import com.linett.strong_love.common.items.custom.DashWeaponItem;
import com.linett.strong_love.common.items.custom.HeartPotionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModItems {
    public static final DeferredRegister<Item> ITEM =
            DeferredRegister.create(ForgeRegistries.ITEMS, Strong_love.MODID);

    public static final Rarity RARITY_LOVE = Rarity.create("love_strong:love_rarity",
            style -> style.withColor(0xff00ab));


    public static final RegistryObject<Item> LOVE_ARTIFACT = ITEM.register("love_artifact",
            () -> new HeartPotionItem(new Item.Properties().stacksTo(16).rarity(RARITY_LOVE)));

    public static final RegistryObject<Item> LOVE_DASH_SWORD = ITEM.register("love_dash_sword",
            () -> new DashWeaponItem(Tiers.NETHERITE, 2, -2.4F, new Item.Properties().stacksTo(1).durability(2031)
            ));




    public static void register(IEventBus eventBus) {
        ITEM.register(eventBus);
    }
}