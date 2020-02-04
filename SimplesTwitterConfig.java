package com.twittertimeline;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

/**
 * Spring Social Configuração para uma API REST padrão do Twitter
 */
@Configuration
public class SimpleTwitterConfig {

	private static Twitter twitter;

	public SimpleTwitterConfig() {

		if (twitter == null) {
			twitter = new TwitterTemplate();
		}
	}

	/**
	 * Um proxy para um objeto com escopo de solicitação que representa a API mais simples do Twitter  - aquela que
* não precisa de autorização
	 */
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public Twitter twitter() {
		return twitter;
	}

}