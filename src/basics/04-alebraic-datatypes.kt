package basics

sealed class Expression
class Atom(val value: Int) : Expression()
class Add(val l: Expression, val r: Expression) : Expression()
class Mul(val l: Expression, val r: Expression) : Expression()

fun precedence(e: Expression): Int = when(e) {
  is Add -> 1
  is Mul -> 2
  is Atom -> 3
}

fun pretty(e: Expression, parentPrecedence: Int = 0): String {
  val prec = precedence(e)
  val noparens = when(e) {
    is Add -> "${pretty(e.l, prec)} + ${pretty(e.r, prec)}"
    is Mul -> "${pretty(e.l, prec)} * ${pretty(e.r, prec)}"
    is Atom -> "${e.value}"
  }
  return if (prec < parentPrecedence) "($noparens)" else noparens
}

fun calculate(e: Expression): Int = when(e) {
  is Atom -> e.value
  is Add -> calculate(e.l) + calculate(e.r)
  is Mul -> calculate(e.l) * calculate(e.r)
}

fun main() {
  val a = Add(Atom(1), Atom(2))

  val b = Mul(a, a)
  println(pretty(b) + " = " + calculate(b)) // Expected: (1 + 2) * (1 + 2) = 9

  val c = Add(Mul(Atom(1), Atom(2)), Atom(3))
  println(pretty(c) + " = " + calculate(c)) // Expected 1 * 2 + 3 = 5
}