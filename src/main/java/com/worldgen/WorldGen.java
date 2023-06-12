package com.worldgen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.ColumnPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldGen implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("worldgen");


    public void onInitialize() {
        LOGGER.info("world gen initialized");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("generate").then(CommandManager.argument("chunk", ColumnPosArgumentType.columnPos()).executes((context) -> {
                ColumnPos pos = ColumnPosArgumentType.getColumnPos(context, "chunk");
                Chunk newChunk = context.getSource().getWorld().getChunk(pos.x(), pos.z(), ChunkStatus.FULL, true);
                long chunktime = -1;
                if (newChunk != null) {
                    chunktime = newChunk.getInhabitedTime();
                    if (chunktime == 0) {
                        newChunk.increaseInhabitedTime(1);
                        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("generated chunk " + pos.x() + " " + pos.z())), true);
                    } else if (chunktime > 0) {
                        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("chunk already exsists")), false);
                    }
                }

                if (chunktime == -1) {
                    context.getSource().sendFeedback(() ->MutableText.of(new LiteralTextContent("no chunk was generated")), false);
                }

                return 1;
            })).then(CommandManager.argument("from chunk", ColumnPosArgumentType.columnPos()).then(CommandManager.argument("to chunk", ColumnPosArgumentType.columnPos()).executes((context) -> {
                ColumnPos fromPos = ColumnPosArgumentType.getColumnPos(context, "from chunk");
                ColumnPos toPos = ColumnPosArgumentType.getColumnPos(context, "to chunk");
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
