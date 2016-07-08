package edgeville.net.future;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * A {@link io.netty.channel.ChannelFutureListener} which automatically closes the channel on completion.
 *
 * @author Simon
 */
public class ClosingChannelFuture implements ChannelFutureListener {

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		future.channel().close();
	}

}
