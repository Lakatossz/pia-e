package cz.zcu.kiv.mjakubas.piae.sem.webapplication.i18n;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Configuration class for internationalization base on baeldung tutorial.
 *
 * @see <a href="https://www.baeldung.com/spring-boot-internationalization">Baeldung</a>
 */
@Configuration
@AllArgsConstructor
public class I18nConfig implements WebMvcConfigurer {
    private final I18nLanguageService i18nLanguageService;

    /**
     * Determines the current locale based on session, cookies, Accept-Language header value or fixed value.
     *
     * @return current locale determiner
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        Locale czechLocale = new Locale(i18nLanguageService.getDefaultLanguageLocale());
        slr.setDefaultLocale(czechLocale);
        return slr;
    }

    /**
     * Allows changing locale base on request parameter.
     *
     * @return locale changer
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
