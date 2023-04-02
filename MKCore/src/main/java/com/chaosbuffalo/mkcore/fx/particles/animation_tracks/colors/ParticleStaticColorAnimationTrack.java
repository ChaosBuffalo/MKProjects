package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleColorAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.ColorFloatAttribute;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;

public class ParticleStaticColorAnimationTrack extends ParticleColorAnimationTrack {
    protected final ColorFloatAttribute red = new ColorFloatAttribute("red", 1.0f);
    protected final ColorFloatAttribute green = new ColorFloatAttribute("green", 1.0f);
    protected final ColorFloatAttribute blue = new ColorFloatAttribute("blue", 1.0f);
    protected final ColorFloatAttribute redVariance = new ColorFloatAttribute("redVariance", 0.0f);
    protected final ColorFloatAttribute greenVariance = new ColorFloatAttribute("greenVariance", 0.0f);
    protected final ColorFloatAttribute blueVariance = new ColorFloatAttribute("blueVariance", 0.0f);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.static_color");
    private final MKParticle.ParticleDataKey COLOR = new MKParticle.ParticleDataKey(this, keyCount++);


    public ParticleStaticColorAnimationTrack(float red, float green, float blue) {
        this();
        this.red.setValue(red);
        this.green.setValue(green);
        this.blue.setValue(blue);
    }

    public ParticleStaticColorAnimationTrack(float red, float green, float blue,
                                             float redVariance, float greenVariance, float blueVariance) {
        this(red, green, blue);
        this.redVariance.setValue(redVariance);
        this.greenVariance.setValue(greenVariance);
        this.blueVariance.setValue(blueVariance);
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        particle.setTrackVector3fData(COLOR, new Vector3f(
                getColorWithVariance(red.value(), redVariance.value(), generateVariance(particle)),
                getColorWithVariance(green.value(), greenVariance.value(), generateVariance(particle)),
                getColorWithVariance(blue.value(), blueVariance.value(), generateVariance(particle))));
    }

    @Override
    public Vector3f getColor(MKParticle particle) {
        return particle.getTrackVector3fData(COLOR);
    }

    @Override
    public ParticleStaticColorAnimationTrack copy() {
        return new ParticleStaticColorAnimationTrack(red.value(), green.value(), blue.value(),
                redVariance.value(), greenVariance.value(), blueVariance.value());
    }

    public ParticleStaticColorAnimationTrack() {
        super(TYPE_NAME);
        addAttributes(red, green, blue, redVariance, greenVariance, blueVariance);
    }


}
