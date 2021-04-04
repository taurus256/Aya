package org.taurus.aya.server.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.User;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JiraService {

	private String JIRA_URL = "https://job-jira.otr.ru";
	private String JIRA_REST_ISSUE = "/rest/api/2/issue/";

	@Autowired
	UserRepository userRepository;

	private class TransitionEntity{
		public TransitionEntity(){
			transition = new HashMap<>();
		}
		private Map<String,String> transition;
	}

	private WebClient client = WebClient.create(JIRA_URL);

	public void performTransition(String jiraTaskId, Long userId, Integer state){
		User user = userRepository.findById(userId).orElseThrow(IllegalArgumentException::new);
		doRest(jiraTaskId, convertState(state), user.getJiraLogin(), user.getJiraPass());
	}

	/**Конвертация кода задачи Aya в код задачи JIRA (JIRA хочет String)*/
	private String convertState(Integer state){
		return "1";
	}

	private void doRest(String task, String stateCode, String login, String password){
		TransitionEntity request = new TransitionEntity();
		request.transition.put("id", stateCode);
		WebClient.ResponseSpec responseSpec = client.post()
				.uri(JIRA_REST_ISSUE)
				.bodyValue(request)
				.headers(headers -> headers.setBasicAuth(login, password))
				.retrieve();
		System.out.println("responseSpec = " + responseSpec);
	}

}
