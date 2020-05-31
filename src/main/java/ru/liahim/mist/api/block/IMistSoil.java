package ru.liahim.mist.api.block;

import net.minecraft.block.Block;

/**@author Liahim*/
public interface IMistSoil {

	public void setSoilBlock(Block soilBlock);

	public Block getSoilBlock();

	public void setGrassBlock(Block grassBlock);

	public Block getGrassBlock();
}