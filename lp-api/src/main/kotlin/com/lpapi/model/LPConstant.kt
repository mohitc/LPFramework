package com.lpapi.model

/** The LP Constant is a defined constant that can be used within the model. The advantage of using constants instead
 * of static values is that the values of the constant can be updated in the model, and will be populated only during
 * the process of solving the model
 */
class LPConstant constructor(override val identifier: String, var value: Double) : LPParameter {

  constructor(identifier: String) : this(identifier, 0.0)

  constructor(identifier: String, number: Number) : this(identifier, number.toDouble())

  fun value(value:Double): LPConstant {
    this.value = value
    return this
  }

  override fun toString():String = "[Constant] $identifier : $value"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LPConstant

    if (identifier != other.identifier) return false
    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + value.hashCode()
    return result
  }

}