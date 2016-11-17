package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.suchfilter.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service zum Verwalten von Gesuche
 */
public interface GesuchService {

	/**
	 * Erstellt ein neues Gesuch in der DB, falls der key noch nicht existiert
	 *
	 * @param gesuch der Gesuch als DTO
	 * @return das gespeicherte Gesuch
	 */
	@Nonnull
	Gesuch createGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Aktualisiert das Gesuch in der DB
	 *
	 * @param gesuch              das Gesuch als DTO
	 * @param saveInStatusHistory true wenn gewollt, dass die Aenderung in der Status gespeichert wird
	 * @return Das aktualisierte Gesuch
	 */
	@Nonnull
	Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory);

	/**
	 * @param key PK (id) des Gesuches
	 * @return Gesuch mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Gesuch> findGesuch(@Nonnull String key);

	/**
	 * Gibt alle existierenden Gesuche zurueck.
	 *
	 * @return Liste aller Gesuche aus der DB
	 */
	@Nonnull
	Collection<Gesuch> getAllGesuche();

	/**
	 * Gibt alle existierenden Gesuche zurueck, deren Status nicht VERFUEGT ist
	 *
	 * @return Liste aller Gesuche aus der DB
	 */
	@Nonnull
	Collection<Gesuch> getAllActiveGesuche();

	/**
	 * entfernt ein Gesuch aus der Database
	 *
	 * @param gesuch der Gesuch zu entfernen
	 */
	void removeGesuch(@Nonnull Gesuch gesuch);

	@Nonnull
	Optional<List<Gesuch>> findGesuchByGSName(String nachname, String vorname);

	/**
	 * Methode welche jeweils eine bestimmte Menge an Suchresultate fuer die Paginatete Suchtabelle zuruckgibt,
	 *
	 * @param antragSearch
	 * @return Resultatpaar, der erste Wert im Paar ist die Anzahl Resultate, der zweite Wert ist die Resultatliste
	 */
	Pair<Long, List<Gesuch>> searchAntraege(AntragTableFilterDTO antragSearch);

	/**
	 * Gibt ein DTO mit saemtlichen Antragen eins bestimmten Falls zurueck
	 */
	@Nonnull
	List<JaxAntragDTO> getAllAntragDTOForFall(String fallId);

	/**
	 * Erstellt eine neue Mutation fuer die Gesuchsperiode und Fall des uebergebenen Antrags. Es wird immer der letzt
	 * verfuegte Antrag kopiert fuer die Mutation. Die Uebergebenen Mutationsdaten werden angehaengt
	 */
	@Nonnull
	Optional<Gesuch> antragMutieren(@Nonnull String antragId, LocalDate eingangsdatum);

	@Nonnull
	Optional<Gesuch> antragMutieren(@Nonnull Long fallNummer, @Nonnull String gesuchsperiodeId,
									@Nonnull LocalDate eingangsdatum);

	/**
	 * Gibt das neuste verfügte Gesuch (mit dem neuesten Verfuegungsdatum) in der gleichen Gesuchsperiode zurück
	 */
	@Nonnull
	Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(Gesuch gesuch);

	@Nonnull
	Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(Gesuchsperiode gesuchsperiode, Fall fall);


	/**
	 * fuellt die laufnummern der Gesuche/Mutationen eines Falls auf (nach timestamperstellt)
	 *
	 * @param fallId
	 */
	void updateLaufnummerOfAllGesucheOfFall(String fallId);
}
