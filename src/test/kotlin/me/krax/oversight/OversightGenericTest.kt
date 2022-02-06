package me.krax.oversight

import org.junit.Assert.assertEquals
import org.junit.Test

class OversightGenericTest
{
    private class OversightSample : Oversight<OversightSample>()

    @Test
    fun test()
    {
        val element = OversightSample().create()
        assertEquals(element.getOversightType(), OversightSample::class.java)
    }
}