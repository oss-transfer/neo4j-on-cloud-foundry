package org.neo4j.cloudfoundry.odb.adapter.command.converter

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.Test
import org.neo4j.cloudfoundry.odb.adapter.command.Fixtures
import org.neo4j.cloudfoundry.odb.adapter.domain.BoshVms
import picocli.CommandLine

class BoshVmConverterTest {

    private val gson = Gson()
    private val subject = BoshVmConverter(gson, MandatoryFieldsValidator())

    @Test
    fun `fails when the payload is not valid`() {
        assertThatExceptionOfType(CommandLine.ParameterException::class.java)
                .isThrownBy { subject.convert("""\salut\""") }
                .withMessage("Parameter 'bosh-VMs' cannot be deserialized")
                .withCauseInstanceOf(JsonSyntaxException::class.java)
    }

    @Test
    fun `fails when the payload is incomplete`() {
        val mandatoryFieldsValidator = mock<MandatoryFieldsValidator>()
        whenever(mandatoryFieldsValidator.validate(any<BoshVms>(), eq("")))
                .thenReturn(listOf("jean", "bonneau"))
        val subject = BoshVmConverter(gson, mandatoryFieldsValidator)

        assertThatExceptionOfType(CommandLine.ParameterException::class.java)
                .isThrownBy { subject.convert("{}") }
                .withMessage("Parameter 'bosh-VMs' is missing mandatory parameters: jean, bonneau")
    }

    @Test
    fun `converts a valid payload`() {
        assertThat(subject.convert(Fixtures.boshVmJson)).isEqualTo(Fixtures.boshVms)
    }
}