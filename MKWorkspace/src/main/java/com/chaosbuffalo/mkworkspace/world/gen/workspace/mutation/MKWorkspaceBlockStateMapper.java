package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MKWorkspaceBlockStateMapper {
    public record MappedState(BlockState state, List<String> preservedProperties, List<String> droppedProperties) {
    }

    public MappedState mapToBlock(BlockState sourceState, Block targetBlock) {
        return mapToState(sourceState, targetBlock.defaultBlockState());
    }

    public MappedState mapToState(BlockState sourceState, BlockState targetDefaultState) {
        BlockState mappedState = targetDefaultState;
        List<String> preservedProperties = new ArrayList<>();
        List<String> droppedProperties = new ArrayList<>();
        for (Property<?> sourceProperty : sourceState.getProperties()) {
            Property<?> targetProperty = mappedState.getBlock().getStateDefinition()
                    .getProperty(sourceProperty.getName());
            if (targetProperty == null || !mappedState.hasProperty(targetProperty)) {
                droppedProperties.add(sourceProperty.getName());
                continue;
            }
            PropertyCopyResult copyResult = copyProperty(sourceState, mappedState, sourceProperty, targetProperty);
            mappedState = copyResult.state();
            if (copyResult.preserved()) {
                preservedProperties.add(sourceProperty.getName());
            } else {
                droppedProperties.add(sourceProperty.getName());
            }
        }
        return new MappedState(mappedState, List.copyOf(preservedProperties), List.copyOf(droppedProperties));
    }

    private <S extends Comparable<S>, T extends Comparable<T>> PropertyCopyResult copyProperty(
            BlockState sourceState,
            BlockState targetState,
            Property<S> sourceProperty,
            Property<T> targetProperty
    ) {
        String sourceValueName = sourceProperty.getName(sourceState.getValue(sourceProperty));
        Optional<T> targetValue = targetProperty.getValue(sourceValueName);
        if (targetValue.isEmpty()) {
            return new PropertyCopyResult(targetState, false);
        }
        return new PropertyCopyResult(targetState.setValue(targetProperty, targetValue.get()), true);
    }

    private record PropertyCopyResult(BlockState state, boolean preserved) {
    }
}
