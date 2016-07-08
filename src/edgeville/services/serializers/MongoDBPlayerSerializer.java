package edgeville.services.serializers;

import java.util.function.Consumer;

import edgeville.model.entity.Player;
import edgeville.model.uid.UIDProvider;

/**
 * @author Simon on 4-3-2015.
 *
 * Serializer which utilizes a MongoDB to store and load player data.
 */
public class MongoDBPlayerSerializer extends PlayerSerializer {

	public MongoDBPlayerSerializer(UIDProvider provider) {
		super(provider);
	}

	@Override
	public boolean loadPlayer(Player player, Object i, String password, Consumer<PlayerLoadResult> fn) {
		fn.accept(PlayerLoadResult.OK);
		return true;
	}

	@Override
	public void savePlayer(Player player) {

	}
}
