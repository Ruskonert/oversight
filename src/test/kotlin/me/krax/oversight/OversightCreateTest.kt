package me.krax.oversight

import org.junit.Test

class OversightCreateTest
{
    private class OversightSample : Oversight<OversightSample>()
    {
        override fun preCreate(): Boolean {
            return false
        }

        override fun afterCreate(): Boolean {
            return false
        }
    }

    @Test
    fun test()
    {
        val element = OversightSample().create(enableSafety = false)
        assert(element.isInitialized())
    }

    @Test
    fun test2()
    {
        val element = OversightSample().create()
        assert(!element.isInitialized())
    }
}
