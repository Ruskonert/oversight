package me.krax.oversight

import java.lang.RuntimeException

/**
 * Oversight과 관련한 Exception 클래스입니다.
 */
open class OversightException(message: String) : RuntimeException(message)