package net.blay09.mods.waystones.item;

import moddedmite.rustedironcore.network.Network;
import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneMessages;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.blay09.mods.waystones.network.S2CWaystoneList;
import net.minecraft.EntityPlayer;
import net.minecraft.CreativeTabs;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.World;
import net.minecraft.Material;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.EnumChatFormatting;
import net.minecraft.I18n;
import net.minecraft.Slot;
import net.minecraft.Entity;
import net.minecraft.IDamageableItem;

import java.util.List;

public final class ItemWarpStone extends Item implements IDamageableItem {
    public ItemWarpStone(int id) {
        super(id, Waystones.MOD_ID + ":warp_stone");
        setMaterial(Material.dye, Material.ender_pearl, Material.emerald);
        setMaxStackSize(1);
        setMaxDamage(100);
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public void registerIcons(IconRegister register) {
        itemIcon = register.registerIcon(Waystones.MOD_ID + ":warp_stone");
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        if (player.onClient()) {
            if (ClientWaystoneState.getLast() != null
                    && System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse()
                    >= WaystoneManager.warpStoneCooldownMs()) {
                player.setHeldItemInUse();
                if (WaystoneConfig.sounds) {
                    player.worldObj.playSoundAtEntity(player, "portal.trigger", 1.0F, 2.0F);
                }
            }
            return true;
        }
        if (player.onServer() && player instanceof ServerPlayer serverPlayer) {
            if (WaystoneManager.getAccessibleWaystones(player).isEmpty()) {
                WaystoneMessages.send(player, "message.waystones.none_activated");
                return true;
            }
            long remaining = WaystoneManager.warpStoneCooldownMs()
                    - (System.currentTimeMillis() - PlayerWaystoneData.getLastWarpStoneUse(player));
            if (!player.inCreativeMode() && remaining > 0) {
                WaystoneMessages.send(player, "message.waystones.cooldown", (remaining + 999) / 1000);
                return true;
            }
            player.setHeldItemInUse();
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumItemInUseAction getItemInUseAction(ItemStack stack, EntityPlayer player) {
        return EnumItemInUseAction.BOW;
    }

    @Override
    public void onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
        if (player.onServer() && player instanceof ServerPlayer serverPlayer) {
            WaystoneManager.openDestinationMenu(serverPlayer, true, false);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        if (world.isRemote) {
            long remaining = WaystoneManager.warpStoneCooldownMs()
                    - (System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse());
            int damage = remaining <= 0 ? 0 : (int) Math.ceil(
                    Math.min(1.0D, remaining / (double) Math.max(1L, WaystoneManager.warpStoneCooldownMs())) * 100.0D);
            stack.setItemDamage(damage);
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse()
                >= WaystoneManager.warpStoneCooldownMs();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean debug, Slot slot) {
        long remaining = WaystoneManager.warpStoneCooldownMs()
                - (System.currentTimeMillis() - ClientWaystoneState.getLastWarpStoneUse());
        if (remaining > 0) {
            tooltip.add(EnumChatFormatting.GRAY + I18n.getStringParams(
                    "tooltip.waystones.cooldown", (remaining + 999L) / 1000L));
        }
    }

    @Override
    public int getNumComponentsForDurability() {
        return 1;
    }

    @Override
    public int getRepairCost() {
        return 0;
    }
}
