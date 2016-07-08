package edgeville.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.combat.Graphic;
import edgeville.combat.magic.AncientSpell;
import edgeville.combat.magic.RegularDamageSpell;
import edgeville.combat.magic.Spell;
import edgeville.model.entity.*;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.NpcSyncInfo;
import edgeville.model.entity.player.PlayerSyncInfo;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.Skulls;
import edgeville.model.entity.player.skills.Prayers;
import edgeville.model.item.Item;
import edgeville.model.map.*;
import edgeville.net.message.game.encoders.PlaySound;
import edgeville.script.TimerKey;
import edgeville.script.TimerRepository;
import edgeville.util.CombatStyle;
import edgeville.util.Varbit;

import java.util.*;

/**
 * @author Simon on 8/22/2014.
 */
public abstract class Entity implements HitOrigin {

	private static final Logger logger = LogManager.getLogger(Entity.class);

	protected Tile tile;
	protected World world;
	protected int index;
	protected PathQueue pathQueue;
	protected Map<AttributeKey, Object> attribs = new EnumMap<>(AttributeKey.class);
	protected Queue<Hit> hits = new LinkedList<>();
	protected TimerRepository timers = new TimerRepository();
	private Map<Entity, Integer> damagers = new HashMap<>();

	private LockType lock = LockType.NONE;

	////// sj
	// private CombatBuilder combatBuilder = new CombatBuilder();

	private boolean damageOn;

	public boolean isDamageOn() {
		return damageOn;
	}

	public void setDamageOn(boolean damageOn) {
		this.damageOn = damageOn;
	}

	private Entity target;
	private Entity lastAttackedBy;

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public Entity getLastAttackedBy() {
		return lastAttackedBy;
	}

	public void setLastAttackedBy(Entity lastAttackedBy) {
		this.lastAttackedBy = lastAttackedBy;
	}

	/**
	 * Information on our synchronization
	 */
	protected SyncInfo sync;

	public Entity() {
		this.tile = new Tile(0, 0, 0);
		this.pathQueue = new PathQueue(this);
		this.damageOn = true;
	}

	public Entity(World world, Tile tile) {
		this.world = world;
		this.tile = new Tile(tile);
		this.pathQueue = new PathQueue(this);
		this.damageOn = true;
	}

	public int index() {
		return index;
	}

	public void index(int i) {
		index = i;
	}

	public World world() {
		return world;
	}

	public void world(World w) {
		world = w;
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	public PathQueue pathQueue() {
		return pathQueue;
	}

	public TimerRepository timers() {
		return timers;
	}

	public void move(Tile tile) {
		move(tile.x, tile.z, tile.level);
	}

	public void move(int x, int z) {
		move(x, z, 0);
	}

	public void move(int x, int z, int level) {
		tile = new Tile(x, z, level);
		sync.teleported(true);
		pathQueue.clear();
	}

	public SyncInfo sync() {
		return sync;
	}

	public void tryAnimate(int id) {
		if (!sync.hasFlag(isPlayer() ? PlayerSyncInfo.Flag.ANIMATION.value : NpcSyncInfo.Flag.ANIMATION.value))
			animate(id); // TODO all the block animations QQ
	}

	public void shout(String text) {
		sync.shout(text);
	}

	public void animate(int id) {
		sync.animation(id, 0);
	}

	public void animate(int id, int delay) {
		sync.animation(id, delay);
	}

	public void graphic(int id) {
		sync.graphic(id, 0, 0);
	}

	public void freeze(int time) {
		if (!timers.has(TimerKey.FROZEN)) {
			timers.extendOrRegister(TimerKey.FROZEN, time);
			pathQueue.clear();
			if (isPlayer())
				((Player) this).message("You have been frozen!");
		}
	}

	public void stun(int time) {
		graphic(254, 100, 0);
		if (!timers.has(TimerKey.STUNNED)) {
			timers.extendOrRegister(TimerKey.STUNNED, time);
			pathQueue.clear();
			if (isPlayer())
				((Player) this).message("You have been stunned!");
		}
	}

	public void graphic(int id, int height, int delay) {
		sync.graphic(id, height, delay);
	}

	public void graphic(Graphic graphic) {
		graphic(graphic.getId(), graphic.getHeight(), graphic.getDelay());
	}

	public <T> T getAttribute(AttributeKey key) {
		return (T) attribs.get(key);
	}

	public <T> T attribute(AttributeKey key, Object defaultValue) {
		return (T) attribs.getOrDefault(key, defaultValue);
	}

	public void clearattrib(AttributeKey key) {
		attribs.remove(key);
	}

	public void putAttribute(AttributeKey key, Object v) {
		attribs.put(key, v);
	}

	public void walkTo(Tile tile, PathQueue.StepType mode) {
		walkTo(tile.x, tile.z, mode);
	}

	public void walkTo(int x, int z, PathQueue.StepType mode) {
		// Are we frozen?
		if (frozen()) {
			message("A magical force stops you from moving.");
			return;
		}

		if (stunned()) {
			message("You're stunned!");
			return;
		}

		FixedTileStrategy target = new FixedTileStrategy(x, z);
		int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, size(), target, true, false);
		int[] bufferX = WalkRouteFinder.getLastPathBufferX();
		int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

		for (int i = steps - 1; i >= 0; i--) {
			pathQueue.interpolate(bufferX[i], bufferZ[i], mode);
		}
	}

