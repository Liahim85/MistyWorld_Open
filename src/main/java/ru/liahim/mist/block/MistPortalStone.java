package ru.liahim.mist.block;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.ModConfig.Dimension;

public class MistPortalStone extends MistBlock {
    public static final PropertyBool ISUP = PropertyBool.create("isup");
    public static final PropertyBool ISNEW = PropertyBool.create("isnew");
    protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0D, 0.6875D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D);
    private final boolean isWork;

    public MistPortalStone(boolean isWork) {
        super(Material.ROCK, MapColor.GRAY);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ISNEW, true).withProperty(ISUP, false));
        this.isWork = isWork;
        if (isWork) {
            this.setLightLevel(0.8F);
        }
        this.setHardness(10.0F);
        this.setResistance(1000);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        if (stack.getItemDamage() == 2) {
            tooltip.add(I18n.format("tile.mist.portal_old_down.tooltip"));
        }
        if (stack.getItemDamage() == 3) {
            tooltip.add(I18n.format("tile.mist.portal_old_up.tooltip"));
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        if (isWork) {
            if (blockState.getValue(ISUP)) {
                return UP_AABB;
            } else {
                return DOWN_AABB;
            }
        } else {
            return FULL_BLOCK_AABB;
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return !this.isWork;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return !this.isWork;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(
        IBlockState blockState,
        IBlockAccess blockAccess,
        BlockPos pos,
        EnumFacing side
    ) {
        if (this.isWork) {
            boolean isUp = blockState.getValue(ISUP);
            if (isUp && side == EnumFacing.DOWN) {
                return false;
            } else return isUp || side != EnumFacing.UP;
        }
        return true;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote && !Dimension.loadedDimBlackList.contains(world.provider.getDimension())) {
            boolean isUp = state.getValue(ISUP);
            BlockPos portalPos = isUp ? pos.down() : pos.up();
            BlockPos anotherBasePos = isUp ? pos.down(2) : pos.up(2);
            IBlockState anotherState = world.getBlockState(anotherBasePos);
            if (
                world.getBlockState(portalPos).getBlock() == Blocks.GOLD_BLOCK &&
                anotherState.getBlock() == MistBlocks.PORTAL_BASE &&
                anotherState.getValue(ISUP) == !isUp
            ) {
                createPortal(world, portalPos);
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!world.isRemote && Sets.newHashSet(MistBlocks.PORTAL, Blocks.AIR, Blocks.GOLD_BLOCK).contains(blockIn)) {
            world.scheduleUpdate(pos, state.getBlock(), 1);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && !Dimension.loadedDimBlackList.contains(world.provider.getDimension())) {
            boolean isUp = state.getValue(ISUP);
            if (!this.isWork) {
                BlockPos portalPos = pos.down();
                BlockPos bottomPos = pos.down(2);
                IBlockState bottomState = world.getBlockState(bottomPos);
                if (
                    isUp &&
                    world.getBlockState(portalPos).getBlock() == Blocks.GOLD_BLOCK &&
                    bottomState.getBlock() == MistBlocks.PORTAL_BASE &&
                    !bottomState.getValue(ISUP)
                ) {
                    createPortal(world, portalPos);
                }
            } else {
                Block portalBlock = world.getBlockState(isUp ? pos.down() : pos.up()).getBlock();

                if (portalBlock == MistBlocks.PORTAL) {
                    return;
                }

                world.setBlockState(
                    pos,
                    MistBlocks
                        .PORTAL_BASE.getDefaultState()
                        .withProperty(ISNEW, state.getValue(ISNEW))
                        .withProperty(ISUP, isUp)
                );
            }
        }
    }

    private void createPortal(World world, BlockPos pos) {
        world.setBlockToAir(pos);
        world.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 1.5F, true);
        world.setBlockState(pos, MistBlocks.PORTAL.getDefaultState());
        world.setBlockState(
            pos.up(),
            MistBlocks
                .PORTAL_WORK.getDefaultState()
                .withProperty(ISUP, true)
                .withProperty(ISNEW, world.getBlockState(pos.up()).getValue(ISNEW))
        );
        world.setBlockState(
            pos.down(),
            MistBlocks
                .PORTAL_WORK.getDefaultState()
                .withProperty(ISUP, false)
                .withProperty(ISNEW, world.getBlockState(pos.down()).getValue(ISNEW))
        );
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!this.isWork) {
            list.add(
                new ItemStack(
                    this,
                    1,
                    this.getMetaFromState(this.getDefaultState().withProperty(ISNEW, true).withProperty(ISUP, false))
                )
            );
            list.add(
                new ItemStack(
                    this,
                    1,
                    this.getMetaFromState(this.getDefaultState().withProperty(ISNEW, true).withProperty(ISUP, true))
                )
            );
            list.add(
                new ItemStack(
                    this,
                    1,
                    this.getMetaFromState(this.getDefaultState().withProperty(ISNEW, false).withProperty(ISUP, false))
                )
            );
            list.add(
                new ItemStack(
                    this,
                    1,
                    this.getMetaFromState(this.getDefaultState().withProperty(ISNEW, false).withProperty(ISUP, true))
                )
            );
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(ISNEW) ? 0 : 1 << 1) | (state.getValue(ISUP) ? 1 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(ISNEW, (meta >> 1) == 0).withProperty(ISUP, (meta & 1) == 1);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(MistBlocks.PORTAL_BASE);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ISUP, ISNEW);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return this.isWork ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
    }

    @Override
    public ItemStack getPickBlock(
        IBlockState state,
        RayTraceResult target,
        World world,
        BlockPos pos,
        EntityPlayer player
    ) {
        return new ItemStack(Item.getItemFromBlock(MistBlocks.PORTAL_BASE), 1, this.damageDropped(state));
    }
}
