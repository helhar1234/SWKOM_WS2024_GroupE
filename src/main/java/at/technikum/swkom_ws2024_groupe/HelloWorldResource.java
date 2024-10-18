package at.technikum.swkom_ws2024_groupe;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldResource {
    @GetMapping("/helloworld")
    public String helloWorld() {
        return "Hello World";
    }

    @GetMapping("/hello/{name}")
    public String helloWorld2(@PathVariable String name) {
        return "Hello World" + name + "!";
    }
}
