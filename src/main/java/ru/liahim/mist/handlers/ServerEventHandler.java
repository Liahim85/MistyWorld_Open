package ru.liahim.mist.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.CreateFluidSourceEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.advancement.PortalType;
import ru.liahim.mist.api.block.IFarmland;
import ru.liahim.mist.api.block.IMistStone;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.api.item.ISuit;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.registry.IMistHarvest;
import ru.liahim.mist.api.registry.MistRegistry;
import ru.liahim.mist.block.MistBlockBranch;
import ru.liahim.mist.block.MistBlockSlab;
import ru.liahim.mist.block.MistBlockStairs;
import ru.liahim.mist.block.MistBlockStep;
import ru.liahim.mist.block.MistLooseRock;
import ru.liahim.mist.block.MistPortalStone;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistOreUpper;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.block.gizmos.MistFlowerPot;
import ru.liahim.mist.block.gizmos.MistChest;
import ru.liahim.mist.capability.MistCapability;
import ru.liahim.mist.capability.SkillCapability;
import ru.liahim.mist.capability.FoodCapability;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.IFoodHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler.HurtType;
import ru.liahim.mist.capability.handler.SkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.capability.handler.FoodHandler;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.EntityAnimalMist;
import ru.liahim.mist.entity.ai.EntityAIEatMistGrass;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModConfig.Dimension;
import ru.liahim.mist.init.ModItems;
import ru.liahim.mist.item.ItemMistMapDown;
import ru.liahim.mist.item.ItemMistMapUp;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketMaskSync;
import ru.liahim.mist.network.PacketMushroomSync;
import ru.liahim.mist.network.PacketSeedSync;
import ru.liahim.mist.network.PacketSkillSync;
import ru.liahim.mist.network.PacketTimeSync;
import ru.liahim.mist.network.PacketToxicFoodSync;
import ru.liahim.mist.network.PacketToxicSync;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.util.PlayerLocationData;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.util.TimeData;
import ru.liahim.mist.world.FogDamage;
import ru.liahim.mist.world.MistWorld;

public class ServerEventHandler {

