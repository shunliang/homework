package com.shun;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {


  @BeforeAll
  static void setup() {
    // setup the common resource(s) for all tests
  }

  @AfterAll
  static void tearDown() {
     // close the common resource(s) for all tests
  }

  @Test
  void testMethod_expect_true() {

    boolean result =  true;
    assertTrue(result);
  }

}

