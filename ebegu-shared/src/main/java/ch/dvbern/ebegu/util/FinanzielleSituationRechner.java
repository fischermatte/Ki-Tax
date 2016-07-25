package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.services.EbeguParameterService;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Ein Rechner mit den ganzen Operationen fuer Finanziellesituation
 * Created by imanol on 22.06.16.
 */
@Dependent
public class FinanzielleSituationRechner {

	private BigDecimal abzugFamiliengroesse3;
	private BigDecimal abzugFamiliengroesse4;
	private BigDecimal abzugFamiliengroesse5;
	private BigDecimal abzugFamiliengroesse6;

	/**
	 * Konstruktor, welcher einen Rechner erstellt, der die Paramter aus der DB liest
	 */
	public FinanzielleSituationRechner() {

	}

	/**
	 * Konstruktor, welcher die Parameter miterhält. Dieser kann in JUnit Tests verwendet werden
     */
	public FinanzielleSituationRechner(BigDecimal abzugFamiliengroesse3, BigDecimal abzugFamiliengroesse4, BigDecimal abzugFamiliengroesse5, BigDecimal abzugFamiliengroesse6) {
		this.abzugFamiliengroesse3 = abzugFamiliengroesse3;
		this.abzugFamiliengroesse4 = abzugFamiliengroesse4;
		this.abzugFamiliengroesse5 = abzugFamiliengroesse5;
		this.abzugFamiliengroesse6 = abzugFamiliengroesse6;
	}

	@Inject
	private EbeguParameterService ebeguParameterService;

	/**
	 * Die Familiengroesse wird folgendermassen kalkuliert:
	 * Familiengrösse = Gesuchsteller1 + Gesuchsteller2 (falls vorhanden) + Faktor Steuerabzug pro Kind (0, 0.5, oder 1)
	 * <p>
	 * Der Faktor wird gemaess Wert des Felds kinderabzug von Kind berechnet:
	 * KEIN_ABZUG = 0
	 * HALBER_ABZUG = 0.5
	 * GANZER_ABZUG = 1
	 * KEINE_STEUERERKLAERUNG = 1
	 * <p>
	 * Nur die Kinder die nach dem uebergebenen Datum geboren sind werden mitberechnet
	 * <p>
	 * 8tung: Bei der Berechnung der Einkommensverschlechterung werden die aktuellen Familienverhältnisse berücksichtigt
	 * (nicht Stand 31.12. des Vorjahres)!
	 *
	 * @param gesuch das Gesuch aus welchem die Daten geholt werden
	 * @param date   das Datum fuer das die familiengroesse kalkuliert werden muss oder null für Einkommensverschlechterung
	 * @return die familiengroesse als double
	 */
	public double calculateFamiliengroesse(Gesuch gesuch, @Nullable LocalDate date) {
		double familiengroesse = 0;
		if (gesuch != null) {
			if (gesuch.getGesuchsteller1() != null) {
				familiengroesse++;
			}
			if (gesuch.getGesuchsteller2() != null) {
				familiengroesse++;
			}
			for (KindContainer kindContainer : gesuch.getKindContainers()) {
				if (kindContainer.getKindJA() != null && (date == null || kindContainer.getKindJA().getGeburtsdatum().isBefore(date))) {
					if (kindContainer.getKindJA().getKinderabzug() == Kinderabzug.HALBER_ABZUG) {
						familiengroesse += 0.5;
					}
					if (kindContainer.getKindJA().getKinderabzug() == Kinderabzug.GANZER_ABZUG || kindContainer.getKindJA().getKinderabzug() == Kinderabzug.KEINE_STEUERERKLAERUNG) {
						familiengroesse++;
					}
				}
			}
		}
		return familiengroesse;
	}

	public BigDecimal calculateAbzugAufgrundFamiliengroesse(LocalDate stichtag, double familiengroesse) {
		BigDecimal abzugProPerson = BigDecimal.ZERO;
		Optional<EbeguParameter> abzugFromServer = Optional.empty();
		if (familiengroesse < 3) {
			// Unter 3 Personen gibt es keinen Abzug!
			abzugFromServer = Optional.empty();
		} else if (familiengroesse < 4) {
			if (abzugFamiliengroesse3 != null) {
				abzugProPerson = abzugFamiliengroesse3;
			} else {
				abzugFromServer = ebeguParameterService.getEbeguParameterByKeyAndDate(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, stichtag);
			}
		} else if (familiengroesse < 5) {
			if (abzugFamiliengroesse4 != null) {
				abzugProPerson = abzugFamiliengroesse4;
			} else {
				abzugFromServer = ebeguParameterService.getEbeguParameterByKeyAndDate(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, stichtag);
			}
		} else if (familiengroesse < 6) {
			if (abzugFamiliengroesse5 != null) {
				abzugProPerson = abzugFamiliengroesse5;
			} else {
				abzugFromServer = ebeguParameterService.getEbeguParameterByKeyAndDate(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, stichtag);
			}
		} else if (familiengroesse >= 6) {
			if (abzugFamiliengroesse6 != null) {
				abzugProPerson = abzugFamiliengroesse6;
			} else {
				abzugFromServer = ebeguParameterService.getEbeguParameterByKeyAndDate(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, stichtag);
			}
		}
		if (abzugFromServer.isPresent()) {
			abzugProPerson = abzugFromServer.get().getAsBigDecimal();
		}
		// Ein Bigdecimal darf nicht aus einem double erzeugt werden, da das Ergebnis nicht genau die gegebene Nummer waere
		// deswegen muss man hier familiengroesse als String uebergeben. Sonst bekommen wir PMD rule AvoidDecimalLiteralsInBigDecimalConstructor
		return new BigDecimal(String.valueOf(familiengroesse)).multiply(abzugProPerson);
	}

}
