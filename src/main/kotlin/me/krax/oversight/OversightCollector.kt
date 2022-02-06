package me.krax.oversight

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 */
@Suppress("UNCHECKED_CAST")
open class OversightCollector<E : Oversight<E>>
{
    fun create(isIndependent : Boolean = false) : OversightCollector<E>
    {
        // 독립적인 collector 형태인지 확인합니다.
        if(isIndependent) {
            Log.print(Log.OVERSIGHT_LOG_WARN, "IndependentType of Collector is not recommend for [${this}], Please manage the instance object manually")
        }
        else {
            Log.print(Log.OVERSIGHT_LOG_DEBUG, "Attempting register type -> [${this.getOversightType()}], which object id is [$this]")

            // 이미 관리하고 있는 collector가 있다면 생성할 필요가 없습니다!
            // 이것은 정상적인 코드 사용이 아닙니다.
            if(isRunningForClass(this.getOversightType())) {
                Log.print(Log.OVERSIGHT_LOG_ERROR, "Failed to register type -> [${this.getOversightType()}], already running for collector of this type?")
                throw OversightException("The ref type -> [${this.getOversightType()}], was already running from independent collector")
            }
            OVERSIGHT_COLLECTOR[this.getOversightType()] = this
            Log.print(Log.OVERSIGHT_LOG_DEBUG, "The independent collector is created successfully: [$this]")
        }
        return this
    }

    /**
     * 해당 객체에 대한 고유 ID입니다. 해당 필드를 임의로 수정하지 마십시오.
     * 해당 필드는 각 객체에 대해서 고유함을 보여줄 수 있는 정체성을 부여합니다.
     * 일반적인 동작에서는 해당 필드는 참조하지 않습니다.
     */
    private var _uid : String = UUID.randomUUID().toString().replace("-", "")

    /**
     * OversightCollector가 참조하고 있는 실제 타입으로, 런타임 시 자동으로 값이 설정됩니다.
     * 이 값을 임의로 수정하지 마십시오.
     */
    private var _reference : Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

    /**
     * OversightCollector가 참조하고 있는 실제 타입입니다. 해당 클래스에 대한 Type 값을 가져와서
     * Reflection 작업을 진행하는 것은 특수한 경우를 제외하고 권장하지 않습니다.
     *
     * @return OversightCollector가 참조하고 있는 타입을 반환합니다.
     */
    fun getOversightType() : Class<E> = this._reference as Class<E>

    /**
     * collector가 관리하는 독립형 객체입니다. Independent 형식은 넣을 수 없습니다.
     */
    private val _store : MutableList<E> = Collections.synchronizedList(ArrayList<E>())

