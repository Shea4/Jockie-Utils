package test;

import java.io.File;
import java.io.FileInputStream;

import com.jockie.bot.core.command.impl.CommandListener;
import com.jockie.bot.core.command.impl.CommandStore;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;

public class Tests {
	
	public static void main(String[] args) throws Exception {
		String token;
		try(FileInputStream stream = new FileInputStream(new File("./example.token"))) {
			token = new String(stream.readAllBytes());
		}
		
		CommandListener listener = new CommandListener()
			.addCommandStore(CommandStore.of("test.command"))
			.addDeveloper(190551803669118976L)
			.setDefaultPrefixes("�");
		
		new JDABuilder(AccountType.BOT).setToken(token)
			.addEventListener(listener)
			.build()
			.awaitReady();
	}
}