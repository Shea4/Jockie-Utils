package com.jockie.bot.core.command.factory.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.jockie.bot.core.argument.IArgument;
import com.jockie.bot.core.argument.factory.IArgumentFactory;
import com.jockie.bot.core.argument.factory.impl.ArgumentFactory;
import com.jockie.bot.core.command.Context;
import com.jockie.bot.core.command.factory.IComponentFactory;
import com.jockie.bot.core.command.manager.IContextManager;
import com.jockie.bot.core.command.manager.impl.ContextManagerFactory;
import com.jockie.bot.core.option.IOption;
import com.jockie.bot.core.option.Option;
import com.jockie.bot.core.option.factory.IOptionFactory;
import com.jockie.bot.core.option.factory.impl.OptionFactory;

public class ComponentFactoryImpl implements IComponentFactory {
	
	private Set<Integer> getContextIndexes(Parameter[] parameters) {
		IContextManager contextManager = ContextManagerFactory.getDefault();
		
		Set<Integer> indexes = new HashSet<>();
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			
			if(contextManager.isEnforcedContext(parameter.getParameterizedType())) {
				indexes.add(i);
			}else if(parameter.isAnnotationPresent(Context.class) || parameter.isAnnotationPresent(Option.class)) {
				indexes.add(i);
			}
		}
		
		return indexes;
	}
	
	public IArgument<?>[] createArguments(Method commandMethod) {
		IArgumentFactory argumentFactory = ArgumentFactory.getDefault();
		
		Parameter[] parameters = commandMethod.getParameters();
		
		Set<Integer> indexes = this.getContextIndexes(parameters);
		IArgument<?>[] arguments = new IArgument<?>[parameters.length - indexes.size()];
		for(int parameterCount = 0, argumentCount = 0; parameterCount < parameters.length; parameterCount++) {
			/* Ignore if it is a context variable */
			if(indexes.contains(parameterCount)) {
				continue;
			}
			
			IArgument<?> argument = argumentFactory.createArgument(parameters[parameterCount]);
			if(arguments.length - 1 != argumentCount) {
				if(argument.isEndless()) {
					throw new IllegalArgumentException("Only the last argument may be endless");
				}
			}
			
			arguments[argumentCount++] = argument;
		}
		
		return arguments;
	}
	
	private Set<String> getOptionTriggers(Parameter[] parameters) {
		Set<String> optionTriggers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for(int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if(!parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			Option option = parameter.getAnnotation(Option.class);
			if(!optionTriggers.add(option.value())) {
				throw new IllegalArgumentException("Option at parameter " + (i + 1) + " has a name, " + option.value() + ", which already exists in another option");
			}
			
			for(String alias : option.aliases()) {
				if(!optionTriggers.add(alias)) {
					throw new IllegalArgumentException("Option at parameter " + (i + 1) + " has an alias, " + alias + ", which already exists in another option");
				}
			}
		}
		
		return optionTriggers;
	}
	
	public IOption<?>[] createOptions(Method commandMethod) {
		IOptionFactory optionFactory = OptionFactory.getDefault();
		
		Parameter[] parameters = commandMethod.getParameters();
		
		Set<String> optionTriggers = this.getOptionTriggers(parameters);
		if(optionTriggers.isEmpty()) {
			return new IOption[0];
		}
		
		IOption<?>[] options = new IOption[optionTriggers.size()];
		for(int parameterCount = 0, optionCount = 0; parameterCount < parameters.length; parameterCount++) {
			Parameter parameter = parameters[parameterCount];
			if(!parameter.isAnnotationPresent(Option.class)) {
				continue;
			}
			
			options[optionCount++] = optionFactory.createOption(parameter);
		}
		
		return options;
	}
}