package org.taurus.aya.server.services;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.User;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JiraService {

	@Value("${aya.jira.url}")
	private String JIRA_URL;
	@Value("${aya.jira.start_code}")
	private String JIRA_START_CODE;
	@Value("${aya.jira.pause_code}")
	private String JIRA_PAUSE_CODE;
	@Value("${aya.jira.stop_code}")
	private String JIRA_STOP_CODE;
	private String JIRA_REST_ISSUE = "/rest/api/2/issue/";

	@Autowired
	UserRepository userRepository;

	private class TransitionEntity{
		public TransitionEntity(){
			transition = new HashMap<>();
		}

		public Map<String, String> getTransition() {
			return transition;
		}

		public void setTransition(Map<String, String> transition) {
			this.transition = transition;
		}

		private Map<String,String> transition;
	}

	private WebClient client = WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(
					HttpClient.create().wiretap(true)
			))
			.baseUrl(JIRA_URL)
			.filters(exchangeFilterFunctions -> {
				exchangeFilterFunctions.add(logRequest());
				exchangeFilterFunctions.add(logResponse());
			})
			.build();
		//WebClient.create(JIRA_URL);

	public JiraService(){
		client = WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(
						HttpClient.create().wiretap(true)
				))
				.filters(exchangeFilterFunctions -> {
					exchangeFilterFunctions.add(logRequest());
					exchangeFilterFunctions.add(logResponse());
				})
				.baseUrl(JIRA_URL)
				.build();
	}

	public void performTransition(String jiraTaskId, Long userId, Integer state){
		User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
		doRest(jiraTaskId, convertState(state), user.getJiraLogin(), user.getJiraPass());
	}

	/**Конвертация кода задачи Aya в код задачи JIRA (JIRA хочет String)*/
	private String convertState(Integer state){
		switch (state){
			case 1: return JIRA_START_CODE;
			case 2:
			case 3: return JIRA_PAUSE_CODE;
			default: return JIRA_PAUSE_CODE;
		}
	}

	private void doRest(String task, String stateCode, String login, String password){
		System.out.println("JIRA_URL = " + JIRA_URL);
		TransitionEntity request = new TransitionEntity();
		request.transition.put("id", stateCode);
		Object obj = client.post()
				.uri(JIRA_URL + JIRA_REST_ISSUE + task + "/transitions")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Mono.just(request),TransitionEntity.class)
				.headers(headers -> headers.setBasicAuth(login, password))
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError,
				         error -> Mono.error(new RuntimeException("API not found"))).bodyToMono(Void.class).block();
		System.out.println("responseSpec = " + obj);
	}

	ExchangeFilterFunction logRequest() {
		return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Request: \n");
				//append clientRequest method and url
				clientRequest
						.headers()
						.forEach((name, values) -> values.forEach(value -> sb.append("header: ").append(value)));
				log.debug(sb.toString());
			}
			return Mono.just(clientRequest);
		});
	}

	ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder("Request: \n");
				//append clientRequest method and url
				sb.append("code: ").append(clientResponse.rawStatusCode());
				log.debug(sb.toString());
			}
			return Mono.just(clientResponse);
		});
	}

}
