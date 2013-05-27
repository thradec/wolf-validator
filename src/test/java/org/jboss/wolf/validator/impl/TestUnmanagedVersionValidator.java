package org.jboss.wolf.validator.impl;

import static org.jboss.wolf.validator.impl.TestUtil.dependency;
import static org.jboss.wolf.validator.impl.TestUtil.pom;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.maven.model.Model;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class TestUnmanagedVersionValidator extends AbstractTest {
    
    @Configuration
    public static class TestConfiguration {

        @Bean
        public IOFileFilter modelValidatorFilter() {
            return new TestFileFilter();
        }
        
        @Bean
        public IOFileFilter unmanagedVersionValidatorFilter() {
            return new TestFileFilter();
        }

    }

    @Test
    public void shouldFindManagedVersions() {
        Model fooApi = pom().artifactId("foo-api").create(repoFooDir);
        Model fooImpl = pom().artifactId("foo-impl").create(repoFooDir);

        pom().artifactId("foo-bom").packaging("pom").
                dependencyManagement(fooApi).
                dependencyManagement(fooImpl).
                create(repoFooDir);

        validator.validate(ctx);

        assertSuccess();
    }
    
    @Test
    public void shouldFindManagedVersionsWithInterpolation() {
        Model fooApi = pom().artifactId("foo-api").create(repoFooDir);
        Model fooImpl = pom().artifactId("foo-impl").create(repoFooDir);

        pom().artifactId("foo-bom").packaging("pom").
                property("version.foo", "1.0").
                dependencyManagement(dependency().to(fooApi).version("${version.foo}").build()).
                dependencyManagement(dependency().to(fooImpl).version("${version.foo}").build()).
                create(repoFooDir);

        validator.validate(ctx);

        assertSuccess();
    }

    @Test
    public void shouldFindUnmanagedVersion() {
        Model fooApi = pom().artifactId("foo-api").create(repoFooDir);
        Model fooImpl = pom().artifactId("foo-impl").create(repoFooDir);
        pom().artifactId("foo-impl").version("2.0").create(repoFooDir);

        pom().artifactId("foo-bom").packaging("pom").
                dependencyManagement(fooApi).
                dependencyManagement(fooImpl).
                create(repoFooDir);

        validator.validate(ctx);

        assertExpectedException(UnmanagedVersionException.class, "project com.acme:foo-impl:jar:2.0 is unmanaged");
    }
    
}