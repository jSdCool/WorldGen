package com.worldgen;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import static com.worldgen.GenerateChunks.stopGenerating;

public class ChunkGenerator extends Thread{

    int fromX;
    int fromZ;
    int lengthX;
    int lengthZ;
    int min,max;
    CommandContext<CommandSourceStack> context;
    GenerateChunks parent;
    public boolean finished = false;
    static int num = 0;
    ChunkGenerator(CommandContext<CommandSourceStack> context,int fromX, int fromZ, int toX, int toZ, int min, int max,GenerateChunks parent){
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
            ChunkAccess newChunk = context.getSource().getLevel().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
            if (newChunk != null) {
                long chunkTime = newChunk.getInhabitedTime();
                if (chunkTime == 0) {
                    newChunk.incrementInhabitedTime(1L);
                    parent.update(true,chunkX,chunkZ);
                    continue;
                }
            }
            parent.update(false,chunkX,chunkZ);
        }
        finished=true;
    }
}
