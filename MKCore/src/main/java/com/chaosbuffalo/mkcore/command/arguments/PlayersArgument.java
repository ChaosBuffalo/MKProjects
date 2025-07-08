package com.chaosbuffalo.mkcore.command.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayersArgument extends EntityArgument {
    private static final Collection<String> EXAMPLES = List.of("Player");
    protected final boolean singleCopy;

    protected PlayersArgument(boolean single) {
        super(single, true);
        singleCopy = single;
    }

    public static PlayersArgument player() {
        return new PlayersArgument(true);
    }

    public static PlayersArgument players() {
        return new PlayersArgument(false);
    }

    @Override
    public EntitySelector parse(StringReader reader) throws CommandSyntaxException {
        EntitySelectorParser entityselectorparser = new EntitySelectorParser(reader, false);
        EntitySelector entityselector = entityselectorparser.parse();
        if (entityselector.getMaxResults() > 1 && singleCopy) {
            reader.setCursor(0);
            throw ERROR_NOT_SINGLE_PLAYER.createWithContext(reader);
        } else if (entityselector.includesEntities() && !entityselector.isSelfSelector()) {
            reader.setCursor(0);
            throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext(reader);
        } else {
            return entityselector;
        }
    }

    @Override
    public <S> EntitySelector parse(StringReader reader, S p_353120_) throws CommandSyntaxException {
        return this.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Object var4 = context.getSource();
        if (var4 instanceof SharedSuggestionProvider sharedsuggestionprovider) {
            StringReader stringreader = new StringReader(builder.getInput());
            stringreader.setCursor(builder.getStart());
            EntitySelectorParser entityselectorparser = new EntitySelectorParser(stringreader, false);

            try {
                entityselectorparser.parse();
            } catch (CommandSyntaxException var7) {
            }

            return entityselectorparser.fillSuggestions(builder, (b) -> {
                Collection<String> collection = sharedsuggestionprovider.getOnlinePlayerNames();
                Iterable<String> iterable = collection;
                SharedSuggestionProvider.suggest(iterable, b);
            });
        } else {
            return Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Info implements ArgumentTypeInfo<PlayersArgument, PlayersArgument.Info.Template> {
        private static final byte FLAG_SINGLE = 1;
        private static final byte FLAG_PLAYERS_ONLY = 2;

        public Info() {
        }

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            int i = 0;
            if (template.single) {
                i |= 1;
            }
            buffer.writeByte(i);
        }


        public PlayersArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            byte b0 = buffer.readByte();
            return new PlayersArgument.Info.Template((b0 & 1) != 0);
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("amount", template.single ? "single" : "multiple");
        }

        @Override
        public Template unpack(PlayersArgument playersArgument) {
            return new PlayersArgument.Info.Template(playersArgument.singleCopy);
        }

        public final class Template implements ArgumentTypeInfo.Template<PlayersArgument> {
            final boolean single;

            Template(boolean single) {
                this.single = single;
            }

            public PlayersArgument instantiate(CommandBuildContext context) {
                return new PlayersArgument(this.single);
            }

            public ArgumentTypeInfo<PlayersArgument, ?> type() {
                return PlayersArgument.Info.this;
            }
        }
    }
}
