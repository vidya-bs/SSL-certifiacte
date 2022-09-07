package com.itorix.apiwiz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class ClasspathScanningTest {

	public static void main(String args[]) {

		ClasspathScanningTest test = new ClasspathScanningTest();
		test.execute();

	}

	private void execute() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true) {
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return true;
			}
		};

		provider.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

		Set<BeanDefinition> candidateComponents = provider.findCandidateComponents("com.itorix.apiwiz");

		Set<BeanDefinition> beanDefinitions = candidateComponents.stream().filter(
				bd -> bd.getBeanClassName().endsWith("Service") || bd.getBeanClassName().endsWith("ServiceImpl"))
				.collect(Collectors.toSet());

		for (BeanDefinition bd : beanDefinitions) {
			try {
				Class<?> aClass = Class.forName(bd.getBeanClassName());
				Method[] declaredMethods = aClass.getDeclaredMethods();
				for (Method m : declaredMethods) {
					Arrays.asList(m.getAnnotations());
				}
			} catch (ClassNotFoundException e) {
				log.error("Exception occurred", e);
			}
		}
	}

}
