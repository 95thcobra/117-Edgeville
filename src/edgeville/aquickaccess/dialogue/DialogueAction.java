package edgeville.aquickaccess.dialogue;

import edgeville.aquickaccess.actions.SpellBook;
import edgeville.model.Locations;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.util.Varbit;

/**
 * Created by Sky on 21-6-2016.
 */
public class DialogueAction {

    private Player player;
    private int option;

    public DialogueAction(Player player, int option) {
        this.player = player;
        this.option = option;
    }

    public void handleDialog() {
        switch (player.getDialogueAction()) {
            // Teleport wizard
            case 1:
                handleTeleportWizard();
                break;

            // Spellbook altar
            case 2:
                handleSwitchSpellbook();
                break;
        }
        player.interfaces().closeChatDialogue();
    }

    ///////////// Add new dialogues below ///////////////////

    private void handleTeleportWizard() {
        switch (option) {
            case 1:
                player.teleport(Locations.EDGEVILLE.getTile());
                player.message("You have teleported to Edgeville.");
                break;
            case 2:
                player.teleport(Locations.VARROCK.getTile());
                player.message("You have teleported to Varrock.");
                break;
            case 3:
                player.teleport(Locations.FALADOR.getTile());
                player.message("You have teleported to Falador.");
                break;
        }
    }

    private void handleSwitchSpellbook() {
        switch (option) {
            case 1:
                player.message("You have switched to modern magic.");
                player.disableAutocasting();
                player.getVarps().setVarbit(Varbit.SPELLBOOK, SpellBook.REGULAR); // Modern
                break;
            case 2:
                player.message("You have switched to ancient magic.");
                player.disableAutocasting();
                player.getVarps().setVarbit(Varbit.SPELLBOOK, SpellBook.ANCIENTS); // ancients
                break;
            case 3:
                player.message("You have switched your to lunar magic.");
                player.disableAutocasting();
                player.getVarps().setVarbit(Varbit.SPELLBOOK, SpellBook.LUNAR);// lunar
                break;
        }
    }
}
