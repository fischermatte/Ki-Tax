package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ABGELTUNG_PRO_TAG_KANTON;

/**
 * Service fuer FinanzielleSituation
 */
@Stateless
@Local(VerfuegungService.class)
public class VerfuegungServiceBean extends AbstractBaseService implements VerfuegungService {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass().getSimpleName());

	@Inject
	private Persistence<Verfuegung> persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private EbeguParameterService ebeguParameterService;

	@Inject
	private MandantService mandantService;


	@Nonnull
	@Override
	public Verfuegung saveVerfuegung(@Nonnull Verfuegung verfuegung) {
		Objects.requireNonNull(verfuegung);
		return persistence.persist(verfuegung);
	}

	@Nonnull
	@Override
	public Optional<Verfuegung> findVerfuegung(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Verfuegung a = persistence.find(Verfuegung.class, id);
		return Optional.ofNullable(a);
	}


	@Nonnull
	@Override
	public Collection<Verfuegung> getAllVerfuegungen() {
		return criteriaQueryHelper.getAll(Verfuegung.class);
	}


	@Override
	public void removeVerfuegung(@Nonnull Verfuegung verfuegung) {
		Validate.notNull(verfuegung);
		Optional<Verfuegung> entityToRempoe = this.findVerfuegung(verfuegung.getId());
		entityToRempoe.orElseThrow(() -> new EbeguEntityNotFoundException("removeVerfuegung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, verfuegung));
		persistence.remove(entityToRempoe.get());
	}


	@Nonnull
	@Override
	public Gesuch calculateVerfuegung(@Nonnull Gesuch gesuch) {
		Mandant mandant = mandantService.getFirst();   //gesuch get mandant?
		BetreuungsgutscheinEvaluator bgEvaluator = initEvaluator(mandant, gesuch.getGesuchsperiode());
		BGRechnerParameterDTO calculatorParameters = loadCalculatorParameters(mandant, gesuch.getGesuchsperiode());
		bgEvaluator.evaluate(gesuch, calculatorParameters);
		return gesuch;
	}


	/**
	 * Diese Methode initialisiert den Calculator mit den richtigen Parametern und benotigten Regeln fuer den Mandanten der
	 * gebraucht wird
	 */
	private BetreuungsgutscheinEvaluator initEvaluator(@Nullable Mandant mandant, @Nonnull Gesuchsperiode gesuchsperiode) {
		BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
		Set<EbeguParameterKey> keysToLoad = ruleConfigurator.getRequiredParametersForMandant(mandant);
		Map<EbeguParameterKey, EbeguParameter> ebeguParameter = loadRuleParameters(mandant, gesuchsperiode, keysToLoad);
		List<Rule> rules = ruleConfigurator.configureRulesForMandant(mandant, ebeguParameter);
		return new BetreuungsgutscheinEvaluator(rules);
	}

	/**
	 * Hinewis, hier muss wohl spaeter der Mandant als Parameter mitgehen
	 *
	 * @return
	 */
	private Map<EbeguParameterKey, EbeguParameter> loadRuleParameters(Mandant mandant, Gesuchsperiode gesuchsperiode, Set<EbeguParameterKey> keysToLoad) {
		//Hinweis, Mandant wird noch ignoriert
		if (mandant != null) {
			LOG.warn("Mandant wird noch nicht beruecksichtigt. Codeaenderung noetig");
		}
		LocalDate stichtag = gesuchsperiode.getGueltigkeit().getGueltigAb();
		Map<EbeguParameterKey, EbeguParameter> ebeguRuleParameters = new HashMap<EbeguParameterKey, EbeguParameter>();
		for (EbeguParameterKey currentParamKey : keysToLoad) {
			Optional<EbeguParameter> param = ebeguParameterService.getEbeguParameterByKeyAndDate(currentParamKey, stichtag);
			if (param.isPresent()) {
				ebeguRuleParameters.put(param.get().getName(), param.get());
			} else {
				LOG.error("Required rule parameter '{}' could not be loaded  for the given Mandant '{}', Gesuchsperiode '{}'", currentParamKey, mandant, gesuchsperiode);
				throw new EbeguEntityNotFoundException("initEvaluator", ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, currentParamKey, Constants.DATE_FORMATTER.format(stichtag));
			}
		}

		return ebeguRuleParameters;
	}

	private BGRechnerParameterDTO loadCalculatorParameters(Mandant mandant, @Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EbeguParameterKey, EbeguParameter> paramMap = ebeguParameterService.getEbeguParameterByGesuchsperiodeAsMap(gesuchsperiode);
		BGRechnerParameterDTO parameterDTO = new BGRechnerParameterDTO(paramMap, gesuchsperiode, mandant);

		//Es gibt aktuell einen Parameter der sich aendert am Jahreswechsel
		int startjahr = gesuchsperiode.getGueltigkeit().getGueltigAb().getYear();
		int endjahr = gesuchsperiode.getGueltigkeit().getGueltigBis().getYear();
		Validate.isTrue(endjahr == startjahr +1, "Startjahr " + startjahr + " muss ein Jahr vor Endjahr"+ endjahr +" sein ");
		BigDecimal abgeltungJahr1 = loadYearlyParameter(PARAM_ABGELTUNG_PRO_TAG_KANTON, startjahr);
		BigDecimal abgeltungJahr2 = loadYearlyParameter(PARAM_ABGELTUNG_PRO_TAG_KANTON, endjahr);
		parameterDTO.setBeitragKantonProTagJahr1((abgeltungJahr1));
		parameterDTO.setBeitragKantonProTagJahr2((abgeltungJahr2));
		return parameterDTO;
	}

	@Nonnull
	private BigDecimal loadYearlyParameter(EbeguParameterKey key, int jahr) {
		Optional<EbeguParameter> result = ebeguParameterService.getEbeguParameterByKeyAndDate(key, LocalDate.of(jahr, 1, 1));
		if (!result.isPresent()) {
			LOG.error("Required yearly calculator parameter '{}' could not be loaded for year {}'", key, jahr);
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, key);
		}
		return result.get().getValueAsBigDecimal();
	}



}