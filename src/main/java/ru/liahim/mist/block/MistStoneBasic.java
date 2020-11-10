package ru.liahim.mist.block;

import ru.liahim.mist.api.block.IMistStoneBasic;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;

import javax.annotation.Nullable;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MistStoneBasic extends MistBlock implements IMistStoneBasic {

	public MistStoneBasic() {
		super(Material.ROCK, MapColor.GRAY);
		this.setHardness(800.0F);
		this.setResistance(8000);
		this.setHarvestLevel("pickaxe", 3);
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
		ISkillCapaHandler.getHandler(player).addSkill(Skill.MASON, 5);
		super.harvestBlock(world, player, pos, state, te, stack);
	}
}