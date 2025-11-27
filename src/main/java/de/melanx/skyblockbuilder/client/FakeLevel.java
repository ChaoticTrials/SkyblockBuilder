package de.melanx.skyblockbuilder.client;

import de.melanx.skyblockbuilder.SkyblockBuilder;
import io.netty.channel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ServerLinks;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.net.SocketAddress;
import java.util.Map;

public class FakeLevel extends ClientLevel {

    private static final ResourceKey<Level> FAKE_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, SkyblockBuilder.getInstance().resource("fake"));
    private static FakeLevel instance;

    public FakeLevel(RegistryAccess registryAccess) {
        super(FakeLevel.fakeClientPacketListener(registryAccess),
                new ClientLevelData(Difficulty.EASY, false, true),
                FakeLevel.FAKE_LEVEL_KEY,
                registryAccess.registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(BuiltinDimensionTypes.OVERWORLD),
                0, 0, Minecraft.getInstance()::getProfiler,
                new LevelRenderer(Minecraft.getInstance(),
                        Minecraft.getInstance().getEntityRenderDispatcher(),
                        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().renderBuffers()),
                false, 0);
    }

    public static FakeLevel getInstance(RegistryAccess registryAccess) {
        if (instance == null) {
            instance = new FakeLevel(registryAccess);
        }

        return instance;
    }

    private static CommonListenerCookie getFakeListenerCookie(RegistryAccess registryAccess) {
        return new CommonListenerCookie(
                Minecraft.getInstance().getGameProfile(),
                Minecraft.getInstance().getTelemetryManager().createWorldSessionManager(false, null, null),
                registryAccess.freeze(),
                FeatureFlags.DEFAULT_FLAGS,
                null, null, null, Map.of(), null, false, Map.of(), ServerLinks.EMPTY, ConnectionType.OTHER
        );
    }

    private static ClientPacketListener fakeClientPacketListener(RegistryAccess registryAccess) {
        Connection connection = new Connection(PacketFlow.CLIENTBOUND);
        connection.channel = new FakeChannel();
        CommonListenerCookie fakeListenerCookie = FakeLevel.getFakeListenerCookie(registryAccess);

        return new ClientPacketListener(Minecraft.getInstance(), connection, fakeListenerCookie);
    }

    private static class FakeChannel extends AbstractChannel {

        private static final ChannelMetadata METADATA = new ChannelMetadata(false);
        private final ChannelConfig config = new DefaultChannelConfig(this);

        protected FakeChannel() {
            super(null);
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return new FakeChannelUnsafe();
        }

        @Override
        protected boolean isCompatible(EventLoop loop) {
            return false;
        }

        @Override
        protected SocketAddress localAddress0() {
            return null;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return null;
        }

        @Override
        protected void doBind(SocketAddress localAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doDisconnect() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doClose() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doBeginRead() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void doWrite(ChannelOutboundBuffer in) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ChannelConfig config() {
            return this.config;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public ChannelMetadata metadata() {
            return METADATA;
        }

        private final class FakeChannelUnsafe extends AbstractUnsafe {
            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                promise.setFailure(new UnsupportedOperationException());
            }
        }
    }
}
