package basics

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Example(
  justAParameter: Int,

  // Prefixing constructor parameters with val or var makes a property and automatically initializes it
  val finalProperty: Int,        // val <=> final in Java
  var mutableProperty: Int = 2,  // var <=> mutable binding

  private val propertiesArePublicByDefault: Boolean = true
) {
  // in the java class this translates to a private field and a getter (a var also has a setter)
  val doubleJustAParameter = justAParameter * 2

  init {
    println("Additional initialization code if needed")
  }

  private val anotherProp: Int

  init {
    println("Can have multiple init blocks")
    val fullMoon = (Math.random() > .5)
    anotherProp = if (fullMoon) 42 else 0
  }

  constructor(p: Int) : this(p * 2, p * 3, p * 4) {
    // Secondary constructor
    // You can't prefix secondary constructors params with val/var
  }

  fun method(someParam: Int): String {
    println("Called method with param $someParam")
    return "${someParam + 1}"
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val x = Example(1, 2)
      println(x.method(5))
    }
  }
}





class ExplicitGettersAndSetters {
  private var _something: Int = 0
  var something
    get() = _something
    set(value) {println("Setting _something to $value"); _something = value}

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val x = ExplicitGettersAndSetters()
      x.something = 10
      println("x.something = ${x.something}")
    }
  }
}





class LazyDelegate {
  init { println("Creating PropertyDelegates") }

  class SomeCostlyObject {
    init { println("Creating SomeCostlyObject") }
  }

  // By default does double-checked locking
  val costlyInstance by lazy {
    SomeCostlyObject()
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val x = LazyDelegate()
      println("----")
      x.costlyInstance
    }
  }
}







// Sadly nested typealias are NOT allowed
typealias PropertyChangeListener<E, F> = (oldValue: E, newValue: E, instance: F, property: KProperty<F>) -> Unit

class HorribleExampleOfCustomDelegate(private val name: String) {
  class PropertiesBus {
    val listeners = CopyOnWriteArrayList<Triple<KClass<*>, KClass<*>, PropertyChangeListener<*, *>>>()

    inline fun <reified E, reified F> addListener(noinline l: PropertyChangeListener<E, F>) =
      listeners.add(Triple(E::class, F::class, l as PropertyChangeListener<*, *>))

    fun <E, F> publish(oldValue: E, newValue: E, instance: F, property: KProperty<F>) = listeners.forEach {
      if (it.second.isInstance(instance) && it.first.isInstance(newValue)) {
        (it.third as PropertyChangeListener<E, F>)(oldValue, newValue, instance, property)
      }
    }
  }

  class BusPublished<E>(val bus: PropertiesBus, block: () -> E) {
    private var value: E = block()
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: E) {
      bus.publish(value, newValue, thisRef, property)
      value = newValue
    }
  }

  var p by BusPublished(bus) { 0 }

  override fun toString() = name

  companion object {
    var bus = PropertiesBus()

    @JvmStatic
    fun main(args: Array<String>) {
      bus.addListener { oldValue: Int, newValue, instance: HorribleExampleOfCustomDelegate, p ->
        println("Changed property [$p] of [$instance] from [$oldValue] to [$newValue]")
      }

      val pippo = HorribleExampleOfCustomDelegate("pippo")
      pippo.p++
    }
  }
}
