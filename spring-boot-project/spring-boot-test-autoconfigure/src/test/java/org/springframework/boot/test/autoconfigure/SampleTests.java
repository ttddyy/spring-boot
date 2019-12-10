package org.springframework.boot.test.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Tadaya Tsuyukubo
 */
public class SampleTests {

	@Test
	void contextRunnerWithConditionOnAutoConfiguration() {
		new ApplicationContextRunner()
				.withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO))
				.withConfiguration(AutoConfigurations.of(MyAutoConfiguration.class))
				.withUserConfiguration(MyUserConfig.class)
				.run(context -> {
					assertThat(context)
							.hasNotFailed()
							.hasBean("foo")
					;
				});
	}

	@Configuration
	@EnableX  // this enables MyAutoConfiguration
	public static class MyUserConfig {
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Import(MyImportSelector.class)
	public @interface EnableX {
	}

	public static class MyImportSelector implements ImportSelector {
		public static boolean enabled;  // flag to enable/disable XAutoConfig

		@Override
		public String[] selectImports(AnnotationMetadata metadata) {
			System.out.println("AAA MyImportSelector");
			enabled = true;
			return new String[0];  // return empty
		}
	}

	@Configuration
	@ConditionalOnX
	static class MyAutoConfiguration {
		@Bean
		public String foo() {
			return "FOO";
		}
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@Conditional(OnXCondition.class)
	public @interface ConditionalOnX {
	}

	public static class OnXCondition extends SpringBootCondition {
		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			System.out.println("AAA enabled = " + MyImportSelector.enabled);
			return MyImportSelector.enabled ? ConditionOutcome.match("enabled") : ConditionOutcome.noMatch("disabled");
		}
	}

}
