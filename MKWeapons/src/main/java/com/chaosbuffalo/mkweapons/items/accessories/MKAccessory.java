package com.chaosbuffalo.mkweapons.items.accessories;

import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemHandler;
import com.chaosbuffalo.mkweapons.capabilities.MKCurioItemProvider;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class MKAccessory extends Item {

    private List<IAccessoryEffect> effects;

    public MKAccessory(Properties properties, IAccessoryEffect... effectsIn) {
        super(properties);
        effects = new ArrayList<>();
        effects.addAll(Arrays.asList(effectsIn));
    }

    public List<? extends IAccessoryEffect> getAccessoryEffects(ItemStack item){
        return item.getCapability(CuriosCapability.ITEM).map(cap -> {
            if (cap instanceof MKCurioItemHandler) {
                return ((MKCurioItemHandler) cap).getEffects();
            } else {
                return effects;
            }
        }).orElse(effects);
    }

    public List<? extends IAccessoryEffect> getAccessoryEffects(){
        return effects;
    }

    public void addToTooltip(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip){
        for (IAccessoryEffect accessoryEffect : getAccessoryEffects(stack)){
            accessoryEffect.addInformation(stack, worldIn, tooltip);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        MKCurioItemProvider provider = new MKCurioItemProvider(stack);
        if (nbt != null){
            provider.deserializeNBT(nbt);
        }
        return provider;
    }

    public static Optional<MKCurioItemHandler> getAccessoryHandler(ItemStack item){
        Optional<ICurio> curioCap = item.getCapability(CuriosCapability.ITEM).resolve();
        if (curioCap.isPresent()){
            ICurio cap = curioCap.get();
            if (cap instanceof MKCurioItemHandler){
                return Optional.of((MKCurioItemHandler) cap);
            }
        }
        return Optional.empty();
    }


    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        // See comment in MKMeleeWeapon#getShareTag
        CompoundTag newTag = new CompoundTag();
        CompoundTag original = super.getShareTag(stack);
        if (original != null) {
            newTag.put("share", original);
        }
        getAccessoryHandler(stack).ifPresent(cap -> newTag.put("accessoryCap", cap.serializeNBT()));
        return newTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag shareTag) {
        if (shareTag == null)
            return;

        if (shareTag.contains("share")) {
            super.readShareTag(stack, shareTag.getCompound("share"));
        }
        if (shareTag.contains("accessoryCap")) {
            getAccessoryHandler(stack).ifPresent(cap -> {
                cap.deserializeNBT(shareTag.getCompound("accessoryCap"));
            });
        }
    }

   public static List<MKCurioItemHandler> getMKCurios(LivingEntity entity){
        List<MKCurioItemHandler> curios = new ArrayList<>();
        CuriosApi.getCuriosHelper().getEquippedCurios(entity).ifPresent(x -> {
           for (int i = 0; i < x.getSlots(); i++){
               ItemStack curioIS = x.getStackInSlot(i);
               if (!curioIS.isEmpty() && curioIS.getItem() instanceof MKAccessory){
                   MKAccessory.getAccessoryHandler(curioIS).ifPresent(curios::add);
               }
           }
        });
       return curios;
   }
}