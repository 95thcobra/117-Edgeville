package edgeville.combat.magic;

import org.apache.commons.lang3.StringUtils;

import edgeville.Constants;
import edgeville.combat.CombatUtil;
import edgeville.combat.Graphic;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.model.item.Item;
import edgeville.script.TimerKey;
import edgeville.util.AccuracyFormula;
import edgeville.util.CombatStyle;
import edgeville.util.TextUtil;

public class SpellOnTargetAction {

	private Player player;
	private Entity target;
	private int interfaceId;
	private int child;

	public SpellOnTargetAction(Player player, Entity target, int interfaceId, int child) {
		this.player = player;
		this.target = target;
		this.interfaceId = interfaceId;
		this.child = child;
	}

	public void start(boolean autocast) {
		switch (interfaceId) {
		case 218:
			if (player.getTile().equals(target.getTile())) {
				return;
			}
			if (!CombatUtil.canAttack(player, target)) {
				return;
			}
			if (target instanceof Player) {
				/*
				 * if (!player.timers().has(TimerKey.IN_COMBAT)) {
				 * player.interfaces().setBountyInterface(true); }
				 */
				player.timers().register(TimerKey.IN_COMBAT, 10);
				if (!player.inSafeArea()) {
					player.timers().register(TimerKey.AFTER_COMBAT, 5);
				}
				/*
				 * if (!target.timers().has(TimerKey.IN_COMBAT)) { ((Player)
				 * target).interfaces().setBountyInterface(true); }
				 */
				target.timers().register(TimerKey.IN_COMBAT, 10);
				if (!((Player)target).inSafeArea()) {
					target.timers().register(TimerKey.AFTER_COMBAT, 5);
				}

				player.setTarget(target);
				target.setLastAttackedBy(player);
			}

			if (autocast && player.isAutoCasting()) {
				autoCastSpell(player.getAutoCastingSpell());
				return;
			}
			handleDamageSpells();
			break;
		}

	}

	private void handleDamageSpells() {
		player.messageDebug("spell:" + child);
		switch (child) {

		// Regular
		case 2:
			cycleRegularDamageSpell(RegularDamageSpell.WIND_STRIKE);
			break;
		case 5:
			cycleRegularDamageSpell(RegularDamageSpell.WATER_STRIKE);
			break;
		case 7:
			cycleRegularDamageSpell(RegularDamageSpell.EARTH_STRIKE);
			break;
		case 9:
			cycleRegularDamageSpell(RegularDamageSpell.FIRE_STRIKE);
			break;

		case 11:
			cycleRegularDamageSpell(RegularDamageSpell.WIND_BOLT);
			break;
		case 15:
			cycleRegularDamageSpell(RegularDamageSpell.WATER_BOLT);
			break;
		case 18:
			cycleRegularDamageSpell(RegularDamageSpell.EARTH_BOLT);
			break;
		case 21:
			cycleRegularDamageSpell(RegularDamageSpell.FIRE_BOLT);
			break;

		case 25:
			cycleRegularDamageSpell(RegularDamageSpell.WIND_BLAST);
			break;
		case 28:
			cycleRegularDamageSpell(RegularDamageSpell.WATER_BLAST);
			break;
		case 34:
			cycleRegularDamageSpell(RegularDamageSpell.EARTH_BLAST);
			break;
		case 39:
			cycleRegularDamageSpell(RegularDamageSpell.FIRE_BLAST);
			break;

		case 46:
			cycleRegularDamageSpell(RegularDamageSpell.WIND_WAVE);
			break;
		case 49:
			cycleRegularDamageSpell(RegularDamageSpell.WATER_WAVE);
			break;
		case 53:
			cycleRegularDamageSpell(RegularDamageSpell.EARTH_WAVE);
			break;
		case 56:
			cycleRegularDamageSpell(RegularDamageSpell.FIRE_WAVE);
			break;

		// Ancients
		case 74:
			cycleAncientSpell(AncientSpell.SMOKE_RUSH);
			break;
		case 78:
			cycleAncientSpell(AncientSpell.SHADOW_RUSH);
			break;
		case 70:
			cycleAncientSpell(AncientSpell.BLOOD_RUSH);
			break;
		case 66:
			cycleAncientSpell(AncientSpell.ICE_RUSH);
			break;

		case 76:
			cycleAncientSpell(AncientSpell.SMOKE_BURST);
			break;
		case 80:
			cycleAncientSpell(AncientSpell.SHADOW_BURST);
			break;
		case 72:
			cycleAncientSpell(AncientSpell.BLOOD_BURST);
			break;
		case 68:
			cycleAncientSpell(AncientSpell.ICE_BURST);
			break;

		case 75:
			cycleAncientSpell(AncientSpell.SMOKE_BLITZ);
			break;
		case 79:
			cycleAncientSpell(AncientSpell.SHADOW_BLITZ);
			break;
		case 71:
			cycleAncientSpell(AncientSpell.BLOOD_BLITZ);
			break;
		case 67:
			cycleAncientSpell(AncientSpell.ICE_BLITZ);
			break;

		case 77:
			cycleAncientSpell(AncientSpell.SMOKE_BARRAGE);
			break;
		case 81:
			cycleAncientSpell(AncientSpell.SHADOW_BARRAGE);
			break;
		case 73:
			cycleAncientSpell(AncientSpell.BLOOD_BARRAGE);
			break;
		case 69:
			cycleAncientSpell(AncientSpell.ICE_BARRAGE);
			break;
		}
	}