	public void walkToNpc(Npc npc) {
		Tile npcTile = npc.getTile();

		pathQueue.clear();

		// Are we frozen?
		if (frozen()) {
			message("A magical force stops you from moving.");
			return;
		}

		if (stunned()) {
			message("You're stunned!");
			return;
		}

		FixedTileStrategy target = new FixedTileStrategy(npcTile.x, npcTile.z);
		int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 2, target, true, false);
		int[] bufferX = WalkRouteFinder.getLastPathBufferX();
		int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

		for (int i = steps - 1; i >= 0; i--) {
			pathQueue.interpolate(bufferX[i], bufferZ[i], PathQueue.StepType.REGULAR);
		}
	}

	public int size() {
		return 1;
	}

	public boolean walkTo(MapObj obj, PathQueue.StepType mode) {
		pathQueue.clear();

		// Are we frozen?
		if (frozen()) {
			message("A magical force stops you from moving.");
			return false;
		}

		if (stunned()) {
			message("You're stunned!");
			return false;
		}

		ObjectStrategy target = new ObjectStrategy(world, obj);
		int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 1, target, true, false);
		int[] bufferX = WalkRouteFinder.getLastPathBufferX();
		int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

		for (int i = steps - 1; i >= 0; i--) {
			pathQueue.interpolate(bufferX[i], bufferZ[i], mode);
		}

		return !WalkRouteFinder.isAlternative;
	}

	public boolean frozen() {
		return timers().has(TimerKey.FROZEN);
	}

	public boolean stunned() {
		return timers().has(TimerKey.STUNNED);
	}

	public Tile stepTowards(Entity e, int maxSteps) {
		return stepTowards(e, e.tile, maxSteps);
	}

	public Tile stepTowards(Entity e) {
		return stepTowards(e, e.tile, 20);
	}

	public Tile stepTowards(Entity e, Tile t, int maxSteps) {
		if (e == null)
			return tile;

		EntityStrategy target = new EntityStrategy(t);
		int steps = WalkRouteFinder.findRoute(world().definitions(), tile.x, tile.z, tile.level, 1, target, true, false);
		int[] bufferX = WalkRouteFinder.getLastPathBufferX();
		int[] bufferZ = WalkRouteFinder.getLastPathBufferZ();

		Tile last = tile;
		for (int i = steps - 1; i >= 0; i--) {
			maxSteps -= pathQueue.interpolate(bufferX[i], bufferZ[i], PathQueue.StepType.REGULAR, maxSteps);

			last = new Tile(bufferX[i], bufferZ[i], tile.level);
			if (maxSteps <= 0)
				break;
		}

		return last;
	}

	public boolean touches(Entity e) {
		return touches(e, tile);
	}

	public boolean touches(Entity e, Tile from) {
		EntityStrategy target = new EntityStrategy(e);
		int[][] clipAround = world.clipAround(e.getTile(), 5); // TODO better
																// algo for
																// determining
																// the size we
																// need..

		return target.canExit(from.x, from.z, 1, clipAround, e.tile.x - 5, e.tile.z - 5);
	}

	public Tile moveCloser(Entity target) {
		pathQueue().clear();

		int steps = pathQueue().running() ? 2 : 1;
		int otherSteps = target.pathQueue().running() ? 2 : 1;

		Tile otherTile = target.pathQueue().peekAfter(otherSteps) == null ? target.getTile() : target.pathQueue().peekAfter(otherSteps).toTile();
		stepTowards(target, otherTile, 25);
		return pathQueue().peekAfter(steps - 1) == null ? getTile() : pathQueue().peekAfter(steps - 1).toTile();
	}

	public boolean locked() {
		return lock == LockType.FULL;
	}

	public boolean moveLocked() {
		return lock == LockType.MOVEMENT;
	}

	public void lock() {
		lock = LockType.FULL;
	}

	public void lockMovement() {
		lock = LockType.MOVEMENT;
	}

	public void unlock() {
		lock = LockType.NONE;
	}

	public abstract void setHp(int hp, int exceed);

	public abstract int hp();

	public abstract int maxHp();

	public void message(String format, Object... params) {
		// Stub to ease player-specific messaging
	}

	public void heal(int amount) {
		heal(amount, 0);
	}

	public void heal(int amount, int exceed) {
		setHp(hp() + amount, exceed);
	}

	public Hit hit(HitOrigin origin, int hit) {
		return hit(origin, hit, 0, null);
	}

	public Hit hit(HitOrigin origin, int hit, int delay) {
		return hit(origin, hit, delay, null);
	}

	public Hit hit(HitOrigin origin, int hit, CombatStyle combatStyle) {
		return hit(origin, hit, 0, combatStyle);
	}

	public Hit hit(HitOrigin origin, int hit, int delay, CombatStyle combatStyle) {
		return hit(origin, hit, delay, null, combatStyle);
	}

	public Hit hit(HitOrigin origin, int oghit, int delay, Hit.Type type, CombatStyle combatStyle) {

		if (this instanceof Player) {
			Player player = (Player) this;

			boolean melee = player.getPrayer().isPrayerOn(Prayers.PROTECT_FROM_MELEE) && combatStyle == CombatStyle.MELEE;
			boolean ranged = player.getPrayer().isPrayerOn(Prayers.PROTECT_FROM_MISSILES) && combatStyle == CombatStyle.RANGED;
			boolean mage = player.getPrayer().isPrayerOn(Prayers.PROTECT_FROM_MAGIC) && combatStyle == CombatStyle.MAGIC;

			if (melee || ranged || mage) {
				if (origin instanceof Npc) {
					oghit = 0;
				} else {
					oghit *= 0.6;
				}
			}
		}

		int hit = oghit;

		Hit h = new Hit(hit, type != null ? type : hit > 0 ? Hit.Type.REGULAR : Hit.Type.MISS, delay).origin(origin);
		hits.add(h);

		if (origin instanceof Player) {
			damagers.compute(((Player) origin), (key, value) -> value == null ? hit : value + hit);
		}

		// 1.33 on controlled for each skill.
		if (origin instanceof Player) {
			Player player = (Player) origin;

			player.messageDebug("style:" + h.style().toString());

			if (combatStyle == CombatStyle.MELEE) {
				switch (((Player) origin).getCombatUtil().getAttackStyle()) {
				case ACCURATE:
					player.messageDebug("acc hit");
					((Player) origin).skills().addXp(Skills.ATTACK, hit * 4);
					break;
				case AGGRESSIVE:
					player.messageDebug("aggre hit");
					((Player) origin).skills().addXp(Skills.STRENGTH, hit * 4);
					break;
				case CONTROLLED:
					player.messageDebug("contr hit");
					((Player) origin).skills().addXp(Skills.ATTACK, hit * 1.33);
					((Player) origin).skills().addXp(Skills.STRENGTH, hit * 1.33);
					((Player) origin).skills().addXp(Skills.DEFENCE, hit * 1.33);
					break;
				case DEFENSIVE:
					player.messageDebug("def hit");
					((Player) origin).skills().addXp(Skills.DEFENCE, hit * 4);
					break;
				}
			} else if (combatStyle == CombatStyle.RANGED) {
				player.messageDebug("Range hit");
				((Player) origin).skills().addXp(Skills.RANGED, hit * 4);
			} else if (combatStyle == CombatStyle.MAGIC) {
				double baseXp;
				Spell spell = player.getLastCastedSpell();
				if (spell instanceof RegularDamageSpell) {
					RegularDamageSpell rdSpell = (RegularDamageSpell) spell;
					baseXp = rdSpell.getMagicExperience();
				} else if (spell instanceof AncientSpell) {
					AncientSpell aSpell = (AncientSpell) spell;
					baseXp = aSpell.getBaseMagicXp();
				} else {
					baseXp = 0;
				}
				if (player.getVarps().getVarbit(Varbit.AUTOCAST) == 1) {
					((Player) origin).skills().addXp(Skills.MAGIC, baseXp + (hit * 0.1));
					((Player) origin).skills().addXp(Skills.DEFENCE, hit * 1.33);
				} else {
					player.messageDebug("Mage hit");
					((Player) origin).skills().addXp(Skills.MAGIC, baseXp + (hit * 0.2));
				}
			}

			((Player) origin).skills().addXp(Skills.HITPOINTS, hit * 1.33);

			((Player) origin).sound(((Entity) origin).getAttackSound());
			if (this instanceof Player) {
				((Player) origin).setSkullHeadIcon(Skulls.WHITE_SKUL.getSkullId());
				((Player) origin).timers().extendOrRegister(TimerKey.SKULL, 2000); // 20

				int drain = (int) Math.round(0.25 * hit);
				((Player)this).skills().alterSkill(Skills.PRAYER, -drain, true);
			}
		}

		if (this instanceof Player) {
			((Player) this).sound(((Entity) origin).getAttackSound());
		}

		return h;

	}

	public Hit hit(HitOrigin origin, int hit, Hit.Type type, CombatStyle combatStyle) {
		return hit(origin, hit, 0, type, combatStyle);
	}

	public abstract int getAttackSound();

	public abstract int getBlockSound();

	public abstract int getBlockAnim();

	public void blockHit() {
		if (getAttribute(AttributeKey.LAST_ATTACKED_BY) instanceof Player) {
			Player target = getAttribute(AttributeKey.LAST_ATTACKED_BY);
			// target.message("playing sound");
			target.sound(getBlockSound());
		}
		if (this instanceof Player) {
			((Player) this).sound(getBlockSound());
		}
		animate(getBlockAnim());
	}

	public Map<Entity, Integer> damagers() {
		return damagers;
	}

	public Entity killer() {
		if (damagers.isEmpty())
			return null;

		Comparator<Map.Entry<Entity, Integer>> valueComparator = (e1, e2) -> e1.getValue().compareTo(e2.getValue());
		return damagers.entrySet().stream().sorted(valueComparator).findFirst().orElse(null).getKey();
	}

	public boolean dead() {
		// int queuedDamage = hits.stream().mapToInt(Hit::damage).sum();
		return hp()/* - queuedDamage */ < 1;
	}

	public void stopActions(boolean cancelMoving) {
		// this.message("stopping actions...");
		world.getEventHandler().stopCancellableEvents(this);
		// world.server().scriptExecutor().interruptFor(this);
		sync.faceEntity(null);
		// animate(-1);
		// graphic(-1);
		if (cancelMoving)
			pathQueue.clear();

		clearattrib(AttributeKey.TARGET);
	}

	public void face(Entity e) {
		sync.faceEntity(e);
	}

	public void faceObj(MapObj obj) {
		int x = obj.tile().x;
		int z = obj.tile().z;

		// Do some trickery to face properly
		if (tile.x == x && tile.z == z && (obj.type() == 0 || obj.type() == 5)) {
			if (obj.rot() == 0) {
				x--;
			} else if (obj.rot() == 1) {
				z++;
			} else if (obj.rot() == 2) {
				x++;
			} else if (obj.rot() == 3) {
				z--;
			}
		}

		int sx = obj.definition(world).sizeX;
		int sz = obj.definition(world).sizeY;

		sync.facetile(new Tile((int) (x * 2) + sx, (int) (z * 2) + sz));
	}

	public void faceTile(Tile tile) {
		sync.facetile(new Tile(tile.x * 2 + 1, tile.z * 2 + 1));
	}

	public void faceTile(double x, double z) {
		sync.facetile(new Tile((int) (x * 2) + 1, (int) (z * 2) + 1));
	}

	public void cycle() {
		timers.cycle();

		// Only process hits if not locked!
		if (!locked() && hp() > 0) {
			for (Iterator<Hit> it = hits.iterator(); it.hasNext() && hp() > 0;) {
				Hit hit = it.next();

				// TODO decrease delay
				if (hit.delay() <= 0) {
					int damage = hit.damage();

					// Protection prayers :)
					if (isPlayer()) {
						Player us = (Player) this;
						if (us.getVarps().getVarbit(Varbit.PROTECT_FROM_MELEE) == 1 && hit.style() == CombatStyle.MELEE) {
							damage -= damage * 0.4;
						} else if (us.getVarps().getVarbit(Varbit.PROTECT_FROM_MAGIC) == 1 && hit.style() == CombatStyle.MAGIC) {
							damage -= damage * 0.4;
						} else if (us.getVarps().getVarbit(Varbit.PROTECT_FROM_MISSILES) == 1 && hit.style() == CombatStyle.RANGED) {
							damage -= damage * 0.4;
						}
					}

					if (damage > hp())
						damage = hp();

					if (damageOn)
						setHp(hp() - damage, 0);
					sync.hit(hit.type().ordinal(), damage);

					if (hit.graphic() != null)
						graphic(hit.graphic());

					if (hit.block())
						blockHit();

					it.remove();
				} else {
					hit.delay(hit.delay() - 1);
				}
			}
		}

		if (hp() < 1 && !locked()) { // Avoid dieing while doing something
										// critical!
			hits.clear();
			die();
		}
	}

	public abstract boolean isPlayer();

	public abstract boolean isNpc();

	protected abstract void die();

}
