package com.zipup.server.global.security.config;

import com.zipup.server.global.config.CorsConfig;
import com.zipup.server.global.security.filter.CustomAuthenticationEntryPoint;
import com.zipup.server.global.security.handler.CustomAccessDeniedHandler;
import com.zipup.server.global.security.handler.CustomAuthenticationFailureHandler;
import com.zipup.server.global.security.handler.CustomAuthenticationSuccessHandler;
import com.zipup.server.global.security.oauth.CustomOAuth2UserService;
import com.zipup.server.global.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.zipup.server.global.security.util.JwtProvider;
import com.zipup.server.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final CorsConfig corsConfig;
  private final JwtProvider jwtProvider;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final UserService userService;
  private static final String[] AUTH_WHITELIST = {
          "/error",
          "/*/oauth2/code/*",
          "/favicon.ico",
          "/configuration/security",
          "/swagger-ui/**",
          "/api-docs/**",
          "/webjars/**",
          "/h2-console/**",
          "/api/v1/user/sign-**",
          "/api/v1/auth/refresh",
          "/v3/api-docs/**"
  };

  @Bean
  public HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository() {
    return new HttpCookieOAuth2AuthorizationRequestRepository();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(CsrfConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .sessionManagement(sessionManagement ->
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(FormLoginConfigurer::disable)
            .httpBasic(HttpBasicConfigurer::disable)
            .headers(headerConfig ->
                    headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            .authorizeRequests(authorizeRequests ->
                    authorizeRequests
                            .antMatchers(HttpMethod.GET, "/api/v1/payment/confirm").authenticated()
                            .antMatchers(HttpMethod.GET).permitAll()
                            .antMatchers(AUTH_WHITELIST).permitAll()
                            .anyRequest().authenticated())

            .oauth2Login(oauth ->
                    oauth.authorizationEndpoint(endpoint ->
                            endpoint.authorizationRequestRepository(authorizationRequestRepository()))
                          .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                          .successHandler(oAuth2AuthenticationSuccessHandler())
                          .failureHandler(oAuth2AuthenticationFailureHandler()))

            .exceptionHandling(exceptionHandlingConfigurer ->
                    exceptionHandlingConfigurer
                            .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                            .accessDeniedHandler(new CustomAccessDeniedHandler()));

    http.apply(new JwtSecurityConfig(jwtProvider));

    return http.build();
  }

  /*
   * 암호화에 필요한 PasswordEncoder Bean 등록
   * */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /*
   * Oauth 인증 성공 핸들러
   * */
  @Bean
  public CustomAuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
    return new CustomAuthenticationSuccessHandler(userService, authorizationRequestRepository());
  }

  /*
   * Oauth 인증 실패 핸들러
   * */
  @Bean
  public CustomAuthenticationFailureHandler oAuth2AuthenticationFailureHandler() {
    return new CustomAuthenticationFailureHandler(authorizationRequestRepository());
  }

}
