package edgeville.plugin;

import edgeville.model.entity.Player;

/**
 * The actual listener that is executed for {@link PluginContext}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public interface PluginListener<T extends PluginContext> {

    /**
     * Executes the code within this listener.
     *
     * @param player
     *            the player to execute this for.
     * @param context
     *            the context to execute this listener in.
     */
    void execute(Player player, T context);
}
