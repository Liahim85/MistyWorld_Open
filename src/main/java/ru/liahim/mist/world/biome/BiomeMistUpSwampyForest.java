package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.entity.EntitySloth;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeMistUpSwampyForest extends BiomeMistUpSwampBase {

	public BiomeMistUpSwampyForest(BiomeProperties properties) {
		super(properties, 4, false);
		getMistBiomeDecorator().grassPerChunk = 4;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntitySloth.class, 20, 1, 2));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) {
		return 0x88bc67;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(1) > 0.7D ? MistWorld.gravelBlock : noises.get(0) > 0.7D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(1) > 0.5D ? MistWorld.gravelBlock : noises.get(0) > 0.6D ? MistBlocks.CLAY.getDefaultState() : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(1) > 0.5D ? MistWorld.gravelBlock : noises.get(0) > 0.7D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	protected IBlockState getSecondBottom(Random rand, ArrayList<Double> noises) {
		return noises.get(1) > 0.4D ? MistWorld.gravelBlock : noises.get(0) > 0.65D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random rand) {
		return new WorldGenTallGrass(rand.nextInt(4) == 0 ? BlockTallGrass.EnumType.GRASS : BlockTallGrass.EnumType.FERN);
	}
}