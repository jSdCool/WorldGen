package com.worldgen;

import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class GenerateChunks extends Thread {
    CommandContext<CommandSourceStack> context;
    int fromX;
    int fromZ;
    int toX;
    int toZ;
    int totalChunks;
    int completed = 0;
    int numThreads=3;
    public static boolean generating=false,stopGenerating=false;
    long startTime;
    ArrayList<ChunkGenerator> generators = new ArrayList<>();

    GenerateChunks(CommandContext<CommandSourceStack> context, int fromX, int fromZ, int toX, int toZ) {
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
            context.getSource().sendFailure(Component.nullToEmpty("chunks are already being generated. please try again later"));

    }

    public void run() {

        context.getSource().sendSuccess(() -> Component.nullToEmpty("generating "+totalChunks+" chunks..."), false);
        generating=true;
        int chunksPerGenerator = totalChunks/numThreads;
        for(int i=0;i<numThreads;i++){
            int min = i*chunksPerGenerator;
            int max = i*chunksPerGenerator+chunksPerGenerator-1;
            if((i + 1) == numThreads){//if on the last thread
                max = totalChunks;
            }
            generators.add(new ChunkGenerator(context,fromX,fromZ,toX,toZ,min,max,this));
        }

        for (ChunkGenerator generator : generators) {
            generator.start();
        }

        int finishedThreads =0;
        do {
            for (int i = 0; i < generators.size(); i++) {
                if (!generators.get(i).isAlive() && !generators.get(i).finished) {
                    stopGenerating = true;
                    break;
                }
                if (generators.get(i).finished) {
                    finishedThreads++;
                    generators.remove(i);
                    break;
                }
            }
            if(stopGenerating) {
                break;
            }
        } while (finishedThreads != numThreads);


        //for(int i = this.fromX; i <= this.toX; ++i) {
        //    for(int j = this.fromZ; j <= this.toZ; ++j) {
        //        ++completed;
        //        Chunk newChunk = context.getSource().getWorld().getChunk(i, j, ChunkStatus.FULL, true);
        //        long chunkTime;
        //        if (newChunk != null) {
        //            chunkTime = newChunk.getInhabitedTime();
        //            if (chunkTime == 0) {
        //                newChunk.increaseInhabitedTime(1L);
        //                double percent = (double)completed / (double)this.totalChunks;
        //                double percentage = (int)(percent * 10000) / 100.0;
        //                int finalI = i;
        //                int finalJ = j;
        //                int finalCompleted = completed;
        //                context.getSource().sendFeedback(() -> MutableText.of(new LiteralTextContent("generated chunk " + finalI + " " + finalJ + " (" + percentage + "%) ETA: "+getETA(finalCompleted))), true);
        //            }
        //        }
                if(stopGenerating){
                    context.getSource().sendFailure(Component.nullToEmpty("generation was stopped by an external event"));
                    stopGenerating=false;
                    generating=false;
                    return;
                }
       //     }
       // }
//
        context.getSource().sendSuccess(() -> Component.nullToEmpty("DONE"), false);
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

    void update(boolean success,int x, int z){
        completed++;
        if(success) {
            double percent = (double) completed / (double) this.totalChunks;
            double percentage = (int) (percent * 10000) / 100.0;
            context.getSource().sendSuccess(() -> Component.nullToEmpty("generated chunk " + x + " " + z + " (" + percentage + "%) ETA: " + getETA(completed)), true);
        }
    }
}
