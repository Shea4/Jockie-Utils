package com.jockie.bot.core.parser.impl.discord;

import java.util.Collections;
import java.util.List;

import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.ArgumentUtility;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;

/* Even though Category technically does implement Channel I do not want it to be a part of the Channel argument, objections? */
public class GuildChannelParser<Component> implements IParser<GuildChannel, Component> {

	@Override
	public ParsedResult<GuildChannel> parse(ParseContext context, Component component, String content) {
		Guild guild = context.getMessage().getGuild();
		
		List<GuildChannel> channels = Collections.unmodifiableList(ArgumentUtility.getTextChannelsByIdOrName(guild, content, true));
		if(channels.size() == 0) {
			channels = Collections.unmodifiableList(ArgumentUtility.getVoiceChannelsByIdOrName(guild, content, true));
		}
		
		if(channels.size() == 1) {
			return new ParsedResult<>(true, channels.get(0));
		}else{
			return new ParsedResult<>(false, null);
		}
	}	
}