package me.krax.oversight

/**
 * DummyCollector는 어떠한 역할을 하지 않는 OversightCollector입니다.
 * Oversight 객체 중에서 OversightCollector에 의존하지 않고, 독립적으로
 * 동작하는 객체는 해당 객체를 사용합니다. 해당 객체를 사용할 때는
 * DummyCollector.INSTANCE 값을 사용하십시오.
 */
class DummyCollector private constructor() : OversightCollector<Dummy>()
{
    companion object {
        val INSTANCE : DummyCollector =
            DummyCollector()
    }
}

/**
 *
 */
class Dummy private constructor() : Oversight<Dummy>()
{
    companion object {
        val INSTANCE : Dummy = Dummy()
    }
    override fun setEnable(enable: Boolean) {
        Log.print(Log.OVERSIGHT_LOG_ERROR, "The oversight attempted to enable the dummy object")
    }
}
