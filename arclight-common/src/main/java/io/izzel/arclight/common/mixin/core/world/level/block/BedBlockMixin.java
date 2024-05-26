package io.izzel.arclight.common.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin {

    // @formatter:off
    @Shadow @Final public static EnumProperty<BedPart> PART;
    @Shadow @Final public static BooleanProperty OCCUPIED;
    @Shadow protected abstract boolean kickVillagerOutOfBed(Level p_49491_, BlockPos p_49492_);
    @Shadow public static boolean canSetSpawn(Level p_49489_) { return false; }
    // @formatter:on

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public InteractionResult use(BlockState p_49515_, Level level, BlockPos p_49517_, Player p_49518_, InteractionHand p_49519_, BlockHitResult p_49520_) {
        if (level.isClientSide) {
            return InteractionResult.CONSUME;
        } else {
            if (p_49515_.getValue(PART) != BedPart.HEAD) {
                p_49517_ = p_49517_.relative(p_49515_.getValue(FACING));
                p_49515_ = level.getBlockState(p_49517_);
                if (!p_49515_.is((BedBlock) (Object) this)) {
                    return InteractionResult.CONSUME;
                }
            }

            /* if (!canSetSpawn(level)) {
                level.removeBlock(p_49517_, false);
                BlockPos blockpos = p_49517_.relative(p_49515_.getValue(FACING).getOpposite());
                if (level.getBlockState(blockpos).is((BedBlock) (Object) this)) {
                    level.removeBlock(blockpos, false);
                }

                level.explode((Entity) null, DamageSource.badRespawnPointExplosion(), (ExplosionDamageCalculator) null, (double) p_49517_.getX() + 0.5D, (double) p_49517_.getY() + 0.5D, (double) p_49517_.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                return InteractionResult.SUCCESS;
            } else */
            if (p_49515_.getValue(OCCUPIED)) {
                if (!this.kickVillagerOutOfBed(level, p_49517_)) {
                    p_49518_.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
                }

                return InteractionResult.SUCCESS;
            } else {
                // Fluorite start - fix sleep_tight mixin conflict
                boolean bedWork = !canSetSpawn(level);
                AtomicBoolean explode = new AtomicBoolean(false);
                p_49518_.startSleepInBed(p_49517_).ifLeft((p_49477_) -> {
                    if (bedWork) {
                        explode.set(true);
                    } else if (p_49477_.getMessage() != null) {
                        p_49518_.displayClientMessage(p_49477_.getMessage(), true);
                    }
                });

                if (explode.getAndSet(false)) {
                    level.removeBlock(p_49517_, false);
                    BlockPos blockpos = p_49517_.relative(p_49515_.getValue(FACING).getOpposite());
                    if (level.getBlockState(blockpos).is((BedBlock) (Object) this)) {
                        level.removeBlock(blockpos, false);
                    }
                    level.explode(null, DamageSource.badRespawnPointExplosion(), null, (double) p_49517_.getX() + 0.5D, (double) p_49517_.getY() + 0.5D, (double) p_49517_.getZ() + 0.5D, 5.0F, true, Explosion.BlockInteraction.DESTROY);
                }
                // Fluorite end

                return InteractionResult.SUCCESS;
            }
        }
    }
}
