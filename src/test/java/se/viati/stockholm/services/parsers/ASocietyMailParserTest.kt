package se.viati.stockholm.services.parsers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import se.viati.stockholm.services.domain.Mail

class ASocietyMailParserTest {

  @Test
  fun parseNewAssignmentsMail() {
    val newAssignmentsMail = javaClass.getResource("/newassignments-email.html").readText()

    val assignments = ASocietyMailParser().parseMail(Mail("ASocietyId", "", newAssignmentsMail))

    Assertions.assertThat(assignments).hasSize(5)
    Assertions.assertThat(assignments[0].originatingMailIds[0]).isEqualTo("ASocietyId")
    Assertions.assertThat(assignments[0].title)
        .isEqualTo("1057 IT Service Responsible ISR to retail company - 780139 - Stockholm")
    Assertions.assertThat(assignments[0].description).contains("We are looking for an IT Delivery Manager")
    Assertions.assertThat(assignments[0].source).isEqualTo("A Society")
    Assertions.assertThat(assignments[1].title)
        .isEqualTo("3325 Requirement Analyst to retail company - 780140 - Stockholm")
    Assertions.assertThat(assignments[1].description).contains("You will have regular contact with surrounding systems")
    Assertions.assertThat(assignments[2].title)
        .isEqualTo("Zero Trust - 900110 - Stockholm")
    Assertions.assertThat(assignments[2].description).contains("Responsible salesperson: Peter Magnusson +46 707 73 82 94, peter.magnusson@asociety.se")
    Assertions.assertThat(assignments[3].title)
        .isEqualTo("IT Architect - 900109 - Stockholm")
    Assertions.assertThat(assignments[3].description).contains("Last response date: 2019-10-25")
    Assertions.assertThat(assignments[4].title)
        .isEqualTo("6423 Testleader to retail company - 780138 - Stockholm")
    Assertions.assertThat(assignments[4].description).contains("The expectation is that the person we are looking for")
    Assertions.assertThat(assignments[4].originatingMailIds[0]).isEqualTo("ASocietyId")
  }
}