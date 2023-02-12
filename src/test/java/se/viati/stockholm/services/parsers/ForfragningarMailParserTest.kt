package se.viati.stockholm.services.parsers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import se.viati.stockholm.services.domain.Assignment
import se.viati.stockholm.services.domain.Mail

class ForfragningarMailParserTest {

  @Test
  fun `parse forfragningar mail`() {
    val forfragningarMail = javaClass.getResource("/forfragningar-email.html").readText()

    val assignments = ForfragningarMailParser().parseMail(Mail("forfragningarId", "", forfragningarMail))

    Assertions.assertThat(assignments).hasSize(6)
    Assertions.assertThat(assignments[0].originatingMailIds[0]).isEqualTo("forfragningarId")
    Assertions.assertThat(assignments[0].title)
        .isEqualTo("1 st IT-kostnadshanterare Bolagsverket Sundsvall 2019-10-30")
    Assertions.assertThat(assignments[0].description).isEmpty()
    Assertions.assertThat(assignments[0].source).isEqualTo("Viati-förfrågningar")
    Assertions.assertThat(assignments[1].title)
        .isEqualTo("1 st Teknisk Projektledare CSN Sundsvall 2019-10-23")
    Assertions.assertThat(assignments[1].description).isEmpty()
    Assertions.assertThat(assignments[2].title)
        .isEqualTo("1 st iOS-Utvecklare SVT Stockholm ASAP")
    Assertions.assertThat(assignments[2].description).isEmpty()
    Assertions.assertThat(assignments[3].title)
        .isEqualTo("1 st Controller/Verksamhetsanalytiker CSN Sundsvall 2019-10-28")
    Assertions.assertThat(assignments[3].description).isEmpty()
    Assertions.assertThat(assignments[4].title)
        .isEqualTo("1 st Uppdragsledare/verksamhetsutvecklare SPV Sundsvall 2019-10-28")
    Assertions.assertThat(assignments[4].description).isEmpty()
    Assertions.assertThat(assignments[5].title)
        .isEqualTo("1 st IT-arkitekt CSN Sundsvall 2019-10-25")
    Assertions.assertThat(assignments[5].description).isEmpty()
    Assertions.assertThat(assignments[5].originatingMailIds[0]).isEqualTo("forfragningarId")
  }

  @Test
  fun groupingTest() {
    val list = listOf(
        Assignment("1", "Apa", "Desc1"),
        Assignment("2", "Hund", "Desc2"),
        Assignment("3", "Hund", "Desc3"),
        Assignment("4", "Fisk", "Desc4"),
        Assignment("5", "Fisk", "Desc5"),
        Assignment("6", "Fisk", "Desc6")
    )
    val firstAssigmentOfEachTitle = list.groupingBy { it.title }
        .reduce { _, accumulator, element -> accumulator.addOriginatingIds(element.originatingMailIds) }
    println(firstAssigmentOfEachTitle)
  }
}