package com.worldgen;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import static com.worldgen.GenerateChunks.stopGenerating;

public class ChunkGenerator extends Thread{

    int fromX;
    int fromZ;
    int lengthX;
    int lengthZ;
    int min,max;
    CommandContext<ServerCommandSource> context;
    GenerateChunks parent;
    public boolean finished = false;
    static int num = 0;
    ChunkGenerator(CommandContext<ServerCommandSource> context,int fromX, int fromZ, int toX, int toZ, int min, int max,GenerateChunks parent){
        super("chunk generation thread "+num);
        num++;
        this.context = context;
        this.fromX = fromX;
        this.fromZ = fromZ;
        lengthX = toX - fromX;
        lengthZ = toZ - fromZ;
        this.min=min;
        this.max=max;
        this.parent=parent;

    }

    public void run(){
        for(int i=min;i<=max;i++){
            if(stopGenerating){
                return;
            }
            int chunkX,chunkZ;
            chunkZ = i / lengthX;
            chunkX = i % lengthX;
            chunkX += fromX;
            chunkZ += fromZ;
            Chunk newChunk = context.getSource().getWorld().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
            if (newChunk != null) {
                long chunkTime = newChunk.getInhabitedTime();
                if (chunkTime == 0) {
                    newChunk.increaseInhabitedTime(1L);
                    parent.update(true,chunkX,chunkZ);
                    continue;
                }
            }
            parent.update(false,chunkX,chunkZ);
        }
        finished=true;
    }
}
