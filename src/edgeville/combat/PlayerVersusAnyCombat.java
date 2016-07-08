package edgeville.combat;

import edgeville.Constants;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.AttributeKey;
import edgeville.model.ChatMessage;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Skulls;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.item.Item;
import edgeville.script.TimerKey;
import edgeville.util.AccuracyFormula;
import edgeville.util.CombatFormula;
import edgeville.util.CombatStyle;
import edgeville.util.EquipmentInfo;
import edgeville.util.Varbit;
import edgeville.util.Varp;

public class PlayerVersusAnyCombat extends Combat {

	private Player player;
	private Entity target;

	public PlayerVersusAnyCombat(Entity entity, Entity target) {
		super(entity, target);
		player = (Player) entity;
		this.target = target;
	}

	@Override
	public void cycle(EventContainer container) {
		// Get weapon data.
		Item weapon = ((Player) getEntity()).getEquipment().get(EquipSlot.WEAPON);
		int weaponId = weapon == null ? -1 : weapon.getId();
		int weaponType = getEntity().world().equipmentInfo().weaponType(weaponId);
		Item ammo = ((Player) getEntity()).getEquipment().get(EquipSlot.AMMO);
		int ammoId = ammo == null ? -1 : ammo.getId();
		String ammoName = ammo == null ? "" : ammo.definition(getEntity().world()).name;

		// Check if players are in wilderness.
		if (target instanceof Player) {
			if (!CombatUtil.canAttack(player, target)) {
				container.stop();
				return;
			}
			// bounty interface for player.
			/*
			 * if (!player.timers().has(TimerKey.IN_COMBAT)) {
			 * player.interfaces().setBountyInterface(true); }
			 */
			player.timers().register(TimerKey.IN_COMBAT, 10);
			if (!player.inSafeArea()) {
				player.timers().register(TimerKey.AFTER_COMBAT, 5);
			}
			// bounty interface for target.
			/*
			 * if (!target.timers().has(TimerKey.IN_COMBAT)) { ((Player)
			 * target).interfaces().setBountyInterface(true); }
			 */
			target.timers().register(TimerKey.IN_COMBAT, 10);
			if (!((Player)target).inSafeArea()) {
				target.timers().register(TimerKey.AFTER_COMBAT, 5);
			}
		}

		// Combat type?
		if (weaponType == WeaponType.BOW || weaponType == WeaponType.CROSSBOW || weaponType == WeaponType.THROWN
				|| weaponType == WeaponType.CHINCHOMPA) {
			// getEntity().message("ranging...");
			handleRangeCombat(weaponId, ammoName, weaponType, container);
		} else {
			// getEntity().message("meleeeing...");
			handleMeleeCombat(weaponId);
		}

		getTarget().putAttribute(AttributeKey.LAST_DAMAGER, getEntity());
		getTarget().putAttribute(AttributeKey.LAST_DAMAGE, System.currentTimeMillis());
	}

	@Override
	public void handleMeleeCombat(int weaponId) {
		Tile currentTile = getEntity().getTile();

		// dont attack if stunned
		if (player.stunned()) {
			return;
		}

		// Move closer if out of range.
		if (!player.touches(getTarget(), currentTile)) {
			if (!player.frozen()) {
				// player.pathQueue().clear();
				currentTile = moveCloser();
			}
			return;
		}

		// Check timer.
		if (!getEntity().timers().has(TimerKey.COMBAT_ATTACK)) {

			if (player.isSpecialAttackEnabled()) {
				doMeleeSpecial(player, weaponId);
				return;
			}

			boolean success = AccuracyFormula.doesHit(((Player) getEntity()), getTarget(), CombatStyle.MELEE);

			int max = CombatFormula.maximumMeleeHit(((Player) getEntity()));
			// if (player.isDebug()) {
			player.getQuestTab().updateMaxHit(max);
			// }
			int hit = getEntity().world().random(max);

			getTarget().hit(getEntity(), success ? hit : 0, CombatStyle.MELEE);
			triggerVeng(success ? hit : 0);

			getEntity().animate(EquipmentInfo.attackAnimationFor(((Player) getEntity())));
			getEntity().timers().register(TimerKey.COMBAT_ATTACK,
					getEntity().world().equipmentInfo().weaponSpeed(weaponId));
		}
	}

	private void triggerVeng(int hit) {
		if (target instanceof Player) {
			if (((Player) target).isVengOn()) {
				player.hit(target, (int) (0.75 * hit));
				((Player) target).shout("Taste Vengeance!");
				((Player) target).setVengOn(false);
			}
		}
	}

