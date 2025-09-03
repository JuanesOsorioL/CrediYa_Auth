//package co.com.crediya.api.config;
//
//
//import co.com.crediya.api.filter.AuthFilter;
//import co.com.crediya.api.segurity.AuthenticationService;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
//
//@Configuration
//public class WebConfig {
//
//    @Bean
//    public WebHttpHandlerBuilder webHttpHandlerBuilder(AuthenticationService authenticationService) {
//        return WebHttpHandlerBuilder.applicationContext(applicationContext)
//                .filters(filters -> filters.add(new AuthFilter(authenticationService)));
//    }
//}
