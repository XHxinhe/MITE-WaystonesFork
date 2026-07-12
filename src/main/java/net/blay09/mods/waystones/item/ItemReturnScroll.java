package net.blay09.mods.waystones.item;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneEntry;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneMessages;
import net.blay09.mods.waystones.ClientWaystoneState;
import net.minecraft.EntityPlayer;
import net.minecraft.CreativeTabs;
import net.minecraft.IconRegister;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.ServerPlayer;
import net.minecraft.World;
import net.minecraft.EnumItemInUseAction;
import net.minecraft.EnumChatFormatting;
import net.minecraft.I18n;
import net.minecraft.Slot;

import java.util.List;

public final class ItemReturnScroll extends Item {
    public ItemReturnScroll(int id) {
        super(id, Waystones.MOD_ID + ":return_scroll");
        setMaterial(Material.paper, Material.gold, Material.ender_pearl);
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public void registerIcons(IconRegister register) {
        itemIcon = register.registerIcon(Waystones.MOD_ID + ":return_scroll");
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partialTick, boolean ctrlIsDown) {
        WaystoneEntry target = player.onClient()
                ? ClientWaystoneState.getLast() : PlayerWaystoneData.getLast(player);
        if (target == null) {
            if (player.onServer()) {
                WaystoneMessages.send(player, "message.waystones.none_activated");
            }
            return true;
        }
        player.setHeldItemInUse();
        if (player.onClient() && WaystoneConfig.sounds) {
            player.worldObj.playSoundAtEntity(player, "portal.trigger", 1.0F, 2.0F);
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
            WaystoneManager.requestReturnConfirmation(serverPlayer, true, false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean debug, Slot slot) {
        WaystoneEntry target = ClientWaystoneState.getLast();
        String name = target == null
                ? I18n.getString("tooltip.waystones.none")
                : EnumChatFormatting.DARK_AQUA + target.name();
        tooltip.add(EnumChatFormatting.GRAY
                + I18n.getStringParams("tooltip.waystones.bound_to", name));
    }
}
