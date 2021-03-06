package org.neo4j.cloudfoundry.odb.adapter.command.converter

import org.neo4j.cloudfoundry.odb.adapter.domain.Mandatory
import java.lang.reflect.Field
import java.util.Spliterator
import java.util.Spliterators
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

class MandatoryFieldsValidator {

    private val scannedPackage = "org.neo4j.cloudfoundry.odb.adapter"

    fun validate(enclosingInstance: Any?, prefix: String = ""): List<String> {
        if (enclosingInstance == null || !enclosingInstance::class.java.name.startsWith(scannedPackage)) {
            return listOf()
        }
        return streamOf(enclosingInstance.javaClass.declaredFields.iterator())
                .flatMap {
                    it.isAccessible = true
                    val qualifiedName = buildQualifiedName(prefix, it)
                    Stream.concat(
                            this.selfStream(enclosingInstance, it, qualifiedName),
                            this.validate(it.get(enclosingInstance), qualifiedName).stream()
                    )
                }
                .collect(Collectors.toList())
    }

    private fun buildQualifiedName(prefix: String, it: Field) =
            if (prefix.isEmpty()) it.name else "$prefix.${it.name}"

    private fun selfStream(enclosingInstance: Any, field: Field, qualifiedName: String?): Stream<String>? {
        return if (isMissingMandatoryField(enclosingInstance, field)) {
            Stream.of(qualifiedName)
        } else Stream.empty()
    }

    private fun isMissingMandatoryField(instance: Any, field: Field): Boolean {
        return field.isAnnotationPresent(Mandatory::class.java) && isUnset(field.get(instance))
    }

    private fun isUnset(value: Any?): Boolean {
        return when(value) {
            null -> true
            is String -> value.isBlank()
            is Array<*> -> value.isEmpty()
            is Iterable<*> -> !value.iterator().hasNext()
            else -> false
        }
    }

    private fun streamOf(fields: Iterator<Field>): Stream<Field> {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED),
                false
        )
    }
}