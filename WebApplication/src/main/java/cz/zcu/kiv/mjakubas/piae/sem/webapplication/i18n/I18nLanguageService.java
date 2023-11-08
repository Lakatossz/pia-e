package cz.zcu.kiv.mjakubas.piae.sem.webapplication.i18n;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * For the possibility of adding new language (which will never happen).
 */
@Service
@Getter
public class I18nLanguageService {

    @Value("${mine.locale.default-language}")
    private String defaultLanguageLocale;

    @Value("#{'${mine.locale.supported-languages}'.split(',')}")
    private List<String> supportedLanguagesLocale;
}
