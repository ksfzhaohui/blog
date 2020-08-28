package com.zh;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @SpringBootTest两个参数
 * 
 * webEnvironment：指定Web应用环境，它可以是以下值
 * MOCK：提供一个模拟的 Servlet 环境，内置的 Servlet 容器没有启动，配合可以与@AutoConfigureMockMvc 结合使用，用于基于 MockMvc 的应用程序测试。
 * RANDOM_PORT：加载一个 EmbeddedWebApplicationContext 并提供一个真正嵌入式的 Servlet 环境，随机端口。
 * DEFINED_PORT：加载一个 EmbeddedWebApplicationContext 并提供一个真正嵌入式的 Servlet 环境，默认端口 8080 或由配置文件指定。
 * NONE：使用 SpringApplication 加载 ApplicationContext，但不提供任何 servlet 环境。
 * 
 * classes：指定应用启动类，通常情况下无需设置，因为 SpringBoot 会自动搜索，直到找到 @SpringBootApplication 或 @SpringBootConfiguration 注解。
 * @author hui.zhao
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest2 {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getHello() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
    }
}