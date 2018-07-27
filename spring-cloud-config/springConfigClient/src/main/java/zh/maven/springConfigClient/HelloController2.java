package zh.maven.springConfigClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController2 {

	@Value("${foo2}")
	String foo;

	@RequestMapping(value = "/hello2")
	public String hello() {
		return foo;
	}
}