    companion object
    {
        init {

        }
        /**
         *
         */
        private val OVERSIGHT_COLLECTOR : ConcurrentHashMap<Class<*>, OversightCollector<*>> = ConcurrentHashMap()

        /**
         * Collector을 처음 사용할 때, 초기화하는 메소드입니다.
         *
         * @return 초기화에 성공했을 경우 true, 아니라면 false를 반환합니다.
         */
        private fun initRun() : Boolean
        {
            OVERSIGHT_COLLECTOR[Dummy::class.java] =
                DummyCollector.INSTANCE
            Log.print(
                Log.OVERSIGHT_LOG_DEBUG,
                "Initialized the OversightCollector for Default"
            )
            return true
        }

        /**
         * target 타입에 대해서 관리하는 독립적인 Collector가 이미 동작하고 있는지 확인합니다.
         * @param target 확인하고자 하는 타입
         * @return target 타입에 대한 Collector 동작 여부
         */
        fun isRunningForClass(target: Class<*>) : Boolean = OVERSIGHT_COLLECTOR.contains(target)

        /**
         * Oversight 타입의 target에 fidlaName의 이름을 가지는 필드를 가져옵니다.
         *
         * @param target 찾고자 하는 객체 대상
         * @param fieldName 찾고자 하는 필드 이름
         * @return 발견된 필드를 반환합니다. 이 값은 Nullable합니다.
         */
        fun findOversightField(target : Class<*> = this::class.java, fieldName : String) : Field?
        {
            var clazz : Class<*> = target
            while(clazz != Oversight::class.java) {
                clazz = clazz.superclass
            }
            return clazz.getDeclaredField(fieldName)
        }

        /**
         * 생성한 객체를 대상으로 collector와 연결합니다. 이미 활성화되어 있다면, 안전성을 위해 재설정을 차단합니다.
         *
         * @param entity 생성한 객체입니다.
         * @param collector 생성한 객체에 연결하고자 하는 collector입니다.
         * @return 연결에 성공하였다면 true, 아니라면 false를 반환합니다.
         */
        private fun setCollectionHandler(entity: Oversight<*>, collector: OversightCollector<*>) : Boolean
        {
            if(entity.isEnabled()) {
                Log.print(
                    Log.OVERSIGHT_LOG_ERROR,
                    "Object[${entity}] was already enabled. it needs to disable the object. Skipping process"
                )
                return false
            }

            val eField = findOversightField(
                entity::class.java,
                "_collector"
            )
            if(eField == null) {
                Log.print(
                    Log.OVERSIGHT_LOG_ERROR,
                    "Object[${entity}] was corrupted! was it the source of class correct?"
                )
                return false
            }
            eField.isAccessible = true

            // collector를 해당 객체의 필드에 연결합니다.
            eField.set(entity, collector)
            entity.setEnable(collector)
            return true
        }

        /**
         * 생성한 객체의 타입을 조사하고, 해당 타입을 관리하는 collector에 연결합니다.
         * @param entity 연결하고자 하는 객체
         */
        fun <E: Oversight<E>> register(entity : Oversight<E>) : Boolean
        {
            val collector : OversightCollector<*>?

            // Collector에 더미가 없다면 추가 작업을 수행
            if(!OVERSIGHT_COLLECTOR.containsKey(Dummy::class.java)) initRun()
            if(!entity.isInitialized()) {
                Log.print(
                    Log.OVERSIGHT_LOG_WARN,
                    "Object[${entity}] is need to initialize process, Aborting"
                )
                return false
            }

            val oversightType = entity.getOversightType()

            // 객체 타입에 대해 관리하는 collector가 존재하지 않는다면, 기본 collector에 연결합니다.
            if(!OVERSIGHT_COLLECTOR.containsKey(oversightType)) {
                Log.print(
                    Log.OVERSIGHT_LOG_WARN,
                    "Not existed collector of Object[${entity}], Use instead default"
                )
                if(!setCollectionHandler(
                        entity,
                        DummyCollector.INSTANCE
                    )
                ) {
                    Log.print(
                        Log.OVERSIGHT_LOG_ERROR,
                        "Failed to register the collector of Object[${entity}]"
                    )
                    return false
                }
            }
            else {
                collector = OVERSIGHT_COLLECTOR[oversightType] as? OversightCollector<E>
                if(collector == null) {
                    Log.print(
                        Log.OVERSIGHT_LOG_WARN,
                        "Object[${entity}]'s collector is null, Something wrong"
                    )
                    return false
                }
                else {
                    if(!setCollectionHandler(
                            entity,
                            collector
                        )
                    ) {
                        Log.print(
                            Log.OVERSIGHT_LOG_ERROR,
                            "Failed to register the collector of Object[${entity}], Something wrong"
                        )
                        return false
                    }
                    collector._store.add(entity as E)
                    Log.print(
                        Log.OVERSIGHT_LOG_WARN,
                        "Object[${entity}] registered successfully from $collector"
                    )
                }
            }
            return true
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other::class.java != this::class.java) return false
        return this._uid == (other as OversightCollector<*>)._uid && this.getOversightType() == other.getOversightType()
    }

    override fun toString(): String = "${this._reference}@${this._uid}"
}