	public void doMeleeSpecial(Player player, int weaponId) {
		player.timers().register(TimerKey.COMBAT_ATTACK, 5);
		SpecialAttacks specialAttack = SpecialAttacks.getSpecialAttack(player, weaponId);
		if (specialAttack == null) {
			return;
		}
		if (!drainSpecialEnergy(player, specialAttack.getSpecialDrain())) {
			return;
		}
		player.animate(specialAttack.getAnimationId());
		if (specialAttack.getGfx() != null) {
			player.graphic(specialAttack.getGfx());
		}
		if (specialAttack.getOpponentGfx() != null) {
			target.graphic(specialAttack.getOpponentGfx());
		}

		double max = CombatFormula.maximumMeleeHit(player) * specialAttack.getMaxHitMultiplier();
		int hit = player.world().random().nextInt((int) Math.round(max));

		if (specialAttack.isHits()) {
			// double max = CombatFormula.maximumMeleeHit(player) *
			// specialAttack.getMaxHitMultiplier();
			// int hit = player.world().random().nextInt((int) Math.round(max));
			target.hit(player, hit, CombatStyle.MELEE);
			triggerVeng(hit);

			if (specialAttack.isDoubleHit()) {
				int hit2 = player.world().random().nextInt((int) Math.round(max));
				target.hit(player, hit2, CombatStyle.MELEE);
			}
		}

		// Do extra action
		specialAttack.action(player, target, hit);
	}

