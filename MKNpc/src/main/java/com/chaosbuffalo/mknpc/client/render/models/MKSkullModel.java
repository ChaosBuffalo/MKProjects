package com.chaosbuffalo.mknpc.client.render.models;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class MKSkullModel<T extends MKEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    protected final ModelPart head;
    protected final ModelPart jaw;

    public MKSkullModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = this.head.getChild("jaw");
    }

    public static MeshDefinition createHeadModel() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 7.0F, 8.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 3.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        return meshDefinition;
    }

    public static LayerDefinition createMobHeadLayer() {
        return LayerDefinition.create(createHeadModel(), 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTicks = ageInTicks - entity.tickCount;
        float attackAnim = entity.getVisualMeleeAttackAnim(partialTicks);
        float windupProgress = entity.getMeleeWindupProgress(partialTicks);
        float idleJaw = (float) (Math.sin(ageInTicks * 0.2F) + 1.0F) * 0.15F;
        float attackCurve = Mth.sin(attackAnim * (float) Math.PI);
        float windupCurve = Mth.sin(windupProgress * ((float) Math.PI / 2.0F));
        float windupPose = Mth.sin(windupProgress * ((float) Math.PI / 2.0F));
        float chatterCurve = Mth.sin(ageInTicks * 2.8F) * windupCurve;
        float attackJaw = attackCurve * 1.5F;
        float windupJaw = windupCurve * 0.45F + Math.abs(chatterCurve) * 0.4F;

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.xRot = headPitch * ((float) Math.PI / 180.0F) * (1.0F - windupPose) - windupPose * ((float) Math.PI / 4.0F) + attackCurve * 0.3F;
        this.head.y = 20.0F - windupPose * 1.0F;
        this.head.z = windupPose * 1.75F;
        this.jaw.xRot = Math.max(idleJaw, Math.max(windupJaw, attackJaw));
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
