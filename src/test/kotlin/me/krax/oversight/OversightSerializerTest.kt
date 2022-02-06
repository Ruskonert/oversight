package me.krax.oversight

import org.junit.Test

class OversightSerializerTest {
    private class OversightSampleCollector : OversightCollector<OversightSample>()
    private class OversightSample : Oversight<OversightSample>()

    @Test
    fun test()
    {
        val oversightSampleCollector = OversightSampleCollector().create()
        val oversightSample = OversightSample().create()
    }
}