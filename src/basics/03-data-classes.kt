package basics

data class X(val a: Int, val b: String)

fun main() {
  val x1 = X(0, "hello")
  val x2 = X(0, "hello")
  val x3 = X(1, "hello")

  println(" x1 == x2: ${x1 == x2}")  // Two equals is a call to .equal
  println("x1 === x2: ${x1 === x2}") // Three equals is instance equality (i.e. Java's ==)
  println(" x1 == x3: ${x1 == x3}")

  println("x1.hashCode() == x2.hashCode(): ${x1.hashCode() == x2.hashCode()}")

  println("x1: $x1")
}