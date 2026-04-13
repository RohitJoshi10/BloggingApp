package com.BloggingApp.BloggingApp;

import com.BloggingApp.BloggingApp.config.AppConstants;
import com.BloggingApp.BloggingApp.entities.Role;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.repositories.RoleRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.List;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.BloggingApp.BloggingApp.repositories")
@EnableAsync
public class BloggingAppApplication implements CommandLineRunner {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(BloggingAppApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        try {
            // 1. Roles Setup
            Role adminRole;
            if (!this.roleRepository.existsById(AppConstants.ADMIN_USER)) {
                Role role1 = new Role();
                role1.setId(AppConstants.ADMIN_USER);
                role1.setName("ROLE_ADMIN");
                adminRole = this.roleRepository.save(role1);
            } else {
                adminRole = this.roleRepository.findById(AppConstants.ADMIN_USER).get();
            }

            Role normalRole;
            if (!this.roleRepository.existsById(AppConstants.NORMAL_USER)) {
                Role role2 = new Role();
                role2.setId(AppConstants.NORMAL_USER);
                role2.setName("ROLE_NORMAL");
                normalRole = this.roleRepository.save(role2);
            } else {
                normalRole = this.roleRepository.findById(AppConstants.NORMAL_USER).get();
            }

            // 2. Default Admin User Setup
            if (this.userRepository.findByEmail("admin@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@gmail.com");
                admin.setAbout("I am the system administrator");
                admin.setPassword(this.passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                admin.getRoles().add(adminRole);
                this.userRepository.save(admin);
                System.out.println("Default Admin Created Successfully!");
            }

            // 3. AI Bot User Setup (Naya Logic)
            if (this.userRepository.findByEmail("ai-bot@blogapp.com").isEmpty()) {
                User bot = new User();
                bot.setName("AI Assistant");
                bot.setEmail("ai-bot@blogapp.com");
                bot.setAbout("I am an AI-powered bot that helps summarize and engage with blog comments.");

                // Password random ya dummy rakho kyunki bot kabhi login nahi karega
                bot.setPassword(this.passwordEncoder.encode("ai_bot_secure_789"));
                bot.setEnabled(true);

                // Bot ko normal role dena kafi hai
                bot.getRoles().add(normalRole);

                this.userRepository.save(bot);
                System.out.println("AI Bot User Created Successfully!");
            }

            System.out.println("Initialization Completed!");

        } catch (Exception e) {
            System.out.println("Error during data initialization: " + e.getMessage());
        }
    }

}
