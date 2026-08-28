package com.chaosbuffalo.mkworkspace.items;

import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceInsertSocketScreenPacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertPlacementContext;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.insert.MKWorkspaceInsertPlacementContextResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;

public class MKWorkspaceInsertToolItem extends Item {
    private final MKWorkspaceInsertPlacementContextResolver contextResolver =
            new MKWorkspaceInsertPlacementContextResolver();

    public MKWorkspaceInsertToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Workspace insert authoring requires creative mode."));
            return InteractionResult.SUCCESS;
        }

        BlockPos socketPos = player.isShiftKeyDown() ?
                context.getClickedPos().relative(context.getClickedFace()) :
                context.getClickedPos();
        Direction socketFacing = socketFacing(player, context.getClickedFace());
        Optional<MKWorkspaceInsertPlacementContext> placementContext =
                contextResolver.resolve(level, socketPos, socketFacing);
        if (placementContext.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "Workspace insert placement failed: target is not inside a workspace authorial template."));
            return InteractionResult.SUCCESS;
        }

        MKWorkspaceInsertPlacementContext resolved = placementContext.get();
        player.connection.send(new OpenWorkspaceInsertSocketScreenPacket(resolved));
        return InteractionResult.SUCCESS;
    }

    private Direction socketFacing(ServerPlayer player, Direction clickedFace) {
        if (clickedFace.getAxis().isVertical()) {
            return clickedFace;
        }
        return player.getDirection().getOpposite();
    }
}
