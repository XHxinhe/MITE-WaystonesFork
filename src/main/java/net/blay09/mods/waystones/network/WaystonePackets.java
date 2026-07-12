package net.blay09.mods.waystones.network;

import moddedmite.rustedironcore.network.PacketReader;
import net.blay09.mods.waystones.client.ClientScreenQueue;
import net.xiaoyu233.fml.FishModLoader;

public final class WaystonePackets {
    private WaystonePackets() {
    }

    public static void init() {
        PacketReader.registerServerPacketReader(C2STeleport.CHANNEL, C2STeleport::new);
        PacketReader.registerServerPacketReader(C2SRenameWaystone.CHANNEL, C2SRenameWaystone::new);
        PacketReader.registerServerPacketReader(C2SConfirmReturn.CHANNEL, C2SConfirmReturn::new);
        PacketReader.registerServerPacketReader(C2SRequestFreeWarp.CHANNEL, C2SRequestFreeWarp::new);
        PacketReader.registerServerPacketReader(C2SSetPinned.CHANNEL, C2SSetPinned::new);
        PacketReader.registerServerPacketReader(C2SForgetWaystone.CHANNEL, C2SForgetWaystone::new);
        if (!FishModLoader.isServer()) {
            ClientScreenQueue.init();
            PacketReader.registerClientPacketReader(S2CWaystoneList.CHANNEL, S2CWaystoneList::new);
            PacketReader.registerClientPacketReader(S2COpenName.CHANNEL, S2COpenName::new);
            PacketReader.registerClientPacketReader(S2COpenReturnConfirm.CHANNEL, S2COpenReturnConfirm::new);
            PacketReader.registerClientPacketReader(S2CTeleportEffect.CHANNEL, S2CTeleportEffect::new);
            PacketReader.registerClientPacketReader(S2CLocalizedMessage.CHANNEL, S2CLocalizedMessage::new);
            PacketReader.registerClientPacketReader(S2CWaystoneConfig.CHANNEL, S2CWaystoneConfig::new);
            PacketReader.registerClientPacketReader(S2CWaystoneState.CHANNEL, S2CWaystoneState::new);
            PacketReader.registerClientPacketReader(S2CActivationEffect.CHANNEL, S2CActivationEffect::new);
            PacketReader.registerClientPacketReader(S2CMapWaypoint.CHANNEL, S2CMapWaypoint::new);
        }
    }
}
