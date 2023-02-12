package se.viati.stockholm.services.parsers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import se.viati.stockholm.services.domain.Mail

class EworkMailParserTest {

  @Test
  fun `parse Ework mail`() {
    val eworkMail = javaClass.getResource("/ework-email.html").readText()

    val assignments = EworkMailParser().parseMail(Mail("eworkId", "", eworkMail))

    Assertions.assertThat(assignments).hasSize(4)
    Assertions.assertThat(assignments[0].originatingMailIds[0]).isEqualTo("eworkId")
    Assertions.assertThat(assignments[0].title)
        .isEqualTo("Senior Windows Server lösningsarkitekt (19-10-0249) , Microsoft Systems development Stockholm (Sweden)")
    Assertions.assertThat(assignments[0].description)
        .contains("Vara tekniskt ledande för carve-out av kundens")
    Assertions.assertThat(assignments[0].source).isEqualTo("Ework")
    Assertions.assertThat(assignments[1].title)
        .isEqualTo("Tool Developer (19-10-0235) , Other Systems Development Södertälje (Sweden)")
    Assertions.assertThat(assignments[1].description)
        .contains("Announced date: 2019-10-17 Apply before: 2019-10-21")
    Assertions.assertThat(assignments[2].title)
        .isEqualTo("Senior DevOps Engineer (Telecom) (19-10-0218) , Other Systems Development Stockholm (Sweden)")
    Assertions.assertThat(assignments[2].description)
        .contains("Read more and apply here:https://www.eworkgroup.com/assignment/eworkid//100300218")
    Assertions.assertThat(assignments[3].title)
        .isEqualTo("2 C++ experts consultants/teachers (19-10-9784) , Other Systems Development Södertälje (Sweden)")
    Assertions.assertThat(assignments[3].description)
        .contains("Start date: ASAP End date: 2020-05-01")
    Assertions.assertThat(assignments[3].originatingMailIds[0]).isEqualTo("eworkId")
  }
}