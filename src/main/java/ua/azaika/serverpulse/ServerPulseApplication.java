package ua.azaika.serverpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ServerPulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerPulseApplication.class, args);
	}

}
