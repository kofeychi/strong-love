package com.linett.strong_love.common.items.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.*;
import java.util.function.Consumer;

public class DashWeaponItem extends SwordItem {
    private static final int DASH_COOLDOWN = 10;
    private static final Map<Player, Boolean> hasKnockback = new HashMap<>();
    private static final Map<Player, Set<UUID>> alreadyHit = new HashMap<>();
    private static final Map<Player, Integer> dashAnimationTicks = new HashMap<>();

    public DashWeaponItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving instanceof Player player && dashAnimationTicks.containsKey(player) && dashAnimationTicks.get(player) > 0) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }
                return HumanoidModel.ArmPose.ITEM;
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            if (player.onGround()) {
                Vec3 lookVec = player.getLookAngle();
                Vec3 knockbackDir = new Vec3(-lookVec.x, 0, -lookVec.z).normalize();

                player.setDeltaMovement(
                        knockbackDir.x * 1.2,
                        0.8,
                        knockbackDir.z * 1.2
                );
                player.hurtMarked = true;

                hasKnockback.put(player, true);

                Level level = player.level();
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.onGround() && hasKnockback.getOrDefault(player, false)) {
            alreadyHit.put(player, new HashSet<>());
            dashAnimationTicks.put(player, 10); // 10 тиков анимации

            performFullDirectionDash(player, stack);
            player.getCooldowns().addCooldown(this, DASH_COOLDOWN);

            hasKnockback.put(player, false);

            // Запускаем анимацию тела
            player.swing(hand);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    private void performFullDirectionDash(Player player, ItemStack stack) {
        Vec3 lookVec = player.getLookAngle();

        player.setDeltaMovement(
                lookVec.x * 2.8,
                lookVec.y * 2.8,
                lookVec.z * 2.8
        );
        player.hurtMarked = true;
        player.resetFallDistance();

        applyInstantDamage(player, stack, lookVec);

        Level level = player.level();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 1.0F, 1.1F);

        if (level.isClientSide) {
            for (int i = 0; i < 25; i++) {
                double progress = i / 25.0;
                double offsetX = -lookVec.x * progress * 3.0;
                double offsetY = -lookVec.y * progress * 3.0;
                double offsetZ = -lookVec.z * progress * 3.0;

                level.addParticle(ParticleTypes.FIREWORK,
                        player.getX() + offsetX + (level.random.nextDouble() - 0.5) * 0.4,
                        player.getY() + offsetY + (level.random.nextDouble() - 0.5) * 0.4,
                        player.getZ() + offsetZ + (level.random.nextDouble() - 0.5) * 0.4,
                        0, 0, 0);
            }
        }
    }

    private void applyInstantDamage(Player player, ItemStack stack, Vec3 direction) {
        Level level = player.level();
        if (level.isClientSide()) return;

        AABB hitBox = player.getBoundingBox().inflate(3.0, 2.0, 3.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, hitBox);

        for (LivingEntity entity : entities) {
            if (entity != player && entity.isAlive() && !isAlreadyHit(player, entity)) {
                double distance = player.position().distanceTo(entity.position());
                if (distance <= 3.5) {
                    applyDamageToEntity(player, stack, entity, direction);
                    markAsHit(player, entity);
                }
            }
        }
    }

    private void applyDamageToEntity(Player player, ItemStack stack, LivingEntity entity, Vec3 direction) {
        Level level = player.level();

        float baseDamage = (float) getDamage();
        float enchantmentDamage = EnchantmentHelper.getDamageBonus(stack, entity.getMobType());
        float totalDamage = (baseDamage + enchantmentDamage) * 2.0F;

        entity.hurt(player.damageSources().playerAttack(player), totalDamage);

        if (EnchantmentHelper.getFireAspect(player) > 0) {
            entity.setSecondsOnFire(EnchantmentHelper.getFireAspect(player) * 4);
        }

        Vec3 knockbackDir = entity.position().subtract(player.position()).normalize();
        entity.setDeltaMovement(
                knockbackDir.x * 2.0,
                0.8,
                knockbackDir.z * 2.0
        );
        entity.hurtMarked = true;

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (level.isClientSide) {
            for (int i = 0; i < 10; i++) {
                level.addParticle(ParticleTypes.CRIT,
                        entity.getX() + (level.random.nextDouble() - 0.5) * entity.getBbWidth(),
                        entity.getY() + level.random.nextDouble() * entity.getBbHeight(),
                        entity.getZ() + (level.random.nextDouble() - 0.5) * entity.getBbWidth(),
                        0, 0, 0);
            }
        }
    }

    private boolean isAlreadyHit(Player player, LivingEntity entity) {
        Set<UUID> hitEntities = alreadyHit.get(player);
        return hitEntities != null && hitEntities.contains(entity.getUUID());
    }

    private void markAsHit(Player player, LivingEntity entity) {
        Set<UUID> hitEntities = alreadyHit.get(player);
        if (hitEntities != null) {
            hitEntities.add(entity.getUUID());
        }
    }

    public static void onPlayerLogout(Player player) {
        hasKnockback.remove(player);
        alreadyHit.remove(player);
        dashAnimationTicks.remove(player);
    }

    public static void onPlayerTick(Player player) {
        if (player.onGround()) {
            alreadyHit.remove(player);
        }

        // Обновляем анимацию
        if (dashAnimationTicks.containsKey(player)) {
            int ticks = dashAnimationTicks.get(player);
            if (ticks > 0) {
                dashAnimationTicks.put(player, ticks - 1);
            } else {
                dashAnimationTicks.remove(player);
            }
        }
    }

    // Метод для получения прогресса анимации (для рендера)
    public static float getDashAnimationProgress(Player player, float partialTicks) {
        if (dashAnimationTicks.containsKey(player)) {
            int ticks = dashAnimationTicks.get(player);
            float progress = (10 - (ticks - partialTicks)) / 10.0F;
            return Mth.clamp(progress, 0.0F, 1.0F);
        }
        return 0.0F;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }
}