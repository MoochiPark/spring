package chapter03.config;

import chapter03.spring.MemberDao;
import chapter03.spring.MemberPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ AppConf1.class, AppConf2.class } )
public class AppConfImport {
}