	private static final HashMap<UUID,ItemStack> maskSync = new HashMap<UUID,ItemStack>();
	private static final HashMap<UUID,Integer> mulchDelay = new HashMap<UUID,Integer>();
	private static final HashMap<UUID,Integer> portDelay = new HashMap<UUID,Integer>();
	private static final HashSet<UUID> milkCheck = new HashSet<UUID>();

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if (event.phase == Phase.START && event.side == Side.SERVER) {
			EntityPlayer player = event.player;
			if (player != null) {
				World world = player.world;
				UUID uuid = player.getUniqueID();
				//Post teleportation fix
				if (!portDelay.isEmpty() && portDelay.containsKey(uuid)) {
					int i = portDelay.get(uuid);
					if (i > 0) portDelay.replace(uuid, i - 1);
					else {
						if (player instanceof EntityPlayerMP) ((EntityPlayerMP)player).clearInvulnerableDimensionChange();
						portDelay.remove(uuid);
					}
				}
				//Mask sync
				if (!maskSync.containsKey(uuid)) maskSync.put(uuid, ItemStack.EMPTY);
				IMistCapaHandler maskHandler = IMistCapaHandler.getHandler(player);
				ItemStack stack = maskHandler.getMask();
				IMask mask = null;
				if (!stack.isEmpty() && stack.getItem() instanceof IMask) {
					mask = (IMask)stack.getItem();
					//Worn Tick
					mask.onWornTick(stack, player);
				}
				if(maskHandler.isMaskChanged() || (mask != null && mask.willAutoSync(stack, player) && !ItemStack.areItemStacksEqual(stack, maskSync.get(uuid)))) {
					try {
						if (maskHandler.isGlobalChanged()) {
							PacketHandler.INSTANCE.sendToDimension(new PacketMaskSync(player, stack), player.world.provider.getDimension());
						} else if (player instanceof EntityPlayerMP) {
							PacketHandler.INSTANCE.sendTo(new PacketMaskSync(player, stack), (EntityPlayerMP)player);
						}
						maskHandler.setMaskChanged(false, false);
					} catch (Exception e) {}
					maskSync.put(uuid, stack); 
				}
				//Sleeping
				if (player.isPlayerSleeping()) {
					if (world.provider.getDimension() == 0) {
						if (player.isPlayerSleeping() && player.ticksExisted % 90 == 0) {
							WorldServer otherWorld = DimensionManager.getWorld(Mist.getID());
							if (otherWorld != null && !otherWorld.playerEntities.isEmpty() && !isAllPlayersAsleep(otherWorld)) {
								player.bedLocation = player.getPosition().down();
								player.wakeUpPlayer(true, false, false);
								player.trySleep(player.bedLocation);
							}
						}
					} else if (world.provider.getDimension() == Mist.getID()) {
						if (player.isPlayerSleeping() && player.ticksExisted % 90 == 0) {
							WorldServer otherWorld = DimensionManager.getWorld(0);
							if (otherWorld != null && !otherWorld.playerEntities.isEmpty() && !isAllPlayersAsleep(otherWorld)) {
								player.bedLocation = player.getPosition().down();
								player.wakeUpPlayer(true, false, false);
								player.trySleep(player.bedLocation);
							}
						}
					}
				} //TODO сделать коррозию блоков вокруг игрока (Блок трухи)
				if (!mulchDelay.isEmpty() && mulchDelay.containsKey(uuid)) {
					int i = mulchDelay.get(uuid);
					if (i > 0) mulchDelay.replace(uuid, i - 1);
					else mulchDelay.remove(uuid);
				}
			}
		}
	}

	private boolean isAllPlayersAsleep(World world) {
        for (EntityPlayer entityplayer : world.playerEntities) {
            if (!entityplayer.isSpectator() && !entityplayer.isPlayerSleeping()) return false;
        }
        return true;
    }

	private boolean isAllPlayersFullyAsleep(World world) {
        for (EntityPlayer entityplayer : world.playerEntities) {
            if (!entityplayer.isSpectator() && !entityplayer.isPlayerFullyAsleep()) return false;
        }
        return true;
    }

	@SubscribeEvent
	public void onPlayerTrySleep(PlayerSleepInBedEvent event) {
		if (event.getEntityPlayer().world.provider.getDimension() == Mist.getID() &&
				event.getPos().getY() < MistWorld.getFogMaxHight() + 4) {
			event.getEntityPlayer().sendMessage(new TextComponentTranslation("tile.bed.in_fog", new Object[0]));
			event.setResult(SleepResult.NOT_POSSIBLE_HERE);
		}
	}

	@SubscribeEvent
	public void onPlayerWakeUp(PlayerWakeUpEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;
		if (!world.isRemote && world.provider.getDimension() == Mist.getID() && player == world.playerEntities.get(0)) {
			WorldServer normalWorld = DimensionManager.getWorld(0);
			boolean normalWorldEmpty = true;
			for (EntityPlayer entityplayer : normalWorld.playerEntities) {
                if (normalWorldEmpty && !entityplayer.isSpectator())
                	normalWorldEmpty = false;
            }
			if (normalWorldEmpty && isAllPlayersFullyAsleep(world) && world.getGameRules().getBoolean("doDaylightCycle")) {
				long i = normalWorld.getWorldInfo().getWorldTime() + 24000L;
				normalWorld.getWorldInfo().setWorldTime(i - i % 24000L);
				normalWorld.provider.resetRainAndThunder();
				MistTime.wakeUp();
			}
		}
	}

	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		if (event.getState().getBlock() instanceof IMistStone) {
			ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
			event.setNewSpeed(MistRegistry.getBreakingSpeed(stack, event.getState(), event.getOriginalSpeed()));
		} else if (event.getState().getBlock() instanceof MistTreeTrunk) {
			int size = event.getState().getValue(MistTreeTrunk.SIZE);
			event.setNewSpeed((event.getOriginalSpeed() + 4 - size) / 2);
		} else if (event.getState().getBlock() instanceof MistSoil) {
			World world = event.getEntityPlayer().world;
			IBlockState state = world.getBlockState(event.getPos().up());
			if (state.getBlock() instanceof MistTreeTrunk && ((MistTreeTrunk)state.getBlock()).getDir(state) == EnumFacing.UP) {
				event.setNewSpeed(Math.min(event.getOriginalSpeed(), event.getEntityPlayer().getDigSpeed(state, event.getPos().up()) / 8));
			}
		} else if (event.getState().getBlock() == MistBlocks.FLOATING_MAT) {
			if (!event.getEntityPlayer().onGround) {
				event.setNewSpeed(event.getOriginalSpeed() / 3);
			}
		}
	}

	@SubscribeEvent
	public void getStone(LivingExperienceDropEvent event) {
		//Drop portal stones
		if (!ModConfig.dimension.disableStoneDrop && event.getAttackingPlayer() != null) {
			World world = event.getAttackingPlayer().world;
			if (!world.isRemote && world.provider.getDimension() != Mist.getID()) {
				if (event.getEntityLiving() instanceof EntityEnderman && event.getAttackingPlayer() instanceof EntityPlayerMP) {
					EntityEnderman enderman = (EntityEnderman)event.getEntityLiving();
					EntityPlayerMP player = (EntityPlayerMP)event.getAttackingPlayer();
					if (world instanceof WorldServer && !player.getAdvancements().getProgress(((WorldServer)world)
							.getAdvancementManager().getAdvancement(new ResourceLocation(Mist.MODID, "story/full_set"))).isDone()) {
						dropStone(world, enderman);
					} else if (world.isThundering() && world.rand.nextInt(10) == 0) {
						dropStone(world, enderman);
					}
				}
			}
		}
	}

	protected void dropStone(World world, EntityEnderman enderman) {
		EntityItem itemPortalDown = new EntityItem(world, enderman.posX, enderman.posY, enderman.posZ, new ItemStack(MistBlocks.PORTAL_BASE, 1, 2));
		EntityItem itemPortalUp = new EntityItem(world, enderman.posX, enderman.posY, enderman.posZ, new ItemStack(MistBlocks.PORTAL_BASE, 1, 3));
		itemPortalDown.setPickupDelay(10);
		itemPortalUp.setPickupDelay(10);
		world.spawnEntity(itemPortalDown);
		world.spawnEntity(itemPortalUp);
	}

	@SubscribeEvent
	public void itemPickup(EntityItemPickupEvent event) {
		if (event.getEntityPlayer() != null && event.getEntityPlayer() instanceof EntityPlayerMP) {
			ModAdvancements.ITEM_PICKUP.trigger((EntityPlayerMP) event.getEntityPlayer(), event.getItem().getItem());
		}
	}

	@SubscribeEvent
	public void itemCrafting(ItemCraftedEvent event) {
		if (event.player != null && event.player instanceof EntityPlayerMP) {
			ModAdvancements.ITEM_CRAFTED.trigger((EntityPlayerMP) event.player, event.crafting);
		}
	}

	@SubscribeEvent
	public void itemSmelting(ItemSmeltedEvent event) {
		if (event.player != null && event.player instanceof EntityPlayerMP) {
			ModAdvancements.ITEM_SMELTED.trigger((EntityPlayerMP) event.player, event.smelting);
		}
	}

	@SubscribeEvent
	public void placeBlock(PlaceEvent event) {
		World world = event.getWorld();
		//Get create portal achievement
		if (!world.isRemote && !Dimension.loadedDimBlackList.contains(world.provider.getDimension())) {
			Block block = event.getState().getBlock();
			if (block == Blocks.GOLD_BLOCK || block == MistBlocks.PORTAL_BASE) {
				boolean check = false;
				BlockPos pos = event.getPos();
				BlockPos portalPos = BlockPos.ORIGIN;
				if (block == Blocks.GOLD_BLOCK) {
					IBlockState up = world.getBlockState(pos.up());
					IBlockState down = world.getBlockState(pos.down());
					if (up.getBlock() == MistBlocks.PORTAL_BASE && up.getValue(MistPortalStone.ISUP) == true &&
							down.getBlock() == MistBlocks.PORTAL_BASE && down.getValue(MistPortalStone.ISUP) == false) {
						check = true;
						portalPos = pos;
					}
				} else {
					if (event.getState().getValue(MistPortalStone.ISUP)) {
						IBlockState center = world.getBlockState(pos.down());
						IBlockState down = world.getBlockState(pos.down(2));
						if (center.getBlock() == Blocks.GOLD_BLOCK &&
								down.getBlock() == MistBlocks.PORTAL_BASE && down.getValue(MistPortalStone.ISUP) == false) {
							check = true;
							portalPos = pos.down();
						}
					} else {
						IBlockState up = world.getBlockState(pos.up(2));
						IBlockState center = world.getBlockState(pos.up());
						if (up.getBlock() == MistBlocks.PORTAL_BASE && up.getValue(MistPortalStone.ISUP) == true &&
								center.getBlock() == Blocks.GOLD_BLOCK) {
							check = true;
							portalPos = pos.up();
						}
					}
				}
				if (check) {
					if (event.getPlayer() != null && event.getPlayer() instanceof EntityPlayerMP) {
						EntityPlayerMP playerMP = (EntityPlayerMP)event.getPlayer();
						ModAdvancements.OPEN_PORTAL.trigger(playerMP, world, portalPos, PortalType.DIMENSION);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void blockHarvest(HarvestDropsEvent event) {
		if (!event.getWorld().isRemote) {
			Block block = event.getState().getBlock();
			//Niobium Pickaxe use
			if (block == MistBlocks.FILTER_COAL_ORE) {
				if (event.getHarvester() != null) {
					Item pickaxe = event.getHarvester().inventory.getCurrentItem().getItem();
					if (pickaxe != MistItems.NIOBIUM_PICKAXE) event.getDrops().clear();
				} else event.getDrops().clear();
			}
			//Crops
			else if (block instanceof BlockCrops) {
				if (((BlockCrops)block).isMaxAge(event.getState())) {
					IBlockState soil = event.getWorld().getBlockState(event.getPos().down());
					if (soil.getBlock() instanceof IWettable) {
						Random rand = event.getWorld().rand;
						boolean norm = IMistHarvest.isSoilSuitable(event.getState().getBlock(), soil);
						Item crop = ((BlockCrops)block).getItemDropped(event.getState(), rand, 0);
						ItemStack stack = ItemStack.EMPTY;
						int count = 0;
						for (int j = 0; j < event.getDrops().size(); ++j) {
							if (event.getDrops().get(j).getItem() == crop) {
								if (stack.isEmpty()) stack = event.getDrops().get(j);
								++count;
							}
						}
						if (count > 0) {
							int k = count;
							if (norm && count == 1) count += rand.nextInt(2);
							else if (!norm && count > 1) count = count/2 + (rand.nextInt(3) == 0 ? 1 : 0);
							if (count != k) {
								for (int j = event.getDrops().size() - 1; j >= 0; --j) {
									if (event.getDrops().get(j).getItem() == crop) event.getDrops().remove(j);
								}
								for (int j = 0; j < count; ++j) {
									event.getDrops().add(stack);
								}
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void breakBlock(BreakEvent event) {
		if (!event.getWorld().isRemote) {
			if (event.getPlayer() != null && !event.getPlayer().isCreative()) {
				//Niobium Pickaxe use
				if (event.getState().getBlock() instanceof MistOreUpper) {
					Item pickaxe = event.getPlayer().inventory.getCurrentItem().getItem();
					if (pickaxe != MistItems.NIOBIUM_PICKAXE) event.setExpToDrop(0);
				}
				//Sapling drop
				else if (event.getState().getBlock() instanceof MistTreeTrunk) {
					event.setCanceled(((MistTreeTrunk)event.getState().getBlock()).dropSapling(event.getWorld(), event.getPos()));
				}
			}
		}
	}

	@SubscribeEvent
	public void useItem(RightClickItem event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		if (item == Items.MAP) {
			EntityPlayer player = event.getEntityPlayer();
			if (player.world.provider.getDimension() == Mist.getID()) {
				ItemStack mistMapUp;
				if (player.getPosition().getY() > MistWorld.getFogHight(player.world, 0) - 6) {
					mistMapUp = ItemMistMapUp.setupNewMap(player.world, player.posX, player.posZ, (byte) 0, true, false);
				} else {
					mistMapUp = ItemMistMapDown.setupNewMap(player.world, player.posX, player.posZ, (byte) 0, true, false);
				}
				stack.shrink(1);
				if (stack.isEmpty()) {
					player.setHeldItem(event.getHand(), mistMapUp);
					event.setCancellationResult(EnumActionResult.SUCCESS);
					event.setCanceled(true);
				} else {
					if (!player.inventory.addItemStackToInventory(mistMapUp.copy())) {
						player.dropItem(mistMapUp, false);
					}
					player.addStat(StatList.getObjectUseStats(item));
					event.setCancellationResult(EnumActionResult.SUCCESS);
					event.setCanceled(true);
				}
			}
		} else if (item != MistItems.GLASS_CONTAINER && (item.getItemUseAction(stack) == EnumAction.EAT || item.getItemUseAction(stack) == EnumAction.DRINK)) {
			EntityPlayer player = event.getEntityPlayer();
			ItemStack mask = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
			if (!IMask.isMask(mask)) {
				IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
				if (capa != null) mask = capa.getMask();
			}
			if (!IMask.canEat(mask)) {
				player.sendMessage(new TextComponentTranslation("item.mist.respirator_close.tooltip", new Object[0]));
				event.setCancellationResult(EnumActionResult.SUCCESS);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void useBlock(RightClickBlock event) {
		//Create Compost Heap & Fire Pit
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty()) {
			World world = event.getWorld();
			BlockPos pos = event.getPos();
			Block block = world.getBlockState(pos).getBlock();
			if (event.getEntityPlayer().isSneaking()) {
				if (block == MistBlocks.CAMPFIRE || block == MistBlocks.CAMP_STICK) {
					if (stack.isStackable()) event.setUseBlock(Result.ALLOW);
				} else if (event.getFace() == EnumFacing.UP) {
					if (world.getBlockState(pos).getMaterial().isReplaceable()) pos = pos.down();
					if (world.getBlockState(pos.up()).getMaterial().isReplaceable()) {
						Vec3d vec = event.getHitVec();
						if (vec == null) vec = new Vec3d(pos);
						if (MistRegistry.checkShiftPlacing(world, pos.up(), stack, event.getEntityPlayer(), (float)vec.x - pos.getX(), (float)vec.y - pos.getY(), (float)vec.z - pos.getZ(), world.getBlockState(pos).getBlockFaceShape(world, pos, EnumFacing.UP))) {
							event.setCanceled(true);
							event.setCancellationResult(EnumActionResult.SUCCESS);
						}
					}
				}
			} else {
				if (block == Blocks.FLOWER_POT) {
					if (MistFlowerPot.canBePotted(stack)) {
						IBlockState pot = MistBlocks.FLOWER_POT.getDefaultState();
						world.setBlockState(pos, pot, 3);
						TileEntityFlowerPot te = MistFlowerPot.getTileEntity(world, pos);
						if (te != null) {
							if (event.getEntityPlayer().capabilities.isCreativeMode) {
								ItemStack fl = stack.copy();
								fl.setCount(1);
								te.setItemStack(fl);
							} else te.setItemStack(stack.splitStack(1));
							te.markDirty();
							world.notifyBlockUpdate(pos, pot, pot, 3);
						}
						event.setCanceled(true);
						event.setCancellationResult(EnumActionResult.SUCCESS);
					}
				} else if (block == MistBlocks.CAMPFIRE) {
					if (stack.getItem() instanceof ItemFlintAndSteel || stack.getItem() instanceof ItemFood) {
						event.setUseItem(Result.DENY);
					}
				}
			}
		}
	}

	public static void setMulchDelay(UUID uuid, int delay) {
		if (mulchDelay.containsKey(uuid)) mulchDelay.replace(uuid, delay);
		else mulchDelay.put(uuid, delay);
	}

	public static boolean isMulchDelay(UUID uuid) {
		return mulchDelay.containsKey(uuid);
	}

	@SubscribeEvent
	public void clickBlock(LeftClickBlock event) {
		if (!event.getWorld().isRemote && !event.getEntityPlayer().isCreative()) {
			IBlockState state = event.getWorld().getBlockState(event.getPos());
			if (state.getBlock() instanceof BlockCrops) {
				if (event.getEntityPlayer() != null) setMulchDelay(event.getEntityPlayer().getUniqueID(), 5);
			} else if (state.getBlock() instanceof IFarmland && state.getValue(IFarmland.MULCH) &&
					event.getHitVec().y - event.getPos().getY() > 0.9375F && !isMulchDelay(event.getEntityPlayer().getUniqueID())) {
				IFarmland.extractMulch(event.getWorld(), event.getPos(), state);
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(Mist.MODID, "container"), new MistCapability.Provider(new MistCapaHandler()));
			event.addCapability(new ResourceLocation(Mist.MODID, "food_capa"), new FoodCapability.Provider());
			event.addCapability(new ResourceLocation(Mist.MODID, "skill_capa"), new SkillCapability.Provider());
		}
	}

	@SubscribeEvent
	public void joinEntity(EntityJoinWorldEvent event) {
		if (!event.getWorld().isRemote) {
			if (event.getEntity() instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) event.getEntity();
				if (event.getWorld().provider.getDimension() == Mist.getID()) PacketHandler.INSTANCE.sendTo(new PacketSeedSync(event.getWorld().getSeed()), playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketTimeSync(MistTime.getDay(), MistTime.getMonth(), MistTime.getYear(), MistTime.getTimeOffset()), playerMP);
				//Mask sync
				IMistCapaHandler maskHandler = IMistCapaHandler.getHandler(playerMP);
				maskHandler.setMaskChanged(true, false);
				for (EntityPlayer player : playerMP.getEntityWorld().playerEntities) {
					if (player.getEntityId() != playerMP.getEntityId()) {
						IMistCapaHandler playerMasks = IMistCapaHandler.getHandler(player);	
						playerMasks.setMaskChanged(true, true);
					}
				}
				maskSync.put(playerMP.getUniqueID(), ItemStack.EMPTY);
				PacketHandler.INSTANCE.sendTo(new PacketSkillSync(ISkillCapaHandler.getHandler(playerMP).getSkillsArray()), playerMP);
				IFoodHandler foodHandler = IFoodHandler.getHandler(playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketMushroomSync(foodHandler.getMushroomList(false), false), playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketMushroomSync(foodHandler.getMushroomList(true), true), playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketToxicFoodSync(foodHandler.getFoodStudyList()), playerMP);
				IMistCapaHandler capa = IMistCapaHandler.getHandler(playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketToxicSync(capa.getPollution(), HurtType.POLLUTION.getID()), playerMP);
				PacketHandler.INSTANCE.sendTo(new PacketToxicSync(capa.getToxic(), HurtType.TOXIC.getID()), playerMP);
			} else if (event.getEntity() instanceof EntityLiving) {
				if (event.getWorld().provider.getDimension() == Mist.getID()) {
					ResourceLocation res = EntityList.getKey(event.getEntity());
					if (res != null && MistRegistry.mobsDimsBlackList.contains(res.getResourceDomain()) || MistRegistry.mobsBlackList.contains(res)) {
						event.setCanceled(true);
					}
				}
				if (event.getEntity() instanceof EntitySheep) {
					EntitySheep sheep = (EntitySheep)event.getEntity();
					sheep.tasks.addTask(5, new EntityAIEatMistGrass(sheep, false));
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDim(PlayerChangedDimensionEvent event) {
		//Post teleportation fix
		if (event.fromDim == Mist.getID() || event.toDim == Mist.getID()) {
			portDelay.put(event.player.getUniqueID(), 200);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP && !event.player.world.isRemote) {
			TileEntityCampfire.updateColorsToClient((EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public void onPlayerConnectedToServer(ClientConnectedToServerEvent event) {
		ModConfig.applyFirePitColors(true);
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		maskSync.remove(event.player.getUniqueID());
	}

	@SubscribeEvent
	public void cloneCapabilitiesEvent(PlayerEvent.Clone event) {		
		try {
			MistCapaHandler original = (MistCapaHandler)IMistCapaHandler.getHandler(event.getOriginal());
			NBTTagCompound nbt = original.serializeNBT();
			MistCapaHandler clone = (MistCapaHandler)IMistCapaHandler.getHandler(event.getEntityPlayer());
			clone.deserializeNBT(nbt);
		} catch (Exception e) {
			Mist.logger.error("Could not clone player [" + event.getOriginal().getName() + "] mask when changing dimensions");
		}
		try {
			FoodHandler original = (FoodHandler)IFoodHandler.getHandler(event.getOriginal());
			NBTTagCompound nbt = original.serializeNBT();
			FoodHandler clone = (FoodHandler)IFoodHandler.getHandler(event.getEntityPlayer());
			clone.deserializeNBT(nbt);
		} catch (Exception e) {
			Mist.logger.error("Could not clone player [" + event.getOriginal().getName() + "] food study list when changing dimensions");
		}
		try {
			SkillCapaHandler original = (SkillCapaHandler)ISkillCapaHandler.getHandler(event.getOriginal());
			NBTTagCompound nbt = original.serializeNBT();
			SkillCapaHandler clone = (SkillCapaHandler)ISkillCapaHandler.getHandler(event.getEntityPlayer());
			clone.deserializeNBT(nbt);
		} catch (Exception e) {
			Mist.logger.error("Could not clone player [" + event.getOriginal().getName() + "] skills list when changing dimensions");
		}
	}

	@SubscribeEvent
	public void playerAttacked(LivingAttackEvent event) {
		if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
			if (!player.isCreative()) {
				if (!event.getSource().isUnblockable() || event.getSource() == MistWorld.DISSOLUTION) {
					boolean asid = event.getSource() == MistWorld.DISSOLUTION;
					float damage = asid ? Math.min(event.getAmount(), 2)/4 : event.getAmount()/4;
					if (damage > 0) {
						if (damage < 1.0F) damage = 1.0F;
						if (asid) {
							ItemStack armor;
							ArmorMaterial material;
							for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
								if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
									armor = player.getItemStackFromSlot(slot);
					            	if (armor.getItem() instanceof ItemArmor && !IMask.isMask(armor)) {
						            	material = ((ItemArmor)armor.getItem()).getArmorMaterial();
						            	if (material != ArmorMaterial.GOLD && material != ModItems.NIOBIUM_ARMOR && material != ModItems.RUBBER_MATERIAL) {
						            		armor.damageItem((int)damage, player);
							                if (armor.getCount() == 0) player.setItemStackToSlot(slot, ItemStack.EMPTY);
						            	}
					            	}
					            }
					        }
						} else if (event.getSource() != MistWorld.IN_FOG) {
							ItemStack armor;
							for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
								if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
									armor = player.getItemStackFromSlot(slot);
					            	if (armor.getItem() instanceof ISuit) {
					            		armor.damageItem((int)damage, player);
						                if (armor.getCount() == 0) player.setItemStackToSlot(slot, ItemStack.EMPTY);
					            	} else {
					            		NBTTagCompound tag = armor.getSubCompound(MistTags.nbtInnerSuitTag);
										if (tag != null) {
											ItemStack suit = new ItemStack(tag);
											suit.damageItem((int)damage, player);
							                if (suit.getCount() == 0) armor.removeSubCompound(MistTags.nbtInnerSuitTag);
							                else {
							                	tag = suit.serializeNBT();
							                	armor.getTagCompound().setTag(MistTags.nbtInnerSuitTag, tag);
							                }
										}
					            	}
					            }
					        }
						}
						if (player.getRNG().nextBoolean()) {
							ItemStack mask = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
							boolean maskSlot = false;
							if (!IMask.isMask(mask)) {
								mask = IMistCapaHandler.getHandler(player).getMask();
								maskSlot = true;
							}
							if (IMask.isMask(mask)) {
								boolean drop = false;
								mask.damageItem((int)damage, player);
								if (maskSlot) {
									IMistCapaHandler maskHandler = IMistCapaHandler.getHandler(player);
									if (maskHandler.getMask().getCount() == 0) {
										maskHandler.setStackInSlot(0, ItemStack.EMPTY);
										//maskHandler.setMaskChanged(true, true);
										drop = true;
									}
								} else {
									if (mask.getCount() == 0) {
										player.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
										drop = true;
									}
								}
								if (drop && !player.world.isRemote) {
									ItemStack filter = IMask.getFilter(mask);
									if (filter.isItemStackDamageable()) player.entityDropItem(filter, 1);
								}
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerDeath(PlayerDropsEvent event) {
		World world = event.getEntity().world;
		if (event.getEntity() instanceof EntityPlayer && !world.isRemote) {
			IMistCapaHandler capa = IMistCapaHandler.getHandler(event.getEntityPlayer());
			if (!capa.getMask().isEmpty() && !event.getEntity().world.getGameRules().getBoolean("keepInventory")) {
				event.getEntityPlayer().entityDropItem(capa.getMask(), 1);
				capa.setStackInSlot(0, ItemStack.EMPTY);
			}
			if (world.provider.getDimension() == Mist.getID() && event.getEntityPlayer() instanceof EntityPlayerMP) {
				setSpawnPos((EntityPlayerMP) event.getEntityPlayer());
			}
		}
	}

	public static void setSpawnPos(EntityPlayerMP player) {
		IBlockState state = null;
		BlockPos pos = player.getBedLocation(Mist.getID());
		if (pos != null) state = DimensionManager.getWorld(Mist.getID()).getBlockState(pos);
		if (pos == null || !state.getBlock().isBed(state, player.world, pos, player)) {
			pos = PlayerLocationData.get(player.world).getSpawnPos(player);
			if (pos != null) {
				player.setSpawnChunk(pos, true, Mist.getID());
				player.connection.sendPacket(new SPacketSpawnPosition(pos));
			}
		}
	}

	@SubscribeEvent
	public void playerRespawn(PlayerRespawnEvent event) {
		EntityPlayer player = event.player;
		IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
		int i = ModConfig.player.effectsReduction * 100;
		if (capa.getToxic() > i) capa.setToxic(i);
		if (capa.getPollution() > i) capa.setPollution(i);
	}

	@SubscribeEvent
	public void entityItemDead(ItemExpireEvent event) {
		World world = event.getEntity().world;
		Item item = event.getEntityItem().getItem().getItem();
		if (item == MistItems.ROCKS || (item == Items.FLINT && world.provider.getDimension() == Mist.getID())) {
			BlockPos pos = event.getEntity().getPosition();
			if (world.getBlockState(pos).getMaterial().isReplaceable() && world.isSideSolid(pos.down(), EnumFacing.UP)) {
				if (item == MistItems.ROCKS) {
					world.setBlockState(pos, MistBlocks.LOOSE_ROCK.getDefaultState());
				} else {
					world.setBlockState(pos, MistBlocks.LOOSE_ROCK.getDefaultState().withProperty(MistLooseRock.TYPE, 1));
				}
			}
		} else if (item == MistItems.REMAINS) {
			BlockPos pos = event.getEntity().getPosition();
			if (world.getBlockState(pos).getMaterial().isReplaceable() && world.isSideSolid(pos.down(), EnumFacing.UP)) {
				world.setBlockState(pos, MistBlocks.REMAINS.getDefaultState());
			}
		}
	}

	@SubscribeEvent
	public void onServerWorldTick(ServerTickEvent event) {
		//Update time
		if(event.phase == Phase.START) {
			MistTime.updateTime(DimensionManager.getWorld(0));
		}
	}

	@SubscribeEvent
	public void onMobsDeath(LivingDeathEvent event) {
		Entity entity = event.getSource().getTrueSource();
		if (entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			Entity mob = event.getEntity();
			if (mob instanceof EntityLiving && !(mob instanceof IMob)) {
				ResourceLocation res = EntityList.getKey(mob);
				if (res != null) {
					int point = 0;
					if (MistRegistry.dimsForSkill.containsKey(res.getResourceDomain())) point = MistRegistry.dimsForSkill.get(res.getResourceDomain());
					if (MistRegistry.mobsForSkill.containsKey(res)) point = MistRegistry.mobsForSkill.get(res);
					if (point > 0) ISkillCapaHandler.getHandler((EntityPlayer)entity).addSkill(Skill.CUTTING, point);
				}
			}
		}
	}

	@SubscribeEvent
	public void getXPOrb(PlayerPickupXpEvent event) {
		if (!event.getEntityPlayer().world.isRemote) {
			IMistCapaHandler maskHandler = IMistCapaHandler.getHandler(event.getEntityPlayer());
			ItemStack mask = maskHandler.getMask();
			if (!mask.isEmpty() && mask.isItemDamaged() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, mask) > 0) {
				event.getEntityPlayer().xpCooldown = 2;
				event.getOrb().world.playSound((EntityPlayer)null, event.getEntityPlayer().posX, event.getEntityPlayer().posY, event.getEntityPlayer().posZ,
						SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.1F, 0.5F * ((event.getEntityPlayer().getRNG().nextFloat() - event.getEntityPlayer().getRNG().nextFloat()) * 0.7F + 1.8F));
                event.getEntityPlayer().onItemPickup(event.getOrb(), 1);
				Iterable<ItemStack> iterable = Enchantments.MENDING.getEntityEquipment(event.getEntityPlayer());
				List<ItemStack> list = Lists.<ItemStack>newArrayList();
				if (iterable != null) {
					for (ItemStack itemstack : iterable) {
						if (!itemstack.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, itemstack) > 0) {
							list.add(itemstack);
						}
					}
				}
				list.add(mask);
				ItemStack itemstack = list.get(event.getEntityPlayer().getRNG().nextInt(list.size()));
				if (!itemstack.isEmpty() && itemstack.isItemDamaged()) {
                    int i = Math.min(event.getOrb().xpValue * 2, itemstack.getItemDamage());
                    event.getOrb().xpValue -= i / 2;
                    itemstack.setItemDamage(itemstack.getItemDamage() - i);
                }
                if (event.getOrb().xpValue > 0) event.getEntityPlayer().addExperience(event.getOrb().xpValue);
                event.getOrb().setDead();
                event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void fogDamage(LivingUpdateEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		FogDamage.calculateFogDamage(entity);
	}

	@SubscribeEvent
	public void itemUseStart(LivingEntityUseItemEvent.Start event) {
		if (!event.getEntity().world.isRemote && event.getItem().getItem() == Items.MILK_BUCKET && event.getEntityLiving() instanceof EntityPlayer) {
			milkCheck.add(event.getEntityLiving().getUniqueID());
		}
	}

	@SubscribeEvent
	public void itemUseStop(LivingEntityUseItemEvent.Stop event) {
		if (!event.getEntity().world.isRemote && milkCheck.contains(event.getEntityLiving().getUniqueID())) {
			milkCheck.remove(event.getEntityLiving().getUniqueID());
		}
	}

	@SubscribeEvent
	public void itemUseFinish(LivingEntityUseItemEvent.Finish event) {
		if (!event.getEntity().world.isRemote && event.getEntityLiving() instanceof EntityPlayer) {
			ItemStack stack = event.getItem();
			Item item = stack.getItem();
			if (item.getItemUseAction(stack) == EnumAction.EAT || item.getItemUseAction(stack) == EnumAction.DRINK) {
				EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				boolean fog = MistWorld.isPosInFog(player.world, (float)player.posY + player.eyeHeight);
				IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
				if (capa != null) {
					if (milkCheck.contains(player.getUniqueID())) {
						if (!fog) {
							capa.addToxic(-20);
							milkCheck.remove(event.getEntityLiving().getUniqueID());
						}
					} else if (fog && item != MistItems.GLASS_CONTAINER) {
						capa.addToxic(32);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void createFluidSource(CreateFluidSourceEvent event) {
		if (!event.getWorld().isRemote) {
			if (event.getState().getBlock() == Blocks.FLOWING_WATER) {
				if (MistWorld.isPosInFog(event.getWorld(), event.getPos().up(8))) {
					event.setResult(Result.DENY);
				}
			} else if (event.getState().getBlock() == MistBlocks.ACID_BLOCK) {
				if (MistWorld.isPosInFog(event.getWorld(), event.getPos().up(8))) {
					event.setResult(Result.ALLOW);
				} else event.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void burningTime(FurnaceFuelBurnTimeEvent event) {
		int i = getBurnTime(event.getItemStack());
		if (i > 0) event.setBurnTime(i);
	}

	private int getBurnTime(ItemStack fuel) {
		Item item = fuel.getItem();
		if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
			Block block = Block.getBlockFromItem(item);
			if (block instanceof MistWoodBlock) {
				int meta = fuel.getItemDamage();
				if (meta == 0 || meta == 3) return 640;
				if (meta == 4) return 620;
				else return 600;
			}
			if (block instanceof MistBlockStairs && block.getDefaultState().getMaterial() == Material.WOOD) return 450;
			if (block instanceof MistBlockSlab && block.getDefaultState().getMaterial() == Material.WOOD) return 300;
			if (block instanceof MistBlockStep && block.getDefaultState().getMaterial() == Material.WOOD) return 150;
			if (block instanceof MistBlockBranch && block.getDefaultState().getMaterial() == Material.WOOD) {
				int meta = fuel.getItemDamage();
				if (meta == 0) return 120;
				if (meta == 3) return 100;
				if (meta == 6) return 170;
				if (meta == 9) return 150;
			}
			if (block == MistBlocks.FILTER_COAL_BLOCK) return (16 - fuel.getItemDamage()) * 1000;
			if (block == MistBlocks.BIO_SHALE_BLOCK) return 16000;
			if (block == MistBlocks.PEAT && fuel.getItemDamage() == 1) return 2400;
			if (block == MistBlocks.SAPROPEL && fuel.getItemDamage() == 1) return 1200;
			if (block == MistBlocks.MULCH_BLOCK) return 180;
		}
		if (item == MistItems.FILTER_COAL) return Math.max(1, 1600 * (fuel.getMaxDamage() - fuel.getItemDamage()) / fuel.getMaxDamage());
		if (item == MistItems.BIO_SHALE) return 1600;
		if (item == MistItems.MULCH) return 20;
		return -1;
	}

	///////////////////////////////////////// Farming /////////////////////////////////////////

	@SubscribeEvent
	public void useHoeEvent(UseHoeEvent event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof MistSoil && !(state.getBlock() instanceof IFarmland) && world.isAirBlock(pos.up())) {
			world.playSound(event.getEntityPlayer(), pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			if (!world.isRemote) world.setBlockState(pos, ((MistSoil)state.getBlock()).getFarmState(state));
			event.setResult(Result.ALLOW);
		}
	}

	@SubscribeEvent
	public void cropGrow(CropGrowEvent.Pre event) {
		if (!event.getWorld().isRemote && event.getState().getBlock() instanceof BlockCrops) {
			if (MistWorld.isPosInFog(event.getWorld(), event.getPos())) {
				event.getWorld().destroyBlock(event.getPos(), false);
				event.setResult(Result.DENY);
			} else {
				IBlockState soil = event.getWorld().getBlockState(event.getPos().down());
				if (soil.getBlock() instanceof MistSoil) {
					if (soil.getValue(IWettable.WET) && SoilHelper.getHumus(soil) > 0) {
						if (IMistHarvest.isSoilSuitable(event.getState().getBlock(), soil))
							event.setResult(Result.DEFAULT);
						else {
							if (event.getWorld().rand.nextBoolean()) event.setResult(Result.DEFAULT);
							else event.setResult(Result.DENY);
						}
					}
					else event.setResult(Result.DENY);
				}
			}
		}
	}

	@SubscribeEvent
	public void cropGrow(CropGrowEvent.Post event) {
		if (!event.getWorld().isRemote && event.getState().getBlock() instanceof BlockCrops) {
			IBlockState soil = event.getWorld().getBlockState(event.getPos().down());
			if (soil.getBlock() instanceof MistSoil && SoilHelper.getHumus(soil) > 0) {
				if (((BlockCrops)event.getState().getBlock()).isMaxAge(event.getState())) {
					SoilHelper.setSoil(event.getWorld(), event.getPos().down(), soil, SoilHelper.getHumus(soil) - 1, true, 2);
				}
			}
		}
	}

	@SubscribeEvent
	public void bonemealEvent(BonemealEvent event) {
		if (event.getBlock().getBlock() instanceof BlockCrops) {
			IBlockState soil = event.getWorld().getBlockState(event.getPos().down());
			int humus = SoilHelper.getHumus(soil);
			if (soil.getBlock() instanceof MistSoil) {
				if (soil.getValue(IWettable.WET) && humus > 0) {
					SoilHelper.setSoil(event.getWorld(), event.getPos().down(), soil, humus - 1, true, 2);
				} else event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void animalTameEvent(AnimalTameEvent event) {
		if (event.getEntityLiving() instanceof EntityAnimalMist) {
			//ISkillCapaHandler.getHandler(event.getTamer()).addSkill(Skill.TAMING, ((EntityAnimalMist)event.getEntityLiving()).getTameLevel());
		}
	}

	@SubscribeEvent
	public void harvestCheck(HarvestCheck event) {
		if (event.getTargetBlock().getBlock() instanceof MistChest) event.setCanHarvest(true);
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (event.getModID().equals(Mist.MODID)) {
			ModConfig.onConfigChange();
		}
	}

	///////////////////////////////////////// Fishing /////////////////////////////////////////

	/*@SubscribeEvent
	public void fishing(ItemFishedEvent event) {
		if (event.getHookEntity().world.getBlockState(new BlockPos(event.getHookEntity())).getBlock() == MistBlocks.ACID_BLOCK) {
			event.damageRodBy(10);
			event.setCanceled(true);
		}
	}*/

	///////////////////////////////////////// World /////////////////////////////////////////

	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event) {
		TimeData.get(event.getWorld()).loadTime();
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if (event.getWorld().provider.getDimension() == Mist.getID()) {
			MistWorld.seasonalTest(event.getChunk());
		}
	}
}