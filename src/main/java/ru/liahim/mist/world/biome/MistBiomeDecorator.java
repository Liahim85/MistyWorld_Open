package ru.liahim.mist.world.biome;

import java.util.Random;

import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.downplant.MistSponge;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.generators.AltarsGen;
import ru.liahim.mist.world.generators.BasementsGen;
import ru.liahim.mist.world.generators.BioShaleAndSulfurGenerator;
import ru.liahim.mist.world.generators.CliffTombGen;
import ru.liahim.mist.world.generators.SingleBlockGenerator;
import ru.liahim.mist.world.generators.LapisGenerator;
import ru.liahim.mist.world.generators.SaltGenerator;
import ru.liahim.mist.world.generators.TombGen;
import ru.liahim.mist.world.generators.WellGen;

public class MistBiomeDecorator extends BiomeDecorator {	

	public WorldGenerator filterCoalGen;
	public WorldGenerator ironGen;
	public WorldGenerator goldGen;
	public WorldGenerator mistGravelGen;
	public WorldGenerator niobiumGen;
	public WorldGenerator bioShaleGen;
	public WorldGenerator saltpeterGen;
	public WorldGenerator saltGen;
	public WorldGenerator lapisGen;
	public WorldGenerator acidPocket;
	public static WorldGenerator wellGen = new WellGen();
	public static WorldGenerator tombGen = new TombGen();
	public static WorldGenerator cliffTombGen = new CliffTombGen();
	public static WorldGenerator altarsGen = new AltarsGen();
	public static WorldGenerator basementsGen = new BasementsGen();

	@Override
	public void decorate(World world, Random rand, Biome biome, BlockPos pos) {
		if (this.decorating) throw new RuntimeException("Already decorating");
		else {
			this.chunkPos = pos;
			this.filterCoalGen = new SingleBlockGenerator(MistBlocks.FILTER_COAL_ORE.getDefaultState(), BlockMatcher.forBlock(MistBlocks.STONE));
			this.ironGen = new WorldGenMinable(MistBlocks.IRON_ORE.getDefaultState(), 9, BlockMatcher.forBlock(MistBlocks.STONE));
			this.mistGravelGen = new WorldGenMinable(MistBlocks.GRAVEL.getDefaultState(), 30, BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.goldGen = new WorldGenMinable(MistBlocks.GOLD_ORE.getDefaultState(), 9, BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.niobiumGen = new WorldGenMinable(MistBlocks.NIOBIUM_ORE.getDefaultState(), 4, BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.bioShaleGen = new BioShaleAndSulfurGenerator(12, BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.saltpeterGen = new WorldGenMinable(MistBlocks.SALTPETER_ORE.getDefaultState(), 4, BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.saltGen = new SaltGenerator();
			this.lapisGen = new LapisGenerator();
			this.acidPocket = new SingleBlockGenerator(MistBlocks.ACID_BLOCK.getDefaultState(), BlockMatcher.forBlock(MistBlocks.STONE_POROUS));
			this.genDecorations((BiomeMist)biome, world, rand);
			this.decorating = false;
		}
	}

	protected void genDecorations(BiomeMist biome, World world, Random rand) {
		ChunkPos forgeChunkPos = new ChunkPos(chunkPos); // actual ChunkPos instead of BlockPos, used for events
        MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, forgeChunkPos));
		this.generateStructures(biome, world, rand);
		this.generateOres(world, rand);
		EnumBiomeType biomeType = biome.getBiomeType();
		if (biomeType == EnumBiomeType.Down && biome != MistBiomes.downCenter) {
			if (rand.nextFloat() < 0.05F) {
	            int rx = rand.nextInt(16) + 8;
	            int rz = rand.nextInt(16) + 8;
	            BlockPos pos = world.getHeight(this.chunkPos.add(rx, 0, rz));
	            ((MistSponge)MistBlocks.SPONGE).generateSponge(world, pos, rand);
			}
		}
		if (biomeType != EnumBiomeType.Down && biomeType != EnumBiomeType.Border) {
			for (int i3 = 0; i3 < this.grassPerChunk; ++i3) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				int h = world.getHeight(this.chunkPos.add(rx, 0, rz)).getY() * 2;
				if (h > 0) {
					int ry = rand.nextInt(h);
					biome.getRandomWorldGenForGrass(rand).generate(world, rand, this.chunkPos.add(rx, ry, rz));
				}
			}
		}
		if (biomeType != EnumBiomeType.Down && biome != MistBiomes.upDesert && biome != MistBiomes.upDunes) {
			int i = biome instanceof BiomeMistBorderDown ? 4 : biome instanceof BiomeMistBorderUp ? (rand.nextInt(4) == 0 ? 1 : 0) : rand.nextInt(2);
			for (int i4 = 0; i4 < i; ++i4) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				int h = world.getHeight(this.chunkPos.add(rx, 0, rz)).getY() * 2;
				if (h > 0) {
					int ry = rand.nextInt(h);
					BiomeMist.looseRockGen.generate(world, rand, this.chunkPos.add(rx, ry, rz));
				}
			}
		}
		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, forgeChunkPos));
    }

	protected void generateStructures(BiomeMist biome, World world, Random rand) {
		boolean check =	basementsGen.generate(world, rand, this.chunkPos);
		if (!check) check =	wellGen.generate(world, rand, this.chunkPos);
		if (!check) check =	tombGen.generate(world, rand, this.chunkPos);
		if (!check) check =	cliffTombGen.generate(world, rand, this.chunkPos);
		if (!check) check =	altarsGen.generate(world, rand, this.chunkPos);
	}

	@Override
	protected void generateOres(World world, Random rand) {
		this.genStandardOre1(world, rand, 2, this.filterCoalGen, MistWorld.fogMaxHight_S + 4, MistWorld.seaLevelUp + 5);
		this.genStandardOre1(world, rand, 3, this.ironGen, MistWorld.fogMaxHight_S, MistWorld.seaLevelUp + 20);
		this.genStandardOre1(world, rand, 8, this.mistGravelGen, MistWorld.lowerStoneHight, MistWorld.fogMaxHight_S + 4);
		this.genStandardOre2(world, rand, 5, this.goldGen, MistWorld.lowerStoneHight, MistWorld.fogMaxHight_S + 4 - MistWorld.lowerStoneHight);
		this.genStandardOre2(world, rand, 50, this.niobiumGen, MistWorld.fogMaxHight_S + 4, MistWorld.fogMaxHight_S + 4 - MistWorld.lowerStoneHight);
		this.genStandardOre2(world, rand, 15, this.bioShaleGen, MistWorld.lowerStoneHight, MistWorld.fogMaxHight_S + 4 - MistWorld.lowerStoneHight);
		this.genStandardOre2(world, rand, 8, this.saltpeterGen, MistWorld.fogMaxHight_S + 4, 50);
		this.saltGen.generate(world, rand, this.chunkPos);
		this.genStandardOre2(world, rand, 3, this.lapisGen, MistWorld.fogMaxHight_S + 4, 15);
		this.genStandardOre2(world, rand, 10, this.acidPocket, MistWorld.lowerStoneHight, 50);
	}
}