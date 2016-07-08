package edgeville.plugin.impl;

import edgeville.plugin.PluginContext;

/**
 * The plugin context for the clicking buttons message.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class ButtonClickPlugin implements PluginContext {

    /**
     * The identifier for the button that was clicked.
     */
    private final int id;

    /**
     * Creates a new {@link ButtonClickPlugin}.
     *
     * @param id the identifier for the button that was clicked.
     */
    public ButtonClickPlugin(int id) {
        this.id = id;
    }

    /**
     * Gets the identifier for the button that was clicked.
     *
     * @return the identifier for the button.
     */
    public int getId() {
        return id;
    }
}
