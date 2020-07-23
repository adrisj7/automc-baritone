package automc.combat;

import java.lang.reflect.Field;
import java.util.HashSet;

import automc.AutoMC;
import automc.Logger;
import automc.control.InputOverrider;
import baritone.api.IBaritone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.util.math.Vec3d;

/**
 * Automatically applies killaura to reachable hostile mobs.
 * This will run in the background and serves purely to defend against mobs that might stop us from accomplishing our goals.
 * 
 * TODO: Add defense against projectiles (skeleton arrows, blaze fireballs, ghast fireballs?)
 */

public class KillAuraForceField {

	private static final boolean PROJECTILE_DEFENSE = true;
	private static final double PROJECTILE_DEFENSE_DISTANCE = 35; // If an arrow is closer than this distance, prepare for impact.
	private static final double SKELETON_PREPARE_DISTANCE = 13; // If a skeleton is pulling its bow from this distance, start preparing.

	private static final double SHIELD_DEFESE_DISTANCE = 1.4;	// If an attacking mob (ex. zombie) is closer than this, use a shield to defend.
	
	private static final int TIMER_RATE = 5;

	private EntityLivingBase target;

	private boolean blocking = false;

	@SuppressWarnings("rawtypes")
	private HashSet<Class> targetClasses;
//	@SuppressWarnings("rawtypes")
//	private HashSet<Class> projectileClasses;

	private int timer = 0;

	@SuppressWarnings("rawtypes")
	public KillAuraForceField() {
		this.target = null;

		targetClasses = new HashSet<Class>();

		// All hostile classes that we ALWAYS want to push away.
		addClassesToTarget(
				EntityZombie.class,
				EntitySkeleton.class,
				EntitySpider.class,
				EntityCreeper.class,
				EntityCaveSpider.class,
				EntityGhast.class,
				EntitySlime.class,
				EntityMagmaCube.class,
				EntityGiantZombie.class,
				EntityZombieVillager.class,
				EntityWither.class,
				EntityWitch.class,
				EntityHusk.class,
				EntitySilverfish.class,
				EntitySpellcasterIllager.class,
				EntityIllusionIllager.class
		);
		/*
		addClassesAsProjectiles(
			EntityArrow.class,
			EntityFireball.class
		);
		*/
	}

	public boolean isAttacking() {
		return target != null;
	}
	public boolean isBlocking() {
		return blocking;
	}

	public void onTick() {
		this.target = getClosestTarget(Minecraft.getMinecraft().player);
		if (timer >= TIMER_RATE) {
			if (this.target != null) {
				attack(this.target);
				timer = 0;
			}
		}

		if (PROJECTILE_DEFENSE) {
			defend();
		}

		++timer;
	}

