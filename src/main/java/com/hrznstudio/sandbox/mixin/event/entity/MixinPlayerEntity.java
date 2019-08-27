package com.hrznstudio.sandbox.mixin.event.entity;

import com.hrznstudio.sandbox.api.entity.ILivingEntity;
import com.hrznstudio.sandbox.api.event.ItemEvent;
import com.hrznstudio.sandbox.api.event.entity.LivingEvent;
import com.hrznstudio.sandbox.event.EventDispatcher;
import com.hrznstudio.sandbox.util.WrappingUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    @Shadow
    @Final
    public PlayerAbilities abilities;
    @Shadow
    @Final
    public PlayerInventory inventory;

    public MixinPlayerEntity(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource source, CallbackInfo info) {
        LivingEvent.Death event = EventDispatcher.publish(new LivingEvent.Death((ILivingEntity) this));
        if (event.isCancelled())
            info.cancel();
    }

    /**
     * @author Coded
     */
    @Overwrite
    public ItemStack getArrowType(ItemStack weapon) {
        ItemEvent.GetArrowType event = EventDispatcher.publish(new ItemEvent.GetArrowType(
                WrappingUtil.cast(weapon, com.hrznstudio.sandbox.api.item.ItemStack.class),
                WrappingUtil.cast(getVanillaArrowType(weapon), com.hrznstudio.sandbox.api.item.ItemStack.class)
        ));
        return WrappingUtil.convert(event.getArrow());
    }

    private ItemStack getVanillaArrowType(ItemStack weapon) {
        if (!(weapon.getItem() instanceof RangedWeaponItem)) {
            return ItemStack.EMPTY;
        } else {
            Predicate<ItemStack> predicate_1 = ((RangedWeaponItem) weapon.getItem()).getHeldProjectiles();
            ItemStack itemStack_2 = RangedWeaponItem.getHeldProjectile(this, predicate_1);
            if (!itemStack_2.isEmpty()) {
                return itemStack_2;
            } else {
                predicate_1 = ((RangedWeaponItem) weapon.getItem()).getProjectiles();

                for (int int_1 = 0; int_1 < this.inventory.getInvSize(); ++int_1) {
                    ItemStack itemStack_3 = this.inventory.getInvStack(int_1);
                    if (predicate_1.test(itemStack_3)) {
                        return itemStack_3;
                    }
                }

                return abilities.creativeMode ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
            }
        }
    }
}
