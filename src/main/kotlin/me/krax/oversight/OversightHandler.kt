package me.krax.oversight

interface OversightHandler<Handler>
{
    fun setEnable(handleInstance : Handler?)

    fun setEnable(enable : Boolean)

    fun isEnabled() : Boolean
}