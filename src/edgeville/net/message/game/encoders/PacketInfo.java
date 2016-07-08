package edgeville.net.message.game.encoders;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Simon on 8/23/2014.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface PacketInfo {

	public int size();

}
