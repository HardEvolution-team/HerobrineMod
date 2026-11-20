package com.electrifiedded.herobrinemod.entity;

import com.electrifiedded.archaiclib.legacy.sound.LegacySounds;
import com.electrifiedded.archaiclib.legacy.world.LegacyParticles;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/** Herobrine Boss: Herobrine with the Wither's brain (flies, wither skulls, block breaking, boss bar). */
public class EntityHerobrineBoss extends EntityMob implements IRangedAttackMob {
    private static final DataParameter<Integer> TARGET0 = EntityDataManager.createKey(EntityHerobrineBoss.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET1 = EntityDataManager.createKey(EntityHerobrineBoss.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET2 = EntityDataManager.createKey(EntityHerobrineBoss.class, DataSerializers.VARINT);
    @SuppressWarnings("unchecked")
    private static final DataParameter<Integer>[] TARGETS = new DataParameter[]{TARGET0, TARGET1, TARGET2};
    private static final DataParameter<Integer> INVUL = EntityDataManager.createKey(EntityHerobrineBoss.class, DataSerializers.VARINT);

    private final float[] xRotationHeads = new float[2];
    private final float[] yRotationHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    private final int[] idleHeadUpdates = new int[2];
    private int blockBreakCounter;
    private final BossInfoServer bossInfo = (BossInfoServer) new BossInfoServer(getDisplayName(), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS).setDarkenSky(true);

    private static final Predicate<Entity> NOT_UNDEAD = e -> e instanceof EntityLivingBase
            && ((EntityLivingBase) e).getCreatureAttribute() != EnumCreatureAttribute.UNDEAD && ((EntityLivingBase) e).attackable();

    public EntityHerobrineBoss(World world) {
        super(world);
        setHealth(getMaxHealth());
        setSize(0.9F, 4.0F);
        isImmuneToFire = true;
        ((net.minecraft.pathfinding.PathNavigateGround) getNavigator()).setCanSwim(true);
        experienceValue = 50;
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackRanged(this, 1.0D, 40, 20.0F));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 0, false, false, NOT_UNDEAD));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        for (DataParameter<Integer> p : TARGETS) dataManager.register(p, 0);
        dataManager.register(INVUL, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Invul", getInvulTime());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setInvulTime(tag.getInteger("Invul"));
        if (hasCustomName()) bossInfo.setName(getDisplayName());
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        bossInfo.setName(getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return LegacySounds.get("mob.wither.idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return LegacySounds.get("mob.wither.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return LegacySounds.get("mob.wither.death");
    }

    @Override
    public void onLivingUpdate() {
        motionY *= 0.6D;
        if (!world.isRemote && getWatchedTargetId(0) > 0) {
            Entity e = world.getEntityByID(getWatchedTargetId(0));
            if (e != null) {
                if (posY < e.posY || !isArmored() && posY < e.posY + 5.0D) {
                    if (motionY < 0.0D) motionY = 0.0D;
                    motionY += (0.5D - motionY) * 0.6D;
                }
                double dx = e.posX - posX, dz = e.posZ - posZ, d = dx * dx + dz * dz;
                if (d > 9.0D) {
                    double r = MathHelper.sqrt(d);
                    motionX += (dx / r * 0.5D - motionX) * 0.6D;
                    motionZ += (dz / r * 0.5D - motionZ) * 0.6D;
                }
            }
        }
        if (motionX * motionX + motionZ * motionZ > 0.05D) {
            rotationYaw = (float) MathHelper.atan2(motionZ, motionX) * (180F / (float) Math.PI) - 90.0F;
        }
        super.onLivingUpdate();
        for (int h = 0; h < 2; h++) {
            int id = getWatchedTargetId(h + 1);
            Entity e = id > 0 ? world.getEntityByID(id) : null;
            if (e != null) {
                double hx = getHeadX(h + 1), hy = getHeadY(h + 1), hz = getHeadZ(h + 1);
                double dx = e.posX - hx, dy = e.posY + e.getEyeHeight() - hy, dz = e.posZ - hz;
                double dh = MathHelper.sqrt(dx * dx + dz * dz);
                float yaw = (float) (MathHelper.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
                float pitch = (float) (-(MathHelper.atan2(dy, dh) * (180D / Math.PI)));
                xRotationHeads[h] = rotlerp(xRotationHeads[h], pitch, 40.0F);
                yRotationHeads[h] = rotlerp(yRotationHeads[h], yaw, 10.0F);
            } else {
                yRotationHeads[h] = rotlerp(yRotationHeads[h], renderYawOffset, 10.0F);
            }
        }
        if (world.isRemote) {
            boolean armored = isArmored();
            for (int h = 0; h < 3; h++) {
                double hx = getHeadX(h), hy = getHeadY(h), hz = getHeadZ(h);
                LegacyParticles.spawn(world, "smoke", hx + rand.nextGaussian() * 0.3D, hy + rand.nextGaussian() * 0.3D, hz + rand.nextGaussian() * 0.3D, 0, 0, 0);
                if (armored && rand.nextInt(4) == 0) {
                    LegacyParticles.spawn(world, "mobSpell", hx + rand.nextGaussian() * 0.3D, hy + rand.nextGaussian() * 0.3D, hz + rand.nextGaussian() * 0.3D, 0.7D, 0.7D, 0.5D);
                }
            }
            if (getInvulTime() > 0) {
                for (int i = 0; i < 3; i++) {
                    LegacyParticles.spawn(world, "mobSpell", posX + rand.nextGaussian(), posY + rand.nextFloat() * 3.3F, posZ + rand.nextGaussian(), 0.7D, 0.7D, 0.9D);
                }
            }
        }
    }

    @Override
    protected void updateAITasks() {
        if (getInvulTime() > 0) {
            int i = getInvulTime() - 1;
            if (i <= 0) {
                world.newExplosion(this, posX, posY + getEyeHeight(), posZ, 7.0F, false, ForgeEventFactory.getMobGriefingEvent(world, this));
                world.playBroadcastSound(1023, new BlockPos(this), 0);
            }
            setInvulTime(i);
            if (ticksExisted % 10 == 0) heal(10.0F);
            return;
        }
        super.updateAITasks();
        for (int ix = 1; ix < 3; ix++) {
            if (ticksExisted < nextHeadUpdate[ix - 1]) continue;
            nextHeadUpdate[ix - 1] = ticksExisted + 10 + rand.nextInt(10);
            if (world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD) {
                int l = idleHeadUpdates[ix - 1]++;
                if (l > 15) {
                    double x = MathHelper.nextDouble(rand, posX - 10.0D, posX + 10.0D);
                    double y = MathHelper.nextDouble(rand, posY - 5.0D, posY + 5.0D);
                    double z = MathHelper.nextDouble(rand, posZ - 10.0D, posZ + 10.0D);
                    launchSkullToCoords(ix + 1, x, y, z, true);
                    idleHeadUpdates[ix - 1] = 0;
                }
            }
            int id = getWatchedTargetId(ix);
            if (id > 0) {
                Entity e = world.getEntityByID(id);
                if (e instanceof EntityLivingBase && e.isEntityAlive() && getDistanceSq(e) <= 900.0D && canEntityBeSeen(e)) {
                    launchSkullToEntity(ix + 1, (EntityLivingBase) e);
                    nextHeadUpdate[ix - 1] = ticksExisted + 40 + rand.nextInt(20);
                    idleHeadUpdates[ix - 1] = 0;
                } else {
                    updateWatchedTargetId(ix, 0);
                }
            } else {
                List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(20.0D, 8.0D, 20.0D), NOT_UNDEAD::apply);
                for (int k = 0; k < 10 && !list.isEmpty(); k++) {
                    EntityLivingBase t = list.get(rand.nextInt(list.size()));
                    if (t != this && t.isEntityAlive() && canEntityBeSeen(t)) {
                        if (!(t instanceof EntityPlayer) || !((EntityPlayer) t).capabilities.disableDamage) updateWatchedTargetId(ix, t.getEntityId());
                        break;
                    }
                    list.remove(t);
                }
            }
        }
        updateWatchedTargetId(0, getAttackTarget() != null ? getAttackTarget().getEntityId() : 0);
        if (blockBreakCounter > 0 && --blockBreakCounter == 0 && ForgeEventFactory.getMobGriefingEvent(world, this)) {
            int y0 = MathHelper.floor(posY), x0 = MathHelper.floor(posX), z0 = MathHelper.floor(posZ);
            boolean broke = false;
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++)
                    for (int dy = 0; dy <= 3; dy++) {
                        BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                        IBlockState s = world.getBlockState(p);
                        if (!s.getBlock().isAir(s, world, p) && s.getBlock().canEntityDestroy(s, world, p, this)
                                && EntityWither.canDestroyBlock(s.getBlock()) && ForgeEventFactory.onEntityDestroyBlock(this, p, s)) {
                            broke = world.destroyBlock(p, true) || broke;
                        }
                    }
            if (broke) world.playEvent(null, 1022, new BlockPos(this), 0);
        }
        if (ticksExisted % 20 == 0) heal(1.0F);
        bossInfo.setPercent(getHealth() / getMaxHealth());
    }

    public void ignite() {
        setInvulTime(220);
        setHealth(getMaxHealth() / 3.0F);
    }

    @Override
    public void setInWeb() {
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        bossInfo.removePlayer(player);
    }

    private double getHeadX(int h) {
        if (h <= 0) return posX;
        float f = (renderYawOffset + 180 * (h - 1)) * 0.017453292F;
        return posX + MathHelper.cos(f) * 1.3D;
    }

    private double getHeadY(int h) {
        return h <= 0 ? posY + 3.0D : posY + 2.2D;
    }

    private double getHeadZ(int h) {
        if (h <= 0) return posZ;
        float f = (renderYawOffset + 180 * (h - 1)) * 0.017453292F;
        return posZ + MathHelper.sin(f) * 1.3D;
    }

    private float rotlerp(float a, float b, float max) {
        float f = MathHelper.wrapDegrees(b - a);
        if (f > max) f = max;
        if (f < -max) f = -max;
        return a + f;
    }

    private void launchSkullToEntity(int head, EntityLivingBase t) {
        launchSkullToCoords(head, t.posX, t.posY + t.getEyeHeight() * 0.5D, t.posZ, head == 0 && rand.nextFloat() < 0.001F);
    }

    private void launchSkullToCoords(int head, double x, double y, double z, boolean invulnerable) {
        world.playEvent(null, 1024, new BlockPos(this), 0);
        double hx = getHeadX(head), hy = getHeadY(head), hz = getHeadZ(head);
        EntityWitherSkull skull = new EntityWitherSkull(world, this, x - hx, y - hy, z - hz);
        if (invulnerable) skull.setInvulnerable(true);
        skull.posY = hy;
        skull.posX = hx;
        skull.posZ = hz;
        world.spawnEntity(skull);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        launchSkullToEntity(0, target);
    }

    @Override
    public void setSwingingArms(boolean swinging) {
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source) || source == DamageSource.DROWN || getInvulTime() > 0) return false;
        if (isArmored() && source.getImmediateSource() instanceof EntityArrow) return false;
        Entity e = source.getTrueSource();
        if (e != null && !(e instanceof EntityPlayer) && e instanceof EntityLivingBase
                && ((EntityLivingBase) e).getCreatureAttribute() == getCreatureAttribute()) return false;
        if (blockBreakCounter <= 0) blockBreakCounter = 20;
        for (int i = 0; i < idleHeadUpdates.length; i++) idleHeadUpdates[i] += 3;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        net.minecraft.entity.item.EntityItem item = dropItem(Items.NETHER_STAR, 1);
        if (item != null) item.setNoDespawn();
    }

    @Override
    @Nullable
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected void despawnEntity() {
        idleTime = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public void addPotionEffect(PotionEffect effect) {
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public float getHeadYRotation(int h) {
        return yRotationHeads[h];
    }

    @SideOnly(Side.CLIENT)
    public float getHeadXRotation(int h) {
        return xRotationHeads[h];
    }

    public int getInvulTime() {
        return dataManager.get(INVUL);
    }

    public void setInvulTime(int t) {
        dataManager.set(INVUL, t);
    }

    public int getWatchedTargetId(int head) {
        return dataManager.get(TARGETS[head]);
    }

    public void updateWatchedTargetId(int head, int id) {
        dataManager.set(TARGETS[head], id);
    }

    public boolean isArmored() {
        return getHealth() <= getMaxHealth() / 2.0F;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEAD;
    }
}
