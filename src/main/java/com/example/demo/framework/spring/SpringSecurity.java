package com.example.demo.framework.spring;

/**
 * Spring Security核心概念
 *
 * 【核心功能】
 * 1. 认证（Authentication）：验证用户身份
 * 2. 授权（Authorization）：验证用户权限
 * 3. 防护（Protection）：CSRF、CORS、Session固定等
 *
 * 【核心组件】
 * - SecurityContext：安全上下文
 * - Authentication：认证信息
 * - UserDetails：用户详情
 * - UserDetailsService：用户详情服务
 * - AuthenticationProvider：认证提供者
 * - FilterChain：过滤器链
 */
public class SpringSecurity {

    // ==================== 认证流程 ====================

    /**
     * Spring Security认证流程：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                      认证流程                                │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 1. 用户提交用户名密码                                        │
     * │                          ↓                                  │
     * │ 2. UsernamePasswordAuthenticationFilter                     │
     * │    创建 UsernamePasswordAuthenticationToken                 │
     * │                          ↓                                  │
     * │ 3. AuthenticationManager.authenticate()                    │
     * │                          ↓                                  │
     * │ 4. ProviderManager 遍历 AuthenticationProvider              │
     * │                          ↓                                  │
     * │ 5. DaoAuthenticationProvider                                │
     * │    调用 UserDetailsService.loadUserByUsername()            │
     * │                          ↓                                  │
     * │ 6. 验证密码（PasswordEncoder）                               │
     * │                          ↓                                  │
     * │ 7. 创建认证成功的 Authentication                            │
     * │                          ↓                                  │
     * │ 8. 存入 SecurityContext                                     │
     * └─────────────────────────────────────────────────────────────┘
     */

    // ==================== 配置示例 ====================

    /**
     * 基础配置
     */
    /*
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                    .anyRequest().authenticated()
                )

                // 表单登录
                .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/doLogin")
                    .defaultSuccessUrl("/home")
                    .failureUrl("/login?error")
                    .permitAll()
                )

                // 注销
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
                )

                // CSRF
                .csrf(csrf -> csrf.disable())

                // Session管理
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // JWT过滤器
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(
                AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }
    }
    */

    // ==================== UserDetailsService ====================

    /**
     * 自定义用户详情服务
     */
    /*
    @Service
    public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));

            // 获取权限
            List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                authorities
            );
        }
    }
    */

    // ==================== JWT认证 ====================

    /**
     * JWT工具类
     */
    /*
    @Component
    public class JwtUtils {

        @Value("${jwt.secret}")
        private String secret;

        @Value("${jwt.expiration}")
        private Long expiration;

        private SecretKey getSigningKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        }

        public String generateToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

            return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
        }

        public String extractUsername(String token) {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        }

        public boolean validateToken(String token, UserDetails userDetails) {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        }

        private boolean isTokenExpired(String token) {
            Date expiration = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
            return expiration.before(new Date());
        }
    }
    */

    /**
     * JWT过滤器
     */
    /*
    @Component
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        @Autowired
        private JwtUtils jwtUtils;

        @Autowired
        private UserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String token = extractToken(request);

            if (token != null && jwtUtils.validateToken(token)) {
                String username = jwtUtils.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }

        private String extractToken(HttpServletRequest request) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
            return null;
        }
    }
    */

    // ==================== 方法级安全 ====================

    /**
     * 启用方法级安全
     */
    /*
    @Configuration
    @EnableMethodSecurity(
        prePostEnabled = true,   // @PreAuthorize, @PostAuthorize
        securedEnabled = true,   // @Secured
        jsr250Enabled = true     // @RolesAllowed
    )
    public class MethodSecurityConfig {
    }
    */

    /**
     * 使用示例
     */
    /*
    @Service
    public class OrderService {

        // 只有ADMIN角色可以访问
        @PreAuthorize("hasRole('ADMIN')")
        public void deleteOrder(Long orderId) {
            // 删除订单
        }

        // 只有订单所有者可以访问
        @PreAuthorize("@orderService.canAccessOrder(#orderId, authentication.name)")
        public Order getOrder(Long orderId) {
            return orderRepository.findById(orderId);
        }

        // 方法执行后检查
        @PostAuthorize("returnObject.userId == authentication.name")
        public Order getOrderWithCheck(Long orderId) {
            return orderRepository.findById(orderId);
        }

        // 过滤返回结果
        @PostFilter("filterObject.userId == authentication.name")
        public List<Order> getOrders() {
            return orderRepository.findAll();
        }

        // 多个条件
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        public void approveOrder(Long orderId) {
            // 审批订单
        }
    }
    */

    // ==================== OAuth2.0 ====================

    /**
     * OAuth2.0授权流程：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │   Client      │     Auth Server    │     Resource Server   │
     * │   (应用)       │     (授权服务器)    │     (资源服务器)       │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 1. 用户点击登录                                            │
     * │ ───────────────→ 重定向到授权服务器                         │
     * │                                                             │
     * │ 2. 用户授权                                                │
     * │                 ←───────────── 授权页面                     │
     * │                 ──────────────→ 用户同意授权                │
     * │                                                             │
     * │ 3. 获取授权码                                              │
     * │ ←─────────────── 重定向回来（带code）                       │
     * │                                                             │
     * │ 4. 用授权码换Token                                         │
     * │                 ──────────────→ code + client_secret        │
     * │                 ←────────────── access_token               │
     * │                                                             │
     * │ 5. 访问资源                                                │
     * │ ──────────────────────────────────────────────→ access_token│
     * │ ←────────────────────────────────────────────── 用户数据    │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 授权模式：
     * 1. 授权码模式（推荐）：最安全，适合Web应用
     * 2. 密码模式：直接传用户名密码，不推荐
     * 3. 客户端凭证模式：适合服务间调用
     * 4. 隐式模式：已废弃
     */

    /**
     * Spring Security OAuth2配置示例
     */
    /*
    @Configuration
    public class OAuth2ResourceServerConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

            return http.build();
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
            return converter;
        }
    }
    */

    // ==================== CSRF防护 ====================

    /**
     * CSRF（Cross-Site Request Forgery）：跨站请求伪造
     *
     * 攻击原理：
     * 1. 用户登录了A网站
     * 2. 访问恶意网站B
     * 3. B网站发起对A网站的请求（携带用户的Cookie）
     * 4. A网站误以为是用户操作
     *
     * 防护方案：
     * 1. CSRF Token：每个请求携带服务端生成的Token
     * 2. SameSite Cookie：限制Cookie跨站发送
     * 3. Referer检查：验证请求来源
     *
     * Spring Security默认开启CSRF防护
     * REST API通常禁用（使用JWT替代）
     */

    // ==================== 常见问题 ====================

    /**
     * Q1: Session vs JWT怎么选？
     * A: 传统Web用Session，前后端分离/移动端用JWT
     *
     * Q2: 如何实现记住我功能？
     * A: 使用TokenBasedRememberMeServices，生成持久化Token
     *
     * Q3: 如何处理密码加密？
     * A: 使用BCryptPasswordEncoder，自动加盐
     *
     * Q4: 如何实现权限动态加载？
     * A: 自定义FilterInvocationSecurityMetadataSource
     */
}