	public Tile moveCloser() {
		player.pathQueue().clear();

		int steps = player.pathQueue().running() ? 2 : 1;
		int otherSteps = target.pathQueue().running() ? 2 : 1;

		Tile otherTile = target.pathQueue().peekAfter(otherSteps) == null ? target.getTile()
				: target.pathQueue().peekAfter(otherSteps).toTile();
		player.stepTowards(target, otherTile, 25);
		return player.pathQueue().peekAfter(steps - 1) == null ? player.getTile()
				: player.pathQueue().peekAfter(steps - 1).toTile();
	}

	private void cycleRegularDamageSpell(RegularDamageSpell spell) {
		cycleRegularDamageSpell(spell, false);
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

	private void cycleRegularDamageSpell(RegularDamageSpell spell, boolean autocast) {
		player.setLastSpellCastChild(child);
		player.setLastCastedSpell(spell);

		int levelReq = spell.getLevelReq();
		if (levelReq > player.skills().level(Skills.MAGIC)) {
			player.message("You need a magic level of %d to cast %s.", levelReq, spell.toString());
			return;
		}

		if (!spell.hasRunes(player)) {
			player.message("You do not have the required runes to cast %s.", spell.toString());
			return;
		}

		player.world().getEventHandler().addEvent(player, new Event() {

			@Override
			public void execute(EventContainer container) {
				if (!CombatUtil.canAttack(player, target)) {
					container.stop();
					return;
				}
				if (target.locked() || target.dead()) {
					container.stop();
					return;
				}
				doRegularDamageSpell(spell, container, false);
			}
		});
	}

	private void autoCastSpell(Spell spell) {
		if (spell instanceof AncientSpell) {
			player.world().getEventHandler().addEvent(player, new Event() {

				@Override
				public void execute(EventContainer container) {
					if (target.locked() || target.dead()) {
						container.stop();
						return;
					}
					if (!CombatUtil.canAttack(player, target)) {
						container.stop();
						return;
					}
					doAncientSpell((AncientSpell) spell, container, true);
				}
			});
		} else {
			player.world().getEventHandler().addEvent(player, new Event() {

				@Override
				public void execute(EventContainer container) {
					if (target.locked() || target.dead()) {
						container.stop();
						return;
					}
					doRegularDamageSpell((RegularDamageSpell) spell, container, true);
				}

			});
		}
	}

	private void cycleAncientSpell(AncientSpell spell) {
		player.setLastSpellCastChild(child);
		player.setLastCastedSpell(spell);

		int levelReq = spell.getLevelReq();
		if (levelReq > player.skills().level(Skills.MAGIC)) {
			player.message("You need a magic level of %d to cast %s.", levelReq, spell.toString());
			return;
		}

		if (!spell.hasRunes(player)) {
			player.message("You do not have the required runes to cast %s.", spell.toString());
			return;
		}

		spell.removeRunes(player);

		player.world().getEventHandler().addEvent(player, new Event() {

			@Override
			public void execute(EventContainer container) {
				if (target.locked() || target.dead()) {
					container.stop();
					return;
				}
				if (!CombatUtil.canAttack(player, target)) {
					container.stop();
					return;
				}
				doAncientSpell(spell, container, false);
			}
		});
	}

	private boolean doRegularDamageSpell(RegularDamageSpell spell, EventContainer container, boolean autocasting) {
		if (player.getTile().distance(target.getTile()) > 7 && !player.frozen() && !player.stunned()) {
			player.stepTowards(target, 2);
			return false;
		}
		if (player.timers().has(TimerKey.COMBAT_ATTACK)) {
			return false;
		}

		spell.removeRunes(player);

		int tileDist = player.getTile().distance(target.getTile());
		player.animate(spell.getAnimation());
		player.graphic(spell.getGfx(), 92, 0);

		if (spell.getProjectileId() > 0) {
			player.world().spawnProjectile(player.getTile(), target, spell.getProjectileId(), 40, 33, 55,
					spell.getProjectileSpeed() * tileDist, 15, 50);
		}

		int delay = (int) (1 + Math.floor(tileDist) / 2.0);
		player.timers().register(TimerKey.COMBAT_ATTACK, spell.getCombatDelayTicks());
		boolean success = AccuracyFormula.doesHit(player, target, CombatStyle.MAGIC);

		int hit = player.world().random(spell.getMaxHit());

		if (success) {
			target.hit(player, hit, delay, CombatStyle.MAGIC).graphic(new Graphic(spell.getGfxOther(), 92, 0));
			triggerVeng(hit);
			if (target instanceof Player) {
				// if (spell.getSoundId() > 0)
				// ((Player) target).sound(spell.getSoundId());
			}
			// player.sound(spell.getSoundId());
		} else {
			target.hit(player, 0, delay).graphic(new Graphic(85, 92, 0));
		}
		if (!autocasting)
			container.stop();
		return true;
	}

	private boolean doAncientSpell(AncientSpell spell, EventContainer container, boolean autocast) {
		if (player.getTile().distance(target.getTile()) > 7 && !player.frozen() && !player.stunned()) {
			player.stepTowards(target, 2);
			return false;
		}
		if (player.timers().has(TimerKey.COMBAT_ATTACK)) {
			return false;
		}

		spell.removeRunes(player);

		int tileDist = player.getTile().distance(target.getTile());
		player.animate(spell.getAnimation());

		if (spell.getProjectileId() > 0/* && !target.pathQueue().empty() */) {
			player.world().spawnProjectile(player.getTile(), target, spell.getProjectileId(), 0, 0, 36,
					spell.getSpeed() * tileDist, 0, 0);
		}

		int delay = (int) (1 + Math.floor(tileDist) / 2.0);
		player.timers().register(TimerKey.COMBAT_ATTACK, spell.getCombatDelayTicks());
		boolean success = AccuracyFormula.doesHit(player, target, CombatStyle.MAGIC);

		int hit = player.world().random(spell.getMaxHit());

		if (success) {
			target.hit(player, hit, delay, CombatStyle.MAGIC).graphic(spell.getGfx());
			triggerVeng(hit);
			if (target instanceof Player) {
				if (spell.getSoundId() > 0)
					((Player) target).sound(spell.getSoundId());
			}
			player.sound(spell.getSoundId());

			// TODO smoke poisons

			if (spell == AncientSpell.SHADOW_RUSH || spell == AncientSpell.SHADOW_BURST) {
				if (target instanceof Player)
					((Player) target).skills().alterSkill(Skills.ATTACK, 0.9);
			}
			if (spell == AncientSpell.SHADOW_BLITZ || spell == AncientSpell.SHADOW_BARRAGE) {
				if (target instanceof Player)
					((Player) target).skills().alterSkill(Skills.ATTACK, 0.85);
			}

			if (spell == AncientSpell.BLOOD_RUSH || spell == AncientSpell.BLOOD_BURST
					|| spell == AncientSpell.BLOOD_BLITZ || spell == AncientSpell.BLOOD_BARRAGE) {
				player.heal(hit / 4);
			}

			if (spell == AncientSpell.ICE_RUSH) {
				target.freeze(8); // 5 second freeze timer
			}

			if (spell == AncientSpell.ICE_BARRAGE) {
				target.freeze(17); // 10 second freeze timer
			}

			if (spell == AncientSpell.ICE_BLITZ) {
				target.freeze(25); // 15 second freeze timer
			}

			if (spell == AncientSpell.ICE_BARRAGE) {
				target.freeze(33); // 20 second freeze timer
			}

		} else {
			target.hit(player, 0, delay).graphic(new Graphic(85, 92, 0));
		}
		if (!autocast)
			container.stop();
		return true;
	}
}
