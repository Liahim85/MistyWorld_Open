package ru.liahim.mist.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler;

public class SetMistEffects extends CommandBase {

	@Override
	public String getName() {
		return "setmisteffects";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands.setmisteffects.usage";
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) throw new WrongUsageException("commands.setmisteffects.usage", new Object[0]);
		else {
			EntityPlayer player = getEntity(server, sender, args[0], EntityPlayer.class);
			IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
			if (capa == null) {
				throw new CommandException("commands.setmisteffects.failure.badcapability", new Object[] { player.getName() });
			}
			if ("clear".equals(args[1])) {
				if (capa.getPollution() == 0 && capa.getToxic() == 0) {
					throw new CommandException("commands.setmisteffects.failure.notactive.all", new Object[] { player.getName() });
				} else {
					capa.setPollution(0);
					capa.setToxic(0);
					notifyCommandListener(sender, this, "commands.setmisteffects.success.removed.all", new Object[] { player.getName() });
				}
			} else if (args.length == 2) throw new NumberInvalidException("commands.setmisteffects.notfound", new Object[] {args[1]});
			else if (args.length == 3) {
				if ("pollution".equals(args[1])) {
					if ("clear".equals(args[2])) {
						if (capa.getPollution() == 0) {
							throw new CommandException("commands.setmisteffects.failure.notactive.pollution", new Object[] { player.getName() });
						} else {
							capa.setPollution(0);
							notifyCommandListener(sender, this, "commands.setmisteffects.success.removed.pollution", new Object[] { player.getName() });
						}
					} else {
						double i = parseDouble(args[2], 0, 100);
						if (i > 0) {
							capa.setPollution((int) (i * 100));
							notifyCommandListener(sender, this, "commands.setmisteffects.success.pollution", new Object[] { player.getName(), i });
						} else if (capa.getPollution() > 0) {
							capa.setPollution(0);
							notifyCommandListener(sender, this, "commands.setmisteffects.success.removed.pollution", new Object[] { player.getName() });
						} else throw new CommandException("commands.setmisteffects.failure.notactive.pollution", new Object[] { player.getName() });
					}
				} else if ("toxic".equals(args[1])) {
					if ("clear".equals(args[2])) {
						if (capa.getToxic() == 0) {
							throw new CommandException("commands.setmisteffects.failure.notactive.toxic", new Object[] { player.getName() });
						} else {
							capa.setToxic(0);
							notifyCommandListener(sender, this, "commands.setmisteffects.success.removed.toxic", new Object[] { player.getName() });
						}
					} else {
						double i = parseDouble(args[2], 0, 100);
						if (i > 0) {
							capa.setToxic((int) (i * 100));
							notifyCommandListener(sender, this, "commands.setmisteffects.success.toxic", new Object[] { player.getName(), i });
						} else if (capa.getToxic() > 0) {
							capa.setToxic(0);
							notifyCommandListener(sender, this, "commands.setmisteffects.success.removed.toxic", new Object[] { player.getName() });
						} else throw new CommandException("commands.setmisteffects.failure.notactive.toxic", new Object[] { player.getName() });
					}
				} 
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, MistCapaHandler.HurtType.commands);
		} else if (args.length == 3) {
			return getListOfStringsMatchingLastWord(args, "clear");
		} else return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}