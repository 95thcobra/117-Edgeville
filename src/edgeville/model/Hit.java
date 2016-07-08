package edgeville.model;

import edgeville.combat.Graphic;
import edgeville.util.CombatStyle;

/**
 * @author Simon on 8/12/2015.
 */
public class Hit {

	private int damage;
	private Type type;
	private int delay; // Ticks
	private HitOrigin origin;
	private Graphic graphic = null;
	private boolean doBlock = true;
	private CombatStyle style = CombatStyle.MELEE;

	public Hit(int damage) {
		this(damage, null);
	}

	public Hit(int damage, Type type) {
		this.damage = damage;
		this.type = type;
	}

	public Hit(int damage, Type type, int delay) {
		this.damage = damage;
		this.type = type;
		this.delay = delay;
	}

	public int damage() {
		return damage;
	}

	public Type type() {
		return type;
	}

	public int delay() {
		return delay;
	}

	public void delay(int d) {
		delay = d;
	}

	public Hit block(boolean b) {
		doBlock = b;
		return this;
	}

	public Hit origin(HitOrigin origin) {
		this.origin = origin;
		return this;
	}

	public CombatStyle style() {
		return style;
	}

	public Hit combatStyle(CombatStyle style) {
		this.style = style;
		return this;
	}

	public boolean fromEntity() {
		return origin instanceof Entity;
	}

	public boolean block() {
		return doBlock;
	}

	public HitOrigin origin() {
		return origin;
	}

	public Hit graphic(Graphic graphic) {
		this.graphic = graphic;
		return this;
	}

	public Hit graphic(int graphicId) {
		this.graphic = new Graphic(graphicId, 0, 0);
		return this;
	}

	public Graphic graphic() {
		return graphic;
	}

	public static enum Type {
		MISS, REGULAR, POISON, DISEASE, OGRE_DISEASE, VENOM
	}

}
