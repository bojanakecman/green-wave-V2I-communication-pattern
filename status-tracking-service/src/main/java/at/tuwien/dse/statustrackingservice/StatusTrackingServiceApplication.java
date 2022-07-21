package at.tuwien.dse.statustrackingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class StatusTrackingServiceApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(StatusTrackingServiceApplication.class, args);
	}

}
