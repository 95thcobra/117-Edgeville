package edgeville.handlers;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceItem;
import edgeville.net.message.game.encoders.InterfaceText;

/**
 * @author William Talleur <talleurw@gmail.com>
 * @date 11/14/2015
 */
public class SkillDialogueHandler {

    private final Player player;
    private final SkillDialogue type;
    private final Object[] data;

    public SkillDialogueHandler(final Player player, final SkillDialogue type, final Object...data) {
        this.player = player;
        this.type = type;
        this.data = data;
    }

    public void open(Player player, SkillDialogue type) {
        type.display(player, this);
    }

    public void create(final int amount, int index) {}

    public int getAll(int index) {
        return 0;
    }

    public Player getPlayer() {
        return player;
    }

    public SkillDialogue getType() {
        return type;
    }

    public Object[] getData() {
        return data;
    }

    protected String getName(Item item) {
        return item.definition(player.world()).name;
    }

    public enum SkillDialogue {

        ONE_OPTION(309, 5, 1) {
            @Override
            public void display(Player player, SkillDialogueHandler handler) {
                final Item item = (Item) handler.getData()[0];
                player.interfaces().send(309, 162, 546, false);
                player.write(new InterfaceText(309, 6, "<br><br><br><br>" + item.definition(player.world()).name));
                player.write(new InterfaceItem(309, 2, 160, item.getId()));
            }

            @Override
            public int getAmount(SkillDialogueHandler handler, final int buttonId) {
                return buttonId == 6 ? 1 : buttonId == 5 ? 5 : buttonId == 4 ? -1 : handler.getAll(getIndex(handler, buttonId));
            }
        },

        TWO_OPTION(303, 0, 2) {
            @Override
            public void display(Player player, SkillDialogueHandler handler) {

            }

        },

        THREE_OPTION(304, 5, 3) {
            @Override
            public void display(Player player, SkillDialogueHandler handler) {
                Item item;
                player.interfaces().send(304, 162, 546, false);
                for (int i = 0; i < 3; i++) {
                    item = (Item) handler.getData()[i];
                    player.write(new InterfaceItem(304, 2 + i, 135, item.getId()));
                    player.write(new InterfaceText(304, (304 - 296) + (i * 4), "<br><br><br><br>" + item.definition(player.world()).name));
                }
            }
        },

        FIVE_OPTION(306, 7, 5) {

            @Override
            public void display(Player player, SkillDialogueHandler handler) {
                Item item;
                player.interfaces().send(306, 162, 546, false);
                for (int i = 0; i < handler.getData().length; i++) {
                    item = (Item) handler.getData()[i];
                    player.write(new InterfaceText(306, 10 + (4 * i), "<br><br><br><br>" + handler.getName(item)));
                    player.write(new InterfaceItem(306, 2 + i, 160, item.getId()));
                }
            }
        };

        private final int interfaceId;
        private final int baseButton;
        private final int length;

        SkillDialogue(final int interfaceId, final int baseButton, final int length) {
            this.interfaceId = interfaceId;
            this.baseButton = baseButton;
            this.length = length;
        }

        public void display(final Player player, final SkillDialogueHandler handler) {}

        public int getAmount(SkillDialogueHandler handler, final int buttonId) {
            for (int index = 0; index < 4; index++) {
                for (int idx = 0; idx < length; idx++) {
                    int val = (baseButton + index) + (4 * idx);
                    if (val == buttonId) {
                        return index == 3 ? 1 : index == 2 ? 5 : index == 1 ? 10 : -1;
                    }
                }
            }
            return 0;
        }

        public int getIndex(SkillDialogueHandler handler, final int buttonId) {
            int index = 0;
            for (int i = 0; i < 4; i++) {
                for (int j = 1; j < length; j++) {
                    int val = (baseButton + j) + (4 * j);
                    if (val == buttonId) {
                        return j + 1;
                    } else if (val <= buttonId) {
                        j++;
                    }
                }
                i = 0;
            }
            return index;
        }
    }
}