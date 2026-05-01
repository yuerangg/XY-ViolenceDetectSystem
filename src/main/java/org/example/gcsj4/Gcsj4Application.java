package org.example.gcsj4;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.gcsj4.mapper")
public class Gcsj4Application {

	public static void main(String[] args) {
		SpringApplication.run(Gcsj4Application.class, args);
	}

}
