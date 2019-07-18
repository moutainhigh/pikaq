package io.github.pikaq.network.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import io.github.pikaq.common.util.PortUtils;
import io.github.pikaq.network.NetworkLocationEnum;
import io.github.pikaq.network.RemotingContext;
import io.github.pikaq.network.RemotingContextHolder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public abstract class AbstractServer implements Server {

	protected List<Thread> shutdownHooks = new ArrayList<>();

	protected EventLoopGroup bossGroup;
	protected EventLoopGroup workGroup;

	@Override
	public void start(ServerConfig serverConfig) {

		Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			
			validate(serverConfig);
			
			logger.info("[{}]开始启动", getServerName());

			final ServerBootstrap bootstrap = new ServerBootstrap();
			this.bossGroup = new NioEventLoopGroup();
			this.workGroup = new NioEventLoopGroup();

			bootstrap.group(bossGroup, workGroup)
					.localAddress(new InetSocketAddress(serverConfig.getListeningPort()))
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, serverConfig.getSoBacklog())
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
					.childOption(ChannelOption.SO_KEEPALIVE, false).childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {

						}
					});

			// 即使是同步等待，依然可以回调
			ChannelFuture f = bootstrap.bind().sync();
			f.addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					logger.info("[{}]服务已启动，监听端口：{}", getServerName(), serverConfig.getListeningPort());
				}
			});
			RemotingContextHolder
					.set(RemotingContext.create()
							.channel(f.channel())
							.serverConfig(serverConfig)
							.build());

			// 最后同步阻塞线程不退出
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			logger.error("[{}]服务启动失败， ", getServerName(), e);
		} finally {
			logger.info("[{}]服务即将关闭，服务运行：cost：{}ms", getServerName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
			shutdown();
		}
	}

	@Override
	public void shutdown() {
		logger.info("{} shutdown netty byebye..", getServerName());
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
			bossGroup = null;
		}
		if (workGroup != null) {
			workGroup.shutdownGracefully();
			workGroup = null;
		}
		RemotingContextHolder.clear();
		doClose();
	}

	protected abstract void doStart(RemotingContext serverInfo);

	protected abstract void doClose();

	protected void validate(ServerConfig serverConfig) throws IllegalArgumentException {
		int soBacklog = serverConfig.getSoBacklog();
		Preconditions.checkArgument(soBacklog > 0, "Socket连接缓冲队列大小配置错误，必须大于0");
		int publicThreadPoolNums = serverConfig.getPublicThreadPoolNums();
		Preconditions.checkArgument(publicThreadPoolNums > 0, "公共线程池数配置错误，必须大于0");
		int listeningPort = serverConfig.getListeningPort();
		Preconditions.checkArgument(listeningPort > 0, "监听放端口配置错误，请设置值");
		Preconditions.checkArgument(PortUtils.checkPortAvailable(listeningPort), listeningPort + "端口被占用，请重新配置。");
	}

	@Override
	public NetworkLocationEnum networkLocation() {
		return NetworkLocationEnum.SERVER;
	}

	@Override
	public void registerShutdownHooks(Thread... hooks) {
		List<Thread> h = Arrays.asList(hooks);
		h.forEach(hook -> Runtime.getRuntime().addShutdownHook(hook));
		this.shutdownHooks.addAll(h);
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
}
