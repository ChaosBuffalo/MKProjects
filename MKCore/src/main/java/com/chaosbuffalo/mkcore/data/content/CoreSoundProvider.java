package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreSounds;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class CoreSoundProvider extends SoundDefinitionsProvider {
    protected CoreSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MKCore.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {

        add(CoreSounds.casting_default, definition()
                .subtitle("mkcore.subtitle.casting_default")
                .with(sound(MKCore.makeRL("casting/general_loop"))
                        .stream())
        );

        add(CoreSounds.spell_cast_default, definition()
                .subtitle("mkcore.subtitle.spell_cast")
                .with(sound(MKCore.makeRL("spells/default_cast")))
        );

        add(CoreSounds.level_up, definition()
                .subtitle("mkcore.subtitle.level_up")
                .with(sound(MKCore.makeRL("other/level_up"))
                        .attenuationDistance(32))
        );

        add(CoreSounds.weapon_block, definition()
                .subtitle("mkcore.subtitle.weapon_block")
                .with(sound(MKCore.makeRL("other/weapon_block"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.block_break, definition()
                .subtitle("mkcore.subtitle.block_break")
                .with(sound(MKCore.makeRL("other/block_break"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.fist_block, definition()
                .subtitle("mkcore.subtitle.fist_block")
                .with(sound(MKCore.makeRL("other/fist_block"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.arrow_block, definition()
                .subtitle("mkcore.subtitle.arrow_block")
                .with(sound(MKCore.makeRL("other/arrow_block"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.parry, definition()
                .subtitle("mkcore.subtitle.parry")
                .with(sound(MKCore.makeRL("other/parry"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.attack_cd_reset, definition()
                .subtitle("mkcore.subtitle.attack_cd_reset")
                .with(sound(MKCore.makeRL("other/attack_cd_reset"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.stun_sound, definition()
                .subtitle("mkcore.subtitle.stun")
                .with(sound(MKCore.makeRL("other/stun"))
                        .attenuationDistance(20))
        );

        add(CoreSounds.quest_complete_sound, definition()
                .subtitle("mkcore.subtitle.quest_complete")
                .with(sound(MKCore.makeRL("other/quest_complete"))
                        .attenuationDistance(20))
        );
    }
}
