package thaumicenergistics.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import thaumcraft.api.aura.AuraHelper;

/**
 * TODO: Remove after alpha
 *
 * @author BrockWS
 */
public class CommandAddVis extends CommandBase {
    @Override
    public String getName() {
        return "addvis";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "addvis <vis>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 1)
            return;
        try {
            AuraHelper.addVis(sender.getEntityWorld(), sender.getPosition(), Float.parseFloat(args[0]));
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error, invalid float"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
