package edgeville.model.entity.player;

import java.util.ArrayList;
import java.util.List;

import edgeville.bank.BankTab;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.interfaces.inputdialog.NumberInputDialog;
import edgeville.model.item.Item;
import edgeville.model.item.ItemContainer;
import edgeville.model.item.ItemContainer.Type;
import edgeville.net.message.game.encoders.InterfaceSettings;
import edgeville.net.message.game.encoders.InvokeScript;
import edgeville.util.Varbit;

public class Bank {
	private Player player;
	private List<Item> bankItems;

	public List<Item> getBankItems() {
		return bankItems;
	}

	private int currentBankTab;

	public Bank(Player player) {
		this.player = player;
		bankItems = new ArrayList<Item>(800);
		currentBankTab = -1;
		makeDirty();
	}

	public void add(Item item) {
		if (bankItems.size() == 800) {
			player.message("Bank is full!");
			return;
		}
		bankItems.add(item);
	}

	/**
	 * When you move an item over an other item.
	 * 
	 * @param slotOther
	 * @param itemOther
	 * @param slot
	 * @param itemId
	 */
	public void moveItemOnItem(int itemId, int slot, int itemOther, int slotOther, int hashthing, int hasthing2) {
		if (hashthing == 10 && slotOther >= 10 && slotOther <= 20) {
			// dragging a tab into a tab.
			if (hasthing2 == 10) {
				return;
			}
			draggingToTabs(slot, slotOther);
			player.message("Draggin to tab");
			return;
		}

		if (isInsertEnabled()) {
			// player.message("here2");
			shiftItems(slot, slotOther);
			return;
		}

		swapItem(slot, slotOther);
	}

	private void changeBankTabSize(int bankTab, int change) {
		int currentSize = getBankTabSize(bankTab);
		if (bankTab > -1) {
			int varbit = Varbit.BANK_TAB + bankTab;
			player.getVarps().setVarbit(varbit, currentSize + change);
			// player.message("varbit: %d, change: %d -> %d", varbit,
			// currentSize, currentSize + change);
		}
	}

	private int getBankTabSize(int bankTab) {
		return player.getVarps().getVarbit(Varbit.BANK_TAB + bankTab);
	}

	public int getBankTabOfSlot(int slot) {
		slot++;
		int[] sizes = new int[10];
		for (int i = 0; i < 9; i++) {
			sizes[i] = this.getBankTabSize(i);
		}

		int cnt = 0;
		for (int i = 0; i < sizes.length; i++) {
			if (slot >= cnt) {
				int size = sizes[i]; // size of tab i
				cnt += size;
				if (slot <= cnt) {
					return i;
				}
			}
		}

		return -1;
	}

	private boolean bankFull() {
		if (bankItems.size() == 800) {
			player.message("Bank is full!");
			return true;
		}
		return false;
	}

	private void shiftItems(int slot, int slotOther) {
		if (bankFull()) {
			return;
		}
		if (slot >= bankItems.size() || slotOther >= bankItems.size()) {
			return;
		}

		Item itemToInsert = bankItems.get(slot);
		int bankTabFrom = getBankTabOfSlot(slot);
		int bankTabTo = getBankTabOfSlot(slotOther);
		changeBankTabSize(bankTabFrom, -1);
		changeBankTabSize(bankTabTo, 1);
		bankItems.remove(slot);
		final boolean SAME_BANK_AND_MAIN = bankTabTo == bankTabFrom && bankTabTo == -1;
		if (!SAME_BANK_AND_MAIN && bankTabTo == -1 || (bankTabTo >= 1 && bankTabFrom < bankTabTo)) {
			slotOther--;
		}
		bankItems.add(slotOther, itemToInsert);
		makeDirty();
	}

	private void swapItem(int slot, int slotOther) {
		Item item1 = bankItems.get(slot);
		Item item2 = bankItems.get(slotOther);
		bankItems.set(slot, item2);
		bankItems.set(slotOther, item1);
		makeDirty();
	}

	public void draggingToTabs(int slot, int slotOther) {
		if (bankFull()) {
			return;
		}
		player.message("slot: %d, slot2: %d", slot, slotOther);

		if (slotOther == 10) {
			Item itemToPutToFirst = bankItems.get(slot);

			// If item comes from a tab, deduct size.
			if (currentBankTab >= 0) {
				this.changeBankTabSize(currentBankTab, -1);
			}

			int bankTabFrom = getBankTabOfSlot(slot);
			player.message("ITEM COMES FROM BANK TAB: %d", bankTabFrom);
			this.changeBankTabSize(bankTabFrom, -1);

			bankItems.remove(slot);

			bankItems.add(itemToPutToFirst);
			makeDirty();
			return;
		}

		Item itemToPutToFirst = bankItems.get(slot);

		// If item comes from a tab, deduct size.
		if (currentBankTab >= 0) {
			this.changeBankTabSize(currentBankTab, -1);
		}

		int bankTabFrom = this.getBankTabOfSlot(slot);
		player.message("ITEM COMES FROM BANK TAB: %d", bankTabFrom);
		changeBankTabSize(bankTabFrom, -1);

		bankItems.remove(slot);

		int bankTab = slotOther - 11; // Starting from 0
		int slotToInsertAt = getSlotToInsertAt(bankTab);

		bankItems.add(slotToInsertAt, itemToPutToFirst);

		changeBankTabSize(bankTab, 1);

		makeDirty();
	}

