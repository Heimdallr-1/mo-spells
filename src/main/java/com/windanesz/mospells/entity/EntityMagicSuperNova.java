package com.windanesz.mospells.entity;

import com.bobmowzie.mowziesmobs.client.particle.MMParticle;
import com.bobmowzie.mowziesmobs.client.particles.util.MowzieParticleBase;
import com.bobmowzie.mowziesmobs.client.particles.util.ParticleComponent;
import com.bobmowzie.mowziesmobs.client.particles.util.RibbonComponent;
import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.damage.DamageUtil;
import com.bobmowzie.mowziesmobs.server.entity.LeaderSunstrikeImmune;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntitySuperNova;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * This class is a slightly modified version of {@link com.bobmowzie.mowziesmobs.server.entity.effects.EntitySuperNova} by WinDanesz
 * Author: Bob Mowzie (Mowzie's Mobs)
 * Refer to LICENSE.md for more details
 */
public class EntityMagicSuperNova extends EntitySuperNova {

	public EntityMagicSuperNova(World world) {
		super(world);
	}

	public EntityMagicSuperNova(World world, EntityLivingBase caster, double x, double y, double z) {
		super(world, caster, x, y, z);
	}

	@Override
	public void onUpdate() {
		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.onEntityUpdate();

		if (ticksExisted == 1) {
			caster = (EntityLivingBase) world.getEntityByID(getCasterID());
		}

		if (caster == null || caster.isDead || caster.getHealth() <= 0) { this.setDead(); }

		if (ticksExisted == 1) {
			playSound(MMSounds.ENTITY_SUPERNOVA_END, 3f, 1f);
			if (world.isRemote) {
				float scale = 8.2f;
				for (int i = 0; i < 15; i++) {
					float phaseOffset = rand.nextFloat();
					MowzieParticleBase.spawnParticle(world, MMParticle.ARROW_HEAD, posX, posY, posZ, 0, 0, 0, false, 0, 0, 0, 0, 8F, 0.95, 0.9, 0.35, 1, 1, 30, true, new ParticleComponent[] {
							new ParticleComponent.Orbit(new Vec3d[] {getPositionVector().add(0, height / 2, 0)}, ParticleComponent.KeyTrack.startAndEnd(0 + phaseOffset, 1.6f + phaseOffset), new ParticleComponent.KeyTrack(
									new float[] {0.2f * scale, 0.63f * scale, 0.87f * scale, 0.974f * scale, 0.998f * scale, 1f * scale},
									new float[] {0, 0.15f, 0.3f, 0.45f, 0.6f, 0.75f}
							), ParticleComponent.KeyTrack.startAndEnd(rand.nextFloat() * 2 - 1, rand.nextFloat() * 2 - 1), ParticleComponent.KeyTrack.startAndEnd(rand.nextFloat() * 2 - 1, rand.nextFloat() * 2 - 1), ParticleComponent.KeyTrack.startAndEnd(rand.nextFloat() * 2 - 1, rand.nextFloat() * 2 - 1), false),
							new RibbonComponent(MMParticle.RIBBON_FLAT, 10, 0, 0, 0, 0.2F, 0.95, 0.9, 0.35, 1, true, true, new ParticleComponent[] {
									new RibbonComponent.PropertyOverLength(RibbonComponent.PropertyOverLength.EnumRibbonProperty.SCALE, ParticleComponent.KeyTrack.startAndEnd(1, 0)),
									new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(1, 0), false)
							}),
							new ParticleComponent.PropertyControl(ParticleComponent.PropertyControl.EnumParticleProperty.ALPHA, ParticleComponent.KeyTrack.startAndEnd(1, 0), false),
							new ParticleComponent.FaceMotion()
					});
				}
			}
		}

		if (caster != null && !world.isRemote) {
			float ageFrac = ticksExisted / (float) (EntitySuperNova.DURATION);
			float scale = (float) Math.pow(ageFrac, 0.5) * 5f;
			setSize(scale, scale);
			setPosition(prevPosX, prevPosY, prevPosZ);
			List<EntityLivingBase> hitList = getEntitiesNearbyCube(EntityLivingBase.class, scale);
			for (EntityLivingBase entity : hitList) {
				if (AllyDesignationSystem.isValidTarget(caster, entity)) {

					if (entity instanceof LeaderSunstrikeImmune) { continue; }
					float damageFire = 2.5f;
					float damageMob = 3f;
					damageFire *= ConfigHandler.MOBS.BARAKO.combatData.attackMultiplier;
					damageMob *= ConfigHandler.MOBS.BARAKO.combatData.attackMultiplier;
					boolean hitWithFire = DamageUtil.dealMixedDamage(entity, DamageSource.causeMobDamage(caster), damageMob, DamageSource.ON_FIRE, damageFire).getRight();
					if (hitWithFire) {
						Vec3d diff = entity.getPositionVector().subtract(getPositionVector());
						diff = diff.normalize();
						entity.knockBack(this, 0.4f, -diff.x, -diff.z);
						entity.setFire(5);
					}
				}
			}
		}
		if (ticksExisted > DURATION) { setDead(); }
	}
}
