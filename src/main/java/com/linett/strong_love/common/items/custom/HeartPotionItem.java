package com.linett.strong_love.common.items.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class HeartPotionItem extends Item {

    private static final int COOLDOWN_TICKS = 600;

    public HeartPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        float healthPercent = player.getHealth() / player.getMaxHealth();

        if (healthPercent > 0.8f) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("message.strong_love.too_healthy"), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;

        if (!level.isClientSide) {
            player.setHealth(player.getMaxHealth());

            player.heal(6.0F);


            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_FALL, SoundSource.PLAYERS, 1.0F, 1.0F);

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        }

        return stack;
    }
}