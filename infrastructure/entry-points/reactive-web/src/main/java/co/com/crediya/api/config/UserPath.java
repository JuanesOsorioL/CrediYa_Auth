package co.com.crediya.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "routes.user")
public class UserPath {
    private String base;
    private String document;
    private String login;
    private String validateToken;
    private String allUsers;
    private String someUsers;
}
