package example.command.info.role;

import com.jockie.bot.core.argument.Argument;
import com.jockie.bot.core.command.impl.CommandEvent;
import com.jockie.bot.core.command.impl.CommandImpl;

import example.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

public class CommandRoleInfo extends CommandImpl {
	
	public CommandRoleInfo() {
		super("role info");
		
		super.setAliases("roleinfo", "rolei", "ri");
		super.setDescription("Get information about a role");
	}
	
	public MessageEmbed onCommand(CommandEvent event, @Argument("Role") Role role) {
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.setColor(role.getColor());
		builder.addField("Name", role.getName(), true);
		builder.addField("Id", role.getName(), true);
		builder.addBlankField(true);
		builder.addField("Mention", role.getAsMention(), true);
		builder.addField("Raw Permissions", String.valueOf(role.getPermissionsRaw()), true);
		builder.addBlankField(true);
		builder.addField("Created", Main.FORMATTER.format(role.getTimeCreated()), true);
		builder.addBlankField(true);
		builder.addBlankField(true);

		return builder.build();
	}
}