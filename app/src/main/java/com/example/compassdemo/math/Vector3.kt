package app.simple.positional.math

import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {

    private val array = floatArrayOf(x, y, z)

    fun cross(other: Vector3): Vector3 {
        return Vector3(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        )
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(
                x - other.x,
                y - other.y,
                z - other.z
        )
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(
                x + other.x,
                y + other.y,
                z + other.z
        )
    }

    operator fun times(factor: Float): Vector3 {
        return Vector3(
                x * factor,
                y * factor,
                z * factor
        )
    }

    fun dot(other: Vector3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun magnitude(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vector3 {
        val mag = magnitude()
        return Vector3(
                x / mag,
                y / mag,
                z / mag
        )
    }

    fun toFloatArray(): FloatArray {
        return array
    }

    companion object {
        val zero = Vector3(0f, 0f, 0f)

        fun from(arr: FloatArray): Vector3 {
            return Vector3(arr[0], arr[1], arr[2])
        }
    }
}