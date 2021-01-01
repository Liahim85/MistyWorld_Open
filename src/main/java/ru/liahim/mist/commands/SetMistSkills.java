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
import net.minecraft.util.math.MathHelper;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;

public class SetMistSkills extends CommandBase {

	@Override
	public String getName() {
		return "setmistskills";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		 return "commands.setmistskills.usage";
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) throw new WrongUsageException("commands.setmistskills.usage", new Object[0]);
		else {
			EntityPlayer player = getEntity(server, sender, args[0], EntityPlayer.class);
			ISkillCapaHandler capa = ISkillCapaHandler.getHandler(player);
			if (capa == null) {
				throw new CommandException("commands.setmistskills.failure.badcapability", new Object[] { player.getName() });
			}
			Skill skill = Skill.fromName(args[1]);
			if (skill == null) throw new NumberInvalidException("commands.setmistskills.notfound", new Object[] {args[1]});
			else if (args.length == 3) {
				int count = skill.getLevelsCount();
				double i = parseDouble(args[2], 1, count);
				if (i < count) {
					int level = MathHelper.floor(i);
					count = skill.getSizeForLevel(level);
					count += skill.getLevelSize(count + 1) * (i - level);
				}
				capa.setSkill(skill, count);
				notifyCommandListener(sender, this, "commands.setmistskills.success", new Object[] { player.getName(), skill.getName(), i });
			}
		}
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1) {
			return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		} else if (args.length == 2) {
			return getListOfStringsMatchingLastWord(args, ISkillCapaHandler.Skill.skills.keySet());
		} else return Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return index == 0;
	}
}