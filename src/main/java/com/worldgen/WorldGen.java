package com.worldgen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldGen implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("worldgen");


    public void onInitialize() {
        LOGGER.info("world gen initialized");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("generate").requires(source -> source.hasPermission(4))
                    .then(Commands.argument("chunk", ColumnPosArgument.columnPos()).executes((context) -> {
                ColumnPos pos = ColumnPosArgument.getColumnPos(context, "chunk");
                ChunkAccess newChunk = context.getSource().getLevel().getChunk(pos.x(), pos.z(), ChunkStatus.FULL, true);
                long chunktime = -1;
                if (newChunk != null) {
                    chunktime = newChunk.getInhabitedTime();
                    if (chunktime == 0) {
                        newChunk.incrementInhabitedTime(1);
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("generated chunk " + pos.x() + " " + pos.z()), true);
                    } else if (chunktime > 0) {
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("chunk already exsists"), false);
                    }
                }

                if (chunktime == -1) {
                    context.getSource().sendSuccess(() ->Component.nullToEmpty("no chunk was generated"), false);
                }

                return 1;
            })).then(Commands.argument("from chunk", ColumnPosArgument.columnPos()).then(Commands.argument("to chunk", ColumnPosArgument.columnPos()).executes((context) -> {
                ColumnPos fromPos = ColumnPosArgument.getColumnPos(context, "from chunk");
                ColumnPos toPos = ColumnPosArgument.getColumnPos(context, "to chunk");
                int fromX = Math.min(fromPos.x(), toPos.x());
                int fromZ = Math.min(fromPos.z(), toPos.z());
                int toX = Math.max(fromPos.x(), toPos.x());
                int toZ = Math.max(fromPos.z(), toPos.z());
                new GenerateChunks(context, fromX, fromZ, toX, toZ);
                return 1;
            }))));
        });
    }
}