	private void attack(EntityLivingBase target) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		AutoMC.getAutoMC().player.inventory.equipBestWeapon();
		controller.attackEntity(player, target);
		//player.swingArm(EnumHand.MAIN_HAND);
		//player.attackTargetEntityWithCurrentItem(target);//attackEntityAsMob(target);
	}

	private EntityLivingBase getClosestTarget(EntityPlayerSP player) {

		double closestDistanceSq = Double.POSITIVE_INFINITY;

		EntityLivingBase closest = null;
		
		for(Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				continue;
			}
			// If the killer is already taking care of this entity, ignore it.
			if (entity.equals(AutoMC.getAutoMC().combatRunner.killer.getTarget())) {
				continue;
			}
			EntityLivingBase livingEntity = (EntityLivingBase)entity;
			if (canAttack(player, livingEntity) && shouldAttack(player, livingEntity)) {
				double distSq = Minecraft.getMinecraft().player.getDistanceSq(livingEntity);
				if (distSq < closestDistanceSq) {
					closestDistanceSq = distSq;
					closest = livingEntity;
				}
			}
		}
		return closest;
	}

	// Only attack hostile mobs.
	// TODO: This can be overriden to change the potential targets to attack.
	// For example, you can change this to exclusively attack players.
	protected boolean shouldAttack(EntityPlayerSP player, EntityLivingBase entity) {
		// If we're the player, don't attack ourselves!
		if (entity.equals(player)) {
			return false;
		}
		// Don't attack dead entities.
		if (!entity.isEntityAlive()) {
			return false;
		}
		// If we're in the list of always hostile
		if (targetClasses.contains(entity.getClass())) {
			return true;
		}
		// If we're being attacked by it.
		if (entity.getRevengeTarget() != null && entity.getRevengeTarget().equals(player)) {
			return true;
		}
		return false;
	}

	// Can the player attack this entity? (Raycast later maybe?)
	private boolean canAttack(EntityPlayerSP player, EntityLivingBase entity) {
		double reachDist = Minecraft.getMinecraft().playerController.getBlockReachDistance();
		return player.getDistanceSq(entity) < reachDist*reachDist && entity.ticksExisted > 10;
	}

	private void defend() {

		if (AutoMC.getAutoMC().player.inventory.getShieldStack().getItem().equals(Items.SHIELD)) {
			
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			// Get closest arrow/projectile.
			double closestSq = Double.POSITIVE_INFINITY;
			Entity closestProjectile = null;
			
			boolean skeletonCharging = false;
			boolean creeperFuse = false;

			for(Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
				if (entity.equals(player)) continue;
				if (entity instanceof EntityArrow) {
					EntityArrow arrow = (EntityArrow) entity;
					// If arrow is shot by player or in the ground, ignore.
					if ( !isArrowFlying(arrow) ) continue;
					if (arrow.shootingEntity != null && arrow.shootingEntity.equals(player)) continue;

					double sqrDist = player.getDistanceSq(arrow);
					if (sqrDist > PROJECTILE_DEFENSE_DISTANCE*PROJECTILE_DEFENSE_DISTANCE) continue;
					if (sqrDist < closestSq) {
						// Check if arrow is GOING TOWARDS player
						Vec3d deltaToPlayer = player.getPositionVector().subtract(arrow.getPositionVector());
						Vec3d velocity = new Vec3d(arrow.motionX, arrow.motionY, arrow.motionZ);
						double dot = deltaToPlayer.dotProduct(velocity);
						if (dot < 0) {
							continue;
						}
						closestSq = sqrDist;
						closestProjectile = arrow;
					}
				} else if (entity instanceof EntityFireball) {
					EntityFireball fireball = (EntityFireball) entity;
					// TODO: Should we block these?
				} else if (entity instanceof EntitySkeleton) {
					if (((EntityLivingBase)entity).isDead) continue;
					EntitySkeleton skelly = (EntitySkeleton)entity;
					
					double sqrDist = player.getDistanceSq(skelly);
					if (sqrDist > SKELETON_PREPARE_DISTANCE*SKELETON_PREPARE_DISTANCE) continue;
					
					int pullDuration = skelly.getActiveItemStack().getMaxItemUseDuration() - skelly.getItemInUseCount();
					if (pullDuration > 10) {
						skeletonCharging = true;
					}
				} else if (entity instanceof EntityCreeper) {
					if (((EntityLivingBase)entity).isDead) continue;
					EntityCreeper creepuh = (EntityCreeper)entity;
					double ignition = creepuh.getCreeperFlashIntensity(1);
					if (ignition > 0.5) {
						creeperFuse = true; // AWWWWWWW MAAANNNNN
						lookAt(creepuh);
					}
				} else if (targetClasses.contains(entity.getClass())) {
					if (!(entity instanceof EntityLivingBase)) continue;
					// We're like a zombie or something
					double distanceSq = entity.getDistanceSq(player);
					if (distanceSq < SHIELD_DEFESE_DISTANCE*SHIELD_DEFESE_DISTANCE) {
						setShield(true);
					}
				}
			}

			// If we have a projectile...
			if (closestProjectile != null) {
				Entity owner = ((EntityArrow) closestProjectile).shootingEntity;
				// LOOK at projectile and RAISE SHIELD
				lookAt(owner);//closestProjectile);
				setShield(true);
			} else {
				
				// If we have a skeleton charging...
				if (skeletonCharging || creeperFuse) {
					setShield(true);
				} else {
					// No skeleton charging, we're not defending.
					setShield(false);
				}
			}
		} else {
			setShield(false);
		}
	}
	
	private void lookAt(Entity entity) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		Vec3d delta = entity.getPositionVector().subtract(player.getPositionVector());
		double yaw = Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90;
		double latDistance = Math.sqrt(delta.z*delta.z + delta.x*delta.x);
		double pitch = Math.toDegrees(Math.atan2(delta.y, latDistance));
		if (pitch < 0) pitch = 0;
		player.rotationYaw = (float)yaw;
		player.rotationPitch = (float)pitch;
		Logger.log(pitch);
	}

	@SuppressWarnings("rawtypes")
	private void addClassesToTarget(Class ...classes) {
		for(Class c : classes) {
			targetClasses.add(c);
		}
	}

	private boolean isArrowFlying(EntityArrow arrow) {
		try {
			Field m = EntityArrow.class.getDeclaredField("inGround");
			m.setAccessible(true);
			return !(boolean)m.get(arrow);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			System.out.println("Couldn't get arrow field...");
			return false;
		}
	}
	
	public void setShield(boolean block) {
		if (blocking == block) return;
		blocking = block;
		AutoMC.getAutoMC().inputOverride.setInputForce(InputOverrider.RIGHT_CLICK, block);
		if (block) {
			IBaritone b = AutoMC.getAutoMC().getBaritone(); 
			b.getMineProcess().onLostControl();
			b.getCustomGoalProcess().onLostControl();
			b.getExploreProcess().onLostControl();
			b.getInputOverrideHandler().clearAllKeys();
		}
	}
	/*
	@SuppressWarnings("rawtypes")
	private void addClassesAsProjectiles(Class ...classes) {
		for (Class c : classes) {
			projectileClasses.add(c);
		}
	}
	*/
}
