package automc.combat;

import java.util.function.Predicate;

import automc.AutoMC;
import automc.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

// Its da killer
// do not die
public class Killer {

	private static final double SHIELD_SAFETY_DIST = 1;
	private static final double HEALTH_PENALTY = 2; // How many blocks away does each piece of health affect the penalty?

	private EntityLivingBase target;
	@SuppressWarnings("rawtypes")
	private Class targetClass;

	public Killer() {
		target = null;
		targetClass = null;
	}

	public boolean isAttacking() {
		return target != null || targetClass != null;
	}
	
	public EntityLivingBase getTarget() {
		return target;
	}

	public void kill(EntityLivingBase target) {
		if (target == null ) {
			stop();
			return;
		}
		if (!target.equals (this.target) || !AutoMC.getAutoMC().getBaritone().getFollowProcess().isActive()) {
			Logger.log("we're following it");
			this.target = target;
			// New kill target
			Predicate<Entity> getTarget = entity -> (entity != null && entity.equals(this.target)); 
			AutoMC.getAutoMC().getBaritone().getFollowProcess().follow(getTarget);
		}
	}

	@SuppressWarnings("rawtypes")
	public void killNearest(Class targetClass) {
		if (this.targetClass != targetClass) {
			this.targetClass = targetClass;
			this.target = null;
		}
	}

	public void onTick() {
		if (isAttacking()) {

			EntityPlayerSP player = Minecraft.getMinecraft().player;
			
			if (targetClass != null) {
				// Scan and find best target. This means the entity that's closest and has lower health.
				double smallestPenalty = Double.POSITIVE_INFINITY;
				EntityLivingBase best = null;
				for(Entity entity : Minecraft.getMinecraft().world.loadedEntityList) {
					if (entity == null || !(entity instanceof EntityLivingBase)) {
						continue;
					}
					EntityLivingBase living = (EntityLivingBase) entity;
					if (living.getClass().equals(targetClass)) {
						double penalty = living.getDistanceSq(player);
						double lostHealth = living.getMaxHealth() - living.getHealth();
						penalty -= (lostHealth * lostHealth * HEALTH_PENALTY * HEALTH_PENALTY);
						
						if (penalty < smallestPenalty) {
							smallestPenalty = penalty;
							best = living;
						}
					}
				}
				if (best != null) {
					stopExploring();
					kill(best);
				} else {
					startExploring();
				}
			}
			if (target != null) {			
				// We have a target, so attack.
				if (target.isDead || target.ticksExisted < 5) {
					stop();
					return;
				}
				double strength = player.getCooledAttackStrength(0.5f);
	
				boolean shouldAttack = canAttack(target) && strength >= 0.99;
	
				boolean shielding = false;
	
				if (shouldAttack) {
					// We can attack since we're close enough and have the strength to.
					attack(target);
				} else {
					// We might want to defend ourselves.
					double distSqr = player.getDistanceSq(target);
					if (distSqr < SHIELD_SAFETY_DIST*SHIELD_SAFETY_DIST) {
						shielding = true;
					}
				}
				setShield(shielding);
			}
		}
	}

	public void stop() {
		target = null;
		targetClass = null;
		AutoMC.getAutoMC().getBaritone().getFollowProcess().onLostControl();
		setShield(false);
	}

	// Can the player attack this entity? (Raycast later maybe?)
	private boolean canAttack(EntityLivingBase entity) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		double reachDist = Minecraft.getMinecraft().playerController.getBlockReachDistance();
		return player.getDistanceSq(entity) < reachDist*reachDist && entity.ticksExisted > 10;
	}
	private void attack(EntityLivingBase entity) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		AutoMC.getAutoMC().player.inventory.equipBestWeapon();
		controller.attackEntity(player, entity);
	}

	private void startExploring() {
		if (!AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive()) {
			BlockPos p = Minecraft.getMinecraft().player.getPosition();
			AutoMC.getAutoMC().getBaritone().getExploreProcess().explore(p.getX(), p.getZ());
		}
	}
	private void stopExploring() {
		AutoMC.getAutoMC().getBaritone().getExploreProcess().onLostControl();
	}

	private void setShield(boolean block) {
		// TODO: Move somewhere better.
		AutoMC.getAutoMC().combatRunner.aura.setShield(block);
	}
}
