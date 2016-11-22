package ch.dvbern.ebegu.tests;

import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mutationsdaten;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

/**
 * Arquillian Tests fuer die Klasse FamiliensituationService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class FamiliensituationServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private FamiliensituationService familiensituationService;

	@Inject
	private EinkommensverschlechterungInfoService evInfoService;

	@Inject
	private Persistence<Gesuch> persistence;



	@Test
	public void testCreateFamiliensituation() {
		Assert.assertNotNull(familiensituationService);
		insertNewEntity();

		Collection<Familiensituation> allFamiliensituation = familiensituationService.getAllFamiliensituatione();
		Assert.assertEquals(1, allFamiliensituation.size());
		Familiensituation nextFamsit = allFamiliensituation.iterator().next();
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, nextFamsit.getFamilienstatus());
		Assert.assertEquals(EnumGesuchstellerKardinalitaet.ALLEINE, nextFamsit.getGesuchstellerKardinalitaet());
	}

	@Test
	public void testUpdateFamiliensituationTest() {
		Optional<Familiensituation> familiensituation = createFamiliensituation();

		familiensituation.get().setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Familiensituation updatedFamsit = familiensituationService.saveFamiliensituation(TestDataUtil.createDefaultGesuch(),
			familiensituation.get(), familiensituation.get());
		Assert.assertEquals(EnumFamilienstatus.KONKUBINAT, updatedFamsit.getFamilienstatus());
		Assert.assertEquals(EnumFamilienstatus.KONKUBINAT,
			familiensituationService.findFamiliensituation(updatedFamsit.getId()).get().getFamilienstatus());
	}

	@Test
	public void testRemoveFamiliensituationTest() {
		Assert.assertNotNull(familiensituationService);
		Familiensituation insertedFamiliensituation = insertNewEntity();
		Assert.assertEquals(1, familiensituationService.getAllFamiliensituatione().size());

		familiensituationService.removeFamiliensituation(insertedFamiliensituation);
		Assert.assertEquals(0, familiensituationService.getAllFamiliensituatione().size());
	}

	@Test
	public void testSaveFamiliensituationMutation() {
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		gesuch.setTyp(AntragTyp.MUTATION);


		final EinkommensverschlechterungInfo evInfo = TestDataUtil.createDefaultEinkommensverschlechterungsInfo(gesuch);
		final Optional<EinkommensverschlechterungInfo> einkommensverschlechterungInfo = evInfoService.createEinkommensverschlechterungInfo(evInfo);
		gesuch.setEinkommensverschlechterungInfo(einkommensverschlechterungInfo.get());

		Optional<Familiensituation> familiensituation = createFamiliensituation();
		final Familiensituation newFamiliensituation = new Familiensituation(familiensituation.get());
		newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		newFamiliensituation.setGemeinsameSteuererklaerung(null);

		final Familiensituation persistedFamiliensituation = familiensituationService.saveFamiliensituation(gesuch,
			familiensituation.get(), newFamiliensituation);

		Assert.assertFalse(persistedFamiliensituation.getGemeinsameSteuererklaerung());
		Assert.assertFalse(gesuch.getEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP1());
		Assert.assertFalse(gesuch.getEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP2());
	}


	// HELP METHODS

	@Nonnull
	private Familiensituation insertNewEntity() {
		Familiensituation familiensituation = TestDataUtil.createDefaultFamiliensituation();
		familiensituationService.saveFamiliensituation(TestDataUtil.createDefaultGesuch(), familiensituation, familiensituation);
		return familiensituation;
	}

	@Nonnull
	private Optional<Familiensituation> createFamiliensituation() {
		Assert.assertNotNull(familiensituationService);
		Familiensituation insertedFamiliensituation = insertNewEntity();
		Optional<Familiensituation> familiensituation = familiensituationService.findFamiliensituation(insertedFamiliensituation.getId());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, familiensituation.get().getFamilienstatus());
		return familiensituation;
	}

}
