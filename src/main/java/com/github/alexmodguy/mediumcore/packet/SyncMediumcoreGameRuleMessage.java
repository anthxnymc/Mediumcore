package com.github.alexmodguy.mediumcore.packet;

import com.github.alexmodguy.mediumcore.Mediumcore;
import toni.lib.networking.ToniPacket;

#if AFTER_21
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
#else
import toni.lib.networking.codecs.StreamCodec;
import toni.lib.networking.codecs.ByteBufCodecs;
#endif

public final class SyncMediumcoreGameRuleMessage extends ToniPacket<SyncMediumcoreGameRuleMessage>
{
    public boolean mediumcoreMode;

    public SyncMediumcoreGameRuleMessage(boolean mediumcoreMode)
    {
        super(Mediumcore.MOD_ID, "main_channel", StreamCodec.composite(
            ByteBufCodecs.BOOL,
            (packet) -> packet.mediumcoreMode,
            SyncMediumcoreGameRuleMessage::new));

        this.mediumcoreMode = mediumcoreMode;
    }

    public static void register() {
        new SyncMediumcoreGameRuleMessage(true).registerType();
    }

    public static void registerClient() {
        new SyncMediumcoreGameRuleMessage(true).registerClientHandler((packet) -> {
            Mediumcore.clientActive = packet.mediumcoreMode;
        });
    }
}
