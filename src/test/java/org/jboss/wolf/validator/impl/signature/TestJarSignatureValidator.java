package org.jboss.wolf.validator.impl.signature;

import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import static org.jboss.wolf.validator.impl.TestUtil.pom;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.jboss.wolf.validator.impl.AbstractTest;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class TestJarSignatureValidator extends AbstractTest {

    @Configuration
    public static class TestConfiguration {

        @Bean
        public IOFileFilter jarSignatureValidatorFilter() {
            return trueFileFilter();
        }

    }

    private final File fooJar = new File(repoFooDir, "com/acme/foo/1.0/foo-1.0.jar");

    @Test
    public void shouldSuccess() throws IOException {
        pom().artifactId("foo").create(repoFooDir);
        validator.validate(ctx);
        assertSuccess();
    }

    @Test
    public void shouldFindSignedJar() throws IOException {
        pom().artifactId("foo").create(repoFooDir);

        FileUtils.deleteQuietly(fooJar);
        FileUtils.copyFile(new File("target/test-classes/empty-signed.jar"), fooJar);

        validator.validate(ctx);

        assertExpectedException(JarSignedException.class, "File target/repos/remote-repo-foo/com/acme/foo/1.0/foo-1.0.jar is signed");
    }

    @Test
    public void shouldFindDamagedSignedJar() throws IOException {
        pom().artifactId("foo").create(repoFooDir);

        FileUtils.deleteQuietly(fooJar);
        FileUtils.copyFile(new File("target/test-classes/empty-signed-damaged.jar"), fooJar);

        validator.validate(ctx);

        assertExpectedException(JarSignatureVerificationException.class, "Unable to verify signature for file target/repos/remote-repo-foo/com/acme/foo/1.0/foo-1.0.jar");
    }

}