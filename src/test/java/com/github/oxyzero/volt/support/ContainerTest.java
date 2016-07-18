package com.github.oxyzero.volt.support;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ContainerTest {

    private Container container = new Container();

    public ContainerTest() {}

    @Before
    public void setUp() {
        this.container = new Container();
    }

    @Test
    public void testSimpleInjection() {
        this.container.register("test", c -> "hello");

        String result = (String) this.container.resolve("test");

        assertTrue(result.equals("hello"));
    }

    @Test
    public void testDependencyInjection() {
        this.container.register("secret", c -> "secret");

        this.container.register("phrase", c -> "This is a " + c.resolve("secret"));

        String phrase = (String) this.container.resolve("phrase");

        assertTrue(phrase.equals("This is a secret"));
    }

    @Test
    public void testCreateSingleton() {
        this.container.register("instance", c -> "hello " + (int) (Math.random() * 100));
        this.container.singleton("singleton", c -> "hello " + (int) (Math.random() * 100));

        // Instances
        String i1 = (String) this.container.resolve("instance");
        String i2 = (String) this.container.resolve("instance");

        // Singletons
        String s1 = (String) this.container.resolve("singleton");
        String s2 = (String) this.container.resolve("singleton");


        assertFalse(i1 == i2);
        assertTrue(s1 == s2);
    }

    @Test
    public void testRemovingServices() {
        this.container.register("test", c -> "hello");
        this.container.singleton("testSing", c -> "hello");

        assertTrue(this.container.has("test"));
        assertTrue(this.container.has("testSing"));

        this.container.remove("test");
        this.container.remove("testSing");

        assertFalse(this.container.has("testSing"));
        assertFalse(this.container.has("test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolvingNonExistentService() {
        this.container.resolve("nonExistingService");
    }
}
