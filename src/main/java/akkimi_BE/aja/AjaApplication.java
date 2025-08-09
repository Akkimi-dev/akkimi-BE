package akkimi_BE.aja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"akkimi_BE.aja", "global"})
public class AjaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AjaApplication.class, args);
	}

}
