package se.viati.stockholm.services.parsers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import se.viati.stockholm.services.domain.Mail

class UpgradedMailParserTest {

  @Test
  fun `parse Upgraded mail correctly`() {
    val upgradedMail = javaClass.getResource("/upgraded-email.html").readText()

    val assignments = UpgradedMailParser().parseMail(Mail("upgradedId", "", upgradedMail))

    Assertions.assertThat(assignments).hasSize(9)
    Assertions.assertThat(assignments[0].originatingMailIds[0]).isEqualTo("upgradedId")
    Assertions.assertThat(assignments[0].title).isEqualTo("IT-strateg server- och applikationsdrift (ref 128367)")
    Assertions.assertThat(assignments[0].description).isEmpty()
    Assertions.assertThat(assignments[0].source).isEqualTo("Upgraded")
    Assertions.assertThat(assignments[1].title).isEqualTo("Agil Coach")
    Assertions.assertThat(assignments[2].title).isEqualTo("Förvaltningsledare (ref 128345)")
    Assertions.assertThat(assignments[3].title).isEqualTo("Junior Devops-utvecklare (ref. 128293)")
    Assertions.assertThat(assignments[4].title).isEqualTo("Informationssäkerhetsspecialist (ref 12069)")
    Assertions.assertThat(assignments[5].title).isEqualTo("Java-utvecklare (ref. 120569)")
    Assertions.assertThat(assignments[6].title).isEqualTo("Service/Drifttekniker - Skype & Exchange (ref. 120386)")
    Assertions.assertThat(assignments[7].title).isEqualTo("Pentest (Penetrationstester) (ref. 120266)")
    Assertions.assertThat(assignments[8].title).isEqualTo("Tekniska Mallförvaltning Officeutveckling (ref 118890)")
  }
}