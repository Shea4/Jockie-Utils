package com.jockie.bot.core.argument.impl;

import java.lang.reflect.Array;
import java.util.Set;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.command.ICommand.ArgumentTrimType;
import com.jockie.bot.core.command.parser.ParseContext;
import com.jockie.bot.core.command.parser.impl.CommandParserImpl;
import com.jockie.bot.core.parser.IParser;
import com.jockie.bot.core.parser.ParsedResult;
import com.jockie.bot.core.utility.StringUtility;

import net.dv8tion.jda.internal.utils.tuple.Pair;

public class EndlessArgumentParser<Type> implements IParser<Type[], IArgument<Type[]>> {
	
	public static final EndlessArgumentParser<Object> INSTANCE = new EndlessArgumentParser<>();
	
	@SuppressWarnings("unchecked")
	public static <T> EndlessArgumentParser<T> getInstance() {
		return (EndlessArgumentParser<T>) INSTANCE;
	}
	
	private EndlessArgumentParser() {}
	
	@SuppressWarnings("unchecked")
	/* TODO: Probably need to look over and re-make this */
	public ParsedResult<Type[]> parse(ParseContext context, IArgument<Type[]> argument, String value) {
		if(!(argument instanceof EndlessArgumentImpl)) {
			throw new UnsupportedOperationException();
		}
		
		EndlessArgumentImpl<Type> self = (EndlessArgumentImpl<Type>) argument;
		
		int argumentCount = 0;
		
		int maxArguments = self.getMaxArguments() > 0 ? self.getMaxArguments() : (int) value.codePoints().filter(c2 -> c2 == ' ').count() + 1;
		Type[] parsedArguments = (Type[]) Array.newInstance(self.getComponentType(), maxArguments);
		
		for(int i = 0; i < parsedArguments.length; i++) {
			if(value.trim().length() == 0) {
				break;
			}
			
			if(i != 0 && value.length() > 0) {
				if(value.startsWith(" ")) {
					ArgumentTrimType trimType = context.getCommand().getArgumentTrimType();
					
					if(!trimType.equals(ArgumentTrimType.NONE)) {
						value = StringUtility.stripLeading(value);
					}else{
						value = value.substring(1);
					}
				}else{
					/* 
					 * It gets here if an argument is parsed with quotes and there is a 
					 * value directly after the quotes without any spacing, like !add "15"5
					 */
					
					/* The argument for some reason does not start with a space */
					return new ParsedResult<>(false, null);
				}
			}
			
			String content = null;
			ParsedResult<Type> parsedArgument;
			if(self.getArgument().getParser().isHandleAll()) {
				parsedArgument = self.getArgument().parse(context, content = value);
				
				if(parsedArgument.getContentLeft() != null) {
					value = parsedArgument.getContentLeft();
				}else{
					value = "";
				}
			}else{
				if(value.length() > 0) {
					if(self.getArgument().acceptQuote()) {
						Set<Pair<Character, Character>> quotesCharacters;
						if(context.getCommandParser() instanceof CommandParserImpl) {
							quotesCharacters = ((CommandParserImpl) context.getCommandParser()).getQuoteCharacters();
						}else{
							/* TODO: Unsure of what to do if it's not a CommandParserImpl */
							quotesCharacters = Set.of(Pair.of('"', '"'));
						}
						
						for(Pair<Character, Character> quotes : quotesCharacters) {
							content = StringUtility.parseWrapped(value, quotes.getLeft(), quotes.getRight());
							if(content != null) {
								value = value.substring(content.length());
								content = StringUtility.unwrap(content, quotes.getLeft(), quotes.getRight());
								
								if(context.getCommand().getArgumentTrimType().equals(ArgumentTrimType.STRICT)) {
									content = StringUtility.strip(content);
								}
								
								break;
							}
						}
					}
					
					if(content == null) {
						content = value.substring(0, (value.contains(" ")) ? value.indexOf(" ") : value.length());
						value = value.substring(content.length());
					}
				}else{
					content = "";
				}
				
				if(content.length() == 0 && !self.getArgument().acceptEmpty()) {
					/* Content may not be empty */
					return new ParsedResult<>(false, null);
				}
				
				parsedArgument = self.getArgument().parse(context, content);
			}
			
			if(parsedArgument.isValid()) {
				parsedArguments[argumentCount++] = (Type) parsedArgument.getObject();
			}else{
				/* "argument at index " + (i + 1) + " is not valid" */
				return new ParsedResult<>(false, null);
			}
		}
		
		if(value.length() > 0) {
			/* Content overflow, when does this happen? */
			
			return new ParsedResult<>(false, null);
		}
		
		if(argumentCount < self.getMinArguments() || ((self.getMaxArguments() > 0) ? argumentCount > self.getMaxArguments() : false)) {
			return new ParsedResult<>(false, null);
		}
		
		Type[] objects = (Type[]) Array.newInstance(self.getComponentType(), argumentCount);
		for(int i2 = 0; i2 < objects.length; i2++) {
			objects[i2] = (Type) parsedArguments[i2];
		}
		
		return new ParsedResult<>(true, objects);
	}
}