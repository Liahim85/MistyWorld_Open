package ru.liahim.mist.world.biome;

import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;

public abstract class BiomeMistUp extends BiomeMist {

	public BiomeMistUp(BiomeProperties properties) {
		super(properties);
		/*this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityRabbit.class, 1, 0, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySpider.class, 20, 0, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 1, 0, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityCaveSpider.class, 2, 0, 1));*/
	}

	@Override
	public boolean isUpBiome() {
		return true;
	}

	@Override
	public void addDefaultFlowers() {
		addFlower(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS), 80);
	}
}