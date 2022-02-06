package me.krax.oversight

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * Oversight는 객체에 정의되어 있는 필드 정보를 자동으로 읽어들이고, 읽어들인
 * 정보를 직렬화하여 JSON 타입으로 변환해주는 Serializer입니다.
 * @author Ruskonert
 */
@Suppress("UNCHECKED_CAST")
open class Oversight<Entity : Oversight<Entity>> : OversightHandler<OversightCollector<*>>
{
    /**
     * 해당 객체에 대한 고유 ID입니다. 해당 필드를 임의로 수정하지 마십시오.
     * 해당 필드는 각 객체에 대해서 고유함을 보여줄 수 있는 정체성을 부여합니다.
     */
    private var _uid : String = UUID.randomUUID().toString().replace("-", "")

    /**
     * 해당 객체에 대한 고유 ID를 반환합니다.
     * @return 해당 객체에 대한 고유 ID를 반환합니다.
     */
    fun getUniqueId() : String = this._uid


    /**
     * Oversight가 참조하고 있는 실제 타입으로, 런타임 시 자동으로 값이 설정됩니다.
     * 이 값을 임의로 수정하지 마십시오.
     */
    @Transient
    private var _reference : Type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]

    /**
     * Oversight가 참조하고 있는 실제 타입입니다. 해당 클래스에 대한 Type 값을 가져와서
     * Reflection 작업을 진행하는 것은 특수한 경우를 제외하고 권장하지 않습니다.
     *
     * @return Oversight가 참조하고 있는 타입을 반환합니다.
     */
    fun getOversightType() : Class<Entity> = this._reference as Class<Entity>

    /**
     * 객체를 생성하는 메소드를 호출하기 전에 실행되는 전처리 메소드이며,
     * 개발자가 원하는 코드를 작성할 수 있습니다. 작업이 정상적으로 완료되면
     * true를, 아니라면 false를 반환하는 방식으로 코드를 작성하시기 바랍니다.
     *
     * @return 해당 메소드가 정상적으로 실행되면 true, 아니라면 false를 반환함
     */
    open fun preCreate() : Boolean = true

    /**
     * 객체 생성이 완료뫼면 실행되는 메소드이며, 개발자가 원하는 코드를 작성할 수 있습니다.
     * 작업이 정상적으로 완료되면 true를, 아니라면 false를 반환하는 방식으로 코드를
     * 작성하시기 바랍니다.
     *
     * @return 해당 메소드가 정상적으로 실행되면 true, 아니라면 false를 반환함
     */
    open fun afterCreate() : Boolean = true

    /**
     * 객체가 생성되었는지 여부를 저장하는 필드입니다.
     */
    @Transient
    private var _isInitialized : Boolean = false

    /**
     * 객체가 생성되었는지 여부를 확인합니다.
     * @return 객체가 생성되었는지 여부
     */
    fun isInitialized() : Boolean = this._isInitialized

    /**
     * 객체를 생성하는 메소드입니다. enableSafety가 false라면, preCreate 메소드에 대한
     * 반환값를 검증하지 않습니다. 해당 메소드에 의해 생성된 객체는 OversightCollector에
     * 의해 자동으로 관리되고, 객체에 대한 고유한 정체성을 부여할 수 있습니다. 이를 통해 중복
     * 객체 생성을 방지하고 하나의 객체를 통해 효율적으로 관리할 수 있습니다. 개발자는 인자
     * 옵션에 따라 객체가 OversightCollector에 의존하지 않고 독립성을 유지할지 여부를 결정할
     * 수 있습니다.
     *
     * @param enableSafety preCreate() 메소드의 return 값을 체크할지 여부를 설정합니다.
     * @param isIndependent 생성한 객체가 OversightCollector에 의해 자동으로 관리하지
     *                      않도록 설정할 것인지 설정합니다.
     * @return 최종적으로 생성이 완료된 객체를 반환합니다.
     */
    fun create(enableSafety : Boolean = true, isIndependent : Boolean = false) : Entity
    {
        if(this._isInitialized) {
            Log.print(
                Log.OVERSIGHT_LOG_WARN,
                "Object[${this._uid}] is already created!"
            )
            return this as Entity
        }
        Log.print(
            Log.OVERSIGHT_LOG_DEBUG,
            "Creating Object[${this._uid}]"
        )

        // preCreate 검증에 실패할 경우
        if(enableSafety && !this.preCreate()) {
            Log.print(
                Log.OVERSIGHT_LOG_ERROR,
                "Object[${this._reference.typeName}::${this._uid}]: " +
                        "Failed to pass from preCreate() method"
            )
            return this as Entity
        }

        this._isInitialized = true

        // 생성하려는 객체가 독립 형태이라면
        if(isIndependent) {
            this.setEnable(DummyCollector.INSTANCE)
            Log.print(
                Log.OVERSIGHT_LOG_WARN,
                "Enabled Object[${this._uid}], which is independent type"
            )
        }
        else {
            OversightCollector.register(this)
            Log.print(
                Log.OVERSIGHT_LOG_WARN,
                "Enabled Object[${this._uid}], Hooked by independent collector -> " +
                        "[${this._reference.typeName}@${this._collector}]"
            )
        }

        Log.print(
            Log.OVERSIGHT_LOG_DEBUG,
            "Object[${this._reference.typeName}::${this._uid}]: created complete"
        )

        // 생성이 완료되면 afterCreate를 호출한 후, 검증에 실패할 경우
        if(enableSafety && !this.afterCreate())
            Log.print(
                Log.OVERSIGHT_LOG_ERROR,
                "Object[${this._reference.typeName}::${this._uid}]: " +
                        "Failed to pass from afterCreate() method"
            )

        return this as Entity
    }

    /**
     * 해당 객체를 관리하는 OversightCollector입니다.
     * 해당 필드를 임의로 수정하지 마십시오.
     */
    @Transient
    private var _collector : OversightCollector<*>? = null

    /**
     * 해당 객체를 관리하고 있는 OversightCollector를 가져옵니다.
     * @return 해당 객체를 관리하고 있는 OversightCollector
     */
    fun getCollector() : OversightCollector<*>? = this._collector!!

    /**
     * 해당 객제가 OversightCollector에 의존하지 않는 독립적인 개체인지
     * 판단합니다.
     * @return 독립적인 객체인지 아닌지 여부를 반환합니다.
     */
    fun isIndependentEntity() : Boolean = this._collector == null

    /**
     * 해당 객체가 활성화 상태인지 아닌지 여부를 판단합니다.
     * @return 해당 객체가 활성화 상태인지 아닌지 여부를 반환합니다.
     */
    override fun isEnabled(): Boolean = this._collector != null

    /**
     * 객체를 활성화합니다. 활성화할 때는 OversightCollector가 필요합니다.
     * @param handleInstance 객체를 활성화하기 위한 OversightCollector
     */
    final override fun setEnable(handleInstance: OversightCollector<*>?) {
        this._collector = handleInstance
        this.setEnable(this._collector != null)
    }

    /**
     * 객체의 활성화 여부에 따라서 원하는 작업을 수행할 수 있습니다.
     *  @param enable 객체 활성화 여부
     */
    override fun setEnable(enable: Boolean) {

    }

    fun stringify(isPretty: Boolean = false) : String
    {
        return ""
    }

    override fun toString(): String = "${this._reference.typeName}@${this._uid}"
}
