package guru.sfg.brewery.config;

import guru.sfg.brewery.security.google.Google2faFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final PersistentTokenRepository persistentTokenRepository;
    private final Google2faFilter google2faFilter;

    // needed for use with Spring Data JPA SPeL
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
        return new SecurityEvaluationContextExtension();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(google2faFilter, SessionManagementFilter.class);

        http.authorizeRequests(authorise -> {
            authorise
                    .antMatchers("/h2-console/**").permitAll() //do not use h2 in production!
                    .antMatchers("/", "/webjars/**", "/login", "/resources/**").permitAll();
        })
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin(loginConfigurer -> {
                    loginConfigurer
                            .loginProcessingUrl("/login")
                            .loginPage("/").permitAll()
                            .successForwardUrl("/")
                            .defaultSuccessUrl("/")
                            .failureUrl("/?error");
                })
                .logout(logoutConfigurer -> {
                    logoutConfigurer
                            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                            .logoutSuccessUrl("/?logout")
                            .permitAll();

                })
                .httpBasic()
                .and().csrf().ignoringAntMatchers("/h2-console/**", "/api/**")
                .and().rememberMe()
                            .tokenRepository(persistentTokenRepository)
                            .userDetailsService(userDetailsService);
//                    .rememberMe()
//                    .key("sfg-key")
//                    .userDetailsService(userDetailsService);

        // h2 console config
        http.headers().frameOptions().sameOrigin();
    }



//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(this.jpaUserDetailService).passwordEncoder(passwordEncoder());

//        auth.inMemoryAuthentication()
//                .withUser("spring")
//                .password("{bcrypt}$2a$10$RYrKaPwVv5GfSQWyUCLgMuIrAQcPbgAXrt0JLZbeIK2bShuszvOVi")
//                .roles("ADMIN")
//                    .and()
//                .withUser("user")
//                .password("{sha256}8406747ca980a511572f32d4656b9e31edcb2caf7ce5c30752ad09eff7ad981b18b499b68753a737")
//                .roles("USER");
//
//        auth.inMemoryAuthentication().withUser("scott").password("{bcrypt10}$2a$10$Jsu.JkVQ7nU9iZbIjsbI7uKD1Dj82eSmX/TT.E4HH13ZI57xk9LnS").roles("CUSTOMER");
//    }

    //    @Override
//    @Bean
//    protected UserDetailsService userDetailsService() {
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("spring")
//                .password("guru")
//                .roles("ADMIN")
//                .build();
//
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(admin, user);
//    }
}
