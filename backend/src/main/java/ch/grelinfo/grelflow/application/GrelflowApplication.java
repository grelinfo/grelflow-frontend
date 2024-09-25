package ch.grelinfo.grelflow.application;

import ch.grelinfo.grelflow.adapter.jira.JiraRestClientConfig;
import ch.grelinfo.grelflow.safetimetracking.SafeTimeTrackingConfig;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties({JiraRestClientConfig.class, SafeTimeTrackingConfig.class})
@ComponentScan(basePackages = "ch.grelinfo.grelflow.safetimetracking")
@ComponentScan(basePackages = "ch.grelinfo.grelflow.adapter.jira")
public class GrelflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrelflowApplication.class, args);
	}

}
