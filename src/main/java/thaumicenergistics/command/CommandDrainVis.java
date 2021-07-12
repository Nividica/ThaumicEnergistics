package thaumicenergistics.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import thaumcraft.api.aura.AuraHelper;

/**
 * @author Alex811
 */
public class CommandDrainVis extends CommandBase {
    @Override
    public String getName() {
        return "drainvis";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "drainvis <vis>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length < 1)
            return;
        try {
            float vis = Float.parseFloat(args[0]);
            AuraHelper.drainVis(sender.getEntityWorld(), sender.getPosition(), vis, false);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Vis drained: " + vis));
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error, invalid float"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
