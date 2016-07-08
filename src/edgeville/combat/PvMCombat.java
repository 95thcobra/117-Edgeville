package edgeville.combat;

import edgeville.model.AttributeKey;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.item.Item;
import edgeville.script.TimerKey;
import edgeville.util.*;

/**
 * Created by Sky on 27-6-2016.
 */
public class PvMCombat extends PlayerVersusAnyCombat {

	private Player player;
	private Npc target;

	public PvMCombat(Player player, Npc target) {
		super(player, target);
		this.player = player;
		this.target = target;
	}

}
