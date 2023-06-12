package com.worldgen;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class GenerateChunks extends Thread {
    CommandContext<ServerCommandSource> context;
    int fromX;
    int fromZ;
    int toX;
    int toZ;
    int totalChunks;
    static boolean generating=false,stopGenerating=false;

    GenerateChunks(CommandContext<ServerCommandSource> context, int fromX, int fromZ, int toX, int toZ) {
        this.context = context;
        this.fromX = fromX;
        this.fromZ = fromZ;
        this.toX = toX;
        this.toZ = toZ;
        this.totalChunks = (toX - fromX + 1) * (toZ - fromZ + 1);
        if(!generating)
            this.start();
        else
            context.getSource().sendError(MutableText.of(new LiteralTextContent("chunks are already being generated. please try again later")));

    }

    public void run() {
        int completed = 0;
        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("generating "+totalChunks+" chunks...")), false);
        generating=true;
        for(int i = this.fromX; i <= this.toX; ++i) {
            for(int j = this.fromZ; j <= this.toZ; ++j) {
                ++completed;
                Chunk newChunk = context.getSource().getWorld().getChunk(i, j, ChunkStatus.FULL, true);
                long chunktime = -1L;
                if (newChunk != null) {
                    chunktime = newChunk.getInhabitedTime();
                    if (chunktime == 0L) {
                        newChunk.increaseInhabitedTime(1L);
                        double percent = (double)completed / (double)this.totalChunks;
                        double percentage = (int)(percent * 10000) / 100.0;
                        int finalI = i;
                        int finalJ = j;
                        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("generated chunk " + finalI + " " + finalJ + " (" + percentage + "%)")), true);
                    }
                }
                if(stopGenerating){
                    context.getSource().sendError(MutableText.of(new LiteralTextContent("generation was stopped by an external event")));
                    stopGenerating=false;
                    generating=false;
                    return;
                }
            }
        }

        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("DONE")), false);
        generating=false;
    }
}
