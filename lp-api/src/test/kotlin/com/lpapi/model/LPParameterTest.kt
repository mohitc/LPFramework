package com.lpapi.model

import org.junit.jupiter.api.Assertions

open class LPParameterTest <T:LPParameter>{

  fun assertEquals(a:T, b: T, assertion: String) {
    Assertions.assertTrue(a==b, "${a::class.simpleName}.equals(): $assertion")
    Assertions.assertEquals(a.hashCode(), b.hashCode(), "${a::class.simpleName}.hashCode(): $assertion")
  }

  fun assertNotEquals(a:T, b: T, assertion: String) {
    Assertions.assertFalse(a==b, "${a::class.simpleName}.equals(): $assertion")
    Assertions.assertNotEquals(a.hashCode(), b.hashCode(), "${a::class.simpleName}.hashCode(): $assertion")
  }

}