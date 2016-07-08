package edgeville.aquickaccess.actions;

import edgeville.util.TextUtil;

public class EquipmentRequirement {

	public enum Skill {
		ATTACK(0),
		DEFENCE(1),
		STRENGTH(2),
		HITPOINTS(3),
		RANGED(4),
		PRAYER(5),
		MAGIC(6),
		COOKING(7),
		WOODCUTTING(8),
		FLETCHING(9),
		FISHING(10),
		FIREMAKING(11),
		CRAFTING(12),
		SMITHING(13),
		MINING(14),
		HERBLORE(15),
		AGILITY(16),
		THIEVING(17),
		SLAYER(18),
		FARMING(19),
		RUNECRAFTING(20),
		HUNTER(21),
		CONSTRUCTION(22),
		SUMMONING(23);
		private int id;

		Skill(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		@Override
		public String toString() {
			return TextUtil.formatEnum(name());
		}
	}

	private int level;
	private Skill skill;

	public EquipmentRequirement(Skill skill, int level) {
		this.level = level;
		this.skill = skill;
	}

	public int getLevel() {
		return level;
	}

	public Skill getSkill() {
		return skill;
	}

}
