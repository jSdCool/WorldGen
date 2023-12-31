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
    public static boolean generating=false,stopGenerating=false;
    long startTime;

    GenerateChunks(CommandContext<ServerCommandSource> context, int fromX, int fromZ, int toX, int toZ) {
        this.context = context;
        this.fromX = fromX;
        this.fromZ = fromZ;
        this.toX = toX;
        this.toZ = toZ;
        this.totalChunks = (toX - fromX + 1) * (toZ - fromZ + 1);
        startTime=System.nanoTime();
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
                long chunkTime;
                if (newChunk != null) {
                    chunkTime = newChunk.getInhabitedTime();
                    if (chunkTime == 0) {
                        newChunk.increaseInhabitedTime(1L);
                        double percent = (double)completed / (double)this.totalChunks;
                        double percentage = (int)(percent * 10000) / 100.0;
                        int finalI = i;
                        int finalJ = j;
                        int finalCompleted = completed;
                        context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("generated chunk " + finalI + " " + finalJ + " (" + percentage + "%) ETA: "+getETA(finalCompleted))), true);
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


    String getETA(int completed){
        long elapsed = System.nanoTime()-startTime;
        double percent = (double)completed / (double)this.totalChunks;
        long ETA = (long)(1/percent*elapsed)-elapsed;
        int hours =    (int)(ETA/ 3600000000000L);
        int mins =     (int)(ETA/ 60000000000L)-hours*60;
        int seconds = (int)((ETA/ 1000000000L)-mins*60-hours*60*60);


        return hours+":"+mins+":"+ seconds;
    }
}
