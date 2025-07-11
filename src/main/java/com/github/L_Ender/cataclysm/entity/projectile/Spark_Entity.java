package com.github.L_Ender.cataclysm.entity.projectile;


import com.github.L_Ender.cataclysm.client.particle.CircleLightningParticle;
import com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;


public class Spark_Entity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(Spark_Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_DAMAGE = SynchedEntityData.defineId(Spark_Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AREA_RADIUS = SynchedEntityData.defineId(Spark_Entity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HP_DAMAGE = SynchedEntityData.defineId(Spark_Entity.class, EntityDataSerializers.FLOAT);
    public Spark_Entity(EntityType<? extends Spark_Entity> type, Level world) {
        super(type, world);
    }

    public Spark_Entity(Level worldIn, LivingEntity throwerIn) {
        super(ModEntities.SPARK.get(), throwerIn, worldIn);
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", getDamage());
        tag.putFloat("AreaDamage", getAreaDamage());
        tag.putFloat("HpDamage", getHpDamage());
        tag.putFloat("Area_Radius", getAreaRadius());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDamage(tag.getFloat("Damage"));
        setHpDamage(tag.getFloat("HpDamage"));
        setAreaDamage(tag.getFloat("HpDamage"));
        setAreaRadius(tag.getFloat("Area_Radius"));
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(DAMAGE,0f);
        this.entityData.define(AREA_RADIUS,0f);
        this.entityData.define(HP_DAMAGE,0f);
        this.entityData.define(AREA_DAMAGE,0f);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, damage);
    }

    public float getAreaDamage() {
        return entityData.get(AREA_DAMAGE);
    }

    public void setAreaDamage(float damage) {
        entityData.set(AREA_DAMAGE, damage);
    }

    public float getHpDamage() {
        return entityData.get(HP_DAMAGE);
    }

    public void setHpDamage(float damage) {
        entityData.set(HP_DAMAGE, damage);
    }

    public float getAreaRadius() {
        return entityData.get(AREA_RADIUS);
    }

    public void setAreaRadius(float radius) {
        entityData.set(AREA_RADIUS, radius);
    }


    public void tick() {
        super.tick();

        if(this.level().isClientSide){
            Vec3 center = this.position().add(this.getDeltaMovement());
            this.level().addParticle(new CircleLightningParticle.CircleData(143, 241, 215), center.x, center.y, center.z, xo, yo, zo);

        }



    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            int standingOnY = Mth.floor(this.getY()) - 3;
            this.spawnArea(this.getX(), this.getZ(),standingOnY, this.getY() + 1);

            LivingEntity entity1 = (LivingEntity) this.getOwner();
            this.level().addFreshEntity(new Lightning_Storm_Entity(this.level(), this.getX(), this.getY(), this.getZ(), this.getYRot(), -9, this.getDamage(), this.getHpDamage(), entity1,2.0F));
            this.discard();
        }

    }


    protected void spawnArea(double x, double z, double minY, double maxY) {
        BlockPos blockpos = BlockPos.containing(x, maxY, z);
        boolean foundGround = false;
        double spawnY = minY;

        do {
            BlockPos below = blockpos.below();
            BlockState groundState = this.level().getBlockState(below);

            if (groundState.isFaceSturdy(this.level(), below, Direction.UP)) {
                if (!this.level().isEmptyBlock(blockpos)) {
                    VoxelShape shape = this.level().getBlockState(blockpos).getCollisionShape(this.level(), blockpos);
                    if (!shape.isEmpty()) {
                        spawnY = blockpos.getY() + shape.max(Direction.Axis.Y);
                    } else {
                        spawnY = blockpos.getY();
                    }
                } else {
                    spawnY = blockpos.getY();
                }

                foundGround = true;
                break;
            }

            blockpos = below;
        } while (blockpos.getY() >= Mth.floor(minY) - 1);

        if (!foundGround) {
            spawnY = minY;
        }

        Lightning_Area_Effect_Entity areaeffectcloud = new Lightning_Area_Effect_Entity(this.level(), x, spawnY, z);
        areaeffectcloud.setRadius(this.getAreaRadius());
        LivingEntity entity1 = (LivingEntity) this.getOwner();
        areaeffectcloud.setOwner(entity1);
        areaeffectcloud.setRadiusOnUse(-1.0F);
        areaeffectcloud.setDamage(this.getAreaDamage());
        areaeffectcloud.setWaitTime(5);
        areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
        areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() * 2 / (float)areaeffectcloud.getDuration());
        this.level().addFreshEntity(areaeffectcloud);
    }

    protected float getGravity() {
        return 0.07F;
    }

    protected void onHit(HitResult ray) {
        HitResult.Type hitresult$type = ray.getType();
        if (hitresult$type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult)ray);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, ray.getLocation(), GameEvent.Context.of(this, (BlockState)null));
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult)ray;
            this.onHitBlock(blockhitresult);
            BlockPos blockpos = blockhitresult.getBlockPos();
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
        }

    }

}