	@Override
	public void handleRangeCombat(int weaponId, String ammoName, int weaponType, EventContainer container) {
		Tile currentTile = player.getTile();
		player.messageDebug("ranging...");

		int maxDist = 7;
		if (weaponType == WeaponType.CHINCHOMPA) {
			maxDist = 4;
		}

		// Are we in range?
		if (currentTile.distance(target.getTile()) > maxDist && !player.frozen() && !player.stunned()) {
			// currentTile = moveCloser();
			player.stepTowards(target, 2);
			return;
		}

		// if (player.pathQueue().projectilesBlocked(player, target)) {
		// player.stepTowards(target, 2);
		// return;
		// }

		if (player.getTile().equals(target.getTile())) {
			currentTile = moveCloser();
			return;
		}
		// player.pathQueue().clear();

		// Can we shoot?
		if (player.timers().has(TimerKey.COMBAT_ATTACK)) {
			return;
		}

		// Do we have ammo?
		if (weaponType != WeaponType.THROWN && weaponType != WeaponType.CHINCHOMPA && ammoName.equals("")
				&& weaponId != 4212) {
			player.message("There's no ammo left in your quiver.");
			container.stop();
			return;
		}

		// Check if ammo is of right type
		if (weaponType == WeaponType.CROSSBOW && !ammoName.contains(" bolts") && !ammoName.contains("Bolt rack")
				&& weaponId != 4212) {
			player.message("You can't use that ammo with your crossbow.");
			container.stop();
			return;
		}
		if (weaponType == WeaponType.BOW && !ammoName.contains(" arrow") && weaponId != 4212) {
			player.message("You can't use that ammo with your bow.");
			container.stop();
			return;
		}

		// Remove the ammo
		Projectile projectile = null;

		// crystal bow doesnt use ammo
		if (weaponId != 4212) {

			if (weaponType != WeaponType.THROWN && weaponType != WeaponType.CHINCHOMPA) {
				Item ammo = player.getEquipment().get(EquipSlot.AMMO);
				projectile = Projectile.getProjectileForAmmoName(ammo.definition(player.world()).name);
				player.getEquipment().set(EquipSlot.AMMO, new Item(ammo.getId(), ammo.getAmount() - 1));

				// If it is thrown
			} else if (weaponType == WeaponType.THROWN || weaponType == WeaponType.CHINCHOMPA) {
				Item item = player.getEquipment().get(EquipSlot.WEAPON);
				if (item != null) {
					// if
					// (item.definition(player.world()).name.contains("knife")
					// || item.definition(player.world()).name.contains("dart"))
					// {
					projectile = Projectile.getProjectileForAmmoName(item.definition(player.world()).name);
					player.getEquipment().set(EquipSlot.WEAPON, new Item(item.getId(), item.getAmount() - 1));
					// player.graphic(new Graphic(225, 92, 0));
					// }
				}
			}

		}

		if (player.isSpecialAttackEnabled()) {
			doRangeSpecial(player, weaponId);
			return;
		}

		if (projectile != null && projectile.getGfx() != null) {
			// if dark bow then another spawnprojec
			if (weaponId == 11235) {
				player.graphic(projectile.getDarkBowGfx());
				// crystal bow, other gfx
			} else {
				player.graphic(projectile.getGfx());
			}
		}

		player.animate(EquipmentInfo.attackAnimationFor(player));
		int distance = player.getTile().distance(target.getTile());
		int cyclesPerTile = 5;
		int baseDelay = 32;
		int startHeight = 35;
		int endHeight = 36;
		int curve = 15;
		int graphic = 228;

		if (weaponType == WeaponType.CROSSBOW) {
			cyclesPerTile = 3;
			baseDelay = 40;
			startHeight = 40;
			endHeight = 40;
			curve = 2;
			graphic = 27;
		}

		if (weaponType == WeaponType.BOW) {
			startHeight = 50;
			endHeight = 50;
		}

		if (projectile != null) {
			graphic = projectile.getProjectileId();
			startHeight = 50;
			endHeight = 50;
		}

		if (weaponType == WeaponType.THROWN) {
			startHeight = 48;
			endHeight = 40;
			baseDelay = 40;
			curve = 10;
		}

		if (weaponId == 6522) {
			cyclesPerTile = 5;
			baseDelay = 60;
			startHeight = 30;
			endHeight = 30;
			curve = 0;
		}

		if (weaponType == WeaponType.CHINCHOMPA) {
			baseDelay = 20;
			startHeight = 25;
			endHeight = 30;
		}

		// crystal bow projectileand gfx
		if (weaponId == 4212) {
			int tileDist = player.getTile().distance(target.getTile());
			player.world().spawnProjectile(player.getTile(), target, 249, 50, 36, 30, 5 * tileDist, 15, 105);
			player.graphic(new Graphic(256, 92, 10));
		} else {
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, baseDelay,
					cyclesPerTile * distance, curve, 105);
		}
		// if dark bow then another spawnprojec
		if (weaponId == 11235) {
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, baseDelay + 7,
					cyclesPerTile * distance, curve, 105);
		}

		long delay = Math.round(Math.floor(baseDelay / 30.0) + (distance * (cyclesPerTile * 0.020) / 0.6));

		boolean success = AccuracyFormula.doesHit(player, target, CombatStyle.RANGED);

		int maxHit = CombatFormula.maximumRangedHit(player);
		int hit = player.world().random(maxHit);

		triggerVeng(success ? hit : 0);

		// target.hit(player, success ? hit : 0,
		// delay).combatStyle(CombatStyle.RANGE);

		target.hit(player, success ? hit : 0, (int) delay, CombatStyle.RANGED);
		// if dark bow then another hit
		if (weaponId == 11235) {
			boolean success2 = AccuracyFormula.doesHit(player, target, CombatStyle.RANGED);
			int hit2 = player.world().random(maxHit);
			target.hit(player, success2 ? hit2 : 0, (int) delay, CombatStyle.RANGED);
		}

		// explode chin on target
		if (weaponType == WeaponType.CHINCHOMPA) {
			target.graphic(new Graphic(157, 92, 50));
		}

		// Timer is downtime.
		int weaponSpeed = player.world().equipmentInfo().weaponSpeed(weaponId);
		if (player.world().equipmentInfo().rapid(player))
			weaponSpeed--;
		player.timers().register(TimerKey.COMBAT_ATTACK, weaponSpeed);

		// After every attack, reset special.
		player.getVarps().setVarp(Varp.SPECIAL_ENABLED, 0);
	}

	private void doRangeSpecial(Player player, int weaponId) {
		player.timers().register(TimerKey.COMBAT_ATTACK, 5);
		SpecialAttacks specialAttack = SpecialAttacks.getSpecialAttack(player, weaponId);
		if (specialAttack == null) {
			return;
		}
		if (!drainSpecialEnergy(player, specialAttack.getSpecialDrain())) {
			return;
		}
		player.animate(specialAttack.getAnimationId());
		if (specialAttack.getGfx() != null) {
			player.graphic(specialAttack.getGfx());
		}
		if (specialAttack.getOpponentGfx() != null) {
			target.graphic(specialAttack.getOpponentGfx());
		}

		int tileDist = player.getTile().distance(target.getTile());
		if (specialAttack.getProjectileId() != -1) {
			player.world().spawnProjectile(player.getTile(), target, specialAttack.getProjectileId(), 40, 36, 32,
					5 * tileDist, 15, 105);

			if (specialAttack.isDoubleProjectile()) {
				player.world().spawnProjectile(player.getTile(), target, specialAttack.getProjectileId(), 40, 36, 52,
						5 * tileDist, 15, 105);
			}
		}

		double max = CombatFormula.maximumMeleeHit(player) * specialAttack.getMaxHitMultiplier();
		int hit = player.world().random().nextInt((int) Math.round(max));
		triggerVeng(hit);

		if (specialAttack.isHits()) {
			int delay = (int) Math.round(Math.floor(32 / 30.0) + (tileDist * (5 * 0.020) / 0.6));
			target.hit(player, hit, delay, CombatStyle.RANGED);

			if (specialAttack.isDoubleHit()) {
				int hit2 = player.world().random().nextInt((int) Math.round(max));
				target.hit(player, hit2, delay, CombatStyle.RANGED);
			}
		}

		// Do extra action
		specialAttack.action(player, target, hit);
	}

	public static void handleGraniteMaul(Player player, Entity target) {
		if (player.getSpecialEnergyAmount() < 50 * 10) {
			player.message("You do not have enough special energy.");
			return;
		}

		if (!player.touches(target, player.getTile())) {
			return;
		}

		player.setSpecialEnergyAmount(player.getSpecialEnergyAmount() - (50 * 10));

		player.animate(1667);
		player.graphic(340, 92, 0);

		double max = CombatFormula.maximumMeleeHit(player);
		int hit = player.world().random().nextInt((int) Math.round(max));
		target.hit(player, hit);
		player.timers().register(TimerKey.COMBAT_ATTACK, target.world().equipmentInfo().weaponSpeed(4153));
	}

	private boolean drainSpecialEnergy(Player player, int amount) {
		return player.drainSpecialEnergy(amount);
	}
}
