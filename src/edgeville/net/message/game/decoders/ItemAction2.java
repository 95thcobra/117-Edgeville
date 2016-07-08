package edgeville.net.message.game.decoders;

import java.util.List;

import edgeville.aquickaccess.actions.EquipmentRequirement;
import edgeville.aquickaccess.actions.EquipmentRequirement.Skill;
import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceText;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.util.CombatFormula;
import edgeville.util.EquipmentInfo;
import edgeville.util.Varp;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon.
 */
@PacketInfo(size = 8)
public class ItemAction2 extends ItemAction {

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		slot = buf.readUShortA();
		hash = buf.readIntV1();
		item = buf.readULEShort();
	}

	@Override
	protected int option() {
		return 1;
	}

	@Override
	public void process(Player player) {
		super.process(player);

		// Not possible when locked
		if (player.locked() || player.dead())
			return;

		// Stop player actions
		player.stopActions(false);

		Item item = player.getInventory().get(slot);
		if (item == null || item.getId() != this.item) {// Avoid reclicking
			player.messageDebug("item is null artm");
			return;
		}

		EquipmentInfo info = player.world().equipmentInfo();
		int targetSlot = info.slotFor(item.getId());
		
		
		// Check the item requirements
		List<EquipmentRequirement> equipmentRequirements = info.getEquipmentRequirements(player, item, targetSlot);
		if (equipmentRequirements != null) {
			boolean meetsRequirements = true;
			for (EquipmentRequirement equipmentRequirement : equipmentRequirements) {
				int level = equipmentRequirement.getLevel();
				Skill skill = equipmentRequirement.getSkill();

				if (player.skills().level(skill.getId()) < level) {
					player.message("You need a %s level of %d to equip %s.", skill.toString(), level, item.definition(player.world()).name);
					meetsRequirements = false;
					return;
				}
			}
			// This way we can show all the requirements not met.
			if (!meetsRequirements)
				return;
		}

		int type = info.typeFor(item.getId());
		if (targetSlot == -1) { // Cannot wear :-(
			player.messageDebug("cannot wear");
			return;
		}
		// Begin by setting the used item to null. This is to make it like osrs.
		// Failing is scary but no worries!
		player.getInventory().set(slot, player.getEquipment().get(targetSlot));

		// If type is 5 it is a two-handed weapon
		if (type == 5 && player.getEquipment().hasAt(EquipSlot.SHIELD)) {
			if (player.getInventory().add(player.getEquipment().get(EquipSlot.SHIELD), false).failed()) {
				player.message("You don't have enough free space to do that.");
				player.getInventory().set(slot, item);
				return;
			}
			player.getEquipment().set(EquipSlot.SHIELD, null);
		}
		
		player.disableAutocasting();

		// If it is a shield and we have a 2h weapon equipped, unequip it
		if (targetSlot == EquipSlot.SHIELD && player.getEquipment().hasAt(EquipSlot.WEAPON)) {
			if (info.typeFor(player.getEquipment().get(EquipSlot.WEAPON).getId()) == 5) { // Is
																							// this
																							// indeed
																							// a
																							// 2h
																							// weapon?
				if (player.getInventory().add(player.getEquipment().get(EquipSlot.WEAPON), false).failed()) {
					player.message("You don't have enough free space to do that.");
					player.getInventory().set(slot, item);
					return;
				}
				player.getEquipment().set(EquipSlot.WEAPON, null);
			}
		}

		// Weapons interrupt special attack
		if (targetSlot == EquipSlot.WEAPON) {
			player.getVarps().setVarp(Varp.SPECIAL_ENABLED, 0);
		}

		// Finally, equip the item we had in mind.
		player.getEquipment().set(targetSlot, item);
		refreshEquipStats(player);
	}

	public void refreshEquipStats(Player p) {
		EquipmentInfo.Bonuses playerBonuses = CombatFormula.totalBonuses(p, p.world().equipmentInfo());

		p.write(new InterfaceText(84, 23, "Stab: " + format(playerBonuses.stab)));
		p.write(new InterfaceText(84, 24, "Slash: " + format(playerBonuses.slash)));
		p.write(new InterfaceText(84, 25, "Crush: " + format(playerBonuses.crush)));
		p.write(new InterfaceText(84, 26, "Magic: " + format(playerBonuses.mage)));
		p.write(new InterfaceText(84, 27, "Range: " + format(playerBonuses.range)));

		p.write(new InterfaceText(84, 29, "Stab: " + format(playerBonuses.stabdef)));
		p.write(new InterfaceText(84, 30, "Slash: " + format(playerBonuses.slashdef)));
		p.write(new InterfaceText(84, 31, "Crush: " + format(playerBonuses.crushdef)));
		p.write(new InterfaceText(84, 32, "Magic: " + format(playerBonuses.magedef)));
		p.write(new InterfaceText(84, 33, "Range: " + format(playerBonuses.rangedef)));

		p.write(new InterfaceText(84, 35, "Melee strength: " + format(playerBonuses.str)));
		p.write(new InterfaceText(84, 36, "Ranged strength: " + format(playerBonuses.rangestr)));
		p.write(new InterfaceText(84, 37, "Magic damage: " + format(playerBonuses.magestr) + "%"));
		p.write(new InterfaceText(84, 38, "Prayer: " + format(playerBonuses.pray)));

		p.write(new InterfaceText(84, 40, "Undead: 0%"));
		p.write(new InterfaceText(84, 41, "Slayer: 0%"));
	}

	public String format(int bonus) {
		String prefix;

		if (String.valueOf(bonus).startsWith("-") || bonus == 0)
			prefix = "";
		else
			prefix = "+";

		return prefix + String.valueOf(bonus);
	}
}