	/**
	 * Gets the last slot of a bankTab.
	 * 
	 * @param bankTab
	 * @return
	 */
	public int getSlotToInsertAt(int bankTab) {
		if (bankTab == -1) {
			return bankItems.size();
		}

		int[] sizes = new int[9]; // Excluding main tab.
		int sizeCount = 0;
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = player.getVarps().getVarbit(Varbit.BANK_TAB + i);
		}

		for (int i = 0; i < sizes.length; i++) {
			sizeCount += sizes[i];
			if (i == bankTab) {
				return sizeCount;
			}
		}
		return -1;
	}

	/**
	 * Get the bank tab of slot.
	 * 
	 * @param slot
	 * @return
	 */
	public int getBankTab(int slot) {
		int[] sizes = new int[9]; // Excluding main tab.
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = player.getVarps().getVarbit(Varbit.BANK_TAB + i);
		}

		int slotAmt = 0;
		for (int i = sizes.length - 1; i >= 0; i--) {

			slotAmt += sizes[i];// 0

			if (slot == 0)
				slot++;
			if (slotAmt >= slot) {// 0 == 0 true
				// player.message("slotamt: %d slot: %d", slotAmt, slot);
				return i;
			}
		}
		return -1;
	}

	public boolean isInsertEnabled() {
		return player.getVarps().getVarbit(Varbit.BANK_INSERT) == 1;
	}

	public void handleClick(int buttonId, int slot, int option) {
		switch (buttonId) {
		case 10:
			currentBankTab = slot - 10;
			// player.message("currentbanktab:%d", currentBankTab);
			break;
		case 12:
			if (option == 9) {
				Item item = bankItems.get(slot + 1);
				player.message(player.world().examineRepository().item(item.getId()));
				return;
			}
			withdraw(buttonId, slot, option);
			break;
		case 16:
			player.getVarps().setVarbit(Varbit.BANK_INSERT, 0);
			break;
		case 18:
			player.getVarps().setVarbit(Varbit.BANK_INSERT, 1);
			break;
		case 21:
			player.getVarps().setVarbit(Varbit.BANK_WITHDRAW_NOTE, 0);
			break;
		case 23:
			player.getVarps().setVarbit(Varbit.BANK_WITHDRAW_NOTE, 1);
			break;

		// bank all inv
		case 27:
			Item[] inv = player.getInventory().getItems();
			for (int i = 0; i < inv.length; i++) {
				Item item = inv[i];
				if (item == null)
					continue;
				moveItemFromInventoryToBank(item.getId(), item.getAmount());
			}
			break;

		// bank all equip
		case 29:
			Item[] equip = player.getEquipment().getItems();
			for (int i = 0; i < equip.length; i++) {
				Item item = equip[i];
				if (item == null)
					continue;
				moveItemFromEquipmentToBank(item.getId(), item.getAmount());
			}
			break;

		// tab options
		case 32:
			player.getVarps().setVarbit(Varbit.BANK_OPTIONS, slot + 1);
			break;
		}
	}

	private boolean dirty;

	public void clean() {
		dirty = false;
	}

	public void makeDirty() {
		dirty = true;
	}

	// dont touch below

	public boolean isDirty() {
		return dirty;
	}

	public void open() {
		if (player.inWilderness()) {
			player.message("You cannot do this while in the wilderness.");
			return;
		}
		player.write(new InvokeScript(917, -1, -2147483648)); // big screen
		player.interfaces().sendMain(12, false);
		player.interfaces().send(15, player.interfaces().activeRoot(), (player.interfaces().resizable() ? 58 : 60), false);

		player.write(new InterfaceSettings(12, 12, 0, 799, 1311998));
		player.write(new InterfaceSettings(12, 12, 809, 817, 2));
		player.write(new InterfaceSettings(12, 12, 818, 827, 1048576));
		player.write(new InterfaceSettings(12, 10, 10, 10, 1048578));
		player.write(new InterfaceSettings(12, 10, 11, 19, 1179714));

		player.write(new InterfaceSettings(15, 3, 0, 27, 1181438));
		player.write(new InterfaceSettings(15, 12, 0, 27, 1054));

		player.write(new InterfaceSettings(12, 32, 0, 3, 2));

		makeDirty();
	}

	public int determineAmountToDeposit(int option, int totalAmount, int id) {
		int amount = 1;

		switch (option) {
		case 1:
			amount = 1;
			break;
		case 2:
			amount = 5;
			break;
		case 3:
			amount = 10;
			break;
		case 6:
			amount = player.getInventory().count(id);
			break;
		}

		return amount;
	}

	public void withdraw(int buttonId, int slot, int option) {
		slot++;
		Item item = bankItems.get(slot);
		int id = item.getId();

		// X
		if (option == 4) {
			NumberInputDialog var = new NumberInputDialog(player) {
				@Override
				public void doAction(int value) {
					moveItemsToInventory(id, value);
				}
			};
			var.send();
			return;
		}

		int amount = determineAmountToWithdraw(option, item.getAmount());
		// player.messageDebug("Amount to withdraw: %d", amount);
		moveItemsToInventory(id, amount);
	}

	public void moveItemsToInventory(int id, int amount) {
		int unnotedId = new Item(id).definition(player.world()).unnotedID;

		int idToAdd;
		if (player.getVarps().getVarbit(Varbit.BANK_WITHDRAW_NOTE) == 1 && unnotedId > 0 && unnotedId > id) {
			idToAdd = unnotedId;
		} else {
			idToAdd = id;
		}
		if (!this.contains(id, amount)) {
			return;
		}

		if (player.getInventory().add(idToAdd, amount).success()) {
			remove(id, amount);
		}
		makeDirty();
	}

	private void remove(int id, int amount) {
		for (int i = 0; i < bankItems.size(); i++) {
			Item item = bankItems.get(i);
			if (item.getId() != id) {
				continue;
			}
			if (item.getAmount() > amount) {
				bankItems.set(i, new Item(item.getId(), item.getAmount() - amount));
				return;
			}
			int slot = getSlotForItem(id);
			int bankTab = getBankTab(slot);
			changeBankTabSize(bankTab, -1);
			bankItems.remove(i);
		}
	}

	public int determineAmountToWithdraw(int option, int totalAmount) {
		int amount = 1;

		switch (option) {
		case 0:
			amount = 1;
			break;
		case 1:
			amount = 5;
			break;
		case 2:
			amount = 10;
			break;
		case 5:
			amount = totalAmount;
			break;
		case 6:
			amount = totalAmount - 1;
			break;
		}

		return amount;
	}

	public void deposit(int buttonId, int slot, int option) {
		if (bankFull()) {
			return;
		}
		slot++;

		// The selected item.
		Item item = player.getInventory().get(slot);

		if (item == null)
			return;

		int id = item.getId();

		// X
		if (option == 5) {
			NumberInputDialog var = new NumberInputDialog(player) {
				@Override
				public void doAction(int value) {
					moveItemFromInventoryToBank(id, value);
				}
			};
			var.send();
			return;
		}
		int amount = determineAmountToDeposit(option, item.getAmount(), id);
		moveItemFromInventoryToBank(id, amount);
	}

	private boolean contains(int id) {
		for (Item item : bankItems) {
			if (item.getId() == id) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(int id, int amount) {
		for (Item item : bankItems) {
			if (item.getId() == id && item.getAmount() >= amount) {
				return true;
			}
		}
		return false;
	}

	private int getSlotForItem(int itemId) {
		for (int i = 0; i < bankItems.size(); i++) {
			Item item = bankItems.get(i);
			if (item.getId() == itemId) {
				return i;
			}
		}
		return -1;
	}

	private void moveItemFromEquipmentToBank(int id, int amount) {
		if (player.getEquipment().remove(id, amount).success()) {
			add(id, amount);
		}
	}

	private void add(int id, int amount) {
		if (bankFull()) {
			return;
		}
		int unnotedId = new Item(id).definition(player.world()).unnotedID;
		int idToAdd;
		if (unnotedId > 0 && unnotedId < id) {
			idToAdd = unnotedId;
		} else {
			idToAdd = id;
		}

		if (contains(idToAdd)) {
			int slot = getSlotForItem(idToAdd);
			Item item = bankItems.get(slot);
			bankItems.set(slot, new Item(idToAdd, item.getAmount() + amount));
		} else {
			int slotToInsertAt = this.getSlotToInsertAt(currentBankTab);
			if (currentBankTab > -1) {
				this.changeBankTabSize(currentBankTab, 1);
			}
			bankItems.add(slotToInsertAt, new Item(idToAdd, amount));
		}
		makeDirty();
	}

	private void moveItemFromInventoryToBank(int id, int amount) {
		if (bankFull()) {
			return;
		}
		if (player.getInventory().remove(id, amount).success()) {
			add(id, amount);
		}
	}
}
