package me.krax.oversight

import org.junit.Test

class OversightCollectorTest
{
    private class OversightSampleCollector : OversightCollector<OversightSample>()
    private class OversightSample : Oversight<OversightSample>()

    @Test
    fun test()
    {
        val collectionElement = OversightSampleCollector().create()
        val element = OversightSample().create()
        assert(element.isEnabled())
        assert(element.getCollector() == collectionElement)
    }
}