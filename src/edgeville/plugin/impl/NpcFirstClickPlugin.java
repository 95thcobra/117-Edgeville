package edgeville.plugin.impl;

import edgeville.model.entity.Npc;
import edgeville.plugin.PluginContext;

/**
 * The plugin context for the npc first click message.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class NpcFirstClickPlugin implements PluginContext {

    /**
     * The npc that was clicked by the player.
     */
    private final Npc npc;

    /**
     * Creates a new {@link NpcFirstClickPlugin}.
     *
     * @param npc the npc that was clicked by the player.
     */
    public NpcFirstClickPlugin(Npc npc) {
        this.npc = npc;
    }

    /**
     * Gets the npc that was clicked by the player.
     *
     * @return the npc that was clicked.
     */
    public Npc getNpc() {
        return npc;
    }
}
