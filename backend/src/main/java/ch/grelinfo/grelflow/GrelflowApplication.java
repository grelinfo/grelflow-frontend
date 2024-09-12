package ch.grelinfo.grelflow;

import ch.grelinfo.grelflow.jiraclient.JiraClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JiraClientConfig.class)
public class GrelflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrelflowApplication.class, args);
	}

}
