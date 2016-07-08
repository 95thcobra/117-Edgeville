package edgeville.combat;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.*;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.item.Item;
import edgeville.script.TimerKey;
import edgeville.util.*;

/**
 * Created by Sky on 25-6-2016.
 */
public class PvPCombat extends PlayerVersusAnyCombat {
	private Player player;
	private Player target;

	public PvPCombat(Player player, Player target) {
		super(player, target);
		this.player = player;
		this.target = target;
	}
}
