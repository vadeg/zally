package de.zalando.zally.util.ast

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonPointer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.ArrayDeque
import java.util.Deque
import java.util.IdentityHashMap
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class ReverseAstBuilder<T : Any> internal constructor(root: T) {
    private val extensionMethodNames = HashSet<String>()

    private val nodes = ArrayDeque<Node>(listOf(Node(root, JsonPointers.EMPTY, null)))
    private val objectsToNodes = IdentityHashMap<Any, Node>()
    private val pointersToNodes = HashMap<String, Node>()

    class ReverseAstException internal constructor(message: String, cause: Throwable) : Exception(message, cause)

    fun withExtensionMethodNames(vararg names: String): ReverseAstBuilder<T> {
        this.extensionMethodNames += listOf(*names)
        return this
    }

    /**
     * Construct a new ReverseAst instance from the root object in this builder.
     * Traverses a Swagger or OpenApi object tree and constructs a map of object nodes to meta information objects.
     *
     * @return A new ReverseAst instance.
     * @throws ReverseAstException If an error occurs during reflection.
     */
    fun build(): ReverseAst {
        while (!nodes.isEmpty()) {
            val node = nodes.pop()
            if (node.obj in objectsToNodes.keys) {
                continue
            }
            if (node.obj.javaClass !in Util.PRIMITIVES) {
                val children = when {
                    node.obj is Map<*, *> -> handleMap(node.obj, node.pointer, node.marker)
                    node.obj is List<*> -> handleList(node.obj, node.pointer, node.marker)
                    node.obj is Set<*> -> handleSet(node.obj, node.pointer, node.marker)
                    node.obj is Array<*> -> handleArray(node.obj, node.pointer, node.marker)
                    else -> handleObject(node.obj, node.pointer, node.marker)
                }
                nodes += children
                node.children += children
            }
            if (!node.skip) {
                objectsToNodes[node.obj] = node
                pointersToNodes[node.pointer.toString()] = node
            }
        }
        return ReverseAst(objectsToNodes, pointersToNodes)
    }

    private fun handleMap(map: Map<*, *>, pointer: JsonPointer, defaultMarker: Marker?): Deque<Node> {
        return ArrayDeque<Node>(
            map
            .filter { (key, value) -> key is String && value != null }
            .map { (key, value) ->
                Node(value!!, pointer.append(JsonPointers.escape(key as String)), getMarker(map) ?: defaultMarker )
            }
        )
    }

    private fun handleList(list: List<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> =
        handleArray(list.toTypedArray(), pointer, marker)

    private fun handleSet(set: Set<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> =
        handleArray(set.toTypedArray(), pointer, marker)

    private fun handleArray(objects: Array<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> =
        ArrayDeque(objects.filterNotNull().mapIndexed { i, value ->
            Node(value, pointer.append(JsonPointers.escape(i.toString())), marker)
        })

    private fun handleObject(obj: Any, pointer: JsonPointer, defaultMarker: Marker?): Deque<Node> {
        val nodes = ArrayDeque<Node>()
        val marker = getMarker(obj) ?: defaultMarker

        for (m in traversalMethods(obj.javaClass)) {
            val name = m.name
            try {
                m.invoke(obj)?.let { value ->
                    if (m.isAnnotationPresent(JsonAnyGetter::class.java)) {
                        // A `JsonAnyGetter` method is simply a wrapper for nested properties.
                        // We must not use the method name but re-use the current pointer.
                        nodes.push(Node(value, pointer, marker, /* skip */true))
                    } else {
                        nodes.push(Node(value, pointer.append(JsonPointers.escape(m)), marker))
                    }
                }
            } catch (e: ReflectiveOperationException) {
                throw ReverseAstException("Error invoking $name on ${obj.javaClass.name} at path $pointer", e)
            }
        }
        return nodes
    }

    private fun getMarker(map: Map<*, *>): Marker? =
        getVendorExtensions(map, Marker.TYPE_X_ZALLY_IGNORE)
            ?.let { Marker(Marker.TYPE_X_ZALLY_IGNORE, it) }

    private fun getMarker(obj: Any): Marker? =
        getVendorExtensions(obj, Marker.TYPE_X_ZALLY_IGNORE)
            ?.let { Marker(Marker.TYPE_X_ZALLY_IGNORE, it) }

    private fun getVendorExtensions(obj: Any, extensionName: String): Collection<String>? {
        if (obj is Map<*, *>) {
            return getVendorExtensions(obj, extensionName)
        }
        for (m in traversalMethods(obj.javaClass)) {
            if (extensionMethodNames.contains(m.name)) {
                try {
                    val extensions = m.invoke(obj)
                    if (extensions is Map<*, *>) {
                        return getVendorExtensions(extensions, extensionName)
                    }
                } catch (e: ReflectiveOperationException) {
                    throw ReverseAstException("Error getting extensions.", e)
                }
            }
        }
        return null
    }

    private fun getVendorExtensions(map: Map<*, *>, extensionName: String): Collection<String>? =
        map[extensionName]?.let { value ->
            when (value) {
                is String -> return setOf(value)
                is Collection<*> -> return value.map { it.toString() }.toSet()
                else -> null
            }
        }

    companion object {
        fun traversalMethods(clazz: Class<*>) = clazz
            .methods
            .filter {
                it.name.startsWith("get") &&
                    it.name != "getClass" &&
                    it.name != "getDeclaringClass" &&
                    it.parameterCount == 0 &&
                    Modifier.isPublic(it.modifiers) &&
                    !it.isAnnotationPresent(JsonIgnore::class.java)
            }
            .sortedWith(Comparator
                .comparing { method: Method -> method.name == "getPaths" }
                .thenComparing { method: Method -> method.name })
    }
}
